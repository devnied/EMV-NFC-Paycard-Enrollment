package com.github.devnied.emvnfccard.iso7816emv;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;

import fr.devnied.bitlib.BytesUtils;

public final class TagValueFactory {

	public static byte[] constructValue(final TagAndLength pTagAndLength) {
		byte ret[] = new byte[pTagAndLength.getLength()];
		byte val[] = null;
		if (pTagAndLength.getTag() == EMVTags.TERMINAL_TRANSACTION_QUALIFIERS) {
			TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
			terminalQual.setContactlessEMVmodeSupported(true);
			ret = terminalQual.getBytes();
		} else if (pTagAndLength.getTag() == EMVTags.TERMINAL_COUNTRY_CODE) {
			val = BytesUtils.toByteArray(CountryCodeEnum.FR.getNumeric());
		} else if (pTagAndLength.getTag() == EMVTags.TRANSACTION_CURRENCY_CODE) {
			val = BytesUtils.toByteArray(CurrencyEnum.EUR.getISOCodeNumeric());
		} else if (pTagAndLength.getTag() == EMVTags.TRANSACTION_DATE) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
			val = BytesUtils.fromString(sdf.format(new Date()));
		} else if (pTagAndLength.getTag() == EMVTags.TRANSACTION_TYPE) {
			val = BytesUtils.fromString("00");
		} else if (pTagAndLength.getTag() == EMVTags.AMOUNT_AUTHORISED_NUMERIC) {
			val = BytesUtils.fromString("99");
		}
		if (val != null) {
			System.arraycopy(val, Math.max(0, val.length - ret.length), ret, Math.max(ret.length - val.length, 0),
					Math.min(val.length, ret.length));
		}
		return ret;
	}

}
