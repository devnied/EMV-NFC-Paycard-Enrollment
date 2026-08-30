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
 * Track 2 data
 *
 * @author MILLAU julien
 *
 */
public class EmvTrack2 extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -2906133619803198319L;

	/**
	 * Raw track 2 data
	 */
	private byte[] raw;

	/**
	 * Card number
	 */
	private String cardNumber;

	/**
	 * Expiration date
	 */
	private Date expireDate;

	/**
	 * Card services
	 */
	private Service service;

	/**
	 * Service code, the three positions field ISO/IEC 7813 places between the
	 * expiration date and the discretionary data.<br>
	 * EMV 4.4 Book 3 Annex A1 lists it among the sub-fields of the Track 2
	 * Equivalent Data (tag '57'), "Service Code - n3", and describes the card
	 * resident copy of the same field, tag '5F30': "Service code as defined in
	 * ISO/IEC 7813 for track 1 and track 2 | ICC | n 3 | '70' or '77' | '5F30'
	 * | 2".
	 * <p>
	 * {@link #service} holds the same three digits decoded into their meaning.
	 * This field keeps the digits themselves, which that decoding loses as soon
	 * as one of them is not a value the enumerations know. It stays null when
	 * the field of the track holds anything else than three digits: Table 2 of
	 * ISO/IEC 7813:2006 codes it as three digits or the separator character,
	 * the separator standing for a field the card leaves unused.
	 * </p>
	 */
	private String serviceCode;

	/**
	 * Discretionary data, the last field of the track.<br>
	 * EMV 4.4 Book 3 Annex A1 lists it among the sub-fields of the Track 2
	 * Equivalent Data (tag '57') as "Discretionary Data (defined by individual
	 * payment systems) - n, var.", right before the "Pad with one Hex 'F' if
	 * needed to ensure whole bytes": EMV specifies none of its content.
	 * <p>
	 * The value is the one read inside the track itself, at the fixed position
	 * ISO/IEC 7813 gives it, and with the trailing 'F' padding removed. It
	 * holds the digits of the field, not their hexadecimal encoding: the
	 * sub-field is numeric and takes one nibble per digit.
	 * </p>
	 */
	private String discretionaryData;

	/**
	 * Raw value of the Track 2 Discretionary Data (tag '9F20'), the card
	 * resident data object that holds the discretionary field on its own.<br>
	 * EMV 4.4 Book 3 Annex A1: "Track 2 Discretionary Data | Discretionary part
	 * of track 2 according to ISO/IEC 7813 | ICC | cn | '70' or '77' | '9F20' |
	 * var." EMV Contactless Book C-2 v2.6 A.1.178 bounds it: "Length: var. up
	 * to 16 / Format: cn".
	 * <p>
	 * The value is <b>not</b> text: EMV 4.4 Book 3 section 4.3 defines the
	 * compressed numeric format as "two numeric digits (having values in the
	 * range Hex '0'-'9') per byte. These data elements are left justified and
	 * padded with trailing hexadecimal 'F's." Reading those bytes as characters
	 * yields nothing; {@link #discretionaryData} holds the digits they code.
	 * </p>
	 * <p>
	 * The data object carries the discretionary field alone: no start sentinel,
	 * no field separator, no end sentinel and no Longitudinal Redundancy Check.
	 * It is read with the READ RECORD command, and EMV 4.4 Book 3 warns that
	 * "Data that can occur in template '70' or '77' can never occur in both".
	 * </p>
	 */
	private byte[] rawDiscretionaryData;
	/**
	 * The four expiration digits 'YYMM' of the Track 2 Equivalent Data (tag
	 * '57') as written on the track. Null when the track was not parsed.
	 */
	private String expirationField;

	/**
	 * The three positions of the Track 2 Equivalent Data (tag '57')
	 * following the expiration date as written: digits, or 'F' nibbles when
	 * the card leaves the service code unused, shorter when the track ends.
	 * Null when the track was not parsed.
	 */
	private String serviceCodeField;

	/**
	 * Everything after the expiration date of the Track 2 Equivalent Data
	 * (tag '57') as written, hexadecimal nibbles: service code positions,
	 * discretionary data and 'F' padding; the source {@link
	 * #getDiscretionaryData()} is cut and cleaned from. Null when the track
	 * was not parsed.
	 */
	private String trailingField;


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
	 * The four expiration digits 'YYMM' of the Track 2 Equivalent Data (tag
	 * '57') as written on the track. Null when the track was not parsed.
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
	 * The three positions of the Track 2 Equivalent Data (tag '57')
	 * following the expiration date as written: digits, or 'F' nibbles when
	 * the card leaves the service code unused, shorter when the track ends.
	 * Null when the track was not parsed.
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
	 * Everything after the expiration date of the Track 2 Equivalent Data
	 * (tag '57') as written, hexadecimal nibbles: service code positions,
	 * discretionary data and 'F' padding; the source {@link
	 * #getDiscretionaryData()} is cut and cleaned from. Null when the track
	 * was not parsed.
	 *
	 * @return the trailing field as written, or null
	 */
	public String getTrailingField() {
		return trailingField;
	}

	/**
	 * Setter for the field trailingField
	 *
	 * @param trailingField
	 *            the trailingField to set
	 */
	public void setTrailingField(final String trailingField) {
		this.trailingField = trailingField;
	}

}
