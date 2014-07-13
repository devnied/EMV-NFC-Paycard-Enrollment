package com.github.devnied.emvnfccard.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EMVCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EMVTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.iso7816emv.TagValueFactory;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TLVUtil;

import fr.devnied.bitlib.BitUtils;
import fr.devnied.bitlib.BytesUtils;

/**
 * Emv Parser.<br/>
 * Class used to read and parse EMV card
 * 
 * @author MILLAU Julien
 * 
 */
public class EMVParser {

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EMVParser.class);

	/**
	 * PPSE directory "2PAY.SYS.DDF01"
	 */
	private static final byte[] PPSE = BytesUtils.fromString("32 50 41 59 2e 53 59 53 2e 44 44 46 30 31");

	/**
	 * PSE directory "1PAY.SYS.DDF01"
	 */
	private static final byte[] PSE = BytesUtils.fromString("31 50 41 59 2e 53 59 53 2e 44 44 46 30 31");

	/**
	 * Provider
	 */
	private IProvider provider;

	/**
	 * use contact less mode
	 */
	private boolean contactLess;

	/**
	 * Constructor
	 * 
	 * @param pProvider
	 *            provider to launch command
	 * @param pContactLess
	 *            boolean to indicate if the EMV card is contact less or not
	 */
	public EMVParser(final IProvider pProvider, final boolean pContactLess) {
		provider = pProvider;
		contactLess = pContactLess;
	}

	/**
	 * Method used to read public data from EMV card
	 * 
	 * @return data read from card or null if any provider match the card type
	 */
	public EMVCard readEmvCard() throws CommunicationException {
		// use PSE first
		EMVCard card = readWithPSE();
		// Find with AID
		if (card == null) {
			card = readWithAID();
		}
		return card;
	}

	/**
	 * Method used to select payment environment PSE or PPSE
	 * 
	 * @return response byte array
	 * @throws CommunicationException
	 */
	protected byte[] selectPaymentEnvironment() throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.info("Select " + (contactLess ? "PPSE" : "PSE") + " Application");
		}
		// Select the PPSE or PSE directory
		return provider.transceive(new CommandApdu(CommandEnum.SELECT, contactLess ? PPSE : PSE, 0).toBytes());
	}

	/**
	 * Method used to parse FCI Proprietary Template
	 * 
	 * @param pData
	 *            data to parse
	 * @return
	 * @throws CommunicationException
	 */
	protected byte[] parseFCIProprietaryTemplate(final byte[] pData) throws CommunicationException {
		// Get SFI
		byte[] data = TLVUtil.getValue(pData, EMVTags.SFI);

		// Check SFI
		if (data != null) {
			int sfi = BytesUtils.byteArrayToInt(data);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info("SFI found:" + sfi);
			}
			data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, 0).toBytes());
			// If LE is not correct
			if (data[data.length - 2] == (byte) 0x6C) {
				data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, sfi << 3 | 4, data[data.length - 1])
						.toBytes());
			}
			return data;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.info("(FCI) Issuer Discretionary Data is already present");
		}
		return pData;
	}

	/**
	 * Method used to extract application label
	 * 
	 * @return decoded application label or null
	 */
	protected String extractApplicationLabel(final byte[] pData) {
		String label = null;
		byte[] labelByte = TLVUtil.getValue(pData, EMVTags.APPLICATION_LABEL);
		if (labelByte != null) {
			label = new String(labelByte);
		}
		return label;
	}

	/**
	 * Read EMV card with Payment System Environment or Proximity Payment System Environment
	 * 
	 * @return Read card
	 */
	protected EMVCard readWithPSE() throws CommunicationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to read card with Payment System Environment");
		}
		EMVCard card = null;
		// Select the PPSE or PSE directory
		byte[] data = selectPaymentEnvironment();
		if (ResponseUtils.isSucceed(data)) {
			// Parse FCI Template
			data = parseFCIProprietaryTemplate(data);
			// Extract application label
			if (ResponseUtils.isSucceed(data)) {
				String label = extractApplicationLabel(data);
				// Get Card
				card = extractPublicData(TLVUtil.getValue(data, EMVTags.AID_CARD), label);
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug((contactLess ? "PPSE" : "PSE") + " not found -> Use kown AID");
		}

		return card;
	}

	/**
	 * Read EMV card with AID
	 * 
	 * @return Card read
	 */
	protected EMVCard readWithAID() throws CommunicationException {
		EMVCard card = null;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Try to read card with AID");
		}
		// Test each card from know EMV AID
		for (EMVCardScheme type : EMVCardScheme.values()) {
			card = extractPublicData(type.getAidByte(), type.getName());
			if (card != null) {
				break;
			}
		}
		return card;
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
		return provider.transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
	}

	/**
	 * Read public card data from parameter AID
	 * 
	 * @param pAid
	 *            card AID in bytes
	 * @param pScheme
	 *            card scheme (Name)
	 * @return card read or null
	 */
	protected EMVCard extractPublicData(final byte[] pAid, final String pScheme) throws CommunicationException {
		EMVCard ret = null;
		// Select AID
		byte[] data = selectAID(pAid);
		// check response
		if (ResponseUtils.isSucceed(data)) {
			// Parse select response
			ret = parse(data, provider);
			if (ret != null) {
				// Get AID
				String aid = BytesUtils.bytesToStringNoSpace(TLVUtil.getValue(data, EMVTags.DEDICATED_FILE_NAME));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Card type :" + pScheme + " with Aid:" + aid);
				}
				ret.setAid(aid);
				ret.setCardLabel(pScheme);
				ret.setType(findCardScheme(aid, ret.getCardNumber()));
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
	protected EMVCardScheme findCardScheme(final String pAid, final String pCardNumber) {
		EMVCardScheme type = EMVCardScheme.getCardTypeByAid(pAid);
		// Get real type for french card
		if (type == EMVCardScheme.CB) {
			type = EMVCardScheme.getCardTypeByCardNumber(pCardNumber);
			if (type != null) {
				LOGGER.info("Real type:" + type.getName());
			}
		}
		return type;
	}

	/**
	 * Method used to parse EMV card
	 */
	public EMVCard parse(final byte[] pSelectResponse, final IProvider pProvider) throws CommunicationException {
		EMVCard card = null;
		// Get TLV log entry
		byte[] logEntry = TLVUtil.getValue(pSelectResponse, EMVTags.LOG_ENTRY);
		// Get PDOL
		byte[] pdol = TLVUtil.getValue(pSelectResponse, EMVTags.PDOL);
		// Send GPO Command
		byte[] gpo = getGetProcessingOptions(pdol, pProvider);

		// Check empty PDOL
		if (!ResponseUtils.isSucceed(gpo)) {
			gpo = getGetProcessingOptions(null, pProvider);
		}

		// Extract commons card data (number, expire date, ...)
		card = extractCommonsCardData(gpo);

		// Extract log entry
		card.setListPayment(extractLogEntry(logEntry));

		return card;
	}

	/**
	 * Method used to extract commons card data
	 * 
	 * @param pCard
	 *            Card data
	 * @param pGpo
	 *            global processing options response
	 */
	protected EMVCard extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
		EMVCard card = null;
		// Extract data from Message Template 1
		byte data[] = TLVUtil.getValue(pGpo, EMVTags.RESPONSE_MESSAGE_TEMPLATE_1);
		if (data != null) {
			data = ArrayUtils.subarray(data, 2, data.length);
		} else { // Extract AFL data from Message template 2
			data = TLVUtil.getValue(pGpo, EMVTags.APPLICATION_FILE_LOCATOR);
		}

		if (data != null) {
			// Get SFI
			List<Afl> listAfl = extractAfl(data);
			// for each AFL
			for (Afl afl : listAfl) {
				// check all records
				for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
					byte[] info = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, 0)
							.toBytes());
					if (info[info.length - 2] == (byte) 0x6C) {
						info = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4,
								info[info.length - 1]).toBytes());
					}

					// Extract card data
					if (ResponseUtils.isSucceed(info)) {
						card = extractTrack2Data(info);
						if (card != null) {
							break;
						}
					}
				}
				if (card != null) {
					break;
				}
			}

		} else { // If Response Message Template Format 2. Extract data
			card = extractTrack2Data(pGpo);
		}
		return card;
	}

	/**
	 * Method used to extract log entry from card
	 * 
	 * @param pLogEntry
	 *            log entry position
	 */
	private List<EMVPaymentRecord> extractLogEntry(final byte[] pLogEntry) throws CommunicationException {
		List<EMVPaymentRecord> listRecord = new ArrayList<EMVPaymentRecord>();
		// If log entry is defined
		if (pLogEntry != null) {

			// read all records
			for (int rec = 1; rec <= pLogEntry[1]; rec++) {
				byte[] response = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, pLogEntry[0] << 3 | 4, 0)
						.toBytes());
				// Extract data
				if (ResponseUtils.isSucceed(response) && response.length >= EMVPaymentRecord.DEFAULT_SIZE / BitUtils.BYTE_SIZE) {
					EMVPaymentRecord record = new EMVPaymentRecord();
					record.parse(response);
					if (record != null) {
						// Unknown currency
						if (record.getCurrency() == null) {
							record.setCurrency(CurrencyEnum.XXX);
						}
						listRecord.add(record);
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
	 * Extract track 2 data (card number + expire date)
	 * 
	 * @param pData
	 *            data
	 */
	protected EMVCard extractTrack2Data(final byte[] pData) {
		EMVCard card = null;
		byte[] track2 = TLVUtil.getValue(pData, EMVTags.TRACK_2_EQV_DATA);
		if (track2 != null) {
			card = new EMVCard();
			BitUtils bit = new BitUtils(track2);
			// Read card number
			card.setCardNumber(bit.getNextHexaString(64));
			// Read expire date
			SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMM", Locale.getDefault());
			try {
				String date = bit.getNextHexaString(5 * 4);
				// fit template "'D'yyMM"
				if (date.length() > 5) {
					date = date.substring(0, 5);
				}
				card.setExpireDate(DateUtils.truncate(sdf.parse(date), Calendar.MONTH));
			} catch (ParseException e) {
				LOGGER.error("Unparsable expire card date");
			}
		}
		return card;
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
		List<TagAndLength> list = TLVUtil.parseTagAndLength(pPdol);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(EMVTags.COMMAND_TEMPLATE.getTagBytes()); // COMMAND TEMPLATE
			out.write(TLVUtil.getLength(list)); // ADD total length
			if (list != null) {
				for (TagAndLength tl : list) {
					out.write(TagValueFactory.constructValue(tl));
				}
			}
		} catch (IOException ioe) {
			LOGGER.error("Construct GPO Command:" + ioe.getMessage(), ioe);
		}
		return pProvider.transceive(new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes());
	}

}
