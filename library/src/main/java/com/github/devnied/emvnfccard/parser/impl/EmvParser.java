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
import com.github.devnied.emvnfccard.iso7816emv.BerTlvInputStream;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.ApplicationInterchangeProfile;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.RecordData;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TagValueDecoder;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	 * True when the track 1 the card carries was read from an application
	 * other than the one being read
	 */
	private boolean track1FromAnotherApplication;

	/**
	 * True when the track 2 the card carries was read from an application
	 * other than the one being read
	 */
	private boolean track2FromAnotherApplication;

	/**
	 * Highest number of records read for the Application File Locator of one
	 * application, the locator being card controlled
	 */
	public static final int MAX_RECORDS = 64;

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
		// Tracks already on the card when this application begins were read
		// from another one: see extractTrackDiscretionaryData
		track1FromAnotherApplication = getTemplate().getCard().getTrack1() != null;
		track2FromAnotherApplication = getTemplate().getCard().getTrack2() != null;
		// The AID the selection is asked with is kept apart from getAid(),
		// which the DF Name the card answers replaces
		if (pApplication.getRequestedAid() == null) {
			pApplication.setRequestedAid(pApplication.getAid());
		}
		// Select AID, appending the Extended Selection when the card asked for
		// it in the payment directory
		byte[] data = selectApplication(pApplication);
		// The outcome of the selection is recorded before it is acted upon: a
		// blocked application is one that answered '6283'
		pApplication.setSelectStatusWord(EmvTemplate.getStatusWord(data));
		// An application the issuer blocked must be skipped. A '6A81' answered
		// to a "next occurrence" SELECT only means that the card does not
		// manage occurrences, it says nothing about the application.
		// An occurrence the issuer blocked is skipped like any other blocked
		// application, and the enumeration carries on with the next one
		if (isApplicationBlocked(data)) {
			LOGGER.warn("Application is blocked: " + BytesUtils.bytesToStringNoSpace(pApplication.getAid()));
			pApplication.setBlocked(true);
			return false;
		}
		if (!pApplication.isNextOccurrence() && isCardBlocked(data)) {
			LOGGER.warn("Card reports the function as not supported: "
					+ BytesUtils.bytesToStringNoSpace(pApplication.getAid()));
			pApplication.setBlocked(true);
			return false;
		}
		// EMV 4.3 Book 1 section 12.3.3 step 7 tells a warning status ('62xx',
		// '63xx') apart from an error: the occurrence cannot be used but the
		// card may hold further ones, so the enumeration has to know
		pApplication.setSelectWarning(isWarning(data));
		// check response
		// Add SW_6285 to fix Interact issue
		if (ResponseUtils.contains(data, SwEnum.SW_9000, SwEnum.SW_6285)) {
			// Update reading state
			pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
			// The status bytes close the response, they are not part of the File
			// Control Information
			data = ResponseUtils.getDataField(data);
			// Parse select response
			ret = parse(data, pApplication);
			// Everything that follows only needs the application to be
			// selected. An application that refused the GET PROCESSING OPTIONS
			// or that exposed no account number still names itself in its File
			// Control Information, and may still answer the GET DATA commands
			// and hold a transaction log: both are optional for the card (EMV
			// 4.3 Book 3 section 7.3 and Annex D4), so both are asked for and
			// may answer nothing. The account number only decides whether the
			// card is reported as read
			// Get AID
			byte[] dedicatedFileName = TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME);
			// The name exactly as the card answered it, null when it sent
			// none
			pApplication.setDedicatedFileName(dedicatedFileName);
			// The Dedicated File Name is mandatory in the File Control
			// Information of an ADF (EMV 4.3 Book 1 Table 45), a card that
			// leaves it out
			// keeps the name it was selected with: the AID of the directory
			// entry, or the one the lookup by AID used, is then the only
			// name known for the application
			if (dedicatedFileName == null || dedicatedFileName.length == 0) {
				LOGGER.warn("The card returned no Dedicated File Name, the AID it was selected with is kept");
				dedicatedFileName = pApplication.getAid();
			}
			String aid = dedicatedFileName != null ? BytesUtils.bytesToStringNoSpace(dedicatedFileName) : null;
			// The scheme the AID names, before any substitution by the
			// scheme of the card number
			pApplication.setAidScheme(EmvCardScheme.getCardTypeByAid(aid));
			String applicationLabel = extractApplicationLabel(data);
			if (applicationLabel == null) {
				applicationLabel = pApplication.getApplicationLabel();
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Application label:" + applicationLabel + " with Aid:" + aid);
			}
			checkDedicatedFileName(pApplication, aid);
			// The scheme has to describe the same account as the card
			// number, and the card number is the one of the first
			// application that carried a track: a later application would
			// otherwise report its own scheme next to another account, and
			// an application that carried none says nothing about the card
			if (ret && getTemplate().getCard().getType() == null) {
				// Where the type comes from: the scheme of the AID before
				// the CB -> PAN substitution, and the AID itself
				getTemplate().getCard().setAidType(EmvCardScheme.getCardTypeByAid(aid));
				getTemplate().getCard().setTypeAid(dedicatedFileName);
				getTemplate().getCard().setType(findCardScheme(aid, getTemplate().getCard().getCardNumber()));
			}
			if (dedicatedFileName != null) {
				pApplication.setAid(dedicatedFileName);
			}
			// The kernel the card asked for, or the one its AID implies
			deriveKernelFromAid(pApplication);
			pApplication.setApplicationLabel(applicationLabel);
			// The transaction log is located by the Log Entry of the File
			// Control Information and read with its own commands, it does not
			// depend on the records of the Application File Locator. The
			// details of the reading (format, records, status words) land on
			// the application
			pApplication.setListTransactions(extractLogEntry(getLogEntry(data), pApplication));
			// The application-aware overloads also record the status word
			// of each GET DATA on the application
			pApplication.setLeftPinTry(getLeftPinTry(pApplication));
			pApplication.setTransactionCounter(getTransactionCounter(pApplication));
			if (getTemplate().getConfig().readExtendedData) {
				pApplication.setLastOnlineTransactionCounter(getLastOnlineTransactionCounter(pApplication));
				pApplication.setOfflineBalance(getOfflineBalance(pApplication));
				// The data envelopes are read after the offline balance:
				// both use the tag '9F79' and the balance owns it on an
				// application that implements electronic cash
				extractDataEnvelopes(pApplication);
			}
			if (ret) {
				getTemplate().getCard().setState(CardStateEnum.ACTIVE);
			}
		}
		return ret;
	}

	/**
	 * Method used to check the Dedicated File Name (tag '84') the card returned
	 * against the AID that was selected.
	 * <p>
	 * EMV 4.3 Book 1 section 12.4 asks the terminal to check that the name
	 * returned in the File Control Information matches the AID of the final
	 * SELECT: exactly for an application selected with its full AID, as a
	 * prefix for one selected with a partial AID. The mismatch is recorded on
	 * the application and never acted upon, a reader reports what it saw
	 * instead of refusing to read.
	 * </p>
	 *
	 * @param pApplication
	 *            application being read
	 * @param pDedicatedFileName
	 *            name the card returned, hexadecimal
	 */
	protected void checkDedicatedFileName(final Application pApplication, final String pDedicatedFileName) {
		byte[] selected = pApplication.getAid();
		if (selected == null || pDedicatedFileName == null || pDedicatedFileName.length() == 0) {
			return;
		}
		String requested = BytesUtils.bytesToStringNoSpace(selected);
		// EMV 4.3 Book 1 draws the line between the two selection paths.
		// Section 12.3.3 step 3, the list of AIDs: "The DF Name will either be
		// identical to the AID (including the length), or the DF Name will
		// start with the AID but will be longer", a partial name selection.
		// Section 12.4, the final SELECT of an ADF Name found in a directory:
		// "the AID used in the final SELECT command exactly matches the DF Name
		// (tag '84') returned by the ICC in the FCI".
		boolean partial = selected.length == EmvTemplate.RID_LENGTH || pApplication.isNextOccurrence();
		boolean matches = partial ? pDedicatedFileName.startsWith(requested) : pDedicatedFileName.equals(requested);
		if (!matches) {
			LOGGER.warn("The card answered the name " + pDedicatedFileName + " to the selection of " + requested);
			pApplication.setDedicatedFileNameMismatch(true);
		}
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
			// A french card carries the network of its co-branded scheme in
			// its card number. When the number matches none of them the card
			// is still a Cartes Bancaires one: the lookup refines the answer,
			// it never invalidates the one the AID already gave
			EmvCardScheme byNumber = EmvCardScheme.getCardTypeByCardNumber(pCardNumber);
			if (byNumber != null) {
				type = byNumber;
				LOGGER.debug("Real type:" + type.getName());
			}
		}
		return type;
	}



	/**
	 * Method used to know if a response to a GET PROCESSING OPTIONS holds
	 * something to parse.
	 * <p>
	 * The card answers either a Response Message Template Format 1 ('80'),
	 * whose value begins with the Application Interchange Profile, or a Format
	 * 2 ('77') holding tagged data objects (EMV 4.3 Book 3 section 6.5.8.4).
	 * A data field that holds no readable data object at all gives the reader
	 * nothing to work with, whatever the status word says.
	 * </p>
	 *
	 * @param pResponse
	 *            response of the card, status word included
	 * @return true when the response holds at least one readable data object
	 */
	protected boolean holdsProcessingOptions(final byte[] pResponse) {
		byte[] data = ResponseUtils.getDataField(pResponse);
		if (data.length == 0) {
			return false;
		}
		// Any data object at all is enough. EMV 4.3 Book 3 section 6.5.8.4
		// names the templates '80' and '77', but a card answering its
		// processing options in a '70' does exist and is read like any other:
		// only a response holding nothing readable is worth a second command
		return TlvUtil.getNextTLV(new BerTlvInputStream(data)) != null;
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
		// The File Control Information of the application, as answered
		pApplication.setRawFci(pSelectResponse);
		// Get PDOL
		byte[] pdol = TlvUtil.getValue(pSelectResponse, EmvTags.PDOL);
		pApplication.setRawPdol(pdol);
		pApplication.setPdol(TlvUtil.parseTagAndLength(pdol));
		// Extract every data object provided by the SELECT response before
		// anything else is asked of the card: an application that refuses the
		// GET PROCESSING OPTIONS still told its language preference, its code
		// table, its issuer... in its File Control Information
		extractExtendedData(pSelectResponse, pApplication);
		// Send GPO Command
		byte[] gpo = getGetProcessingOptions(pdol, pApplication);

		// Check empty PDOL
		// A card that answers '9000' with no processing options at all has
		// failed just as surely as one that refused the command, and the same
		// two fallbacks are worth trying
		if (!ResponseUtils.isSucceedOrWarning(gpo) || !holdsProcessingOptions(gpo)) {
			if (pdol != null) {
				// The GPO built from the PDOL was refused, it is sent again
				// with an empty command template
				pApplication.setGpoRetriedWithoutPdol(true);
				gpo = getGetProcessingOptions(null, pApplication);
			}

			// Check response
			if (pdol == null || !ResponseUtils.isSucceedOrWarning(gpo) || !holdsProcessingOptions(gpo)) {
				// Try to read EF 1 and record 1
				pApplication.setGpoReplacedByReadRecord(true);
				gpo = getTemplate().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, 1, 0x0C, 0).toBytes());
				// The fallback record is a READ RECORD like those of the AFL
				pApplication.getRecords().add(new RecordData(1, 1, ResponseUtils.getDataField(gpo), EmvTemplate.getStatusWord(gpo)));
				if (!ResponseUtils.isSucceedOrWarning(gpo)) {
					pApplication.setGpoStatusWord(EmvTemplate.getStatusWord(gpo));
					// The texts of the File Control Information are decoded
					// with the code table it named
					decodeTextData(pApplication);
					return false;
				}
			}
		}
		// The status word of the response that is parsed as GPO response
		pApplication.setGpoStatusWord(EmvTemplate.getStatusWord(gpo));
		// Same here, a data object never holds the status bytes
		gpo = ResponseUtils.getDataField(gpo);
		pApplication.setRawGpoResponse(gpo);
		// Update Reading state
		pApplication.setReadingStep(ApplicationStepEnum.READ);

		// Extract commons card data (number, expire date, ...)
		boolean ret = extractCommonsCardData(gpo, pApplication);
		// Every response has been read, the Issuer Code Table Index is known
		decodeTextData(pApplication);

		return ret;
	}

	/**
	 * Method used to extract commons card data
	 *
	 * @param pGpo
	 *            global processing options response
	 * @return true if the extraction succeed
	 * @throws CommunicationException communication error
	 * @deprecated the data objects of the specification are now extracted into
	 *             the application being read, use
	 *             {@link #extractCommonsCardData(byte[], Application)}. This
	 *             overload keeps the previous signature so that the code
	 *             calling it still compiles and still links, but it is not
	 *             behaviour preserving:
	 *             <ul>
	 *             <li>what lands on the {@link com.github.devnied.emvnfccard.model.EmvCard}
	 *             is unchanged (track 1, track 2, cardholder name, BIC and
	 *             IBAN), the per application data objects are extracted into a
	 *             throwaway application and lost</li>
	 *             <li>the whole Application File Locator is read unless
	 *             {@code Config.readAllRecords} is turned off, where this
	 *             method used to stop as soon as a record yielded a track: the
	 *             same card answers more READ RECORD commands</li>
	 *             <li>a card exposing no track at all now has its track 2
	 *             rebuilt from the Application PAN, so the method returns true
	 *             where it used to return false</li>
	 *             <li>a subclass that <i>overrides</i> this method is no
	 *             longer called by the parser, whose single call site uses the
	 *             two arguments version: move the override there</li>
	 *             </ul>
	 */
	@Deprecated
	protected boolean extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
		// The implementation of this class is called, and not the two
		// arguments method: a subclass that moved its override there and whose
		// override calls this one would otherwise recurse until the stack ends
		return extractCardData(pGpo, new Application());
	}

	/**
	 * Method used to extract commons card data
	 *
	 * @param pGpo
	 *            global processing options response
	 * @param pApplication
	 *            application to fill with the extracted data objects
	 * @return true if the extraction succeed
	 * @throws CommunicationException communication error
	 */
	protected boolean extractCommonsCardData(final byte[] pGpo, final Application pApplication) throws CommunicationException {
		return extractCardData(pGpo, pApplication);
	}

	/**
	 * Implementation of the extraction, called by both
	 * {@link #extractCommonsCardData(byte[], Application)} and the deprecated
	 * overload it replaces.
	 *
	 * @param pGpo
	 *            global processing options response
	 * @param pApplication
	 *            application to fill with the extracted data objects
	 * @return true if the extraction succeed
	 * @throws CommunicationException communication error
	 */
	private boolean extractCardData(final byte[] pGpo, final Application pApplication) throws CommunicationException {
		boolean ret = false;
		boolean readAllRecords = getTemplate().getConfig().readAllRecords;
		// Extract data from Message Template 1
		byte data[] = TlvUtil.getValue(pGpo, EmvTags.RESPONSE_MESSAGE_TEMPLATE_1);
		if (data != null) {
			// Format 1 has no tag, the AIP is the first 2 bytes and the AFL
			// everything that follows
			if (data.length >= 2 && pApplication.getInterchangeProfile() == null) {
				pApplication.setInterchangeProfile(new ApplicationInterchangeProfile(data));
			}
			data = ArrayUtils.subarray(data, 2, data.length);
		} else { // Extract AFL data from Message template 2
			ret = extractTrackData(getTemplate().getCard(), pGpo);
			if (!ret || readAllRecords) {
				data = TlvUtil.getValue(pGpo, EmvTags.APPLICATION_FILE_LOCATOR);
			}
		}
		extractExtendedData(pGpo, pApplication);

		if (data != null) {
			// The locator as the card sent it, every entry included
			pApplication.setRawAfl(data);
			pApplication.getAfl().addAll(parseAflEntries(data));
			// Extract Afl
			List<Afl> listAfl = extractAfl(data);
			// The Application File Locator is card controlled: an entry may
			// declare 255 records and a response may hold dozens of entries,
			// so the total number of records read is bounded. Without it a
			// single malformed locator turns one tap into thousands of
			// exchanges, all the more since the whole file is read by default.
			int records = 0;
			// for each AFL
			for (Afl afl : listAfl) {
				// check all records
				for (int index = afl.getFirstRecord(); index <= afl.getLastRecord() && records <= MAX_RECORDS; index++) {
					if (++records > MAX_RECORDS) {
						LOGGER.warn("The Application File Locator declares more than " + MAX_RECORDS
								+ " records, the remaining ones are not read");
						pApplication.setAflTruncated(true);
						break;
					}
					byte[] info = getTemplate().getProvider()
							.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, 0).toBytes());
					// Every exchange is kept, whatever the card answered
					pApplication.getRecords()
							.add(new RecordData(afl.getSfi(), index, ResponseUtils.getDataField(info), EmvTemplate.getStatusWord(info)));
					pApplication.setRecordsRead(pApplication.getRecordsRead() + 1);
					// Extract card data
					if (ResponseUtils.isSucceedOrWarning(info)) {
						info = ResponseUtils.getDataField(info);
						extractExtendedData(info, pApplication);
						if (extractTrackData(getTemplate().getCard(), info)) {
							ret = true;
							// Stop as soon as the card number is known unless
							// the whole application file has to be read
							if (!readAllRecords) {
								return true;
							}
						}
					}
				}
			}
		}
		// A lot of cards do not expose the track 2 equivalent data in
		// contactless, rebuild it from the PAN and the expiration date
		if (!ret) {
			ret = buildTrackFromApplicationData(pApplication);
		}
		return ret;
	}

	/**
	 * Method used to rebuild the track 2 data from the Application PAN (tag
	 * '5A'), the Application Expiration Date (tag '5F24') and the Service Code
	 * (tag '5F30') when the card does not provide any track.
	 *
	 * @param pApplication
	 *            application holding the extracted data objects
	 * @return true if a card number could be rebuilt
	 */
	protected boolean buildTrackFromApplicationData(final Application pApplication) {
		if (pApplication.getPan() == null || getTemplate().getCard().getTrack2() != null) {
			return false;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("No track data found, rebuild track 2 from the application data");
		}
		EmvTrack2 track2 = new EmvTrack2();
		track2.setCardNumber(pApplication.getPan());
		track2.setExpireDate(pApplication.getExpirationDate());
		track2.setService(pApplication.getServiceCode());
		track2.setServiceCode(pApplication.getServiceCodeDigits());
		getTemplate().getCard().setTrack2(track2);
		return true;
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
		for (Afl afl : parseAflEntries(pAfl)) {
			if (afl.isValid()) {
				list.add(afl);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Skip invalid AFL entry: SFI " + afl.getSfi() + " records " + afl.getFirstRecord() + " to "
						+ afl.getLastRecord());
			}
		}
		return list;
	}

	/**
	 * Method used to parse every entry of an Application File Locator, the
	 * invalid ones included.
	 * <p>
	 * EMV 4.3 Book 3 section 10.2 codes each entry on 4 bytes: the SFI in the
	 * five high order bits of the first byte, the first and last record
	 * numbers, then the number of records taking part in the offline data
	 * authentication. Unlike {@link #extractAfl(byte[])}, which drives the
	 * reading and keeps the valid entries alone, this method reports the
	 * locator as the card sent it: {@link Afl#isValid()} tells the entries
	 * apart. A trailing incomplete entry is ignored.
	 * </p>
	 *
	 * @param pAfl
	 *            AFL data (value of the tag '94', or bytes 3 and following of
	 *            the Response Message Template Format 1), may be null
	 * @return every entry in card order, never null
	 */
	protected List<Afl> parseAflEntries(final byte[] pAfl) {
		return TagValueDecoder.parseAflEntries(pAfl);
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
		return getGetProcessingOptions(pPdol, null);
	}

	/**
	 * Method used to create GPO command and execute it, recording on the
	 * application what was sent.
	 * <p>
	 * The terminal value built for each entry of the PDOL (tag '9F38') lands in
	 * {@link Application#getPdolTerminalValues()}, keyed by the hexadecimal
	 * tag of the entry (the map is cleared first, so that it reflects the
	 * command whose response is parsed), and the data field of the command in
	 * {@link Application#getGpoCommandTemplate()}. When the PDOL asks for more
	 * than a command can carry the template stays null and the map holds the
	 * values that were built.
	 * </p>
	 *
	 * @param pPdol
	 *            PDOL raw data, null for an empty command template
	 * @param pApplication
	 *            application being read, may be null
	 * @return return data
	 * @throws CommunicationException communication error
	 */
	protected byte[] getGetProcessingOptions(final byte[] pPdol, final Application pApplication) throws CommunicationException {
		// List Tag and length from PDOL
		List<TagAndLength> list = TlvUtil.parseTagAndLength(pPdol);
		Map<String, byte[]> terminalValues = pApplication != null ? pApplication.getPdolTerminalValues() : null;
		if (terminalValues != null) {
			terminalValues.clear();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(EmvTags.COMMAND_TEMPLATE.getTagBytes()); // COMMAND
			// TEMPLATE
			// The length of the command template is a BER length (EMV 4.3
			// Book 3 Annex B2): from 128 bytes on it takes the long form, a
			// single byte would announce a number of following length bytes
			// that are not there
			out.write(BerTlvInputStream.encodeLength(TlvUtil.getLength(list)));
			for (TagAndLength tl : list) {
				byte[] value = getTemplate().getTerminal().constructValue(tl);
				if (terminalValues != null && tl.getTag() != null && value != null) {
					String key = BytesUtils.bytesToStringNoSpace(tl.getTag().getTagBytes()).toUpperCase(Locale.ENGLISH);
					if (!terminalValues.containsKey(key)) {
						terminalValues.put(key, value);
					}
				}
				out.write(value);
			}
		} catch (IOException ioe) {
			LOGGER.error("Construct GPO Command:" + ioe.getMessage(), ioe);
		}
		byte[] command;
		try {
			command = new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes();
		} catch (IllegalArgumentException e) {
			// A malformed PDOL may ask for more than a short APDU can carry:
			// answer the card with an empty command template rather than
			// aborting the whole reading, the caller of this method already
			// falls back on that
			LOGGER.warn("The PDOL asks for more data than a command can carry: " + e.getMessage());
			return BytesUtils.fromString("6700");
		}
		if (pApplication != null) {
			pApplication.setGpoCommandTemplate(out.toByteArray());
		}
		return getTemplate().getProvider().transceive(command);
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
		// The first track found is the one of the highest priority application,
		// the records and the applications read afterwards must neither erase
		// it nor replace it with a companion or expired account
		byte[] rawTrack1 = TlvUtil.getValue(pData, EmvTags.TRACK1_DATA);
		EmvTrack1 track1 = TrackUtils.extractTrack1Data(rawTrack1);
		if (track1 != null && pEmvCard.getTrack1() == null) {
			pEmvCard.setTrack1(track1);
		}
		// A track the card sent but that could not be parsed is kept as it
		// is, the first one only
		if (rawTrack1 != null && track1 == null && pEmvCard.getUnparsedTrack1() == null) {
			pEmvCard.setUnparsedTrack1(rawTrack1);
		}
		byte[] rawTrack2 = TlvUtil.getValue(pData, EmvTags.TRACK_2_EQV_DATA, EmvTags.TRACK2_DATA);
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(rawTrack2);
		if (track2 != null && pEmvCard.getTrack2() == null) {
			pEmvCard.setTrack2(track2);
		}
		if (rawTrack2 != null && track2 == null && pEmvCard.getUnparsedTrack2() == null) {
			pEmvCard.setUnparsedTrack2(rawTrack2);
		}
		extractTrackDiscretionaryData(pEmvCard, pData);
		return track1 != null || track2 != null;
	}

	/**
	 * Method used to read the discretionary data objects a card holds next to
	 * its tracks: the Track 1 Discretionary Data (tag '9F1F') and the Track 2
	 * Discretionary Data (tag '9F20').
	 * <p>
	 * EMV 4.4 Book 3 Annex A1 describes them as the "Discretionary part of
	 * track 1 according to ISO/IEC 7813" and the "Discretionary part of track 2
	 * according to ISO/IEC 7813", both read from the card with the READ RECORD
	 * command. They carry the discretionary field alone, without any sentinel,
	 * separator or Longitudinal Redundancy Check, so they say nothing of the
	 * account they belong to: a card sending one without the matching track
	 * leaves nothing to attach it to, and the value is dropped rather than
	 * turned into a track holding no account number.
	 * </p>
	 * <p>
	 * The discretionary data read inside the track itself is kept when the two
	 * sources disagree: the track is the field the payment system reads, and
	 * the card is free to answer these data objects from another record than
	 * the one the track was read in.
	 * </p>
	 *
	 * @param pEmvCard
	 *            Card data
	 * @param pData
	 *            data send by card
	 */
	protected void extractTrackDiscretionaryData(final EmvCard pEmvCard, final byte[] pData) {
		// The track objects live on the card, not on the application, so the
		// one read in an earlier application is still there. A standalone
		// discretionary data object belongs to the application that sent it:
		// merging it into another application's track would describe another
		// account. It is kept whole on the card instead. Within one
		// application the objects may come from different records, which is
		// why the scope is the application and not the response
		EmvTrack1 track1 = track1FromAnotherApplication ? null : pEmvCard.getTrack1();
		byte[] rawTrack1DiscretionaryData = TlvUtil.getValue(pData, EmvTags.TRACK1_DISCRETIONARY_DATA);
		if (track1 != null && rawTrack1DiscretionaryData != null && track1.getRawDiscretionaryData() == null) {
			track1.setRawDiscretionaryData(rawTrack1DiscretionaryData);
			if (track1.getDiscretionaryData() == null) {
				track1.setDiscretionaryData(TrackUtils.extractTrack1DiscretionaryData(rawTrack1DiscretionaryData));
			}
		}
		// A discretionary data object with no track to attach it to is kept
		// on the card as it is, the first one only
		if (track1 == null && rawTrack1DiscretionaryData != null && pEmvCard.getOrphanTrack1DiscretionaryData() == null) {
			pEmvCard.setOrphanTrack1DiscretionaryData(rawTrack1DiscretionaryData);
		}
		EmvTrack2 track2 = track2FromAnotherApplication ? null : pEmvCard.getTrack2();
		byte[] rawTrack2DiscretionaryData = TlvUtil.getValue(pData, EmvTags.TRACK2_DISCRETIONARY_DATA);
		if (track2 != null && rawTrack2DiscretionaryData != null && track2.getRawDiscretionaryData() == null) {
			track2.setRawDiscretionaryData(rawTrack2DiscretionaryData);
			if (track2.getDiscretionaryData() == null) {
				track2.setDiscretionaryData(TrackUtils.extractTrack2DiscretionaryData(rawTrack2DiscretionaryData));
			}
		}
		if (track2 == null && rawTrack2DiscretionaryData != null && pEmvCard.getOrphanTrack2DiscretionaryData() == null) {
			pEmvCard.setOrphanTrack2DiscretionaryData(rawTrack2DiscretionaryData);
		}
	}

}
