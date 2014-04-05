package com.github.devnied.emvnfccard.model;

import java.util.Date;
import java.util.List;

import com.github.devnied.emvnfccard.enums.EMVCardTypeEnum;

/**
 * Bean used to describe data in EMV card
 * 
 * @author julien MILLAU
 * 
 */
public class EMVCard extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 736740432469989941L;

	/**
	 * Card AID
	 */
	private String aid;

	/**
	 * Holder first name
	 */
	private String fisrtName;

	/**
	 * Holder last Name
	 */
	private String lastName;

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
	private EMVCardTypeEnum type;

	/**
	 * Card label
	 */
	private String cardLabel;

	/**
	 * List of issued payment
	 */
	private List<EMVPaymentRecord> listPayment;

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
	 * Method used to get the field fisrtName
	 * 
	 * @return the fisrtName
	 */
	public String getFisrtName() {
		return fisrtName;
	}

	/**
	 * Setter for the field fisrtName
	 * 
	 * @param fisrtName
	 *            the fisrtName to set
	 */
	public void setFisrtName(final String fisrtName) {
		this.fisrtName = fisrtName;
	}

	/**
	 * Method used to get the field lastName
	 * 
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Setter for the field lastName
	 * 
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
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
	 * Method used to get the field listPayment
	 * 
	 * @return the listPayment
	 */
	public List<EMVPaymentRecord> getListPayment() {
		return listPayment;
	}

	/**
	 * Setter for the field listPayment
	 * 
	 * @param listPayment
	 *            the listPayment to set
	 */
	public void setListPayment(final List<EMVPaymentRecord> listPayment) {
		this.listPayment = listPayment;
	}

	/**
	 * Method used to get the field type
	 * 
	 * @return the type
	 */
	public EMVCardTypeEnum getType() {
		return type;
	}

	/**
	 * Setter for the field type
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(final EMVCardTypeEnum type) {
		this.type = type;
	}

	/**
	 * Method used to get the field cardLabel
	 * 
	 * @return the cardLabel
	 */
	public String getCardLabel() {
		return cardLabel;
	}

	/**
	 * Setter for the field cardLabel
	 * 
	 * @param cardLabel
	 *            the cardLabel to set
	 */
	public void setCardLabel(final String cardLabel) {
		this.cardLabel = cardLabel;
	}

	@Override
	public boolean equals(final Object arg0) {
		return arg0 instanceof EMVCard && cardNumber != null && cardNumber.equals(((EMVCard) arg0).getCardNumber());
	}
}
