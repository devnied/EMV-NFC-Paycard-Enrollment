/*
 * Copyright (C) 2015 MILLAU Julien
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Emv default Parser <br/>
 * 
 * Paypass: <br/>
 * - https://www.paypass.com/pdf/public_documents/Terminal%20
 * Optimization%20v2-0.pdf
 * 
 * @author julien
 *
 */
public class EmvParser extends AbstractParser {

	/**
	 * Unknown response
	 */
	public static final int UNKNOW = -1;

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EmvParser.class);

	/**
	 * Default EMV pattern
	 */
	private static final Pattern PATTERN = Pattern.compile(".*");

	/**
	 * Default constructor
	 * 
	 * @param pTemplate
	 *            parser template
	 */
	public EmvParser(EmvTemplate pTemplate) {
		super(pTemplate);
	}

	@Override
	public Pattern getId() {
		return PATTERN;
	}

	@Override
	public boolean parse(Application pApplication) throws CommunicationException {
		return extractPublicData(pApplication);
	}

	/**
	 * Method used to get Transaction counter
	 *
	 * @return the number of card transaction
	 * @throws CommunicationException
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
	 * Select application with AID or RID
	 *
	 * @param pAid
	 *            byte array containing AID or RID
	 * @return response byte array
	 * @throws CommunicationException
	 */
	protected byte[] selectAID(final byte[] pAid) throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Select AID: " + BytesUtils.bytesToString(pAid));
		}
		return template.get().getProvider().transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
	}

	/**
	 * Method used to get the number of pin try left
	 *
	 * @return the number of pin try left
	 * @throws CommunicationException
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
	 * Method used to extract application label
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
	 * Read public card data from parameter AID
	 *
	 * @param pApplication
	 *            application data
	 * @return true if succeed false otherwise
	 */
	protected boolean extractPublicData(final Application pApplication) throws CommunicationException {
		boolean ret = false;
		// Select AID
		byte[] data = selectAID(pApplication.getAid());
		// check response
		// Add SW_6285 to fix Interact issue
		if (ResponseUtils.contains(data, SwEnum.SW_9000, SwEnum.SW_6285)) {
			// Update reading state
			pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
			// Parse select response
			if ((ret = parse(data, pApplication)) == true) {
				// Get AID
				String aid = BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
				String applicationLabel = extractApplicationLabel(data);
				if (applicationLabel == null) {
					applicationLabel = pApplication.getApplicationLabel();
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Application label:" + applicationLabel + " with Aid:" + aid);
				}
				template.get().getCard().setType(findCardScheme(aid, template.get().getCard().getCardNumber()));
				pApplication.setAid(BytesUtils.fromString(aid));
				pApplication.setApplicationLabel(applicationLabel);
				pApplication.setLeftPinTry(getLeftPinTry());
				pApplication.setTransactionCounter(getTransactionCounter());
				template.get().getCard().setState(CardStateEnum.ACTIVE);
			}
		}
		return ret;
	}

	/**
	 * Method used to find the real card scheme
	 *
	 * @param pAid
	 *            card complete AID
	 * @param pCardNumber
	 *            card number
	 * @return card scheme
	 */
	protected EmvCardScheme findCardScheme(final String pAid, final String pCardNumber) {
		EmvCardScheme type = EmvCardScheme.getCardTypeByAid(pAid);
		// Get real type for french card
		if (type == EmvCardScheme.CB) {
			type = EmvCardScheme.getCardTypeByCardNumber(pCardNumber);
			if (type != null) {
				LOGGER.debug("Real type:" + type.getName());
			}
		}
		return type;
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
	 * Method used to parse EMV card
	 *
	 * @param pSelectResponse
	 *            select response data
	 * @param pApplication
	 *            application selected
	 * @return true if the parsing succeed false otherwise
	 * @throws CommunicationException
	 */
	protected boolean parse(final byte[] pSelectResponse, final Application pApplication) throws CommunicationException {
		boolean ret = false;
		// Get TLV log entry
		byte[] logEntry = getLogEntry(pSelectResponse);
		// Get PDOL
		byte[] pdol = TlvUtil.getValue(pSelectResponse, EmvTags.PDOL);
		// Send GPO Command
		byte[] gpo = getGetProcessingOptions(pdol, template.get().getProvider());
		// Extract Bank data
		extractBankData(pSelectResponse);

		// Check empty PDOL
		if (!ResponseUtils.isSucceed(gpo)) {
			if (pdol != null) {
				gpo = getGetProcessingOptions(null, template.get().getProvider());
			}

			// Check response
			if (pdol == null || !ResponseUtils.isSucceed(gpo)) {
				// Try to read EF 1 and record 1
				gpo = template.get().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, 1, 0x0C, 0).toBytes());
				if (!ResponseUtils.isSucceed(gpo)) {
					return false;
				}
			}
		}
		// Update Reading state
		pApplication.setReadingStep(ApplicationStepEnum.GPO_PERFORMED);

		// Extract commons card data (number, expire date, ...)
		if (extractCommonsCardData(gpo)) {
			// Extract log entry
			pApplication.setListTransactions(extractLogEntry(logEntry));
			ret = true;
		}

		return ret;
	}

	/**
	 * Method used to extract commons card data
	 *
	 * @param pGpo
	 *            global processing options response
	 */
	protected boolean extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
		boolean ret = false;
		// Extract data from Message Template 1
		byte data[] = TlvUtil.getValue(pGpo, EmvTags.RESPONSE_MESSAGE_TEMPLATE_1);
		if (data != null) {
			data = ArrayUtils.subarray(data, 2, data.length);
		} else { // Extract AFL data from Message template 2
			ret = TrackUtils.extractTrackData(template.get().getCard(), pGpo);
			if (!ret) {
				data = TlvUtil.getValue(pGpo, EmvTags.APPLICATION_FILE_LOCATOR);
			} else {
				extractCardHolderName(pGpo);
			}
		}

		if (data != null) {
			// Extract Afl
			List<Afl> listAfl = extractAfl(data);
			// for each AFL
			for (Afl afl : listAfl) {
				// check all records
				for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
					byte[] info = template.get().getProvider()
							.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, 0).toBytes());
					if (ResponseUtils.isEquals(info, SwEnum.SW_6C)) {
						info = template
								.get()
								.getProvider()
								.transceive(
										new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, info[info.length - 1])
												.toBytes());
					}

					// Extract card data
					if (ResponseUtils.isSucceed(info)) {
						extractCardHolderName(info);
						if (TrackUtils.extractTrackData(template.get().getCard(), info)) {
							return true;
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Method used to get log format
	 *
	 * @return list of tag and length for the log format
	 * @throws CommunicationException
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

	/**
	 * Extract list of application file locator from Afl response
	 *
	 * @param pAfl
	 *            AFL data
	 * @return list of AFL
	 */
	protected List<Afl> extractAfl(final byte[] pAfl) {
		List<Afl> list = new ArrayList<Afl>();
		ByteArrayInputStream bai = new ByteArrayInputStream(pAfl);
		while (bai.available() >= 4) {
			Afl afl = new Afl();
			afl.setSfi(bai.read() >> 3);
			afl.setFirstRecord(bai.read());
			afl.setLastRecord(bai.read());
			afl.setOfflineAuthentication(bai.read() == 1);
			list.add(afl);
		}
		return list;
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
	 * Method used to create GPO command and execute it
	 *
	 * @param pPdol
	 *            PDOL data
	 * @param pProvider
	 *            provider
	 * @return return data
	 */
	protected byte[] getGetProcessingOptions(final byte[] pPdol, final IProvider pProvider) throws CommunicationException {
		// List Tag and length from PDOL
		List<TagAndLength> list = TlvUtil.parseTagAndLength(pPdol);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(EmvTags.COMMAND_TEMPLATE.getTagBytes()); // COMMAND
			// TEMPLATE
			out.write(TlvUtil.getLength(list)); // ADD total length
			if (list != null) {
				for (TagAndLength tl : list) {
					out.write(template.get().getTerminal().constructValue(tl));
				}
			}
		} catch (IOException ioe) {
			LOGGER.error("Construct GPO Command:" + ioe.getMessage(), ioe);
		}
		return pProvider.transceive(new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes());
	}
}
