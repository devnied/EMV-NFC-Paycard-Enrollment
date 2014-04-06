package com.github.devnied.emvnfccard.model;

import java.io.Serializable;
import java.util.Date;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
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
	 * Amount authorized
	 */
	@Data(index = 1, size = 48)
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
	 * Transaction type
	 */
	@Data(index = 6, size = 16)
	private Integer transactionType;

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
	public Integer getTransactionType() {
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

	@Override
	public int getDefaultSize() {
		return DEFAULT_SIZE;
	}

}
