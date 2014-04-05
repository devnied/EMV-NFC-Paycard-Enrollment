package com.github.devnied.emvnfccard.utils;

import org.apache.commons.lang3.StringUtils;

import fr.devnied.bitlib.BitUtils;
import fr.devnied.bitlib.BytesUtils;

/**
 * Utils class used to extract TLV data (Tag - length - value)
 * 
 * @author julien Millau
 * 
 */
public final class TLVUtils {

	/**
	 * SFi Tag
	 */
	public static final String SFI = "88";

	/**
	 * DF name
	 */
	public static final String DF_NAME = "84";

	/**
	 * AID Tag
	 */
	public static final String AID = "4F";

	/**
	 * PDOL Tag
	 */
	public static final String PDOL = "9F 38";

	/**
	 * Log Tag
	 */
	public static final String LOG_ENTRY = "9F 4D";

	/**
	 * Track 2 Tag
	 */
	public static final String TRACK2 = "57";

	/**
	 * Application label Tag
	 */
	public static final String APPLICATION_LABEL = "50";

	/**
	 * Get TLV value as integer
	 * 
	 * @param pArray
	 *            array
	 * @param pTag
	 *            template to find
	 * @return int value
	 */
	public static int getIntValue(final byte[] pArray, final String pTag) {
		int ret = -1;
		String val = StringUtils.substringAfter(BytesUtils.bytesToString(pArray), pTag);
		if (StringUtils.isNotBlank(val)) {
			BitUtils bits = new BitUtils(BytesUtils.fromString(val));
			ret = bits.getNextInteger(bits.getNextInteger(8) * 8);
		}
		return ret;
	}

	/**
	 * Get TLV value as hexa string
	 * 
	 * @param pArray
	 *            response array
	 * @param pTag
	 *            template to parse
	 * @return hexa string or null
	 */
	public static String getHexaValue(final byte[] pArray, final String pTag) {
		String ret = null;
		String val = StringUtils.substringAfter(BytesUtils.bytesToString(pArray), pTag);
		if (StringUtils.isNotBlank(val)) {
			BitUtils bits = new BitUtils(BytesUtils.fromString(val));
			ret = bits.getNextHexaString(bits.getNextInteger(8) * 8);
		}
		return ret;
	}

	public static byte[] getArrayValue(final byte[] pArray, final String pTag) {
		byte[] ret = null;
		String val = StringUtils.substringAfter(BytesUtils.bytesToString(pArray), pTag);
		if (StringUtils.isNotBlank(val)) {
			BitUtils bits = new BitUtils(BytesUtils.fromString(val));
			int length = bits.getNextInteger(8);
			ret = new byte[length];
			for (int i = 0; i < length; i++) {
				ret[i] = (byte) bits.getNextInteger(8);
			}
		}

		return ret;
	}

	/**
	 * Private constructor
	 */
	private TLVUtils() {
	}

}
