package com.github.devnied.emvnfccard.iso7816emv;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;

import fr.devnied.bitlib.BytesUtils;

/**
 * Factory to create Tag value
 * 
 * @author Millau Julien
 * 
 */
public final class EMVTerminal {

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
		if (pTagAndLength.getTag() == EMVTags.TERMINAL_TRANSACTION_QUALIFIERS) {
			TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
			terminalQual.setContactlessEMVmodeSupported(true);
			ret = terminalQual.getBytes();
		} else if (pTagAndLength.getTag() == EMVTags.TERMINAL_COUNTRY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CountryCodeEnum.FR.getNumeric()), 4, "0"));
		} else if (pTagAndLength.getTag() == EMVTags.TRANSACTION_CURRENCY_CODE) {
			val = BytesUtils.fromString(StringUtils.leftPad(String.valueOf(CurrencyEnum.EUR.getISOCodeNumeric()), 4, "0"));
		} else if (pTagAndLength.getTag() == EMVTags.TRANSACTION_DATE) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			val = BytesUtils.fromString(sdf.format(new Date()));
		} else if (pTagAndLength.getTag() == EMVTags.TRANSACTION_TYPE) {
			val = new byte[] { (byte) TransactionTypeEnum.PURCHASE.getKey() };
		} else if (pTagAndLength.getTag() == EMVTags.AMOUNT_AUTHORISED_NUMERIC) {
			val = BytesUtils.fromString("00");
		} else if (pTagAndLength.getTag() == EMVTags.UNPREDICTABLE_NUMBER) {
			random.nextBytes(ret);
		}
		if (val != null) {
			System.arraycopy(val, Math.max(0, val.length - ret.length), ret, Math.max(ret.length - val.length, 0),
					Math.min(val.length, ret.length));
		}
		return ret;
	}

	/**
	 * Private Constructor
	 */
	private EMVTerminal() {
	}

}
