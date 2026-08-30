/*
 * Copyright (C) 2019 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * Wrapper for IProvider.
 * <p>
 * It implements the procedure bytes of EMV 4.3 Book 1 section 9.3.1.2:
 * </p>
 * <ul>
 * <li>'61xx' asks the terminal to issue a GET RESPONSE command to read the
 * 'xx' bytes still available. The card may answer '61xx' again, so the
 * exchange is repeated and every data field read is concatenated</li>
 * <li>'6Cxx' asks the terminal to resend the very same command with Le =
 * 'xx'</li>
 * </ul>
 *
 * @author MILLAU Julien
 *
 */
public class ProviderWrapper implements IProvider{

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProviderWrapper.class);

	/**
	 * Maximum number of GET RESPONSE commands sent for a single command, a card
	 * answering '61xx' forever must not hang the reading.<br>
	 * A card answering '6Cxx' to a GET RESPONSE adds one exchange each time, so
	 * the number of round trips is bounded by twice this value.
	 */
	public static final int MAX_CHAINED_RESPONSES = 16;

	/**
	 * Size of the status word (SW1 SW2) that ends every response
	 */
	private static final int SW_SIZE = 2;

	/**
	 * Provider
	 */
	private final IProvider provider;

	/**
	 * Constructor
	 * @param pProvider provider
	 */
	public ProviderWrapper(IProvider pProvider) {
		provider = pProvider;
	}

	@Override
	public byte[] transceive(byte[] pCommand) throws CommunicationException {
		// The command belongs to the caller, '6Cxx' is answered with a copy
		byte[] command = pCommand != null ? pCommand.clone() : null;
		byte[] ret = provider.transceive(command);
		// If LE is not correct, resend the same command with the length the
		// card asks for. Only a command that ends with a Le byte can be
		// patched that way, and EMV 4.3 Book 1 section 9.3.1.2 only allows
		// '6Cxx' for the case 2 and case 4 commands, which are exactly those
		if (ResponseUtils.isEquals(ret, SwEnum.SW_6C) && endsWithLe(command)) {
			command[command.length - 1] = ret[ret.length - 1];
			ret = provider.transceive(command);
		}
		if (!ResponseUtils.isEquals(ret, SwEnum.SW_61)) {
			return ret;
		}
		return getResponse(ret);
	}

	/**
	 * Method used to read the data the card announced with the procedure bytes
	 * '61xx'.<br>
	 * A card is allowed to answer data followed by '61xx' (EMV 4.3 Book 1
	 * section 9.3.1.2.1 rule 3), so the data field of every response is kept
	 * and concatenated with the following ones.
	 *
	 * @param pResponse
	 *            first response of the card, ending with '61xx'
	 * @return the concatenated data followed by the status word of the last
	 *         response
	 * @throws CommunicationException
	 *             communication error
	 */
	protected byte[] getResponse(final byte[] pResponse) throws CommunicationException {
		byte[] ret = pResponse;
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		int exchanges = 0;
		while (ResponseUtils.isEquals(ret, SwEnum.SW_61)) {
			if (exchanges++ >= MAX_CHAINED_RESPONSES) {
				LOGGER.warn("The card keeps asking for a GET RESPONSE, stop after " + MAX_CHAINED_RESPONSES + " exchanges");
				break;
			}
			// Keep the data already returned, only the status word is dropped
			data.write(ret, 0, ret.length - SW_SIZE);
			byte[] command = new CommandApdu(CommandEnum.GET_RESPONSE, null, ret[ret.length - 1] & 0xFF).toBytes();
			ret = provider.transceive(command);
			// The GET RESPONSE itself may be answered '6Cxx'
			if (ResponseUtils.isEquals(ret, SwEnum.SW_6C)) {
				command[command.length - 1] = ret[ret.length - 1];
				ret = provider.transceive(command);
			}
		}
		// A provider that answers nothing keeps the same meaning as everywhere
		// else, whatever has already been read is dropped
		if (ret == null || data.size() == 0) {
			return ret;
		}
		// Data of the last response and its status word
		data.write(ret, 0, ret.length);
		return data.toByteArray();
	}

	/**
	 * Method used to know if the last byte of a command is its Le, the only
	 * case where the answer '6Cxx' can be applied to it. A case 2 command is
	 * CLA INS P1 P2 Le, a case 4 command is CLA INS P1 P2 Lc DATA Le.
	 *
	 * @param pCommand
	 *            command sent to the card
	 * @return true if the command ends with a Le byte
	 */
	private static boolean endsWithLe(final byte[] pCommand) {
		if (pCommand == null) {
			return false;
		}
		return pCommand.length == 5 || pCommand.length > 5 && pCommand.length == 6 + (pCommand[4] & 0xFF);
	}

	@Override
	public byte[] getAt() {
		return provider.getAt();
	}

}
