package com.github.devnied.emvnfccard.utils;

import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;

/**
 * Utils class for card
 *
 * @author Millau Julien
 *
 */
public final class CardUtils {

	/**
	 * Method used to format card number
	 *
	 * @param pCardNumber
	 *            card number to display
	 * @param pType
	 *            card type
	 *
	 * @return the card number formated
	 */
	public static String formatCardNumber(final String pCardNumber, final EmvCardScheme pType) {
		String ret = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(pCardNumber)) {
			// format amex
			if (pType != null && pType == EmvCardScheme.AMERICAN_EXPRESS) {
				ret = StringUtils.deleteWhitespace(pCardNumber).replaceFirst("\\d{4}", "$0 ").replaceFirst("\\d{6}", "$0 ")
						.replaceFirst("\\d{5}", "$0").trim();
			} else {
				ret = StringUtils.deleteWhitespace(pCardNumber).replaceAll("\\d{4}", "$0 ").trim();
			}
		} else {
			ret = "0000 0000 0000 0000";
		}
		return ret;
	}

	/**
	 * Method used to format AID
	 *
	 * @param pAid
	 *            card aid
	 * @return formated AID
	 */
	public static String formatAid(final String pAid) {
		String ret = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(pAid)) {
			ret = StringUtils.deleteWhitespace(pAid).replaceAll("[A-F0-9]{2}", "$0 ").trim();
		}
		return ret;
	}

	/**
	 * Method used to get the resource Id for card type
	 *
	 * @param pEnum
	 *            card enum
	 * @return resource id
	 */
	public static int getResourceIdCardType(final EmvCardScheme pEnum) {
		int ret = 0;
		if (pEnum != null) {
			switch (pEnum) {
			case AMERICAN_EXPRESS:
				ret = R.drawable.amex;
				break;
			case MASTER_CARD:
				ret = R.drawable.mastercard;
				break;
			case VISA:
				ret = R.drawable.visa;
				break;
			case JCB:
				ret = R.drawable.jcb;
				break;
			case UNIONPAY:
				ret = R.drawable.unionpay;
				break;
			default:
				break;

			}
		}
		return ret;
	}

	/**
	 * Private constructor
	 */
	private CardUtils() {
	}

}
