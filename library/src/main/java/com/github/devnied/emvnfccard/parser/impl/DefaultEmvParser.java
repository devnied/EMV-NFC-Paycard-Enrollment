package com.github.devnied.emvnfccard.parser.impl;

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
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EMVTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.iso7816emv.TagValueFactory;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.IParser;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TLVUtil;

import fr.devnied.bitlib.BitUtils;

/**
 * Parser Implementation for EMV card
 * 
 * @author julien Millau
 * 
 */
public class DefaultEmvParser implements IParser {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEmvParser.class);

	/**
	 * Singleton instance
	 */
	private static final DefaultEmvParser INSTANCE = new DefaultEmvParser();

	/**
	 * Get the singleton instance
	 * 
	 * @return the unique instance
	 */
	public static DefaultEmvParser getInstance() {
		return INSTANCE;
	}

	/**
	 * Method used to parse EMV card
	 */
	@Override
	public EMVCard parse(final byte[] pSelectResponse, final IProvider pProvider) throws CommunicationException {
		EMVCard card = new EMVCard();
		// Get TLV log entry
		byte[] logEntry = TLVUtil.getValue(pSelectResponse, EMVTags.LOG_ENTRY);
		// Get PDOL
		byte[] pdol = TLVUtil.getValue(pSelectResponse, EMVTags.PDOL);
		// send GPO Command
		byte[] gpo = getGetProcessingOptions(pdol, pProvider);

		// check empty PDOL
		if (!ResponseUtils.isSucceed(gpo)) {
			gpo = getGetProcessingOptions(null, pProvider);
		}

		// Extract commons card data (number, expire date, ...)
		extractCommonsCardData(pProvider, card, gpo);

		// Extract log entry
		extractLogEntry(pProvider, card, logEntry);

		return card;
	}

	/**
	 * Method used to extract commons card data
	 * 
	 * @param pProvider
	 *            provider
	 * @param pCard
	 *            Card data
	 * @param pGpo
	 *            global processing options response
	 */
	private void extractCommonsCardData(final IProvider pProvider, final EMVCard pCard, final byte[] pGpo)
			throws CommunicationException {
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
			boolean found = false;
			// for each Afl
			for (Afl afl : listAfl) {
				// check all records
				for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
					byte[] info = pProvider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4, 0)
							.toBytes());
					if (info[info.length - 2] == (byte) 0x6C) {
						info = pProvider.transceive(new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4,
								info[info.length - 1]).toBytes());
					}

					// Extract card data
					if (ResponseUtils.isSucceed(info)) {
						try {
							if (found = extractCardData(pCard, info)) {
								break;
							}
						} catch (Exception e) {
							// do nothing
						}
					}
				}
				if (found) {
					break;
				}
			}

		} else { // If Response Message Template Format 2. Extract data
			try {
				extractCardData(pCard, pGpo);
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Method used to extract log entry from card
	 * 
	 * @param pProvider
	 *            provider to used
	 * @param pCard
	 *            card data
	 * @param pLogEntry
	 *            log entry position
	 */
	private void extractLogEntry(final IProvider pProvider, final EMVCard pCard, final byte[] pLogEntry)
			throws CommunicationException {
		// If log entry is defined
		if (pLogEntry != null) {
			List<EMVPaymentRecord> listRecord = new ArrayList<EMVPaymentRecord>();
			// read all records
			for (int rec = 1; rec < pLogEntry[1]; rec++) {
				byte[] response = pProvider.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, pLogEntry[0] << 3 | 4, 0)
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
			// Add list of record
			pCard.setListPayment(listRecord);
		}
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
	 * Extract card data (card number + expire date)
	 * 
	 * @param pCard
	 *            card object
	 * @param pData
	 */
	protected boolean extractCardData(final EMVCard pCard, final byte[] pData) {
		boolean ret = false;
		byte[] track2 = TLVUtil.getValue(pData, EMVTags.TRACK_2_EQV_DATA);
		if (track2 != null) {
			BitUtils bit = new BitUtils(track2);
			// Read card number
			pCard.setCardNumber(bit.getNextHexaString(64));
			// Read expire date
			SimpleDateFormat sdf = new SimpleDateFormat("'D'yyMM", Locale.getDefault());
			try {
				String date = bit.getNextHexaString(5 * 4);
				// fit template "'D'yyMM"
				if (date.length() > 5) {
					date = date.substring(0, 5);
				}
				pCard.setExpireDate(DateUtils.truncate(sdf.parse(date), Calendar.MONTH));
				ret = true;
			} catch (ParseException e) {
				LOGGER.error("Unparsable expire card date");
			}
		}
		return ret;
	}

	/**
	 * Method used to get GPO
	 * 
	 * @param pPdol
	 *            PDOL data
	 * @param pProvider
	 *            provider
	 * @return return data
	 */
	protected byte[] getGetProcessingOptions(final byte[] pPdol, final IProvider pProvider) throws CommunicationException {
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

	/**
	 * Private constructor
	 */
	private DefaultEmvParser() {
	}
}
