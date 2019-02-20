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
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IParser;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Parser with provider attribute
 *
 * @author MILLAU Julien
 *
 */
public abstract class AbstractParser implements IParser {

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParser.class);

	/**
	 * Unknown response
	 */
	public static final int UNKNOW = -1;

	/**
	 * Card provider used
	 */
	protected final WeakReference<EmvTemplate> template;

	/**
	 * Default protected constructor
	 *
	 * @param pTemplate
	 *            Emv template
	 */
	protected AbstractParser(EmvTemplate pTemplate) {
		template = new WeakReference<EmvTemplate>(pTemplate);
	}

	/**
	 * Select application with AID or RID
	 *
	 * @param pAid
	 *            byte array containing AID or RID
	 * @return response byte array
	 * @throws CommunicationException communication error
	 */
	protected byte[] selectAID(final byte[] pAid) throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Select AID: " + BytesUtils.bytesToString(pAid));
		}
		return template.get().getProvider().transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
	}

	/**
	 * Method used to extract application label
	 *
	 * @param pData
	 * 			raw response data
	 *
	 * @return decoded application label or null
	 */
	protected String extractApplicationLabel(final byte[] pData) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Extract Application label");
		}
		String label = null;
		// Get Preferred name first
		byte[] labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_PREFERRED_NAME);
		// Get Application label
		if (labelByte == null) {
			labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_LABEL);
		}
		// Convert to String
		if (labelByte != null) {
			label = new String(labelByte);
		}
		return label;
	}

	/**
	 * Extract bank data (BIC and IBAN)
	 *
	 * @param pData
	 *            card data
	 */
	protected void extractBankData(final byte[] pData) {
		// Extract BIC data
		byte[] bic = TlvUtil.getValue(pData, EmvTags.BANK_IDENTIFIER_CODE);
		if (bic != null) {
			template.get().getCard().setBic(new String(bic));
		}
		// Extract IBAN
		byte[] iban = TlvUtil.getValue(pData, EmvTags.IBAN);
		if (iban != null) {
			template.get().getCard().setIban(new String(iban));
		}
	}

	/**
	 * Extract card holder lastname and firstname
	 *
	 * @param pData
	 *            card data
	 */
	protected void extractCardHolderName(final byte[] pData) {
		// Extract Card Holder name (if exist)
		byte[] cardHolderByte = TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME);
		if (cardHolderByte != null) {
			String[] name = StringUtils.split(new String(cardHolderByte).trim(), TrackUtils.CARD_HOLDER_NAME_SEPARATOR);
			if (name != null && name.length > 0) {
				template.get().getCard().setHolderLastname(StringUtils.trimToNull(name[0]));
				if (name.length == 2) {
					template.get().getCard().setHolderFirstname(StringUtils.trimToNull(name[1]));
				}
			}
		}
	}

	/**
	 * Method used to extract Log Entry from Select response
	 *
	 * @param pSelectResponse
	 *            select response
	 * @return byte array
	 */
	protected byte[] getLogEntry(final byte[] pSelectResponse) {
		return TlvUtil.getValue(pSelectResponse, EmvTags.LOG_ENTRY, EmvTags.VISA_LOG_ENTRY);
	}

	/**
	 * Method used to get Transaction counter
	 *
	 * @return the number of card transaction
	 * @throws CommunicationException communication error
	 */
	protected int getTransactionCounter() throws CommunicationException {
		int ret = UNKNOW;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get Transaction Counter ATC");
		}
		byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x36, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {
			// Extract ATC
			byte[] val = TlvUtil.getValue(data, EmvTags.APP_TRANSACTION_COUNTER);
			if (val != null) {
				ret = BytesUtils.byteArrayToInt(val);
			}
		}
		return ret;
	}

	/**
	 * Method used to get the number of pin try left
	 *
	 * @return the number of pin try left
	 * @throws CommunicationException communication error
	 */
	protected int getLeftPinTry() throws CommunicationException {
		int ret = UNKNOW;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Get Left PIN try");
		}
		// Left PIN try command
		byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x17, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {
			// Extract PIN try counter
			byte[] val = TlvUtil.getValue(data, EmvTags.PIN_TRY_COUNTER);
			if (val != null) {
				ret = BytesUtils.byteArrayToInt(val);
			}
		}
		return ret;
	}

	/**
	 * Method used to get log format
	 *
	 * @return list of tag and length for the log format
	 * @throws CommunicationException communication error
	 */
	protected List<TagAndLength> getLogFormat() throws CommunicationException {
		List<TagAndLength> ret = new ArrayList<TagAndLength>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("GET log format");
		}
		// Get log format
		byte[] data = template.get().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x4F, 0).toBytes());
		if (ResponseUtils.isSucceed(data)) {
			ret = TlvUtil.parseTagAndLength(TlvUtil.getValue(data, EmvTags.LOG_FORMAT));
		} else {
			LOGGER.warn("No Log format found");
		}
		return ret;
	}

	/**
	 * Method used to extract log entry from card
	 *
	 * @param pLogEntry
	 *            log entry position
	 * @return list of transaction records
	 * @throws CommunicationException communication error
	 */
	protected List<EmvTransactionRecord> extractLogEntry(final byte[] pLogEntry) throws CommunicationException {
		List<EmvTransactionRecord> listRecord = new ArrayList<EmvTransactionRecord>();
		// If log entry is defined
		if (template.get().getConfig().readTransactions && pLogEntry != null) {
			List<TagAndLength> tals = getLogFormat();
			if (tals != null && !tals.isEmpty()) {
				// read all records
				for (int rec = 1; rec <= pLogEntry[1]; rec++) {
					byte[] response = template.get().getProvider()
							.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, pLogEntry[0] << 3 | 4, 0).toBytes());
					// Extract data
					if (ResponseUtils.isSucceed(response)) {
						try {
							EmvTransactionRecord record = new EmvTransactionRecord();
							record.parse(response, tals);

							if (record.getAmount() != null) {
								// Fix artifact in EMV VISA card
								if (record.getAmount() >= 1500000000) {
									record.setAmount(record.getAmount() - 1500000000);
								}

								// Skip transaction with null amount
								if (record.getAmount() == null || record.getAmount() <= 1) {
									continue;
								}
							}

							if (record != null) {
								// Unknown currency
								if (record.getCurrency() == null) {
									record.setCurrency(CurrencyEnum.XXX);
								}
								listRecord.add(record);
							}
						} catch (Exception e) {
							LOGGER.error("Error in transaction format: " + e.getMessage(), e);
						}
					} else {
						// No more transaction log or transaction disabled
						break;
					}
				}
			}
		}
		return listRecord;
	}

}
