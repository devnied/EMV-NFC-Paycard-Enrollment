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
	 * Holder Lastname
	 */
	private String holderLastname;

	/**
	 * Holder Firstname
	 */
	private String holderFirstname;

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
	 * Track 2 data
	 */
	private EmvTrack2 track2;

	/**
	 * Track 1 data
	 */
	private EmvTrack1 track1;

	/**
	 * Indicate if the nfc is locked on the card
	 */
	private boolean nfcLocked;

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
	 * Method used to get the field holderLastname
	 *
	 * @return the holderLastname
	 */
	public String getHolderLastname() {
		String ret = holderLastname;
		if (ret == null && track1 != null) {
			ret = track1.getHolderLastname();
		}
		return ret;
	}

	/**
	 * Setter for the field holderLastname
	 *
	 * @param holderLastname
	 *            the holderLastname to set
	 */
	public void setHolderLastname(final String holderLastname) {
		this.holderLastname = holderLastname;
	}

	/**
	 * Method used to get the field holderFirstname
	 *
	 * @return the holderFirstname
	 */
	public String getHolderFirstname() {
		String ret = holderFirstname;
		if (ret == null && track1 != null) {
			ret = track1.getHolderFirstname();
		}
		return ret;
	}

	/**
	 * Setter for the field holderFirstname
	 *
	 * @param holderFirstname
	 *            the holderFirstname to set
	 */
	public void setHolderFirstname(final String holderFirstname) {
		this.holderFirstname = holderFirstname;
	}

	/**
	 * Method used to get the field cardNumber
	 *
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		String ret = null;
		if (track2 != null) {
			ret = track2.getCardNumber();
		}
		if (ret == null && track1 != null) {
			ret = track1.getCardNumber();
		}
		return ret;
	}

	/**
	 * Method used to get the field expireDate
	 *
	 * @return the expireDate
	 */
	public Date getExpireDate() {
		Date ret = null;
		if (track2 != null) {
			ret = track2.getExpireDate();
		}
		if (ret == null && track1 != null) {
			ret = track1.getExpireDate();
		}
		return ret;
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
		return arg0 instanceof EmvCard && getCardNumber() != null && getCardNumber().equals(((EmvCard) arg0).getCardNumber());
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

	/**
	 * Method used to get the field nfcLocked
	 *
	 * @return the nfcLocked
	 */
	public boolean isNfcLocked() {
		return nfcLocked;
	}

	/**
	 * Setter for the field nfcLocked
	 *
	 * @param nfcLocked
	 *            the nfcLocked to set
	 */
	public void setNfcLocked(final boolean nfcLocked) {
		this.nfcLocked = nfcLocked;
	}

	/**
	 * @return the track2
	 */
	public EmvTrack2 getTrack2() {
		return track2;
	}

	/**
	 * @param track2
	 *            the track2 to set
	 */
	public void setTrack2(final EmvTrack2 track2) {
		this.track2 = track2;
	}

	/**
	 * @return the track1
	 */
	public EmvTrack1 getTrack1() {
		return track1;
	}

	/**
	 * @param track1
	 *            the track1 to set
	 */
	public void setTrack1(final EmvTrack1 track1) {
		this.track1 = track1;
	}

}
