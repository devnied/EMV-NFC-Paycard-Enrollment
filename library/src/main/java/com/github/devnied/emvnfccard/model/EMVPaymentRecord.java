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
 * Bean used to describe EMV payment record
 * 
 * @author julien MILLAU
 * 
 */
public class EMVPaymentRecord extends AbstractByteBean<EMVPaymentRecord> implements Serializable {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -7050737312961921452L;

	/**
	 * Default size
	 */
	public final static int DEFAULT_SIZE = 128;

	/**
	 * Amount authorized (Amount need to be formated with currency)
	 */
	@Data(index = 1, size = 48, format = DataFactory.BCD_FORMAT)
	private Float amount;

	/**
	 * Cryptogram information data
	 */
	@Data(index = 2, size = 8, readHexa = true)
	private String cyptogramData;

	/**
	 * Terminal country code
	 */
	@Data(index = 3, size = 16)
	private CountryCodeEnum terminalCountry;

	/**
	 * Currency
	 */
	@Data(index = 4, size = 16)
	private CurrencyEnum currency;

	/**
	 * Transaction date
	 */
	@Data(index = 5, size = 24, dateStandard = DataFactory.BCD_DATE, format = "yyMMdd")
	private Date transactionDate;

	/**
	 * Transaction type (0:Payment, other:Withdrawal)
	 */
	@Data(index = 6, size = 8, readHexa = true)
	private TransactionTypeEnum transactionType;

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
	 * Method used to get the field transactionDate
	 * 
	 * @return the transactionDate
	 */
	public Date getTransactionDate() {
		return transactionDate;
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
	 * Setter for the field transactionDate
	 * 
	 * @param transactionDate
	 *            the transactionDate to set
	 */
	public void setTransactionDate(final Date transactionDate) {
		this.transactionDate = transactionDate;
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

	@Override
	public int getDefaultSize() {
		return DEFAULT_SIZE;
	}

}
