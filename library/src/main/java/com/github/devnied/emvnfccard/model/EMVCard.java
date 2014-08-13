package com.github.devnied.emvnfccard.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;

/**
 * Bean used to describe data in EMV card
 * 
 * @author MILLAU Julien
 * 
 */
public class EmvCard extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 736740432469989941L;

	/**
	 * Card AID
	 */
	private String aid;

	/**
	 * Holder Name
	 */
	private String holderName;

	/**
	 * Card number
	 */
	private String cardNumber;

	/**
	 * Expiration date
	 */
	private Date expireDate;

	/**
	 * Card type
	 */
	private EmvCardScheme type;

	/**
	 * Left PIN try
	 */
	private int leftPinTry;

	/**
	 * Application label
	 */
	private String applicationLabel;

	/**
	 * List of issued payment
	 */
	private List<EmvTransactionRecord> listTransactions;

	/**
	 * List of Atr description
	 */
	private Collection<String> atrDescription;

	/**
	 * Method used to get the field aid
	 * 
	 * @return the aid
	 */
	public String getAid() {
		return aid;
	}

	/**
	 * Setter for the field aid
	 * 
	 * @param aid
	 *            the aid to set
	 */
	public void setAid(final String aid) {
		this.aid = aid;
	}

	/**
	 * Method used to get the field holderName
	 * 
	 * @return the holderName
	 */
	public String getHolderName() {
		return holderName;
	}

	/**
	 * Setter for the field holderName
	 * 
	 * @param holderName
	 *            the holderName to set
	 */
	public void setHolderName(final String holderName) {
		this.holderName = holderName;
	}

	/**
	 * Method used to get the field cardNumber
	 * 
	 * @return the cardNumber
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
	 * Method used to get the field expireDate
	 * 
	 * @return the expireDate
	 */
	public Date getExpireDate() {
		return expireDate;
	}

	/**
	 * Setter for the field expireDate
	 * 
	 * @param expireDate
	 *            the expireDate to set
	 */
	public void setExpireDate(final Date expireDate) {
		this.expireDate = expireDate;
	}

	/**
	 * Method used to get the field listTransactions
	 * 
	 * @return the listTransactions
	 */
	public List<EmvTransactionRecord> getListTransactions() {
		return listTransactions;
	}

	/**
	 * Setter for the field listTransactions
	 * 
	 * @param listTransactions
	 *            the listTransactions to set
	 */
	public void setListTransactions(final List<EmvTransactionRecord> listTransactions) {
		this.listTransactions = listTransactions;
	}

	/**
	 * Method used to get the field type
	 * 
	 * @return the type
	 */
	public EmvCardScheme getType() {
		return type;
	}

	/**
	 * Setter for the field type
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(final EmvCardScheme type) {
		this.type = type;
	}

	/**
	 * Method used to get the field applicationLabel
	 * 
	 * @return the applicationLabel
	 */
	public String getApplicationLabel() {
		return applicationLabel;
	}

	/**
	 * Setter for the field applicationLabel
	 * 
	 * @param applicationLabel
	 *            the applicationLabel to set
	 */
	public void setApplicationLabel(final String applicationLabel) {
		this.applicationLabel = applicationLabel;
	}

	@Override
	public boolean equals(final Object arg0) {
		return arg0 instanceof EmvCard && cardNumber != null && cardNumber.equals(((EmvCard) arg0).getCardNumber());
	}

	/**
	 * Method used to get the field leftPinTry
	 * 
	 * @return the leftPinTry
	 */
	public int getLeftPinTry() {
		return leftPinTry;
	}

	/**
	 * Setter for the field leftPinTry
	 * 
	 * @param leftPinTry
	 *            the leftPinTry to set
	 */
	public void setLeftPinTry(final int leftPinTry) {
		this.leftPinTry = leftPinTry;
	}

	/**
	 * Method used to get the field atrDescription
	 * 
	 * @return the atrDescription
	 */
	public Collection<String> getAtrDescription() {
		return atrDescription;
	}

	/**
	 * Setter for the field atrDescription
	 * 
	 * @param atrDescription
	 *            the atrDescription to set
	 */
	public void setAtrDescription(final Collection<String> atrDescription) {
		this.atrDescription = atrDescription;
	}

}
