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

import java.util.Date;

/**
 * Track 1 data
 *
 * @author MILLAU julien
 *
 */
public class EmvTrack1 extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 6619730513813482135L;

	/**
	 * Raw track 1 data
	 */
	private byte[] raw;

	/**
	 * Format code
	 */
	private String formatCode;

	/**
	 * Card number
	 */
	private String cardNumber;

	/**
	 * Expiration date
	 */
	private Date expireDate;

	/**
	 * Holder Lastname
	 */
	private String holderLastname;

	/**
	 * Holder Firstname
	 */
	private String holderFirstname;

	/**
	 * Card services
	 */
	private Service service;

	/**
	 * Service code, the three characters field ISO/IEC 7813 Structure B places
	 * between the expiration date and the discretionary data.<br>
	 * EMV 4.4 Book 3 Annex A1 describes the card resident copy of that field,
	 * tag '5F30': "Service code as defined in ISO/IEC 7813 for track 1 and
	 * track 2 | ICC | n 3 | '70' or '77' | '5F30' | 2".
	 * <p>
	 * {@link #service} holds the same three digits decoded into their meaning.
	 * This field keeps the digits themselves, which that decoding loses as soon
	 * as one of them is not a value the enumerations know. It stays null when
	 * the field of the track holds anything else than three digits: Table 1 of
	 * ISO/IEC 7813:2006 codes it as three digits or the separator character,
	 * the separator standing for a field the card leaves unused.
	 * </p>
	 */
	private String serviceCode;

	/**
	 * Discretionary data, the last field of the track.<br>
	 * EMV Contactless Book C-2 v2.6 A.1.175 closes the sub-fields of the Track
	 * 1 Data (tag '56') with "Discretionary Data : var., ans", and EMV 4.4 Book
	 * 3 Annex A1 says of the same field of the second track that it is "defined
	 * by individual payment systems": EMV specifies none of its content.
	 * <p>
	 * The value is the one read inside the track itself, the trailing padding
	 * removed. EMV 4.4 Book 3 Annex A2 states that "A data element in format a,
	 * an, or ans is left justified and padded with trailing hexadecimal
	 * zeroes", and the tracks met in the field also pad with spaces.
	 * </p>
	 */
	private String discretionaryData;

	/**
	 * Raw value of the Track 1 Discretionary Data (tag '9F1F'), the card
	 * resident data object that holds the discretionary field on its own.<br>
	 * EMV 4.4 Book 3 Annex A1: "Track 1 Discretionary Data | Discretionary part
	 * of track 1 according to ISO/IEC 7813 | ICC | ans | '70' or '77' | '9F1F'
	 * | var." EMV Contactless Book C-2 v2.6 A.1.176 bounds it: "Length: var. up
	 * to 54 / Format: ans".
	 * <p>
	 * The data object carries the discretionary field alone: no start sentinel,
	 * no format code, no field separator, no end sentinel and no Longitudinal
	 * Redundancy Check. It is read with the READ RECORD command, and EMV 4.4
	 * Book 3 warns that "Data that can occur in template '70' or '77' can never
	 * occur in both".
	 * </p>
	 */
	private byte[] rawDiscretionaryData;
	/**
	 * True when the Track 1 Data (tag '56') begins with the '%' start
	 * sentinel of ISO/IEC 7813
	 */
	private boolean startSentinel;

	/**
	 * True when the Track 1 Data (tag '56') ends with the '?' end sentinel
	 * of ISO/IEC 7813
	 */
	private boolean endSentinel;

	/**
	 * Optional field of the Track 1 Data (tag '56') between the PAN and the
	 * name, null when absent
	 */
	private String optionalField;

	/**
	 * Name field of the Track 1 Data (tag '56') exactly as written,
	 * untrimmed, whatever the number of '/' separators; the holder last
	 * name and first name stay null when it does not split in two. Null
	 * when the track was not parsed.
	 */
	private String nameField;

	/**
	 * Expiration field of the Track 1 Data (tag '56') as written: 'YYMM',
	 * or '^' when the card leaves it unused. Null when the track was not
	 * parsed.
	 */
	private String expirationField;

	/**
	 * Service code field of the Track 1 Data (tag '56') as written: three
	 * digits, or '^' when the card leaves it unused ({@link
	 * #getServiceCode()} is null in that case). Null when the track was not
	 * parsed.
	 */
	private String serviceCodeField;

	/**
	 * Discretionary field of the Track 1 Data (tag '56') as written, before
	 * the removal of the padding. Null when the track was not parsed.
	 */
	private String rawDiscretionaryField;


	/**
	 * @return the raw
	 */
	public byte[] getRaw() {
		return raw;
	}

	/**
	 * @param raw
	 *            the raw to set
	 */
	public void setRaw(final byte[] raw) {
		this.raw = raw;
	}

	/**
	 * @return the cardNumber
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * @param cardNumber
	 *            the cardNumber to set
	 */
	public void setCardNumber(final String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * @return the expireDate
	 */
	public Date getExpireDate() {
		return expireDate;
	}

	/**
	 * @param expireDate
	 *            the expireDate to set
	 */
	public void setExpireDate(final Date expireDate) {
		this.expireDate = expireDate;
	}

	/**
	 * @return the formatCode
	 */
	public String getFormatCode() {
		return formatCode;
	}

	/**
	 * @param formatCode
	 *            the formatCode to set
	 */
	public void setFormatCode(final String formatCode) {
		this.formatCode = formatCode;
	}

	/**
	 * @return the holderLastname
	 */
	public String getHolderLastname() {
		return holderLastname;
	}

	/**
	 * @param holderLastname
	 *            the holderLastname to set
	 */
	public void setHolderLastname(final String holderLastname) {
		this.holderLastname = holderLastname;
	}

	/**
	 * @return the holderFirstname
	 */
	public String getHolderFirstname() {
		return holderFirstname;
	}

	/**
	 * @param holderFirstname
	 *            the holderFirstname to set
	 */
	public void setHolderFirstname(final String holderFirstname) {
		this.holderFirstname = holderFirstname;
	}

	/**
	 * @return the service
	 */
	public Service getService() {
		return service;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public void setService(final Service service) {
		this.service = service;
	}

	/**
	 * @return the serviceCode
	 */
	public String getServiceCode() {
		return serviceCode;
	}

	/**
	 * @param serviceCode
	 *            the serviceCode to set
	 */
	public void setServiceCode(final String serviceCode) {
		this.serviceCode = serviceCode;
	}

	/**
	 * @return the discretionaryData
	 */
	public String getDiscretionaryData() {
		return discretionaryData;
	}

	/**
	 * @param discretionaryData
	 *            the discretionaryData to set
	 */
	public void setDiscretionaryData(final String discretionaryData) {
		this.discretionaryData = discretionaryData;
	}

	/**
	 * @return the rawDiscretionaryData
	 */
	public byte[] getRawDiscretionaryData() {
		return rawDiscretionaryData;
	}

	/**
	 * @param rawDiscretionaryData
	 *            the rawDiscretionaryData to set
	 */
	public void setRawDiscretionaryData(final byte[] rawDiscretionaryData) {
		this.rawDiscretionaryData = rawDiscretionaryData;
	}

	/**
	 * True when the Track 1 Data (tag '56') begins with the '%' start
	 * sentinel of ISO/IEC 7813
	 *
	 * @return true when the track carries the start sentinel
	 */
	public boolean isStartSentinel() {
		return startSentinel;
	}

	/**
	 * Setter for the field startSentinel
	 *
	 * @param startSentinel
	 *            the startSentinel to set
	 */
	public void setStartSentinel(final boolean startSentinel) {
		this.startSentinel = startSentinel;
	}

	/**
	 * True when the Track 1 Data (tag '56') ends with the '?' end sentinel
	 * of ISO/IEC 7813
	 *
	 * @return true when the track carries the end sentinel
	 */
	public boolean isEndSentinel() {
		return endSentinel;
	}

	/**
	 * Setter for the field endSentinel
	 *
	 * @param endSentinel
	 *            the endSentinel to set
	 */
	public void setEndSentinel(final boolean endSentinel) {
		this.endSentinel = endSentinel;
	}

	/**
	 * Optional field of the Track 1 Data (tag '56') between the PAN and the
	 * name, null when absent
	 *
	 * @return the optional field, or null
	 */
	public String getOptionalField() {
		return optionalField;
	}

	/**
	 * Setter for the field optionalField
	 *
	 * @param optionalField
	 *            the optionalField to set
	 */
	public void setOptionalField(final String optionalField) {
		this.optionalField = optionalField;
	}

	/**
	 * Name field of the Track 1 Data (tag '56') exactly as written,
	 * untrimmed, whatever the number of '/' separators; the holder last
	 * name and first name stay null when it does not split in two. Null
	 * when the track was not parsed.
	 *
	 * @return the name field as written, or null
	 */
	public String getNameField() {
		return nameField;
	}

	/**
	 * Setter for the field nameField
	 *
	 * @param nameField
	 *            the nameField to set
	 */
	public void setNameField(final String nameField) {
		this.nameField = nameField;
	}

	/**
	 * Expiration field of the Track 1 Data (tag '56') as written: 'YYMM',
	 * or '^' when the card leaves it unused. Null when the track was not
	 * parsed.
	 *
	 * @return the expiration field as written, or null
	 */
	public String getExpirationField() {
		return expirationField;
	}

	/**
	 * Setter for the field expirationField
	 *
	 * @param expirationField
	 *            the expirationField to set
	 */
	public void setExpirationField(final String expirationField) {
		this.expirationField = expirationField;
	}

	/**
	 * Service code field of the Track 1 Data (tag '56') as written: three
	 * digits, or '^' when the card leaves it unused ({@link
	 * #getServiceCode()} is null in that case). Null when the track was not
	 * parsed.
	 *
	 * @return the service code field as written, or null
	 */
	public String getServiceCodeField() {
		return serviceCodeField;
	}

	/**
	 * Setter for the field serviceCodeField
	 *
	 * @param serviceCodeField
	 *            the serviceCodeField to set
	 */
	public void setServiceCodeField(final String serviceCodeField) {
		this.serviceCodeField = serviceCodeField;
	}

	/**
	 * Discretionary field of the Track 1 Data (tag '56') as written, before
	 * the removal of the padding. Null when the track was not parsed.
	 *
	 * @return the discretionary field as written, or null
	 */
	public String getRawDiscretionaryField() {
		return rawDiscretionaryField;
	}

	/**
	 * Setter for the field rawDiscretionaryField
	 *
	 * @param rawDiscretionaryField
	 *            the rawDiscretionaryField to set
	 */
	public void setRawDiscretionaryField(final String rawDiscretionaryField) {
		this.rawDiscretionaryField = rawDiscretionaryField;
	}

}
