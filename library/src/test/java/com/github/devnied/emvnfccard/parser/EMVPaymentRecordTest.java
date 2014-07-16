package com.github.devnied.emvnfccard.parser;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.utils.TLVUtil;

import fr.devnied.bitlib.BytesUtils;

public class EMVPaymentRecordTest {

	@Test
	public void test() {
		EMVPaymentRecord record = new EMVPaymentRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 00 00 80 02 50 09 78 14 07 14 00 90 00"), null);
		Assertions.assertThat(record.getAmount()).isEqualTo(0);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("80");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getTransactionDate()).isNotNull();

		List<TagAndLength> list = TLVUtil.parseTagAndLength(BytesUtils.fromString("9F 02 05 5F 2A 02"));

		record = new EMVPaymentRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 00 09 78 00 90 00"), list);
		Assertions.assertThat(record.getAmount()).isEqualTo(0);
		Assertions.assertThat(record.getCyptogramData()).isNull();
		Assertions.assertThat(record.getTransactionType()).isNull();
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isNull();
		Assertions.assertThat(record.getTransactionDate()).isNull();

		record = new EMVPaymentRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 00 00 80 02 50 09 78 14 07 14 00 90 00"), null);
		Assertions.assertThat(record.getAmount()).isEqualTo(0);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("80");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getTransactionDate()).isNotNull();

	}

}
