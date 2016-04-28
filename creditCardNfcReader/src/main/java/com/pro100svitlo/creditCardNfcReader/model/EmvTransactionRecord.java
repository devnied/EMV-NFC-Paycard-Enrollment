package com.pro100svitlo.creditCardNfcReader.model;

import com.pro100svitlo.creditCardNfcReader.model.enums.CountryCodeEnum;
import com.pro100svitlo.creditCardNfcReader.model.enums.CurrencyEnum;
import com.pro100svitlo.creditCardNfcReader.model.enums.TransactionTypeEnum;
import com.pro100svitlo.creditCardNfcReader.parser.apdu.annotation.Data;
import com.pro100svitlo.creditCardNfcReader.parser.apdu.impl.AbstractByteBean;
import com.pro100svitlo.creditCardNfcReader.parser.apdu.impl.DataFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * Bean used to describe EMV transaction record
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
