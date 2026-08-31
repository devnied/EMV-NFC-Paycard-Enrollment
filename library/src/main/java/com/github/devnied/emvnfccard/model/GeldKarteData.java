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
package com.github.devnied.emvnfccard.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Purse files of a GeldKarte application, the German electronic purse.
 * <p>
 * The GeldKarte specification (ZKA "Schnittstellenspezifikation fuer die
 * ec-Karte mit Chip", Geldkarte part) places the purse data in three files
 * read with READ RECORD: EF_ID (SFI 0x17) identifies the card, EF_BETRAG (SFI
 * 0x18) holds the balance and the limits, and EF_BLOG (SFI 0x1D) holds the
 * loading and payment log. The layout of EF_ID is: byte 0
 * Branchenhauptschluessel, bytes 1-3 Kurz-BLZ (BCD), bytes 4-8 Kartennummer
 * (BCD), byte 9 high nibble Pruefziffer, bytes 12-14 Aktivierungsdatum
 * (YYMMDD BCD), bytes 15-16 Laenderkennzeichen (ISO 3166 numeric BCD), bytes
 * 17-19 Waehrungskennzeichen (ASCII), byte 20 Wertigkeit der Waehrung, byte
 * 21 Chiptyp, bytes 22-23 OS version.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class GeldKarteData extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 2214587309126473152L;

	/**
	 * Data field of READ RECORD 1 of EF_ID (SFI 0x17), null when not answered
	 */
	private byte[] rawEfId;

	/**
	 * Status bytes SW1 SW2 of the EF_ID READ RECORD, four upper-case
	 * hexadecimal characters, null when not sent
	 */
	private String efIdStatusWord;

	/**
	 * EF_ID byte 0, Branchenhauptschluessel (0x67 for GeldKarte), or
	 * {@link AbstractData#UNKNOWN}
	 */
	private int industryKey = UNKNOWN;

	/**
	 * EF_ID bytes 1-3, Kurz-BLZ, the six BCD digits as sent, null when absent
	 */
	private String bankSortCode;

	/**
	 * EF_ID bytes 4-8, Kartennummer, ten BCD digits, null when absent
	 */
	private String cardNumber;

	/**
	 * EF_ID byte 9 high nibble, Pruefziffer, or {@link AbstractData#UNKNOWN}
	 */
	private int checkDigit = UNKNOWN;

	/**
	 * EF_ID bytes 12-14, Aktivierungsdatum, YY MM DD packed BCD. Null when
	 * unparsable or when the record is too short.
	 */
	private Date activationDate;

	/**
	 * EF_ID bytes 15-16, Laenderkennzeichen, ISO 3166 numeric code packed BCD
	 * (0280 for Germany), or {@link AbstractData#UNKNOWN}
	 */
	private int countryCode = UNKNOWN;

	/**
	 * EF_ID bytes 17-19, Waehrungskennzeichen, ASCII ('EUR'), null when
	 * absent
	 */
	private String currency;

	/**
	 * EF_ID byte 20, Wertigkeit der Waehrung, or {@link AbstractData#UNKNOWN}
	 */
	private int currencyUnit = UNKNOWN;

	/**
	 * EF_ID byte 21, Chiptyp, or {@link AbstractData#UNKNOWN}
	 */
	private int chipType = UNKNOWN;

	/**
	 * EF_ID bytes 22-23, OS version, binary, or {@link AbstractData#UNKNOWN}
	 */
	private int osVersion = UNKNOWN;

	/**
	 * Data field of READ RECORD 1 of EF_BETRAG (SFI 0x18), null when not
	 * answered
	 */
	private byte[] rawEfBetrag;

	/**
	 * Status bytes SW1 SW2 of the EF_BETRAG READ RECORD, four upper-case
	 * hexadecimal characters, null when not sent
	 */
	private String efBetragStatusWord;

	/**
	 * EF_BETRAG bytes 3-5, Maximalbetrag (packed BCD, two decimals, EUR). Null
	 * when the record is shorter than six bytes or not BCD.
	 */
	private BigDecimal maximumBalance;

	/**
	 * EF_BETRAG bytes 6-8, Maximaler Transaktionsbetrag (packed BCD, two
	 * decimals). Null when the record is shorter than nine bytes or not BCD.
	 */
	private BigDecimal maximumTransaction;

	/**
	 * Data field of every EF_BLOG record (SFI 0x1D) answered with '9000', in
	 * record order, malformed ones included. Never null.
	 */
	private List<byte[]> rawEfBlogRecords = new ArrayList<byte[]>();

	/**
	 * Status bytes SW1 SW2 that ended the EF_BLOG loop, four upper-case
	 * hexadecimal characters, null when the file was not read
	 */
	private String efBlogEndStatusWord;

	/**
	 * Get the data field of READ RECORD 1 of EF_ID (SFI 0x17)
	 *
	 * @return the record, or null when not answered
	 */
	public byte[] getRawEfId() {
		return rawEfId;
	}

	/**
	 * Setter for the field rawEfId
	 *
	 * @param rawEfId
	 *            the rawEfId to set
	 */
	public void setRawEfId(final byte[] rawEfId) {
		this.rawEfId = rawEfId;
	}

	/**
	 * Get the status bytes SW1 SW2 of the EF_ID READ RECORD
	 *
	 * @return four upper-case hexadecimal characters, or null
	 */
	public String getEfIdStatusWord() {
		return efIdStatusWord;
	}

	/**
	 * Setter for the field efIdStatusWord
	 *
	 * @param efIdStatusWord
	 *            the efIdStatusWord to set
	 */
	public void setEfIdStatusWord(final String efIdStatusWord) {
		this.efIdStatusWord = efIdStatusWord;
	}

	/**
	 * Get the Branchenhauptschluessel, EF_ID byte 0 (0x67 for GeldKarte)
	 *
	 * @return the industry key, or {@link AbstractData#UNKNOWN}
	 */
	public int getIndustryKey() {
		return industryKey;
	}

	/**
	 * Setter for the field industryKey
	 *
	 * @param industryKey
	 *            the industryKey to set
	 */
	public void setIndustryKey(final int industryKey) {
		this.industryKey = industryKey;
	}

	/**
	 * Get the Kurz-BLZ, EF_ID bytes 1-3, six BCD digits as sent
	 *
	 * @return the bank sort code, or null
	 */
	public String getBankSortCode() {
		return bankSortCode;
	}

	/**
	 * Setter for the field bankSortCode
	 *
	 * @param bankSortCode
	 *            the bankSortCode to set
	 */
	public void setBankSortCode(final String bankSortCode) {
		this.bankSortCode = bankSortCode;
	}

	/**
	 * Get the Kartennummer, EF_ID bytes 4-8, ten BCD digits (the same value as
	 * the card number of the track 2)
	 *
	 * @return the card number, or null
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * Setter for the field cardNumber
	 *
	 * @param cardNumber
	 *            the cardNumber to set
	 */
	public void setCardNumber(final String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * Get the Pruefziffer, EF_ID byte 9 high nibble
	 *
	 * @return the check digit, or {@link AbstractData#UNKNOWN}
	 */
	public int getCheckDigit() {
		return checkDigit;
	}

	/**
	 * Setter for the field checkDigit
	 *
	 * @param checkDigit
	 *            the checkDigit to set
	 */
	public void setCheckDigit(final int checkDigit) {
		this.checkDigit = checkDigit;
	}

	/**
	 * Get the Aktivierungsdatum, EF_ID bytes 12-14 (YY MM DD packed BCD)
	 *
	 * @return the activation date, or null when unparsable or the record is
	 *         too short
	 */
	public Date getActivationDate() {
		return activationDate;
	}

	/**
	 * Setter for the field activationDate
	 *
	 * @param activationDate
	 *            the activationDate to set
	 */
	public void setActivationDate(final Date activationDate) {
		this.activationDate = activationDate;
	}

	/**
	 * Get the Laenderkennzeichen, EF_ID bytes 15-16, ISO 3166 numeric code
	 * (0280 for Germany)
	 *
	 * @return the country code, or {@link AbstractData#UNKNOWN}
	 */
	public int getCountryCode() {
		return countryCode;
	}

	/**
	 * Setter for the field countryCode
	 *
	 * @param countryCode
	 *            the countryCode to set
	 */
	public void setCountryCode(final int countryCode) {
		this.countryCode = countryCode;
	}

	/**
	 * Get the Waehrungskennzeichen, EF_ID bytes 17-19, ASCII ('EUR')
	 *
	 * @return the currency, or null
	 */
	public String getCurrency() {
		return currency;
	}

	/**
	 * Setter for the field currency
	 *
	 * @param currency
	 *            the currency to set
	 */
	public void setCurrency(final String currency) {
		this.currency = currency;
	}

	/**
	 * Get the Wertigkeit der Waehrung, EF_ID byte 20
	 *
	 * @return the currency unit, or {@link AbstractData#UNKNOWN}
	 */
	public int getCurrencyUnit() {
		return currencyUnit;
	}

	/**
	 * Setter for the field currencyUnit
	 *
	 * @param currencyUnit
	 *            the currencyUnit to set
	 */
	public void setCurrencyUnit(final int currencyUnit) {
		this.currencyUnit = currencyUnit;
	}

	/**
	 * Get the Chiptyp, EF_ID byte 21
	 *
	 * @return the chip type, or {@link AbstractData#UNKNOWN}
	 */
	public int getChipType() {
		return chipType;
	}

	/**
	 * Setter for the field chipType
	 *
	 * @param chipType
	 *            the chipType to set
	 */
	public void setChipType(final int chipType) {
		this.chipType = chipType;
	}

	/**
	 * Get the OS version, EF_ID bytes 22-23, binary
	 *
	 * @return the OS version, or {@link AbstractData#UNKNOWN}
	 */
	public int getOsVersion() {
		return osVersion;
	}

	/**
	 * Setter for the field osVersion
	 *
	 * @param osVersion
	 *            the osVersion to set
	 */
	public void setOsVersion(final int osVersion) {
		this.osVersion = osVersion;
	}

	/**
	 * Get the data field of READ RECORD 1 of EF_BETRAG (SFI 0x18)
	 *
	 * @return the record, or null when not answered
	 */
	public byte[] getRawEfBetrag() {
		return rawEfBetrag;
	}

	/**
	 * Setter for the field rawEfBetrag
	 *
	 * @param rawEfBetrag
	 *            the rawEfBetrag to set
	 */
	public void setRawEfBetrag(final byte[] rawEfBetrag) {
		this.rawEfBetrag = rawEfBetrag;
	}

	/**
	 * Get the status bytes SW1 SW2 of the EF_BETRAG READ RECORD
	 *
	 * @return four upper-case hexadecimal characters, or null
	 */
	public String getEfBetragStatusWord() {
		return efBetragStatusWord;
	}

	/**
	 * Setter for the field efBetragStatusWord
	 *
	 * @param efBetragStatusWord
	 *            the efBetragStatusWord to set
	 */
	public void setEfBetragStatusWord(final String efBetragStatusWord) {
		this.efBetragStatusWord = efBetragStatusWord;
	}

	/**
	 * Get the Maximalbetrag, EF_BETRAG bytes 3-5 (packed BCD, two decimals,
	 * EUR)
	 *
	 * @return the maximum balance, or null when the record is shorter than six
	 *         bytes or not BCD
	 */
	public BigDecimal getMaximumBalance() {
		return maximumBalance;
	}

	/**
	 * Setter for the field maximumBalance
	 *
	 * @param maximumBalance
	 *            the maximumBalance to set
	 */
	public void setMaximumBalance(final BigDecimal maximumBalance) {
		this.maximumBalance = maximumBalance;
	}

	/**
	 * Get the Maximaler Transaktionsbetrag, EF_BETRAG bytes 6-8 (packed BCD,
	 * two decimals)
	 *
	 * @return the maximum transaction amount, or null when the record is
	 *         shorter than nine bytes or not BCD
	 */
	public BigDecimal getMaximumTransaction() {
		return maximumTransaction;
	}

	/**
	 * Setter for the field maximumTransaction
	 *
	 * @param maximumTransaction
	 *            the maximumTransaction to set
	 */
	public void setMaximumTransaction(final BigDecimal maximumTransaction) {
		this.maximumTransaction = maximumTransaction;
	}

	/**
	 * Get the data field of every EF_BLOG record (SFI 0x1D) answered with
	 * '9000', in record order, malformed ones included
	 *
	 * @return the records, never null
	 */
	public List<byte[]> getRawEfBlogRecords() {
		return rawEfBlogRecords;
	}

	/**
	 * Setter for the field rawEfBlogRecords
	 *
	 * @param rawEfBlogRecords
	 *            the rawEfBlogRecords to set, null resets to an empty list
	 */
	public void setRawEfBlogRecords(final List<byte[]> rawEfBlogRecords) {
		this.rawEfBlogRecords = rawEfBlogRecords != null ? rawEfBlogRecords : new ArrayList<byte[]>();
	}

	/**
	 * Get the status bytes SW1 SW2 that ended the EF_BLOG loop
	 *
	 * @return four upper-case hexadecimal characters, or null when the file
	 *         was not read
	 */
	public String getEfBlogEndStatusWord() {
		return efBlogEndStatusWord;
	}

	/**
	 * Setter for the field efBlogEndStatusWord
	 *
	 * @param efBlogEndStatusWord
	 *            the efBlogEndStatusWord to set
	 */
	public void setEfBlogEndStatusWord(final String efBlogEndStatusWord) {
		this.efBlogEndStatusWord = efBlogEndStatusWord;
	}

}
