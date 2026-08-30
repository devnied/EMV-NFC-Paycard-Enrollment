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

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.apdu.annotation.Data;
import com.github.devnied.emvnfccard.parser.apdu.impl.AbstractByteBean;
import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;

/**
 * Bean used to describe EMV transaction record
 * 
 * @author MILLAU Julien
 * 
 */
public class EmvTransactionRecord extends AbstractByteBean<EmvTransactionRecord> implements Serializable {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -7050737312961921452L;

	/**
	 * Amount authorized (Amount need to be formated with currency)
	 */
	@Data(index = 1, size = 48, format = DataFactory.BCD_FORMAT, tag = "9f02")
	private Float amount;

	/**
	 * Cryptogram information data
	 */
	@Data(index = 2, size = 8, readHexa = true, tag = "9f27")
	private String cyptogramData;

	/**
	 * Terminal country code
	 */
	@Data(index = 3, size = 16, tag = "9f1a")
	private CountryCodeEnum terminalCountry;

	/**
	 * Currency
	 */
	@Data(index = 4, size = 16, tag = "5f2a")
	private CurrencyEnum currency;

	/**
	 * Transaction date
	 */
	@Data(index = 5, size = 24, dateStandard = DataFactory.BCD_DATE, format = "yyMMdd", tag = "9a")
	private Date date;

	/**
	 * Transaction type (0:Payment, other:Withdrawal)
	 */
	@Data(index = 6, size = 8, readHexa = true, tag = "9c")
	private TransactionTypeEnum transactionType;

	/**
	 * Transaction time
	 */
	@Data(index = 7, size = 24, dateStandard = DataFactory.BCD_DATE, format = "HHmmss", tag = "9f21")
	private Date time;
	/**
	 * Data field of the log record (status bytes removed) this transaction
	 * was parsed from. Null when unknown.
	 */
	private byte[] raw;

	/**
	 * Record number in the log file, or {@link AbstractData#UNKNOWN}
	 */
	private int recordNumber = UNKNOWN;

	/**
	 * Value of every Log Format (tag '9F4F') entry not mapped to a field of
	 * this bean (anything but '9F02', '9F27', '9F1A', '5F2A', '9A', '9C'
	 * and '9F21'), keyed by upper-case hexadecimal tag, in Log Format
	 * order: e.g. '9F36' ATC, '9F26', '9F10', '9F52', 'DF3E', '9F7C',
	 * '9F4E', '9F37', '9F03', '9F34'. Never null.
	 */
	private Map<String, byte[]> otherTags = new LinkedHashMap<String, byte[]>();

	/**
	 * Amount Authorised (tag '9F02') as decoded, before the correction of
	 * the Visa artefact (an amount of 1500000000 or more rewritten); equal
	 * to {@link #getAmount()} when nothing was corrected. Null when absent.
	 */
	private Float originalAmount;

	/**
	 * Numeric ISO 4217 code of the Transaction Currency Code (tag '5F2A')
	 * as sent, even when the currency enumeration has no value for it
	 * ({@link #getCurrency()} is then XXX). {@link AbstractData#UNKNOWN}
	 * when absent.
	 */
	private int currencyCode = UNKNOWN;

	/**
	 * Numeric ISO 3166 code of the Terminal Country Code (tag '9F1A') as
	 * sent, or {@link AbstractData#UNKNOWN}
	 */
	private int terminalCountryCodeNumeric = UNKNOWN;

	/**
	 * Value of the Transaction Type (tag '9C') as sent ({@link
	 * #getTransactionType()} may be null for an unknown code), or {@link
	 * AbstractData#UNKNOWN}
	 */
	private int transactionTypeCode = UNKNOWN;


	/**
	 * Method used to get the field amount
	 * 
	 * @return the amount
	 */
	public Float getAmount() {
		return amount;
	}

	/**
	 * Method used to get the field cyptogramData
	 * 
	 * @return the cyptogramData
	 */
	public String getCyptogramData() {
		return cyptogramData;
	}

	/**
	 * Method used to get the field currency
	 * 
	 * @return the currency
	 */
	public CurrencyEnum getCurrency() {
		return currency;
	}

	/**
	 * Method used to get the field transactionType
	 * 
	 * @return the transactionType
	 */
	public TransactionTypeEnum getTransactionType() {
		return transactionType;
	}

	/**
	 * Method used to get the field terminalCountry
	 * 
	 * @return the terminalCountry
	 */
	public CountryCodeEnum getTerminalCountry() {
		return terminalCountry;
	}

	/**
	 * Setter for the field amount
	 * 
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(final Float amount) {
		this.amount = amount;
	}

	/**
	 * Setter for the field cyptogramData
	 * 
	 * @param cyptogramData
	 *            the cyptogramData to set
	 */
	public void setCyptogramData(final String cyptogramData) {
		this.cyptogramData = cyptogramData;
	}

	/**
	 * Setter for the field terminalCountry
	 * 
	 * @param terminalCountry
	 *            the terminalCountry to set
	 */
	public void setTerminalCountry(final CountryCodeEnum terminalCountry) {
		this.terminalCountry = terminalCountry;
	}

