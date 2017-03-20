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
package com.github.devnied.emvnfccard.enums;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Class used to define all supported NFC EMV paycard. <link>http://en.wikipedia.org/wiki/Europay_Mastercard_Visa</link>
 * 
 * @author MILLAU Julien
 * 
 */
public enum EmvCardScheme {

	VISA("VISA", "^4[0-9]{6,}$", "A0 00 00 00 03", "A0 00 00 00 03 10 10", "A0 00 00 00 98 08 48"), //
	MASTER_CARD("Master card", "^5[1-5][0-9]{5,}$", "A0 00 00 00 04", "A0 00 00 00 05"), //
	AMERICAN_EXPRESS("American express", "^3[47][0-9]{5,}$", "A0 00 00 00 25"), //
	CB("CB", null, "A0 00 00 00 42"), //
	LINK("LINK", null, "A0 00 00 00 29"), //
	JCB("JCB", "^(?:2131|1800|35[0-9]{3})[0-9]{3,}$", "A0 00 00 00 65"), //
	DANKORT("Dankort", null, "A0 00 00 01 21 10 10"), //
	COGEBAN("CoGeBan", null, "A0 00 00 01 41 00 01"), //
	DISCOVER("Discover", "(6011|65|64[4-9]|622)[0-9]*", "A0 00 00 01 52 30 10"), //
	BANRISUL("Banrisul", null, "A0 00 00 01 54"), //
	SPAN("Saudi Payments Network", null, "A0 00 00 02 28"), //
	INTERAC("Interac", null, "A0 00 00 02 77"), //
	ZIP("Discover Card", null, "A0 00 00 03 24"), //
	UNIONPAY("UnionPay", "^62[0-9]{14,17}", "A0 00 00 03 33"), //
	EAPS("Euro Alliance of Payment Schemes", null, "A0 00 00 03 59"), //
	VERVE("Verve", null, "A0 00 00 03 71"), //
	TENN("The Exchange Network ATM Network", null, "A0 00 00 04 39"), //
	RUPAY("Rupay", null, "A0 00 00 05 24 10 10"), //
	ПРО100("ПРО100", null, "A0 00 00 04 32 00 01"), //
	ZKA("ZKA", null, "D2 76 00 00 25 45 50 01 00"), //
	BANKAXEPT("Bankaxept", null, "D5 78 00 00 02 10 10"), //
	BRADESCO("BRADESCO", null, "F0 00 00 00 03 00 01"),
	MIDLAND("Midland", null, "A0 00 00 00 24 01"), //
	PBS("PBS", null, "A0 00 00 01 21 10 10"), //
	ETRANZACT("eTranzact", null, "A0 00 00 04 54"), //
	GOOGLE("Google", null, "A0 00 00 04 76 6C"), //
	INTER_SWITCH("InterSwitch", null, "A0 00 00 03 71 00 01");

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
	 * Constructor using fields
	 * 
	 * @param pAid
	 *            Card AID or RID
	 * @param pScheme
	 *            scheme name
	 * @param pRegex
	 *            Card regex
	 */
	private EmvCardScheme(final String pScheme, final String pRegex, final String... pAids) {
		aids = pAids;
		aidsByte = new byte[pAids.length][];
		for (int i = 0; i < aids.length; i++) {
			aidsByte[i] = BytesUtils.fromString(pAids[i]);
		}
		name = pScheme;
		if (StringUtils.isNotBlank(pRegex)) {
			pattern = Pattern.compile(pRegex);
		} else {
			pattern = null;
		}
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
			for (EmvCardScheme val : EmvCardScheme.values()) {
				for (String schemeAid : val.getAid()) {
					if (aid.startsWith(StringUtils.deleteWhitespace(schemeAid))) {
						ret = val;
						break;
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
