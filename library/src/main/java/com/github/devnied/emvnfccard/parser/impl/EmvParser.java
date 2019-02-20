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
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Emv default Parser <br>
 *
 * Paypass: <br>
 * - https://www.paypass.com/pdf/public_documents/Terminal%20
 * Optimization%20v2-0.pdf
 *
 * @author julien
 *
 */
public class EmvParser extends AbstractParser {

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
	 * Read public card data from parameter AID
	 *
	 * @param pApplication
	 *            application data
	 * @return true if succeed false otherwise
	 * @throws CommunicationException communication error
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
			ret = parse(data, pApplication);
			if (ret) {
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
	 * Method used to parse EMV card
	 *
	 * @param pSelectResponse
	 *            select response data
	 * @param pApplication
	 *            application selected
	 * @return true if the parsing succeed false otherwise
	 * @throws CommunicationException communication error
	 */
	protected boolean parse(final byte[] pSelectResponse, final Application pApplication) throws CommunicationException {
		boolean ret = false;
		// Get TLV log entry
		byte[] logEntry = getLogEntry(pSelectResponse);
		// Get PDOL
		byte[] pdol = TlvUtil.getValue(pSelectResponse, EmvTags.PDOL);
		// Send GPO Command
		byte[] gpo = getGetProcessingOptions(pdol);
		// Extract Bank data
		extractBankData(pSelectResponse);

		// Check empty PDOL
		if (!ResponseUtils.isSucceed(gpo)) {
			if (pdol != null) {
				gpo = getGetProcessingOptions(null);
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
		pApplication.setReadingStep(ApplicationStepEnum.READ);

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
	 * @return true if the extraction succeed
	 * @throws CommunicationException communication error
	 */
	protected boolean extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
		boolean ret = false;
		// Extract data from Message Template 1
		byte data[] = TlvUtil.getValue(pGpo, EmvTags.RESPONSE_MESSAGE_TEMPLATE_1);
		if (data != null) {
			data = ArrayUtils.subarray(data, 2, data.length);
		} else { // Extract AFL data from Message template 2
			ret = extractTrackData(template.get().getCard(), pGpo);
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
					// Extract card data
					if (ResponseUtils.isSucceed(info)) {
						extractCardHolderName(info);
						if (extractTrackData(template.get().getCard(), info)) {
							return true;
						}
					}
				}
			}
		}
		return ret;
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
	 * Method used to create GPO command and execute it
	 *
	 * @param pPdol
	 *            PDOL raw data
	 * @return return data
	 * @throws CommunicationException communication error
	 */
	protected byte[] getGetProcessingOptions(final byte[] pPdol) throws CommunicationException {
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
		return template.get().getProvider().transceive(new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes());
	}

	/**
	 * Method used to extract track data from response
	 *
	 * @param pEmvCard
	 *            Card data
	 * @param pData
	 *            data send by card
	 * @return true if track 1 or track 2 can be read
	 */
	protected boolean extractTrackData(final EmvCard pEmvCard, final byte[] pData) {
		template.get().getCard().setTrack1(TrackUtils.extractTrack1Data(TlvUtil.getValue(pData, EmvTags.TRACK1_DATA)));
		template.get().getCard().setTrack2(TrackUtils.extractTrack2EquivalentData(TlvUtil.getValue(pData, EmvTags.TRACK_2_EQV_DATA, EmvTags.TRACK2_DATA)));
		return pEmvCard.getTrack1() != null || pEmvCard.getTrack2() != null;
	}

}
