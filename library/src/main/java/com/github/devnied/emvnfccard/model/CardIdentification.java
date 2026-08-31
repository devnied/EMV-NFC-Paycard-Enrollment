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

import java.util.ArrayList;
import java.util.Collection;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.IdentificationConfidenceEnum;
import com.github.devnied.emvnfccard.model.enums.IssuerIdentifierSourceEnum;

/**
 * What a card says about the network that runs it and about the institution
 * that issued it, gathered in one place with the confidence each answer
 * deserves.
 * <p>
 * The bean holds no name of a bank. Only two data objects of EMV identify a
 * financial institution, the Bank Identifier Code (tag '5F54') and the
 * International Bank Account Number (tag '5F53'), and both are optional; every
 * other route to the issuer ends on an Issuer Identification Number, which is a
 * key into the register of ISO/IEC 7812-1. That register is not part of any
 * specification, is not public and is not shipped here, so the number is
 * reported as read and left unresolved. A table of ranges to bank names built
 * from anything else would be a guess wearing the clothes of a lookup, and it
 * is deliberately absent: see {@link #getConfidence()}.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class CardIdentification extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 3070297561243286845L;

	/**
	 * Payment scheme the card belongs to, resolved from the Application
	 * Identifier when the card provided one and from the account number
	 * otherwise, or null when neither matched a known scheme
	 */
	private EmvCardScheme scheme;

	/**
	 * True when {@link #scheme} was resolved from the Application Identifier,
	 * false when it was guessed from the leading digits of the account number.
	 * <p>
	 * The Application Identifier is the reliable route: EMV 4.1 Book 1 section
	 * 12.2.1 gives an Application Identifier "A Registered Application Provider
	 * Identifier (RID) of 5 bytes, unique to an application provider". The
	 * account number route rests on the ranges the schemes publish, which the
	 * specification does not define.
	 * </p>
	 */
	private boolean schemeFromAid;

	/**
	 * Application Identifier (tag '4F' or '84') the identification was made on,
	 * raw value, or null
	 */
	private byte[] aid;

	/**
	 * Issuer Identification Number retained, the most explicit of the ones the
	 * card offered, or null when none could be established.<br>
	 * {@link #issuerIdentificationNumberSource} says where it was read.
	 */
	private String issuerIdentificationNumber;

	/**
	 * Data object {@link #issuerIdentificationNumber} was taken from
	 */
	private IssuerIdentifierSourceEnum issuerIdentificationNumberSource = IssuerIdentifierSourceEnum.NONE;

	/**
	 * Issuer Identification Number as read from the tag '42', six digits, or
	 * null when the card carries none
	 */
	private String cardIssuerIdentificationNumber;

	/**
	 * Issuer Identification Number Extended as read from the tag '9F0C', six or
	 * eight digits, or null when the card carries none
	 */
	private String cardIssuerIdentificationNumberExtended;

	/**
	 * Eight leading digits of the Primary Account Number, the length ISO/IEC
	 * 7812-1:2017 gives an Issuer Identification Number, or null when the
	 * account number is unknown or shorter.
	 * <p>
	 * The Foreword of the fifth edition lists among the key changes of the
	 * edition: "Clause 4: revised the length of an IIN to 8 digits (from 6
	 * digits)" and "Clause 4: revised the minimum length of a PAN to 10 digits
	 * (from 8 digits)". A card does not say which length its own issuer was
	 * assigned under, so both this prefix and
	 * {@link #getPanLegacyIssuerIdentificationNumber()} are reported and
	 * neither is presented as the answer.
	 * </p>
	 */
	private String panIssuerIdentificationNumber;

	/**
	 * Six leading digits of the Primary Account Number, the length an Issuer
	 * Identification Number had before ISO/IEC 7812-1:2017 and the only length
	 * the tag '42' can carry, or null when the account number is unknown or
	 * shorter
	 */
	private String panLegacyIssuerIdentificationNumber;

	/**
	 * True when the card carried an Issuer Identification Number and its digits
	 * are the leading digits of the account number.
	 * <p>
	 * The two disagreeing is not an error of the reader and does not make the
	 * card invalid: EMV 4.4 Book 3 section 10.2 puts the tags '42' and '9F0C'
	 * among the objects for which the terminal "shall ignore the formatting
	 * error and continue processing". A caller weighing the two values should
	 * know they were compared and how it turned out.
	 * </p>
	 */
	private boolean issuerIdentificationNumberMatchingPan;

	/**
	 * Bank Identifier Code (tag '5F54'), ISO 9362, or null.<br>
	 * With the International Bank Account Number, one of the only two data
	 * objects of the specification that names the institution.
	 */
	private String bic;

	/**
	 * International Bank Account Number (tag '5F53'), ISO 13616, or null
	 */
	private String iban;

	/**
	 * Country of the issuer, from the Issuer Country Code in its numeric form
	 * (tag '5F28') or one of its alphabetic forms (tags '5F55' and '5F56'), or
	 * null
	 */
	private CountryCodeEnum issuerCountry;

	/**
	 * Country the scheme is operated in when the scheme is a domestic one, null
	 * for an international scheme.<br>
	 * It is a property of the network, not a statement of the card about its
	 * issuer, and it never feeds {@link #getConfidence()}.
	 */
	private CountryCodeEnum schemeCountry;

	/**
	 * Descriptions matched for the Answer To Reset or the Answer To Select of
	 * the card, never null.
	 * <p>
	 * They are informative only and carry no weight in
	 * {@link #getConfidence()}, for two independent reasons. The text comes
	 * from a community maintained list distributed with this library
	 * (smartcard_list.txt, Copyright (C) 2002-2023 Ludovic Rousseau, GNU
	 * General Public License), not from a specification and not from the
	 * issuer. And over the contactless interface the bytes being matched were
	 * not even emitted by the card: PC/SC Part 3 section 3.1.3.2.3 states that
	 * "For contactless ICCs, the IFD subsystem must construct an ATR from the
	 * fixed elements that identify the cards", its Table 3-5 being titled "ATR
	 * Returned by IFD Handler for Contactless Smart Card ICCs".
	 * </p>
	 */
	private Collection<String> atrDescriptions = new ArrayList<String>();

	/**
	 * How far the card went towards naming its issuer
	 */
	private IdentificationConfidenceEnum confidence = IdentificationConfidenceEnum.NONE;

	/**
	 * Method used to get the field scheme
	 *
	 * @return the payment scheme or null
	 */
	public EmvCardScheme getScheme() {
		return scheme;
	}

	/**
	 * Setter for the field scheme
	 *
	 * @param scheme
	 *            the scheme to set
	 */
	public void setScheme(final EmvCardScheme scheme) {
		this.scheme = scheme;
	}

	/**
	 * Method used to know whether the scheme was resolved from the Application
	 * Identifier
	 *
	 * @return true when the scheme comes from the AID, false when it was
	 *         guessed from the account number
	 */
	public boolean isSchemeFromAid() {
		return schemeFromAid;
	}

	/**
	 * Setter for the field schemeFromAid
	 *
	 * @param schemeFromAid
	 *            the schemeFromAid to set
	 */
	public void setSchemeFromAid(final boolean schemeFromAid) {
		this.schemeFromAid = schemeFromAid;
	}

	/**
	 * Method used to get the field aid
	 *
	 * @return the Application Identifier or null
	 */
	public byte[] getAid() {
		return aid;
	}

	/**
	 * Setter for the field aid
	 *
	 * @param aid
	 *            the aid to set
	 */
	public void setAid(final byte[] aid) {
		this.aid = aid;
	}

	/**
	 * Method used to get the Issuer Identification Number retained
	 *
	 * @return the issuer identification number or null
	 */
	public String getIssuerIdentificationNumber() {
		return issuerIdentificationNumber;
	}

	/**
	 * Setter for the field issuerIdentificationNumber
	 *
	 * @param issuerIdentificationNumber
	 *            the issuerIdentificationNumber to set
	 */
	public void setIssuerIdentificationNumber(final String issuerIdentificationNumber) {
		this.issuerIdentificationNumber = issuerIdentificationNumber;
	}

	/**
	 * Method used to get the field issuerIdentificationNumberSource
	 *
	 * @return the data object the retained number was read from, never null
	 */
	public IssuerIdentifierSourceEnum getIssuerIdentificationNumberSource() {
		return issuerIdentificationNumberSource;
	}

	/**
	 * Setter for the field issuerIdentificationNumberSource
	 *
	 * @param issuerIdentificationNumberSource
	 *            the issuerIdentificationNumberSource to set
	 */
	public void setIssuerIdentificationNumberSource(final IssuerIdentifierSourceEnum issuerIdentificationNumberSource) {
		this.issuerIdentificationNumberSource = issuerIdentificationNumberSource;
	}

	/**
	 * Method used to get the field cardIssuerIdentificationNumber, the tag '42'
	 *
	 * @return the six digit issuer identification number or null
	 */
	public String getCardIssuerIdentificationNumber() {
		return cardIssuerIdentificationNumber;
	}

	/**
	 * Setter for the field cardIssuerIdentificationNumber
	 *
	 * @param cardIssuerIdentificationNumber
	 *            the cardIssuerIdentificationNumber to set
	 */
	public void setCardIssuerIdentificationNumber(final String cardIssuerIdentificationNumber) {
		this.cardIssuerIdentificationNumber = cardIssuerIdentificationNumber;
	}

	/**
	 * Method used to get the field cardIssuerIdentificationNumberExtended, the
	 * tag '9F0C'
	 *
	 * @return the six or eight digit issuer identification number or null
	 */
	public String getCardIssuerIdentificationNumberExtended() {
		return cardIssuerIdentificationNumberExtended;
	}

	/**
	 * Setter for the field cardIssuerIdentificationNumberExtended
	 *
	 * @param cardIssuerIdentificationNumberExtended
	 *            the cardIssuerIdentificationNumberExtended to set
	 */
	public void setCardIssuerIdentificationNumberExtended(final String cardIssuerIdentificationNumberExtended) {
		this.cardIssuerIdentificationNumberExtended = cardIssuerIdentificationNumberExtended;
	}

	/**
	 * Method used to get the field panIssuerIdentificationNumber, the eight
	 * leading digits of the account number
	 *
	 * @return the eight digit prefix or null
	 */
	public String getPanIssuerIdentificationNumber() {
		return panIssuerIdentificationNumber;
	}

	/**
	 * Setter for the field panIssuerIdentificationNumber
	 *
	 * @param panIssuerIdentificationNumber
	 *            the panIssuerIdentificationNumber to set
	 */
	public void setPanIssuerIdentificationNumber(final String panIssuerIdentificationNumber) {
		this.panIssuerIdentificationNumber = panIssuerIdentificationNumber;
	}

	/**
	 * Method used to get the field panLegacyIssuerIdentificationNumber, the six
	 * leading digits of the account number
	 *
	 * @return the six digit prefix or null
	 */
	public String getPanLegacyIssuerIdentificationNumber() {
		return panLegacyIssuerIdentificationNumber;
	}

	/**
	 * Setter for the field panLegacyIssuerIdentificationNumber
	 *
	 * @param panLegacyIssuerIdentificationNumber
	 *            the panLegacyIssuerIdentificationNumber to set
	 */
	public void setPanLegacyIssuerIdentificationNumber(final String panLegacyIssuerIdentificationNumber) {
		this.panLegacyIssuerIdentificationNumber = panLegacyIssuerIdentificationNumber;
	}

	/**
	 * Method used to know whether the Issuer Identification Number the card
	 * carries agrees with the account number
	 *
	 * @return true when the card carried a number and it prefixes the account
	 *         number
	 */
	public boolean isIssuerIdentificationNumberMatchingPan() {
		return issuerIdentificationNumberMatchingPan;
	}

	/**
	 * Setter for the field issuerIdentificationNumberMatchingPan
	 *
	 * @param issuerIdentificationNumberMatchingPan
	 *            the issuerIdentificationNumberMatchingPan to set
	 */
	public void setIssuerIdentificationNumberMatchingPan(final boolean issuerIdentificationNumberMatchingPan) {
		this.issuerIdentificationNumberMatchingPan = issuerIdentificationNumberMatchingPan;
	}

	/**
	 * Method used to get the field bic
	 *
	 * @return the Bank Identifier Code or null
	 */
	public String getBic() {
		return bic;
	}

	/**
	 * Setter for the field bic
	 *
	 * @param bic
	 *            the bic to set
	 */
	public void setBic(final String bic) {
		this.bic = bic;
	}

	/**
	 * Method used to get the field iban
	 *
	 * @return the International Bank Account Number or null
	 */
	public String getIban() {
		return iban;
	}

	/**
	 * Setter for the field iban
	 *
	 * @param iban
	 *            the iban to set
	 */
	public void setIban(final String iban) {
		this.iban = iban;
	}

	/**
	 * Method used to get the field issuerCountry
	 *
	 * @return the country of the issuer or null
	 */
	public CountryCodeEnum getIssuerCountry() {
		return issuerCountry;
	}

	/**
	 * Setter for the field issuerCountry
	 *
	 * @param issuerCountry
	 *            the issuerCountry to set
	 */
	public void setIssuerCountry(final CountryCodeEnum issuerCountry) {
		this.issuerCountry = issuerCountry;
	}

	/**
	 * Method used to get the field schemeCountry
	 *
	 * @return the country a domestic scheme is operated in, null for an
	 *         international scheme
	 */
	public CountryCodeEnum getSchemeCountry() {
		return schemeCountry;
	}

	/**
	 * Setter for the field schemeCountry
	 *
	 * @param schemeCountry
	 *            the schemeCountry to set
	 */
	public void setSchemeCountry(final CountryCodeEnum schemeCountry) {
		this.schemeCountry = schemeCountry;
	}

	/**
	 * Method used to get the field atrDescriptions
	 *
	 * @return the descriptions matched for the ATR or the ATS, never null
	 */
	public Collection<String> getAtrDescriptions() {
		return atrDescriptions;
	}

	/**
	 * Setter for the field atrDescriptions
	 *
	 * @param atrDescriptions
	 *            the atrDescriptions to set, null being read as an empty list
	 */
	public void setAtrDescriptions(final Collection<String> atrDescriptions) {
		this.atrDescriptions = atrDescriptions == null ? new ArrayList<String>() : atrDescriptions;
	}

	/**
	 * Method used to get the field confidence, how far the card went towards
	 * naming its issuer.
	 * <p>
	 * {@link IdentificationConfidenceEnum#ISSUER_NAMED} is the only level at
	 * which the institution is known, and it is reached only when the card
	 * carries a Bank Identifier Code or an International Bank Account Number.
	 * {@link IdentificationConfidenceEnum#ISSUER_RANGE} means that a number was
	 * obtained and that resolving it needs a register this library does not
	 * hold.
	 * </p>
	 *
	 * @return the confidence level, never null
	 */
	public IdentificationConfidenceEnum getConfidence() {
		return confidence;
	}

	/**
	 * Setter for the field confidence
	 *
	 * @param confidence
	 *            the confidence to set, null being read as
	 *            {@link IdentificationConfidenceEnum#NONE}
	 */
	public void setConfidence(final IdentificationConfidenceEnum confidence) {
		this.confidence = confidence == null ? IdentificationConfidenceEnum.NONE : confidence;
	}

	/**
	 * Method used to know whether the card named the institution that issued
	 * it, which it does only through its Bank Identifier Code or its
	 * International Bank Account Number
	 *
	 * @return true when the issuer is named by the card itself
	 */
	public boolean isIssuerNamed() {
		return confidence == IdentificationConfidenceEnum.ISSUER_NAMED;
	}

}
