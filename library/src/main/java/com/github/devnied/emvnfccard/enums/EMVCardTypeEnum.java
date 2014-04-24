package com.github.devnied.emvnfccard.enums;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Class used to define all supported NFC EMV paycard. <link>http://en.wikipedia.org/wiki/Europay_Mastercard_Visa</link>
 * 
 * @author julien Millau
 * 
 */
public enum EMVCardTypeEnum {

	VISA("A0 00 00 00 03", "VISA", "^4[0-9]{12,15}"), //
	MASTER_CARD1("A0 00 00 00 04", "Master card 1", "^5[0-5][0-9]{14}"), //
	MASTER_CARD2("A0 00 00 00 05", "Master card 2", "^5[0-5][0-9]{14}"), //
	AMERICAN_EXPRESS("A0 00 00 00 25", "American express", "^3[47][0-9]{13}"), //
	CB("A0 00 00 00 42", "CB"), //
	LINK("A0 00 00 00 29", "LINK"), //
	JCB("A0 00 00 00 65", "JCB", "^35[0-9]{14}"), //
	DANKORT("A0 00 00 01 21", "Dankort"), //
	COGEBAN("A0 00 00 01 41", "CoGeBan"), //
	DISCOVER("A0 00 00 01 52", "Discover", "(6011|65|64[4-9]|622)[0-9]*"), //
	BANRISUL("A0 00 00 01 54", "Banrisul"), //
	SPAN("A0 00 00 02 28", "Saudi Payments Network"), //
	INTERAC("A0 00 00 02 77", "Interac"), //
	ZIP("A0 00 00 03 24", "Discover Card"), //
	UNIONPAY("A0 00 00 03 33", "UnionPay", "^62[0-9]{14,17}"), //
	EAPS("A0 00 00 03 59", "Euro Alliance of Payment Schemes"), //
	VERVE("A0 00 00 03 71", "Verve"), //
	TENN("A0 00 00 04 39", "The Exchange Network ATM Network"), //
	RUPAY("A0 00 00 05 24", "Rupay"), //
	ПРО100("A0 00 00 04 32", "ПРО100");

	/**
	 * Card AID or partial AID (RID)
	 */
	private final String aid;

	/**
	 * Card scheme (card number IIN ranges)
	 */
	private final String scheme;

	/**
	 * Card number regex
	 */
	private final String regex;

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
	private EMVCardTypeEnum(final String pAid, final String pScheme, final String pRegex) {
		aid = pAid;
		scheme = pScheme;
		regex = pRegex;
	}

	/**
	 * Constructor using fields
	 * 
	 * @param pAid
	 *            Card AID or RID
	 * @param pScheme
	 *            scheme name
	 */
	private EMVCardTypeEnum(final String pAid, final String pScheme) {
		aid = pAid;
		scheme = pScheme;
		regex = null;
	}

	/**
	 * Method used to get the field aid
	 * 
	 * @return the aid
	 */
	public String getAid() {
		return aid;
	}

	/**
	 * Method used to get the field scheme
	 * 
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Get card type by AID
	 * 
	 * @param pAid
	 *            card AID
	 * @return CardType or null
	 */
	public static EMVCardTypeEnum getCardTypeByAid(final String pAid) {
		EMVCardTypeEnum ret = null;
		if (pAid != null) {
			for (EMVCardTypeEnum val : EMVCardTypeEnum.values()) {
				if (pAid.startsWith(StringUtils.deleteWhitespace(val.getAid()))) {
					ret = val;
					break;
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
	public static EMVCardTypeEnum getCardTypeByCardNumber(final String pCardNumber) {
		EMVCardTypeEnum ret = null;
		if (pCardNumber != null) {
			for (EMVCardTypeEnum val : EMVCardTypeEnum.values()) {
				if (val.regex != null && Pattern.matches(val.regex, StringUtils.deleteWhitespace(pCardNumber))) {
					ret = val;
					break;
				}
			}
		}
		return ret;
	}

}
