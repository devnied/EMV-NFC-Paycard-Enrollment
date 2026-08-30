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
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.GeldKarteData;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.EmvDataUtils;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import fr.devnied.bitlib.BytesUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * GeldKarte parser <br>
 * Documentation: <br>
 * - ftp://ftp.ccc.de/documentation/cards/geldkarte.pdf
 * - http://www.bensin.org/haxor/haxor_src/ic35/crap/stash/scinfo_khf%20-%20geldkarte%20ic35/doku/gk.log
 * - http://ftp.chaos-darmstadt.de/docs/cards/geldkarte.pdf
 * - https://code.google.com/p/android/issues/detail?id=62976
 * - http://www.openscdp.org/scripts/geldkarte/jsdoc/symbols/src/dump_girogo.js.html
 * - http://www.wrankl.de/UThings/Geldkarte.pdf
 * <p>
 * The scheme itself has been switched off: no card carrying the purse has been
 * issued since 2020, the acceptance ended on 31 December 2024 and the balances
 * could be refunded until 31 March 2025. The parser is kept for the cards that
 * are still around, it only handles the purse AIDs and leaves the girocard
 * applications, which are built on the same German RID, to {@link EmvParser}.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class GeldKarteParser extends AbstractParser {

	/**
	 * Class Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GeldKarteParser.class);

	/**
	 * Geldkarte pattern, built from the purse AIDs only.
	 * <p>
	 * The scheme entry also holds the bare German ZKA RID 'D276000025', which
	 * the girocard applications are built on: matching it would capture a live
	 * girocard card and keep it away from {@link EmvParser}, so it would be
	 * read as a purse instead of being read as the EMV application it is.
	 * </p>
	 */
	private static final Pattern PATTERN = Pattern.compile(buildPurseAidPattern());

	/**
	 * Minimum length of an EF_BETRAG response: a 3 bytes amount followed by
	 * the status word
	 */
	private static final int EF_BETRAG_SIZE = 5;

	/**
	 * Minimum length of an EF_ID response: 12 bytes of record (the card number
	 * and the expiration date are read up to the offset 11) followed by the
	 * status word
	 */
	private static final int EF_ID_SIZE = 14;

	/**
	 * Minimum length of an EF_BLOG record: 36 bytes of record (the time is read
	 * up to the offset 35) followed by the status word
	 */
	private static final int EF_BLOG_SIZE = 38;

	/**
	 * Length of the EF_ID record the documented layout covers: industry key
	 * (1), bank sort code (3), card number (5), check digit (1), expiration
	 * date (2), activation date (3), country (2), currency (3), currency unit
	 * (1), chip type (1) and OS version (2)
	 */
	private static final int EF_ID_RECORD_LENGTH = 24;

	/**
	 * Length of the EF_BETRAG record with its two limits: balance (3), maximum
	 * balance (3) and maximum transaction amount (3)
	 */
	private static final int EF_BETRAG_RECORD_LENGTH = 9;

	/**
	 * Number of decimals of the GeldKarte amounts, expressed in euro cents
	 */
	private static final int GELDKARTE_AMOUNT_SCALE = 2;

	/**
	 * Method used to build the pattern of the AIDs this parser handles: every
	 * GeldKarte AID but the Registered Application Provider Identifier, which
	 * is shared with the other German applications.
	 *
	 * @return the regular expression matching the purse AIDs
	 */
	private static String buildPurseAidPattern() {
		StringBuilder builder = new StringBuilder();
		for (String aid : EmvCardScheme.GELDKARTE.getAid()) {
			String value = StringUtils.deleteWhitespace(aid);
			// A RID alone identifies a provider, not an application
			if (value.length() <= EmvTemplate.RID_LENGTH * 2) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('|');
			}
			builder.append(value);
		}
		if (builder.length() == 0) {
			// An empty alternative would build "().*", a pattern matching every
			// AID: this parser would then claim every card of the reader
			throw new IllegalStateException("No GeldKarte purse AID declared in " + EmvCardScheme.GELDKARTE);
		}
		return "(" + builder + ").*";
	}

	public GeldKarteParser(EmvTemplate pTemplate) {
		super(pTemplate);
	}

	@Override
	public Pattern getId() {
		return PATTERN;
	}

	@Override
	public boolean parse(Application pApplication) throws CommunicationException {
		boolean ret = false;
		// The name the application is selected with is kept: the card may
		// answer another Dedicated File Name
		pApplication.setRequestedAid(pApplication.getAid());
		pApplication.setSelectedName(pApplication.getAid());
		// Select AID
		byte[] data = selectAID(pApplication.getAid());
		pApplication.setSelectStatusWord(statusWordOf(data));
		// Check response
		if (ResponseUtils.isSucceed(data)) {
			pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
			// The status bytes close the response, they are not data objects
			data = ResponseUtils.getDataField(data);
			if (pApplication.getRawFci() == null) {
				pApplication.setRawFci(data);
			}
			// Get TLV log entry
			byte[] logEntry = getLogEntry(data);
			// Get AID
			pApplication.setAid(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
			// Application label
			pApplication.setApplicationLabel(extractApplicationLabel(data));
			// Add type, and the AID it was resolved from
			EmvCardScheme scheme = EmvCardScheme.getCardTypeByAid(BytesUtils.bytesToStringNoSpace(pApplication.getAid()));
			// First application named wins, as in EmvParser: a purse read after
			// a payment application (a GeldKarte next to a girocard, both
			// selected when the whole AID list is walked) must not rename the
			// card after itself
			// The three fields are tested, not just the type: EmvParser writes
			// them together but any of them can stay null, an unknown scheme
			// leaving the type null and a missing DF Name the AID
			if (getTemplate().getCard().getType() == null && getTemplate().getCard().getAidType() == null
					&& getTemplate().getCard().getTypeAid() == null) {
				getTemplate().getCard().setType(scheme);
				getTemplate().getCard().setAidType(scheme);
				getTemplate().getCard().setTypeAid(pApplication.getAid());
			}
			// Extract Bank data (BIC/IBAN) and every additional data object of
			// the FCI (language preference, issuer country, ...)
			extractExtendedData(data, pApplication);

			decodeTextData(pApplication);

			// The purse files are kept whole on the application
			if (pApplication.getGeldKarte() == null) {
				pApplication.setGeldKarte(new GeldKarteData());
			}

			// read Ef_ID
			extractEF_ID(pApplication);

			// Read EF_BETRAG
			readEfBetrag(pApplication);

			// Read EF_BLOG
			readEF_BLOG(pApplication);

			pApplication.setLeftPinTry(getLeftPinTry(pApplication));
			pApplication.setTransactionCounter(getTransactionCounter(pApplication));
			// TODO remove this
			pApplication.getListTransactions().addAll(extractLogEntry(logEntry, pApplication));
			// Update ret
			getTemplate().getCard().setState(CardStateEnum.ACTIVE);
			ret = true;
		}

		return ret;
	}

	/**
	 * Read EF_BLOG
	 *
	 * @param pApplication emv application
	 * @throws CommunicationException communication error
	 */
	protected void readEF_BLOG(final Application pApplication) throws CommunicationException{
		List<EmvTransactionRecord> list = new ArrayList<EmvTransactionRecord>();
		// The locale is forced: the default calendar of the device may not be
		// the Gregorian one the card dates are expressed in
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		GeldKarteData purse = pApplication.getGeldKarte();
		// Read each records
		for (int i = 1; i < 16 ; i++) {
			byte[] data = getTemplate().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, i, 0xEC, 0).toBytes());
			// Check response
			if (ResponseUtils.isSucceed(data)) {
				// Every record answered is kept raw, the malformed ones too
				byte[] dataField = ResponseUtils.getDataField(data);
				if (purse != null) {
					purse.getRawEfBlogRecords().add(dataField);
				}
				// The record is read up to the offset 35 and is followed by the
				// status word: a shorter response would decode the status word
				// as an amount and a time
				if (data.length < EF_BLOG_SIZE){
					LOGGER.warn("Malformed EF_BLOG record: " + BytesUtils.bytesToStringNoSpace(data));
					continue;
				}
				EmvTransactionRecord record = new EmvTransactionRecord();
				record.setRaw(dataField);
				record.setRecordNumber(i);
				record.setCurrency(CurrencyEnum.EUR);
				record.setTransactionType(getType(data[0]));

				try {
					// Every field is packed BCD, a record holding another
					// encoding is skipped instead of aborting the whole read
					record.setAmount(Float.parseFloat(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(data, 21, 24))) / 100L);
					record.setDate(dateFormat.parse(String.format("%02x.%02x.%02x%02x", data[32], data[31], data[29], data[30])));
					record.setTime(timeFormat.parse(String.format("%02x:%02x:%02x", data[33], data[34], data[35])));
				} catch (ParseException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (NumberFormatException e) {
					LOGGER.warn("Unreadable amount in an EF_BLOG record: " + BytesUtils.bytesToStringNoSpace(data));
					continue;
				}
				list.add(record);
			} else {
				if (purse != null) {
					purse.setEfBlogEndStatusWord(statusWordOf(data));
				}
				break;
			}
		}
		pApplication.setListTransactions(list);
	}

	/**
	 * Method used to get the transaction type
	 * @param logstate the log state
	 * @return the transaction type or null
	 */
	protected TransactionTypeEnum getType(byte logstate) {
		switch ( (logstate & 0x60) >> 5) {
		case 0: return TransactionTypeEnum.LOADED;
		case 1: return TransactionTypeEnum.UNLOADED;
		case 2: return TransactionTypeEnum.PURCHASE;
		case 3: return TransactionTypeEnum.REFUND;
		}
		return null;
    }

	/**
	 * read EF_BETRAG
	 *
	 * @param pApplication EMV application
	 * @throws CommunicationException communication error
	 */
	protected void readEfBetrag(final Application pApplication) throws CommunicationException {
		// 00 B2 01 C4 00
		byte[] data = getTemplate().getProvider()
				.transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xC4, 0).toBytes());
		// The whole record and its status word are kept, whatever the outcome
		GeldKarteData purse = pApplication.getGeldKarte();
		if (purse != null) {
			purse.setEfBetragStatusWord(statusWordOf(data));
			byte[] record = ResponseUtils.getDataField(data);
			if (record.length > 0) {
				purse.setRawEfBetrag(record);
			}
			// Bytes 3-5 Maximalbetrag and bytes 6-8 maximaler Transaktionsbetrag,
			// packed BCD in euro cents like the balance
			if (ResponseUtils.isSucceed(data) && record.length >= EF_BETRAG_RECORD_LENGTH) {
				purse.setMaximumBalance(parseBcdAmount(Arrays.copyOfRange(record, 3, 6)));
				purse.setMaximumTransaction(parseBcdAmount(Arrays.copyOfRange(record, 6, 9)));
			}
		}
		// Check response, the record holds a 3 bytes amount followed by the
		// status word: a shorter one is a malformed record, not an amount
		if (ResponseUtils.isSucceed(data) && data.length >= EF_BETRAG_SIZE) {
			try {
				pApplication.setAmount(Float.parseFloat(String.format("%02x%02x%02x", data[0], data[1], data[2]))/100.0f);
			} catch (NumberFormatException e) {
				// The balance is packed BCD, another encoding is not an amount
				LOGGER.warn("Unreadable EF_BETRAG balance: " + BytesUtils.bytesToStringNoSpace(data));
			}
		} else if (ResponseUtils.isSucceed(data)) {
			LOGGER.warn("Malformed EF_BETRAG record: " + BytesUtils.bytesToStringNoSpace(data));
		}
	}

	/**
	 * Method used to decode a packed BCD amount of the purse (euro cents) into
	 * an exact amount.
	 *
	 * @param pValue
	 *            packed BCD digits
	 * @return the amount with 2 decimals, or null when the value is not packed
	 *         BCD
	 */
	protected BigDecimal parseBcdAmount(final byte[] pValue) {
		if (pValue == null || pValue.length == 0) {
			return null;
		}
		try {
			return new BigDecimal(new BigInteger(BytesUtils.bytesToStringNoSpace(pValue)), GELDKARTE_AMOUNT_SCALE);
		} catch (NumberFormatException e) {
			LOGGER.warn("Unreadable GeldKarte amount: " + BytesUtils.bytesToStringNoSpace(pValue));
			return null;
		}
	}

	/**
	 * Method used to extract Ef_iD record
	 *
	 * @param pApplication EMV application
	 *
	 * @throws CommunicationException communication error
	 */
	protected void extractEF_ID(final Application pApplication) throws CommunicationException {
		// 00B201BC00
		byte[] data = getTemplate().getProvider().transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xBC, 0).toBytes());
		// The whole record and its status word are kept, whatever the outcome
		GeldKarteData purse = pApplication.getGeldKarte();
		if (purse != null) {
			purse.setEfIdStatusWord(statusWordOf(data));
			byte[] record = ResponseUtils.getDataField(data);
			if (record.length > 0) {
				purse.setRawEfId(record);
			}
			if (ResponseUtils.isSucceed(data)) {
				decodeEfId(record, purse);
			}
		}
		if (ResponseUtils.isSucceed(data) && data.length < EF_ID_SIZE) {
			LOGGER.warn("Malformed EF_ID record: " + BytesUtils.bytesToStringNoSpace(data));
			return;
		}
		if (ResponseUtils.isSucceed(data)) {
			pApplication.setReadingStep(ApplicationStepEnum.READ);
			// Date, the locale is forced: the default calendar of the device
			// may not be the Gregorian one the card expiration date uses
			SimpleDateFormat format = new SimpleDateFormat("MM/yy", Locale.ENGLISH);

			// Track 2
			EmvTrack2 track2 = new EmvTrack2();
			track2.setCardNumber(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(data, 4, 9)));
			try {
				track2.setExpireDate(format.parse(String.format("%02x/%02x", data[11], data[10])));
			} catch (ParseException e) {
				LOGGER.error(e.getMessage(),e);
			}
			getTemplate().getCard().setTrack2(track2);
		}
	}

	/**
	 * Method used to decode the documented fields of an EF_ID record (SFI
	 * 0x17), see http://www.wrankl.de/UThings/Geldkarte.pdf:
	 * <ul>
	 * <li>byte 0: Branchenhauptschluessel, 0x67 for GeldKarte</li>
	 * <li>bytes 1-3: Kurz-BLZ, 6 BCD digits</li>
	 * <li>bytes 4-8: Kartennummer, 10 BCD digits</li>
	 * <li>byte 9: Pruefziffer in the high nibble</li>
	 * <li>bytes 10-11: Verfalldatum, read by {@link #extractEF_ID(Application)}
	 * into the track 2</li>
	 * <li>bytes 12-14: Aktivierungsdatum YY MM DD, packed BCD</li>
	 * <li>bytes 15-16: Laenderkennzeichen, ISO 3166 numeric, packed BCD</li>
	 * <li>bytes 17-19: Waehrungskennzeichen, ASCII</li>
	 * <li>byte 20: Wertigkeit der Waehrung</li>
	 * <li>byte 21: Chiptyp</li>
	 * <li>bytes 22-23: OS version, binary</li>
	 * </ul>
	 * Every field is bounds checked, a record too short for a field leaves it
	 * unset, and a field that is not encoded as documented is reported as
	 * unknown instead of aborting the reading.
	 *
	 * @param pRecord
	 *            data field of the EF_ID record
	 * @param pPurse
	 *            purse data to fill
	 */
	protected void decodeEfId(final byte[] pRecord, final GeldKarteData pPurse) {
		if (pRecord == null || pPurse == null) {
			return;
		}
		if (pRecord.length > 0) {
			pPurse.setIndustryKey(pRecord[0] & 0xFF);
		}
		if (pRecord.length >= 4) {
			pPurse.setBankSortCode(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pRecord, 1, 4)));
		}
		if (pRecord.length >= 9) {
			pPurse.setCardNumber(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pRecord, 4, 9)));
		}
		if (pRecord.length >= 10) {
			pPurse.setCheckDigit((pRecord[9] & 0xF0) >> 4);
		}
		if (pRecord.length >= 15) {
			// The locale is forced: the default calendar of the device may not
			// be the Gregorian one the card dates are expressed in. The parsing
			// is strict, the bytes are not a date if they are not BCD digits
			SimpleDateFormat format = new SimpleDateFormat("yyMMdd", Locale.ENGLISH);
			format.setLenient(false);
			String date = BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(pRecord, 12, 15));
			try {
				pPurse.setActivationDate(format.parse(date));
			} catch (ParseException e) {
				LOGGER.warn("Unreadable EF_ID activation date: " + date);
			}
		}
		if (pRecord.length >= 17) {
			pPurse.setCountryCode(EmvDataUtils.getNumericIntValue(Arrays.copyOfRange(pRecord, 15, 17), UNKNOW));
		}
		if (pRecord.length >= 20) {
			pPurse.setCurrency(EmvDataUtils.decodeText(Arrays.copyOfRange(pRecord, 17, 20), 0));
		}
		if (pRecord.length >= 21) {
			pPurse.setCurrencyUnit(pRecord[20] & 0xFF);
		}
		if (pRecord.length >= 22) {
			pPurse.setChipType(pRecord[21] & 0xFF);
		}
		if (pRecord.length >= EF_ID_RECORD_LENGTH) {
			pPurse.setOsVersion((int) EmvDataUtils.getBinaryValue(Arrays.copyOfRange(pRecord, 22, 24), UNKNOW));
		}
	}

}
