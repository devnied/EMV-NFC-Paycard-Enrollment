package com.pro100svitlo.creditCardNfcReader.iso7816emv;

import com.pro100svitlo.creditCardNfcReader.utils.BytesUtils;
import com.pro100svitlo.creditCardNfcReader.model.enums.CountryCodeEnum;
import com.pro100svitlo.creditCardNfcReader.model.enums.CurrencyEnum;
import com.pro100svitlo.creditCardNfcReader.model.enums.TransactionTypeEnum;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Factory to create Tag value
 */
public final class EmvTerminal {

	/**
	 * Random
	 */
	private static final SecureRandom random = new SecureRandom();

	/**
	 * Method used to construct value from tag and length
	 *
	 * @param pTagAndLength
	 *            tag and length value
	 * @return tag value in byte
	 */
	public static byte[] constructValue(final TagAndLength pTagAndLength) {
		byte ret[] = new byte[pTagAndLength.getLength()];
		byte val[] = null;
		if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) {
			TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
			terminalQual.setContactlessEMVmodeSupported(true);
			terminalQual.setReaderIsOfflineOnly(true);
			val = terminalQual.getBytes();
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_COUNTRY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CountryCodeEnum.FR.getNumeric()), pTagAndLength.getLength() * 2,
					"0"));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CurrencyEnum.EUR.getISOCodeNumeric()),
					pTagAndLength.getLength() * 2, "0"));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_DATE) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			val = BytesUtils.fromString(sdf.format(new Date()));
		} else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TYPE) {
			val = new byte[] { (byte) TransactionTypeEnum.PURCHASE.getKey() };
		} else if (pTagAndLength.getTag() == EmvTags.AMOUNT_AUTHORISED_NUMERIC) {
			val = BytesUtils.fromString("00");
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TYPE) {
			val = new byte[] { 0x22 };
		} else if (pTagAndLength.getTag() == EmvTags.TERMINAL_CAPABILITIES) {
			val = new byte[] { (byte) 0xE0, (byte) 0xA0, 0x00 };
		} else if (pTagAndLength.getTag() == EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES) {
			val = new byte[] { (byte) 0x8e, (byte) 0, (byte) 0xb0, 0x50, 0x05 };
		} else if (pTagAndLength.getTag() == EmvTags.DS_REQUESTED_OPERATOR_ID) {
			val = BytesUtils.fromString("7345123215904501");
		} else if (pTagAndLength.getTag() == EmvTags.UNPREDICTABLE_NUMBER) {
			random.nextBytes(ret);
		}
		if (val != null) {
			System.arraycopy(val, 0, ret, 0, Math.min(val.length, ret.length));
		}
		return ret;
	}

	/**
	 * Private Constructor
	 */
	private EmvTerminal() {
	}

}
