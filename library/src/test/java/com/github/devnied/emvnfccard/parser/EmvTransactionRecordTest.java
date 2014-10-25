package com.github.devnied.emvnfccard.parser;

import java.text.SimpleDateFormat;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class EmvTransactionRecordTest {

	@Test
	public void test() {
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");
		EmvTransactionRecord record = new EmvTransactionRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 00 00 80 02 50 09 78 14 07 14 00 10 23 45 90 00"), null);
		Assertions.assertThat(record.getAmount()).isEqualTo(0);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("80");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(time.format(record.getTime())).isEqualTo("102345");

		List<TagAndLength> list = TlvUtil.parseTagAndLength(BytesUtils.fromString("9F 02 05 5F 2A 02"));

		record = new EmvTransactionRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 00 09 78 00 90 00"), list);
		Assertions.assertThat(record.getAmount()).isEqualTo(0);
		Assertions.assertThat(record.getCyptogramData()).isNull();
		Assertions.assertThat(record.getTransactionType()).isNull();
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isNull();
		Assertions.assertThat(record.getDate()).isNull();

		record = new EmvTransactionRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 00 00 80 02 50 09 78 14 07 14 00 10 23 45 90 00"), null);
		Assertions.assertThat(record.getAmount()).isEqualTo(0);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("80");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(time.format(record.getTime())).isEqualTo("102345");

		record = new EmvTransactionRecord();
		record.parse(BytesUtils.fromString("00 00 00 00 20 00 40 02 50 09 78 13 11 22 01 10 23 45 90 00"), null);
		Assertions.assertThat(record.getAmount()).isEqualTo(2000);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.CASH_ADVANCE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(time.format(record.getTime())).isEqualTo("102345");

		record = new EmvTransactionRecord();
		record.parse(BytesUtils.fromString("00 00 00 01 71 00 40 07 92 09 49 13 08 05 00 10 23 45 90 00"), null);
		Assertions.assertThat(record.getAmount()).isEqualTo(17100);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.TRY);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.TR);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(time.format(record.getTime())).isEqualTo("102345");

	}

	@Test
	public void testCurrency() {
		SimpleDateFormat time = new SimpleDateFormat("HHmmss");
		List<TagAndLength> list = TlvUtil.parseTagAndLength(BytesUtils
				.fromString("9F 27 01 9F 02 06 5F 2A 02 9A 03 9F 36 02 9F 52 06 DF 3E 01 9F 21 03 9F 7C 14"));

		EmvTransactionRecord record = new EmvTransactionRecord();
		record.parse(
				BytesUtils
						.fromString("40 00 00 00 00 26 30 09 78 14 09 20 00 44 60 10 03 24 00 00 00 21 43 57 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 90 00"),
				list);
		Assertions.assertThat(record.getAmount()).isEqualTo(2630);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(null);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(null);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(time.format(record.getTime())).isEqualTo("214357");
	}

}
