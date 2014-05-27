package com.github.devnied.emvnfccard.parser.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.IParser;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TLVUtils;

import fr.devnied.bitlib.BitUtils;
import fr.devnied.bitlib.BytesUtils;

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
		byte[] logEntry = TLVUtils.getArrayValue(pSelectResponse, TLVUtils.LOG_ENTRY);
		// Get PDOL
		byte[] pdol = TLVUtils.getArrayValue(pSelectResponse, TLVUtils.PDOL);
		// send GPO Command
		byte[] gpo = getGetProcessingOptions(pdol, pProvider);

		// The value of the AFL is normally ‘08 01 01 00 10 01 01 01 18 01 02 00’ for PayPass
		// see https://www.paypass.com/pdf/public_documents/Terminal%20Optimization%20v2-0.pdf
		if (!ResponseUtils.isSucceed(gpo)) {
			gpo = BytesUtils.fromString("08 01 01 00 10 01 01 01 18 01 02 00 20 01 02 00 90 00");
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
		// Get SFI
		List<Afl> listAfl = extractAfl(pGpo);
		boolean found = false;
		// for each Afl
		for (Afl afl : listAfl) {
			int sfi = afl.getSfi();
			// check all records
			for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
				byte[] info = pProvider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, index << 3 | 4, 0).toBytes());
				if (info[info.length - 2] == (byte) 0x6C) {
					info = pProvider.transceive(new CommandApdu(CommandEnum.READ_RECORD, sfi, index << 3 | 4,
							info[info.length - 1]).toBytes());
				}

				// Extract card data
				if (ResponseUtils.isSucceed(info)) {
					try {
						extractCardData(pCard, info);
						found = true;
						break;
					} catch (Exception e) {
						// do nothing
					}
				}
			}
			if (found) {
				break;
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
	 * Extract list of application file locator from GPO response
	 * 
	 * @param pGpo
	 *            GPO response
	 * @return list of AFL
	 */
	protected List<Afl> extractAfl(final byte[] pGpo) {
		List<Afl> list = new ArrayList<Afl>();
		if (pGpo.length - 4 > 0) {
			for (int i = 0; i < (pGpo.length - 4) / 4; i++) {
				Afl afl = new Afl();
				afl.setSfi(pGpo[4 + i * 4] >> 3);
				afl.setFirstRecord(pGpo[5 + i * 4]);
				afl.setLastRecord(pGpo[6 + i * 4]);
				afl.setOfflineAuthentication(pGpo[7 + i * 4] == 1);
				list.add(afl);
			}
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
	protected void extractCardData(final EMVCard pCard, final byte[] pData) {
		byte[] track2 = TLVUtils.getArrayValue(pData, TLVUtils.TRACK2);
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
			} catch (ParseException e) {
				LOGGER.error("Unparsable expire card date");
			}
		}

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
		byte[] data = null;
		// if PDOL Data
		if (pPdol != null) {
			data = new byte[pPdol.length + 2];
			data[0] = (byte) 0x83;
			data[1] = (byte) pPdol.length;
			System.arraycopy(pPdol, 0, data, 2, pPdol.length);
		} else {
			data = new byte[] { (byte) 0x83 };
		}
		return pProvider.transceive(new CommandApdu(CommandEnum.GPO, data, 0).toBytes());

	}

	/**
	 * Private constructor
	 */
	private DefaultEmvParser() {
	}
}
