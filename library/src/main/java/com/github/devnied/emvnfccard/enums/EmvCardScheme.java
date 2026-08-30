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
package com.github.devnied.emvnfccard.enums;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Class used to define all supported NFC EMV paycard. <link>http://en.wikipedia.org/wiki/Europay_Mastercard_Visa</link>
 * <p>
 * International schemes are declared with their Registered Application Provider
 * Identifier (RID) so that every product of the brand is matched, domestic
 * schemes are declared with their full Application Identifier (AID) and with
 * the country they are operated in.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public enum EmvCardScheme {

	VISA("VISA", "^4[0-9]{6,}$", null, "A0 00 00 00 03", "A0 00 00 00 03 10 10", "A0 00 00 00 03 20 10",
			"A0 00 00 00 03 20 20", "A0 00 00 00 03 30 10", "A0 00 00 00 03 80 10", "A0 00 00 00 98 08 48",
			"A0 00 00 00 98 08 40"), //
	MASTER_CARD("Master card", "^5[1-5][0-9]{5,}$", null, "A0 00 00 00 04", "A0 00 00 00 05", "A0 00 00 00 04 10 10",
			"A0 00 00 00 04 10 10 12 13", "A0 00 00 00 04 10 10 12 15", "A0 00 00 00 04 22 03", "A0 00 00 00 04 30 60",
			"A0 00 00 00 04 60 00"), //
	AMERICAN_EXPRESS("American express", "^3[47][0-9]{5,}$", null, "A0 00 00 00 25"), //
	CB("CB", null, CountryCodeEnum.FR, "A0 00 00 00 42"), //
	LINK("LINK", null, CountryCodeEnum.GB, "A0 00 00 00 29"), //
	JCB("JCB", "^(?:2131|1800|35[0-9]{3})[0-9]{3,}$", CountryCodeEnum.JP, "A0 00 00 00 65"), //
	DANKORT("Dankort", null, CountryCodeEnum.DK, "A0 00 00 01 21 10 10"), //
	COGEBAN("CoGeBan", null, CountryCodeEnum.IT, "A0 00 00 01 41 00 01"), //
	DISCOVER("Discover", "(6011|65|64[4-9]|622)[0-9]*", null, "A0 00 00 01 52 30 10"), //
	DINERS_CLUB("Diners Club", null, null, "A0 00 00 01 52 40 10"), //
	BANRISUL("Banrisul", null, CountryCodeEnum.BR, "A0 00 00 01 54"), //
	SPAN("Saudi Payments Network", null, CountryCodeEnum.SA, "A0 00 00 02 28"), //
	INTERAC("Interac", null, CountryCodeEnum.CA, "A0 00 00 02 77"), //
	ZIP("Discover Card", null, null, "A0 00 00 03 24"), //
	UNIONPAY("UnionPay", "^62[0-9]{14,17}", CountryCodeEnum.CN, "A0 00 00 03 33"), //
	CHIPKNIP("Chipknip", null, CountryCodeEnum.NL, "A0 00 00 03 15 60 20"), //
	EAPS("Euro Alliance of Payment Schemes", null, null, "A0 00 00 03 59"), //
	GIROCARD("girocard", null, CountryCodeEnum.DE, "A0 00 00 03 59 10 10 02 80 01", "D2 76 00 00 25 47 41 01 00"), //
	VERVE("Verve", null, CountryCodeEnum.NG, "A0 00 00 03 71"), //
	TENN("The Exchange Network ATM Network", null, CountryCodeEnum.CA, "A0 00 00 04 39"), //
	RUPAY("Rupay", null, CountryCodeEnum.IN, "A0 00 00 05 24 10 10"), //
	ПРО100("ПРО100", null, CountryCodeEnum.RU, "A0 00 00 04 32 00 01"), //
	GELDKARTE("GeldKarte/ZKA", null, CountryCodeEnum.DE, "D2 76 00 00 25 45 50 02 00", "D2 76 00 00 25 45 50 01 00",
			"D2 76 00 00 25"), //
	BANKAXEPT("Bankaxept", null, CountryCodeEnum.NO, "D5 78 00 00 02 10 10"), //
	BRADESCO("BRADESCO", null, CountryCodeEnum.BR, "F0 00 00 00 03 00 01"),
	ELO("Elo", null, CountryCodeEnum.BR, "A0 00 00 05 32 10 10"), //
	MIDLAND("Midland", null, CountryCodeEnum.GB, "A0 00 00 00 24 01"), //
	PBS("PBS", null, CountryCodeEnum.DK, "A0 00 00 01 21 10 10"), //
	ETRANZACT("eTranzact", null, CountryCodeEnum.NG, "A0 00 00 04 54"), //
	GOOGLE("Google", null, null, "A0 00 00 04 76 6C"), //
	INTER_SWITCH("InterSwitch", null, CountryCodeEnum.NG, "A0 00 00 03 71 00 01"),
	MIR("МИР", null, CountryCodeEnum.RU, "A0 00 00 06 58 20 10", "A0 00 00 06 58 10 10", "A0 00 00 06 58 10 11"),
	PROSTIR("Простiр", null, CountryCodeEnum.UA, "D8 04 00 00 01 30 10"),
	TROY("Troy", null, CountryCodeEnum.TR, "A0 00 00 06 72 30 10", "A0 00 00 06 72 30 20"), //
	MEEZA("Meeza", "^50[0-9]{11}(?:[0-9]{3})?$", CountryCodeEnum.EG, "A0 00 00 07 32 10 01 23");

	/**
	 * array of Card AID or partial AID (RID)
	 */
	private final String[] aids;

	/**
	 * array of Aid in byte
	 */
	private final byte[][] aidsByte;

	/**
	 * Card scheme (card number IIN ranges)
	 */
	private final String name;

	/**
	 * Card number pattern regex
	 */
	private final Pattern pattern;

	/**
	 * Country the scheme is operated in, null for the international schemes
	 */
	private final CountryCodeEnum country;

	/**
	 * Constructor using fields
	 *
	 * @param pScheme
	 *            scheme name
	 * @param pRegex
	 *            Card regex
	 * @param pCountry
	 *            country of a domestic scheme, null for an international one
	 * @param pAids
	 *            list of cards AID or RID
	 */
	private EmvCardScheme(final String pScheme, final String pRegex, final CountryCodeEnum pCountry, final String... pAids) {
		aids = pAids;
		aidsByte = new byte[pAids.length][];
		for (int i = 0; i < aids.length; i++) {
			aidsByte[i] = BytesUtils.fromString(pAids[i]);
		}
		name = pScheme;
		country = pCountry;
		if (StringUtils.isNotBlank(pRegex)) {
			pattern = Pattern.compile(pRegex);
		} else {
			pattern = null;
		}
	}

	/**
	 * Method used to get the field country
	 *
	 * @return the country the scheme is operated in or null for an
	 *         international scheme
	 */
	public CountryCodeEnum getCountry() {
		return country;
	}

	/**
	 * Method used to know if the scheme is a domestic one (only usable in the
	 * country of the issuer).
	 *
	 * @return true if the scheme is a domestic scheme
	 */
	public boolean isDomestic() {
		return country != null;
	}

	/**
	 * Method used to get the field aid
	 *
	 * @return the aid
	 */
	public String[] getAid() {
		return aids;
	}

	/**
	 * Method used to get the field name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get card type by AID
	 *
	 * @param pAid
	 *            card AID
	 * @return CardType or null
	 */
	public static EmvCardScheme getCardTypeByAid(final String pAid) {
		EmvCardScheme ret = null;
		if (pAid != null) {
			String aid = StringUtils.deleteWhitespace(pAid);
			int bestLength = 0;
			for (EmvCardScheme val : EmvCardScheme.values()) {
				for (String schemeAid : val.getAid()) {
					String candidate = StringUtils.deleteWhitespace(schemeAid);
					// The most specific AID wins, so a domestic scheme declared
					// with its full AID is preferred over the RID of the
					// international scheme it is built on. Two schemes sharing
					// the very same AID (Dankort and PBS) are still resolved
					// the historical way: the last one declared wins.
					if (candidate.length() >= bestLength && aid.startsWith(candidate)) {
						ret = val;
						bestLength = candidate.length();
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Method used to the the card type with regex
	 *
	 * @param pCardNumber
	 *            card number
	 * @return the type of the card using regex
	 */
	public static EmvCardScheme getCardTypeByCardNumber(final String pCardNumber) {
		EmvCardScheme ret = null;
		if (pCardNumber != null) {
			for (EmvCardScheme val : EmvCardScheme.values()) {
				if (val.pattern != null && val.pattern.matcher(StringUtils.deleteWhitespace(pCardNumber)).matches()) {
					ret = val;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * Method used to get the field aidByte
	 *
	 * @return the aidByte
	 */
	public byte[][] getAidByte() {
		return aidsByte;
	}

}
