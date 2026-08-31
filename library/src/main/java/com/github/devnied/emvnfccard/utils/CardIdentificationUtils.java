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
package com.github.devnied.emvnfccard.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.CardIdentification;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.IdentificationConfidenceEnum;
import com.github.devnied.emvnfccard.model.enums.IssuerIdentifierSourceEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Utility class gathering what a card discloses about the network that runs it
 * and about the institution that issued it, and stating how far that goes.
 *
 * <h3>What the card can prove</h3>
 * <p>
 * Only two data objects of the specification name a financial institution. EMV
 * 4.4 Book 3 Annex A1: "Bank Identifier Code (BIC) | Uniquely identifies a bank
 * as defined in ISO 9362" (tag '5F54') and "International Bank Account Number
 * (IBAN) | Uniquely identifies the account of a customer at a financial
 * institution as defined in ISO 13616" (tag '5F53'). Both are optional and both
 * are rare. When a card carries one, the issuer is known and the confidence is
 * {@link IdentificationConfidenceEnum#ISSUER_NAMED}.
 * </p>
 * <p>
 * Everything else stops short of a name. The Issuer Identification Number (tag
 * '42', or tag '9F0C' for its extended form), and the leading digits of the
 * account number it prefixes, designate the issuer only through the register
 * ISO/IEC 7812-1 defines. That register is not part of the standard, is not
 * public, and no free and verifiable substitute for it was found: this class
 * therefore reports the number and does not resolve it, which is
 * {@link IdentificationConfidenceEnum#ISSUER_RANGE}. <b>No table mapping ranges
 * to bank names is shipped with this library.</b> Building one from
 * unattributable sources would produce confident wrong answers on exactly the
 * cards a reader cannot check, so its absence is a decision and not a gap.
 * </p>
 * <p>
 * The Application Identifier identifies the network and stops there. EMV 4.1
 * Book 1 section 12.2.1 splits it into "A Registered Application Provider
 * Identifier (RID) of 5 bytes, unique to an application provider" and a
 * "Proprietary Application Identifier Extension (PIX)" whose "meaning [...] is
 * defined only for the specific RID": nothing below the provider is registered,
 * so an Application Identifier never reaches the issuing bank. That is
 * {@link IdentificationConfidenceEnum#SCHEME}.
 * </p>
 * <p>
 * The description matched for the Answer To Reset or the Answer To Select is
 * carried for information and never raises the confidence. It comes from a
 * community maintained list, and over the contactless interface the bytes it
 * matches were not sent by the card at all: PC/SC Part 3 section 3.1.3.2.3
 * requires that "For contactless ICCs, the IFD subsystem must construct an ATR
 * from the fixed elements that identify the cards", its Table 3-5 being titled
 * "ATR Returned by IFD Handler for Contactless Smart Card ICCs".
 * </p>
 *
 * <h3>Two lengths for one number</h3>
 * <p>
 * ISO/IEC 7812-1:2017 (fifth edition, 2017-01) lists among the key changes of
 * the edition, in its Foreword: "Clause 4: revised the length of an IIN to 8
 * digits (from 6 digits)". The tag '42' of EMV predates that and is coded "n6"
 * over three bytes, so it cannot carry the new length; the tag '9F0C' can, EMV
 * 4.4 Book 3 Annex A1 coding it "n 6 or 8". A card discloses no boundary inside
 * its account number, so both prefixes are reported, at six and at eight
 * digits, and neither is presented as the answer.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public final class CardIdentificationUtils {

	/**
	 * Length of an Issuer Identification Number since ISO/IEC 7812-1:2017
	 */
	public static final int IIN_LENGTH = 8;

	/**
	 * Length of an Issuer Identification Number before ISO/IEC 7812-1:2017, and
	 * the only length the tag '42' can carry (EMV 4.4 Book 3 Annex A1 codes it
	 * "n6")
	 */
	public static final int LEGACY_IIN_LENGTH = 6;

	/**
	 * Method used to identify a card from everything it disclosed while it was
	 * read.<br>
	 * The first application of the card is used, which is the one the selection
	 * retained.
	 *
	 * @param pCard
	 *            card that was read
	 * @return the identification, never null
	 */
	public static CardIdentification identify(final EmvCard pCard) {
		Application application = null;
		if (pCard != null && !pCard.getApplications().isEmpty()) {
			application = pCard.getApplications().get(0);
		}
		return identify(pCard, application);
	}

	/**
	 * Method used to identify a card from everything it disclosed while it was
	 * read, for one of its applications.
	 *
	 * @param pCard
	 *            card that was read, may be null
	 * @param pApplication
	 *            application to identify, may be null
	 * @return the identification, never null
	 */
	public static CardIdentification identify(final EmvCard pCard, final Application pApplication) {
		CardIdentification ret = new CardIdentification();
		if (pCard == null && pApplication == null) {
			return ret;
		}
		String pan = digitsOf(pCard != null ? pCard.getCardNumber() : null);
		identifyScheme(ret, pCard, pApplication, pan);
		identifyIssuer(ret, pCard, pApplication, pan);
		if (pCard != null && pCard.getAtrDescription() != null) {
			ret.setAtrDescriptions(new ArrayList<String>(pCard.getAtrDescription()));
		}
		ret.setConfidence(computeConfidence(ret));
		return ret;
	}

	/**
	 * Method used to resolve the payment scheme, from the Application
	 * Identifier first and from the account number only as a fallback.
	 * <p>
	 * The two can name different brands on a co-badged card, whose selected
	 * application belongs to a domestic scheme while its account number belongs
	 * to the international one it is co-badged with. The Application Identifier
	 * is what the card announced, so it is what is reported here;
	 * {@link EmvCard#getType()} answers the other question and may therefore
	 * differ.
	 * </p>
	 *
	 * @param pIdentification
	 *            identification being built
	 * @param pCard
	 *            card that was read, may be null
	 * @param pApplication
	 *            application to identify, may be null
	 * @param pPan
	 *            account number reduced to its digits, may be null
	 */
	private static void identifyScheme(final CardIdentification pIdentification, final EmvCard pCard,
			final Application pApplication, final String pPan) {
		byte[] aid = pApplication != null ? pApplication.getAid() : null;
		EmvCardScheme scheme = null;
		if (aid != null) {
			pIdentification.setAid(aid);
			scheme = EmvCardScheme.getCardTypeByAid(BytesUtils.bytesToStringNoSpace(aid));
		}
		if (scheme != null) {
			pIdentification.setSchemeFromAid(true);
		} else {
			// The account number ranges are published by the schemes, not by
			// the specification: they are only used when the card named no
			// application this library knows
			scheme = EmvCardScheme.getCardTypeByCardNumber(pPan);
			if (scheme == null && pCard != null) {
				scheme = pCard.getType();
			}
		}
		pIdentification.setScheme(scheme);
		if (scheme != null) {
			pIdentification.setSchemeCountry(scheme.getCountry());
		}
	}

	/**
	 * Method used to gather everything that bears on the issuer: the Issuer
	 * Identification Number in each of the forms the card may hold it, the
	 * prefixes of the account number, the country, and the two objects that
	 * name the institution outright.
	 *
	 * @param pIdentification
	 *            identification being built
	 * @param pCard
	 *            card that was read, may be null
	 * @param pApplication
	 *            application to identify, may be null
	 * @param pPan
	 *            account number reduced to its digits, may be null
	 */
	private static void identifyIssuer(final CardIdentification pIdentification, final EmvCard pCard,
			final Application pApplication, final String pPan) {
		if (pApplication != null) {
			pIdentification.setCardIssuerIdentificationNumber(
					StringUtils.trimToNull(pApplication.getIssuerIdentificationNumber()));
			pIdentification.setCardIssuerIdentificationNumberExtended(
					StringUtils.trimToNull(pApplication.getIssuerIdentificationNumberExtended()));
			pIdentification.setIssuerCountry(pApplication.getIssuerCountryCode());
		}
		if (pCard != null) {
			pIdentification.setBic(StringUtils.trimToNull(pCard.getBic()));
			pIdentification.setIban(StringUtils.trimToNull(pCard.getIban()));
		}
		pIdentification.setPanIssuerIdentificationNumber(prefix(pPan, IIN_LENGTH));
		pIdentification.setPanLegacyIssuerIdentificationNumber(prefix(pPan, LEGACY_IIN_LENGTH));

		// The extended form is preferred: it is the only one able to carry the
		// eight digits of ISO/IEC 7812-1:2017, and EMV 4.4 Book 3 Annex A1
		// states that "the first 6 digits of the IINE (tag '9F0C') and IIN (tag
		// '42') are the same"
		String number = pIdentification.getCardIssuerIdentificationNumberExtended();
		IssuerIdentifierSourceEnum source = IssuerIdentifierSourceEnum.ISSUER_IDENTIFICATION_NUMBER_EXTENDED;
		if (number == null) {
			number = pIdentification.getCardIssuerIdentificationNumber();
			source = IssuerIdentifierSourceEnum.ISSUER_IDENTIFICATION_NUMBER;
		}
		if (number == null) {
			// Only the account number is mandatory (EMV 4.4 Book 3 Table 28),
			// so it is the last resort. The eight digit prefix is reported
			// because it is the length ISO/IEC 7812-1:2017 defines, not because
			// the card said where its issuer's number ends
			number = pIdentification.getPanIssuerIdentificationNumber();
			source = IssuerIdentifierSourceEnum.PAN;
		}
		if (number == null) {
			source = IssuerIdentifierSourceEnum.NONE;
		}
		pIdentification.setIssuerIdentificationNumber(number);
		pIdentification.setIssuerIdentificationNumberSource(source);
		pIdentification.setIssuerIdentificationNumberMatchingPan(matchesPan(pIdentification, pPan));
	}

	/**
	 * Method used to compare the Issuer Identification Number the card carries
	 * with the account number it is supposed to prefix.
	 * <p>
	 * A card is allowed to disagree with itself here. EMV 4.4 Book 3 section
	 * 10.2 lists the tags '42' and '9F0C' among the objects for which, if the
	 * terminal "recognises that any of the following data objects are
	 * incorrectly formatted, it shall ignore the formatting error and continue
	 * processing". The comparison is reported, never enforced.
	 * </p>
	 *
	 * @param pIdentification
	 *            identification being built
	 * @param pPan
	 *            account number reduced to its digits, may be null
	 * @return true when the card carried a number and it prefixes the account
	 *         number
	 */
	private static boolean matchesPan(final CardIdentification pIdentification, final String pPan) {
		if (pPan == null) {
			return false;
		}
		List<String> carried = new ArrayList<String>();
		if (pIdentification.getCardIssuerIdentificationNumberExtended() != null) {
			carried.add(pIdentification.getCardIssuerIdentificationNumberExtended());
		}
		if (pIdentification.getCardIssuerIdentificationNumber() != null) {
			carried.add(pIdentification.getCardIssuerIdentificationNumber());
		}
		if (carried.isEmpty()) {
			return false;
		}
		for (String value : carried) {
			if (!pPan.startsWith(value)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method used to grade an identification, from what it actually holds.
	 *
	 * @param pIdentification
	 *            identification being built
	 * @return the confidence level, never null
	 */
	private static IdentificationConfidenceEnum computeConfidence(final CardIdentification pIdentification) {
		// The card named its institution, which only the Bank Identifier Code
		// and the International Bank Account Number do
		if (pIdentification.getBic() != null || pIdentification.getIban() != null) {
			return IdentificationConfidenceEnum.ISSUER_NAMED;
		}
		// A number designating the issuer in a register this library does not
		// hold
		if (pIdentification.getIssuerIdentificationNumber() != null) {
			return IdentificationConfidenceEnum.ISSUER_RANGE;
		}
		// The country of the issuer is a statement of the card about its
		// issuer; the country a domestic scheme is operated in is not, so
		// getSchemeCountry() deliberately does not appear here
		if (pIdentification.getIssuerCountry() != null) {
			return IdentificationConfidenceEnum.COUNTRY;
		}
		if (pIdentification.getScheme() != null) {
			return IdentificationConfidenceEnum.SCHEME;
		}
		return IdentificationConfidenceEnum.NONE;
	}

	/**
	 * Method used to keep the leading digits of a value
	 *
	 * @param pValue
	 *            value to cut, may be null
	 * @param pLength
	 *            number of digits to keep
	 * @return the prefix, or null when the value is null or shorter than the
	 *         requested length
	 */
	private static String prefix(final String pValue, final int pLength) {
		return pValue == null || pValue.length() < pLength ? null : pValue.substring(0, pLength);
	}

	/**
	 * Method used to reduce a value to its decimal digits.<br>
	 * An account number read from a track carries its padding and its
	 * separators, and the comparisons made here are on digits only.
	 *
	 * @param pValue
	 *            value to reduce, may be null
	 * @return the digits, or null when the value holds none
	 */
	private static String digitsOf(final String pValue) {
		if (pValue == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder(pValue.length());
		for (int i = 0; i < pValue.length(); i++) {
			char current = pValue.charAt(i);
			if (current >= '0' && current <= '9') {
				builder.append(current);
			}
		}
		return builder.length() == 0 ? null : builder.toString();
	}

	/**
	 * Private constructor
	 */
	private CardIdentificationUtils() {
	}

}