	/**
	 * Setter for the field currency
	 * 
	 * @param currency
	 *            the currency to set
	 */
	public void setCurrency(final CurrencyEnum currency) {
		this.currency = currency;
	}

	/**
	 * Setter for the field transactionType
	 * 
	 * @param transactionType
	 *            the transactionType to set
	 */
	public void setTransactionType(final TransactionTypeEnum transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * Method used to get the field date
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Setter for the field date
	 * 
	 * @param date
	 *            the date to set
	 */
	public void setDate(final Date date) {
		this.date = date;
	}

	/**
	 * Method used to get the field time
	 * 
	 * @return the time
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * Setter for the field time
	 * 
	 * @param time
	 *            the time to set
	 */
	public void setTime(final Date time) {
		this.time = time;
	}

	/**
	 * Data field of the log record (status bytes removed) this transaction
	 * was parsed from. Null when unknown.
	 *
	 * @return the raw record, or null
	 */
	public byte[] getRaw() {
		return raw;
	}

	/**
	 * Setter for the field raw
	 *
	 * @param raw
	 *            the raw to set
	 */
	public void setRaw(final byte[] raw) {
		this.raw = raw;
	}

	/**
	 * Record number in the log file, or {@link AbstractData#UNKNOWN}
	 *
	 * @return the record number, or {@link AbstractData#UNKNOWN}
	 */
	public int getRecordNumber() {
		return recordNumber;
	}

	/**
	 * Setter for the field recordNumber
	 *
	 * @param recordNumber
	 *            the recordNumber to set
	 */
	public void setRecordNumber(final int recordNumber) {
		this.recordNumber = recordNumber;
	}

	/**
	 * Value of every Log Format (tag '9F4F') entry not mapped to a field of
	 * this bean (anything but '9F02', '9F27', '9F1A', '5F2A', '9A', '9C'
	 * and '9F21'), keyed by upper-case hexadecimal tag, in Log Format
	 * order: e.g. '9F36' ATC, '9F26', '9F10', '9F52', 'DF3E', '9F7C',
	 * '9F4E', '9F37', '9F03', '9F34'. Never null.
	 *
	 * @return the other logged data objects, never null
	 */
	public Map<String, byte[]> getOtherTags() {
		return otherTags;
	}

	/**
	 * Setter for the field otherTags
	 *
	 * @param otherTags
	 *            the otherTags to set, null resets to an empty map
	 */
	public void setOtherTags(final Map<String, byte[]> otherTags) {
		this.otherTags = otherTags != null ? otherTags : new LinkedHashMap<String, byte[]>();
	}

	/**
	 * Amount Authorised (tag '9F02') as decoded, before the correction of
	 * the Visa artefact (an amount of 1500000000 or more rewritten); equal
	 * to {@link #getAmount()} when nothing was corrected. Null when absent.
	 *
	 * @return the amount as decoded, or null
	 */
	public Float getOriginalAmount() {
		return originalAmount;
	}

	/**
	 * Setter for the field originalAmount
	 *
	 * @param originalAmount
	 *            the originalAmount to set
	 */
	public void setOriginalAmount(final Float originalAmount) {
		this.originalAmount = originalAmount;
	}

	/**
	 * Numeric ISO 4217 code of the Transaction Currency Code (tag '5F2A')
	 * as sent, even when the currency enumeration has no value for it
	 * ({@link #getCurrency()} is then XXX). {@link AbstractData#UNKNOWN}
	 * when absent.
	 *
	 * @return the numeric currency code, or {@link AbstractData#UNKNOWN}
	 */
	public int getCurrencyCode() {
		return currencyCode;
	}

	/**
	 * Setter for the field currencyCode
	 *
	 * @param currencyCode
	 *            the currencyCode to set
	 */
	public void setCurrencyCode(final int currencyCode) {
		this.currencyCode = currencyCode;
	}

	/**
	 * Numeric ISO 3166 code of the Terminal Country Code (tag '9F1A') as
	 * sent, or {@link AbstractData#UNKNOWN}
	 *
	 * @return the numeric terminal country code, or {@link AbstractData#UNKNOWN}
	 */
	public int getTerminalCountryCodeNumeric() {
		return terminalCountryCodeNumeric;
	}

	/**
	 * Setter for the field terminalCountryCodeNumeric
	 *
	 * @param terminalCountryCodeNumeric
	 *            the terminalCountryCodeNumeric to set
	 */
	public void setTerminalCountryCodeNumeric(final int terminalCountryCodeNumeric) {
		this.terminalCountryCodeNumeric = terminalCountryCodeNumeric;
	}

	/**
	 * Value of the Transaction Type (tag '9C') as sent ({@link
	 * #getTransactionType()} may be null for an unknown code), or {@link
	 * AbstractData#UNKNOWN}
	 *
	 * @return the transaction type code, or {@link AbstractData#UNKNOWN}
	 */
	public int getTransactionTypeCode() {
		return transactionTypeCode;
	}

	/**
	 * Setter for the field transactionTypeCode
	 *
	 * @param transactionTypeCode
	 *            the transactionTypeCode to set
	 */
	public void setTransactionTypeCode(final int transactionTypeCode) {
		this.transactionTypeCode = transactionTypeCode;
	}

}
