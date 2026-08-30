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
package com.github.devnied.emvnfccard.utils;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.SwEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Method used to manipulate response from APDU command
 *
 * @author MILLAU Julien
 *
 */
public final class ResponseUtils {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseUtils.class);

	/**
	 * Method used to check if the last command return SW1SW2 == 9000
	 *
	 * @param pByte
	 *            response to the last command
	 * @return true if the status is 9000 false otherwise
	 */
	public static boolean isSucceed(final byte[] pByte) {
		return contains(pByte, SwEnum.SW_9000);
	}

	/**
	 * Method used to check if the last command succeeded, or ended with a
	 * warning <b>and</b> still carried a data field.
	 * <p>
	 * ISO/IEC 7816-4:2005 section 5.1.3 classifies '62XX' and '63XX' under
	 * "process completed", as a warning processing, and never requires their
	 * data field to be absent: the only such requirement of the standard is
	 * "if the process is aborted with a value of SW1 from '64' to '6F', then
	 * the response data field shall be absent". Its Table 6 gives '6282' as
	 * "end of file or record reached before reading Ne bytes", which a card
	 * answers to a READ RECORD whose record is shorter than the length the
	 * terminal asked for, having sent the record all the same.
	 * </p>
	 * <p>
	 * A response that holds <b>nothing but its status word</b> is therefore not
	 * accepted: there is nothing to parse, and reading it as a success would
	 * hide the failure from the caller. The GET PROCESSING OPTIONS of
	 * {@link com.github.devnied.emvnfccard.parser.impl.EmvParser} for instance
	 * has to see the failure to retry the command without its PDOL.
	 * </p>
	 * <p>
	 * '61XX' followed by data is accepted: ISO/IEC 7816-4 section 5.1.3 lists
	 * it under normal processing, and a response still carrying it has had its
	 * GET RESPONSE chain cut short by
	 * {@link com.github.devnied.emvnfccard.parser.impl.ProviderWrapper}.
	 * </p>
	 * <p>
	 * '90XX' other than '9000' is not accepted either: ISO/IEC 7816-4 leaves it
	 * proprietary, and the values of {@link SwEnum} that use it ('9004', '9008',
	 * '9080') all report a failure.
	 * </p>
	 * <p>
	 * <b>This is a deliberate deviation from EMV.</b> EMV 4.3 Book 1 section
	 * 12.3.2 asks the terminal to clear its candidate list when the reading of a
	 * payment system directory returns "a SW1 SW2 different from '90 00' or
	 * '6A 83'", and Book 3 section 10.2 asks it to terminate the transaction
	 * when "any SW1 SW2 other than '9000'" results from reading a record. This
	 * library extracts public data and never runs a transaction, so it keeps
	 * what the card did send rather than reporting an empty card.
	 * </p>
	 *
	 * @param pByte
	 *            response to the last command
	 * @return true when the response carries data to be parsed
	 */
	public static boolean isSucceedOrWarning(final byte[] pByte) {
		if (isSucceed(pByte)) {
			return true;
		}
		// A response of two bytes is a status word alone, it carries no data
		if (pByte == null || pByte.length <= 2) {
			return false;
		}
		int sw1 = pByte[pByte.length - 2] & 0xFF;
		// '61XX' is normal processing for ISO/IEC 7816-4 section 5.1.3, the
		// card only says that more data is waiting. ProviderWrapper chains the
		// GET RESPONSE commands itself, so a response reaching a caller with
		// '61XX' still on it is one whose chain was cut short: the data
		// already gathered is worth keeping rather than reporting an empty read
		if (sw1 != 0x61 && sw1 != 0x62 && sw1 != 0x63) {
			return false;
		}
		LOGGER.warn("The card answered a warning status, its response is read: "
				+ BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pByte, pByte.length - 2, pByte.length)));
		return true;
	}

	/**
	 * Method used to extract the data field of a response, that is to say the
	 * response without the two status bytes that close it.
	 * <p>
	 * A response APDU is a data field followed by SW1 SW2 (ISO/IEC 7816-4
	 * section 5.1). The two must be separated <b>before</b> the data field is
	 * parsed: a data object whose declared length is longer than what the card
	 * sent would otherwise be completed with the status bytes and handed back as
	 * a value the card never sent. A card number truncated by two bytes reads as
	 * a complete one ending with '9000'.
	 * </p>
	 *
	 * @param pResponse
	 *            response to a command, status bytes included
	 * @return the data field of the response, empty when it holds none
	 */
	public static byte[] getDataField(final byte[] pResponse) {
		if (pResponse == null || pResponse.length <= 2) {
			return new byte[0];
		}
		return Arrays.copyOf(pResponse, pResponse.length - 2);
	}

	/**
	 * Method used to check equality with the last command return SW1SW2 == pEnum
	 *
	 * @param pByte
	 *            response to the last command
	 * @param pEnum
	 *            response to check
	 * @return true if the response of the last command is equals to pEnum
	 */
	public static boolean isEquals(final byte[] pByte, final SwEnum pEnum) {
		return contains(pByte, pEnum);
	}

	/**
	 * Method used to check equality with the last command return SW1SW2 ==
	 * pEnum
	 *
	 * @param pByte
	 *            response to the last command
	 * @param pEnum
	 *            responses to check
	 * @return true if the response of the last command is contained in pEnum
	 */
	public static boolean contains(final byte[] pByte, final SwEnum... pEnum) {
		SwEnum val = SwEnum.getSW(pByte);
		if (LOGGER.isDebugEnabled() && pByte != null) {
			LOGGER.debug("Response Status <"
					+ BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pByte, Math.max(pByte.length - 2, 0), pByte.length)) + "> : "
					+ (val != null ? val.getDetail() : "Unknow"));
		}
		return val != null && ArrayUtils.contains(pEnum, val);
	}

	/**
	 * Private constructor
	 */
	private ResponseUtils() {
	}

}
