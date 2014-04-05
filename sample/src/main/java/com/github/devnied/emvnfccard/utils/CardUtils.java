package com.github.devnied.emvnfccard.utils;

import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.enums.EMVCardTypeEnum;

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
	 * @return the card number formated
	 */
	public static String formatCardNumber(final String pCardNumber) {
		if (StringUtils.isBlank(pCardNumber)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.deleteWhitespace(pCardNumber).replaceAll("\\d{4}", "$0 ").trim();
	}

	/**
	 * Method used to get the resource Id for card type
	 * 
	 * @param pEnum
	 *            card enum
	 * @return resource id
	 */
	public static int getResourceIdCardType(final EMVCardTypeEnum pEnum) {
		int ret = 0;
		switch (pEnum) {
		case AMERICAN_EXPRESS:
			ret = R.drawable.amex;
			break;
		case MASTER_CARD1:
		case MASTER_CARD2:
			ret = R.drawable.mastercard;
			break;
		case VISA:
			ret = R.drawable.visa;
			break;
		default:
			break;

		}
		return ret;
	}

	/**
	 * Private constructor
	 */
	private CardUtils() {
	}

}
