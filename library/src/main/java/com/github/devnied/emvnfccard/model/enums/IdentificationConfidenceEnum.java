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
package com.github.devnied.emvnfccard.model.enums;

/**
 * How far the data a card actually carries goes towards naming the institution
 * that issued it.
 * <p>
 * The levels are ordered, {@link #getKey()} increases with the precision of the
 * identification, and each one states what the card proves rather than what it
 * suggests. The ladder is short on purpose: the only data objects of EMV that
 * name a financial institution outright are the Bank Identifier Code and the
 * International Bank Account Number, and both are optional. Everything below
 * them is either a range that has to be looked up in a register this library
 * does not ship, or a payment network, or nothing at all.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public enum IdentificationConfidenceEnum implements IKeyEnum {

	/**
	 * The card exposed nothing that bears on its issuer.
	 */
	NONE(0, "No issuer information"),

	/**
	 * Only the payment network is known, from the Registered Application
	 * Provider Identifier of the Application Identifier.
	 * <p>
	 * EMV 4.1 Book 1 section 12.2.1 splits an Application Identifier into "A
	 * Registered Application Provider Identifier (RID) of 5 bytes, unique to an
	 * application provider" and "an optional field assigned by the application
	 * provider of up to 11 bytes [...] known as a Proprietary Application
	 * Identifier Extension (PIX)". The registered part identifies the
	 * application provider, that is the scheme, and the part below it is left
	 * to that provider: an Application Identifier never descends to the issuing
	 * bank.
	 * </p>
	 */
	SCHEME(1, "Payment scheme only"),

	/**
	 * The country of the issuer is known, from the Issuer Country Code (tag
	 * '5F28', '5F55' or '5F56'), but not the issuer itself.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1: "Issuer Country Code | Indicates the country of
	 * the issuer according to ISO 3166". A country narrows the field of
	 * candidates, it names no institution.
	 * </p>
	 */
	COUNTRY(2, "Issuer country only"),

	/**
	 * An Issuer Identification Number is available, either read from the card
	 * or taken from the leading digits of the Primary Account Number.
	 * <p>
	 * The number designates the issuer in the register ISO/IEC 7812-1 defines,
	 * and that register is not part of the specification, is not public and is
	 * not shipped with this library: the number is reported, the name of the
	 * bank behind it is not resolved. EMV 4.4 Book 3 Annex A1 describes the tag
	 * '42' as "The number that identifies the major industry and the card
	 * issuer and that forms the first part of the Primary Account Number
	 * (PAN)".
	 * </p>
	 */
	ISSUER_RANGE(3, "Issuer identification number known, issuer name not resolved"),

	/**
	 * The card names its financial institution itself, through the Bank
	 * Identifier Code (tag '5F54') or the International Bank Account Number
	 * (tag '5F53').
	 * <p>
	 * EMV 4.4 Book 3 Annex A1: "Bank Identifier Code (BIC) | Uniquely
	 * identifies a bank as defined in ISO 9362" and "International Bank Account
	 * Number (IBAN) | Uniquely identifies the account of a customer at a
	 * financial institution as defined in ISO 13616". These are the only two
	 * data elements of the specification that identify the institution rather
	 * than a range or a network, and both are optional.
	 * </p>
	 */
	ISSUER_NAMED(4, "Issuer named by the card");

	/**
	 * Rank of the level, the higher the more precise
	 */
	private final int key;

	/**
	 * Level label
	 */
	private final String label;

	/**
	 * Constructor using fields
	 *
	 * @param pKey
	 *            rank of the level
	 * @param pLabel
	 *            level label
	 */
	private IdentificationConfidenceEnum(final int pKey, final String pLabel) {
		key = pKey;
		label = pLabel;
	}

	@Override
	public int getKey() {
		return key;
	}

	/**
	 * Get the field label
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Method used to compare two levels
	 *
	 * @param pLevel
	 *            level to compare this one with, null being read as
	 *            {@link #NONE}
	 * @return true when this level is at least as precise as the parameter
	 */
	public boolean isAtLeast(final IdentificationConfidenceEnum pLevel) {
		return pLevel == null || key >= pLevel.key;
	}

	@Override
	public String toString() {
		return label;
	}

}
