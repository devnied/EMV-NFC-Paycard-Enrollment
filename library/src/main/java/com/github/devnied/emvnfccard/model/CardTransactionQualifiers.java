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

/**
 * Card Transaction Qualifiers (tag '9F6C'), the two bytes a Visa (Kernel 3)
 * card answers to tell the reader what it requires of the transaction.
 * <p>
 * EMV Contactless Book C-3 Annex A and the Visa Contactless Payment
 * Specification define the bits:
 * </p>
 * <ul>
 * <li>Byte 1: b8 Online PIN Required, b7 Signature Required, b6 Go Online if
 * Offline Data Authentication Fails and Reader is online capable, b5 Switch
 * Interface if Offline Data Authentication fails and Reader supports contact
 * chip, b4 Go Online if Application Expired, b3 Switch Interface for Cash
 * Transactions, b2 Switch Interface for Cashback Transactions, b1 RFU</li>
 * <li>Byte 2: b8 Consumer Device CVM Performed, b7 Card supports Issuer Update
 * Processing at the POS, b6 to b1 RFU</li>
 * </ul>
 * <p>
 * A flag whose byte is missing from a shorter value is false.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class CardTransactionQualifiers extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 7392021145908733127L;

	/**
	 * Raw value of the tag '9F6C'
	 */
	private byte[] raw;

	/**
	 * Byte 1 bit 8: Online PIN Required
	 */
	private boolean onlinePinRequired;

	/**
	 * Byte 1 bit 7: Signature Required
	 */
	private boolean signatureRequired;

	/**
	 * Byte 1 bit 6: Go Online if Offline Data Authentication Fails and Reader
	 * is online capable
	 */
	private boolean goOnlineIfOdaFailsAndReaderOnlineCapable;

	/**
	 * Byte 1 bit 5: Switch Interface if Offline Data Authentication fails and
	 * Reader supports contact chip
	 */
	private boolean switchInterfaceIfOdaFails;

	/**
	 * Byte 1 bit 4: Go Online if Application Expired
	 */
	private boolean goOnlineIfApplicationExpired;

	/**
	 * Byte 1 bit 3: Switch Interface for Cash Transactions
	 */
	private boolean switchInterfaceForCashTransactions;

	/**
	 * Byte 1 bit 2: Switch Interface for Cashback Transactions
	 */
	private boolean switchInterfaceForCashbackTransactions;

	/**
	 * Byte 2 bit 8: Consumer Device CVM Performed
	 */
	private boolean consumerDeviceCvmPerformed;

	/**
	 * Byte 2 bit 7: Card supports Issuer Update Processing at the POS
	 */
	private boolean issuerUpdateProcessingSupported;

	/**
	 * Default constructor, every flag false
	 */
	public CardTransactionQualifiers() {
		super();
	}

	/**
	 * Constructor decoding the raw value of the tag '9F6C'. A null or short
	 * value leaves the flags of the missing bytes false.
	 *
	 * @param pRaw
	 *            raw value of the tag '9F6C'
	 */
	public CardTransactionQualifiers(final byte[] pRaw) {
		super();
		raw = pRaw;
		if (pRaw != null && pRaw.length > 0) {
			int byte1 = pRaw[0] & 0xFF;
			onlinePinRequired = (byte1 & 0x80) != 0;
			signatureRequired = (byte1 & 0x40) != 0;
			goOnlineIfOdaFailsAndReaderOnlineCapable = (byte1 & 0x20) != 0;
			switchInterfaceIfOdaFails = (byte1 & 0x10) != 0;
			goOnlineIfApplicationExpired = (byte1 & 0x08) != 0;
			switchInterfaceForCashTransactions = (byte1 & 0x04) != 0;
			switchInterfaceForCashbackTransactions = (byte1 & 0x02) != 0;
		}
		if (pRaw != null && pRaw.length > 1) {
			int byte2 = pRaw[1] & 0xFF;
			consumerDeviceCvmPerformed = (byte2 & 0x80) != 0;
			issuerUpdateProcessingSupported = (byte2 & 0x40) != 0;
		}
	}

	/**
	 * Get the raw value of the tag '9F6C'
	 *
	 * @return the raw value, or null
	 */
	public byte[] getRaw() {
		return raw;
	}

	/**
	 * Setter for the field raw, the flags are left as they are
	 *
	 * @param raw
	 *            the raw to set
	 */
	public void setRaw(final byte[] raw) {
		this.raw = raw;
	}

	/**
	 * Byte 1 bit 8 of the tag '9F6C'
	 *
	 * @return true when Online PIN is required
	 */
	public boolean isOnlinePinRequired() {
		return onlinePinRequired;
	}

	/**
	 * Setter for the field onlinePinRequired
	 *
	 * @param onlinePinRequired
	 *            the onlinePinRequired to set
	 */
	public void setOnlinePinRequired(final boolean onlinePinRequired) {
		this.onlinePinRequired = onlinePinRequired;
	}

	/**
	 * Byte 1 bit 7 of the tag '9F6C'
	 *
	 * @return true when a signature is required
	 */
	public boolean isSignatureRequired() {
		return signatureRequired;
	}

	/**
	 * Setter for the field signatureRequired
	 *
	 * @param signatureRequired
	 *            the signatureRequired to set
	 */
	public void setSignatureRequired(final boolean signatureRequired) {
		this.signatureRequired = signatureRequired;
	}

	/**
	 * Byte 1 bit 6 of the tag '9F6C'
	 *
	 * @return true when the card asks to go online if the offline data
	 *         authentication fails and the reader is online capable
	 */
	public boolean isGoOnlineIfOdaFailsAndReaderOnlineCapable() {
		return goOnlineIfOdaFailsAndReaderOnlineCapable;
	}

	/**
	 * Setter for the field goOnlineIfOdaFailsAndReaderOnlineCapable
	 *
	 * @param goOnlineIfOdaFailsAndReaderOnlineCapable
	 *            the value to set
	 */
	public void setGoOnlineIfOdaFailsAndReaderOnlineCapable(final boolean goOnlineIfOdaFailsAndReaderOnlineCapable) {
		this.goOnlineIfOdaFailsAndReaderOnlineCapable = goOnlineIfOdaFailsAndReaderOnlineCapable;
	}

	/**
	 * Byte 1 bit 5 of the tag '9F6C'
	 *
	 * @return true when the card asks to switch interface if the offline data
	 *         authentication fails and the reader supports contact chip
	 */
	public boolean isSwitchInterfaceIfOdaFails() {
		return switchInterfaceIfOdaFails;
	}

	/**
	 * Setter for the field switchInterfaceIfOdaFails
	 *
	 * @param switchInterfaceIfOdaFails
	 *            the switchInterfaceIfOdaFails to set
	 */
	public void setSwitchInterfaceIfOdaFails(final boolean switchInterfaceIfOdaFails) {
		this.switchInterfaceIfOdaFails = switchInterfaceIfOdaFails;
	}

	/**
	 * Byte 1 bit 4 of the tag '9F6C'
	 *
	 * @return true when the card asks to go online if the application is
	 *         expired
	 */
	public boolean isGoOnlineIfApplicationExpired() {
		return goOnlineIfApplicationExpired;
	}

	/**
	 * Setter for the field goOnlineIfApplicationExpired
	 *
	 * @param goOnlineIfApplicationExpired
	 *            the goOnlineIfApplicationExpired to set
	 */
	public void setGoOnlineIfApplicationExpired(final boolean goOnlineIfApplicationExpired) {
		this.goOnlineIfApplicationExpired = goOnlineIfApplicationExpired;
	}

	/**
	 * Byte 1 bit 3 of the tag '9F6C'
	 *
	 * @return true when the card asks to switch interface for cash
	 *         transactions
	 */
	public boolean isSwitchInterfaceForCashTransactions() {
		return switchInterfaceForCashTransactions;
	}

	/**
	 * Setter for the field switchInterfaceForCashTransactions
	 *
	 * @param switchInterfaceForCashTransactions
	 *            the switchInterfaceForCashTransactions to set
	 */
	public void setSwitchInterfaceForCashTransactions(final boolean switchInterfaceForCashTransactions) {
		this.switchInterfaceForCashTransactions = switchInterfaceForCashTransactions;
	}

	/**
	 * Byte 1 bit 2 of the tag '9F6C'
	 *
	 * @return true when the card asks to switch interface for cashback
	 *         transactions
	 */
	public boolean isSwitchInterfaceForCashbackTransactions() {
		return switchInterfaceForCashbackTransactions;
	}

	/**
	 * Setter for the field switchInterfaceForCashbackTransactions
	 *
	 * @param switchInterfaceForCashbackTransactions
	 *            the switchInterfaceForCashbackTransactions to set
	 */
	public void setSwitchInterfaceForCashbackTransactions(final boolean switchInterfaceForCashbackTransactions) {
		this.switchInterfaceForCashbackTransactions = switchInterfaceForCashbackTransactions;
	}

	/**
	 * Byte 2 bit 8 of the tag '9F6C'
	 *
	 * @return true when a Consumer Device CVM was performed
	 */
	public boolean isConsumerDeviceCvmPerformed() {
		return consumerDeviceCvmPerformed;
	}

	/**
	 * Setter for the field consumerDeviceCvmPerformed
	 *
	 * @param consumerDeviceCvmPerformed
	 *            the consumerDeviceCvmPerformed to set
	 */
	public void setConsumerDeviceCvmPerformed(final boolean consumerDeviceCvmPerformed) {
		this.consumerDeviceCvmPerformed = consumerDeviceCvmPerformed;
	}

	/**
	 * Byte 2 bit 7 of the tag '9F6C'
	 *
	 * @return true when the card supports Issuer Update Processing at the POS
	 */
	public boolean isIssuerUpdateProcessingSupported() {
		return issuerUpdateProcessingSupported;
	}

	/**
	 * Setter for the field issuerUpdateProcessingSupported
	 *
	 * @param issuerUpdateProcessingSupported
	 *            the issuerUpdateProcessingSupported to set
	 */
	public void setIssuerUpdateProcessingSupported(final boolean issuerUpdateProcessingSupported) {
		this.issuerUpdateProcessingSupported = issuerUpdateProcessingSupported;
	}

}
