/*
 * Copyright (C) 2013 MILLAU Julien
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

}
