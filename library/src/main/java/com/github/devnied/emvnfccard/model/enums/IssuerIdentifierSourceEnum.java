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
 * Data object an Issuer Identification Number was taken from.
 * <p>
 * A card may hold the number in more than one place, and the places do not
 * agree by construction: EMV 4.4 Book 3 section 10.2 lists the tag '42' and the
 * tag '9F0C' among the objects whose "correct formatting [...] is not critical
 * for completion of a transaction" and asks the terminal to "ignore the
 * formatting error and continue processing". A reader that reports a number
 * therefore has to report where it read it, so that a caller can tell a number
 * the card asserted from one derived from the account number.
 * </p>
 * <p>
 * {@link #getKey()} orders the sources by preference, the highest being the
 * most explicit.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public enum IssuerIdentifierSourceEnum implements IKeyEnum {

	/**
	 * No Issuer Identification Number could be established.
	 */
	NONE(0, "None"),

	/**
	 * Derived from the leading digits of the Primary Account Number, which is
	 * the only mandatory source.
	 * <p>
	 * EMV 4.4 Book 3 Table 28 makes the Application Primary Account Number (tag
	 * '5A') a mandatory data object, while the tags '42' and '9F0C' are
	 * optional. The digits are a prefix of the account number and nothing more:
	 * the card does not say how many of them belong to the issuer.
	 * </p>
	 */
	PAN(1, "Primary Account Number"),

	/**
	 * Read from the Issuer Identification Number (tag '42'), the six digit
	 * form.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1 codes the tag "n6" over a length of 3 bytes.
	 * </p>
	 */
	ISSUER_IDENTIFICATION_NUMBER(2, "Issuer Identification Number (tag '42')"),

	/**
	 * Read from the Issuer Identification Number Extended (tag '9F0C'), the six
	 * or eight digit form.
	 * <p>
	 * EMV 4.4 Book 3 Annex A1 codes the tag "n 6 or 8" over a length of "var. 3
	 * or 4". It is the only card resident object able to carry the eight digit
	 * number of ISO/IEC 7812-1:2017, so it is preferred over the tag '42'.
	 * </p>
	 */
	ISSUER_IDENTIFICATION_NUMBER_EXTENDED(3, "Issuer Identification Number Extended (tag '9F0C')");

	/**
	 * Rank of the source, the higher the more explicit
	 */
	private final int key;

	/**
	 * Source label
	 */
	private final String label;

	/**
	 * Constructor using fields
	 *
	 * @param pKey
	 *            rank of the source
	 * @param pLabel
	 *            source label
	 */
	private IssuerIdentifierSourceEnum(final int pKey, final String pLabel) {
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
	 * Method used to know whether the number was asserted by the card itself
	 * rather than derived from the account number
	 *
	 * @return true when the number comes from the tag '42' or the tag '9F0C'
	 */
	public boolean isCardResident() {
		return this == ISSUER_IDENTIFICATION_NUMBER || this == ISSUER_IDENTIFICATION_NUMBER_EXTENDED;
	}

	@Override
	public String toString() {
		return label;
	}

}
