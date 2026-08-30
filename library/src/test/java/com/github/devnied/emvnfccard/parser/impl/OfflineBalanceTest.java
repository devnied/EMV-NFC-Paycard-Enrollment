package com.github.devnied.emvnfccard.parser.impl;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * The offline (electronic cash) balance is a 6 bytes packed BCD amount
 * expressed in the minor unit of the application currency: tag '9F79'
 * (Electronic Cash Balance) for the Chinese UnionPay/PBOC applications, tag
 * '9F50' (Offline Accumulator Balance) for the M/Chip kernel.
 */
public class OfflineBalanceTest {

	/**
	 * The balance decoding does not need any card exchange, a parser without
	 * template is enough to exercise it.
	 */
	private static final AbstractParser PARSER = new AbstractParser(null) {

		@Override
		public Pattern getId() {
			return Pattern.compile(".*");
		}

		@Override
		public boolean parse(final Application pApplication) {
			return false;
		}
	};

	private Application application(final int pCurrencyExponent) {
		Application app = new Application();
		app.setCurrencyExponent(pCurrencyExponent);
		return app;
	}

	@Test
	public void testDefaultExponent() {
		// 000000012345 -> 123.45 with the ISO 4217 default of 2 decimals
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("000000012345"), application(2)))
				.isEqualTo(new BigDecimal("123.45"));
		// The exponent is unknown, the ISO 4217 default is used
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("000000012345"), application(AbstractData.UNKNOWN)))
				.isEqualTo(new BigDecimal("123.45"));
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("000000012345"), null)).isEqualTo(new BigDecimal("123.45"));
	}

	@Test
	public void testCurrencyWithoutDecimals() {
		// The Japanese yen and the Korean won have no minor unit (exponent 0)
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("000000012345"), application(0)))
				.isEqualTo(new BigDecimal("12345"));
		// The Tunisian dinar and the Jordanian dinar have 3 decimals
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("000000012345"), application(3)))
				.isEqualTo(new BigDecimal("12.345"));
	}

	@Test
	public void testLargeBalanceKeepsEveryDigit() {
		// A float only carries ~7 significant digits, the n12 balance carries 12
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("999999999999"), application(2)))
				.isEqualTo(new BigDecimal("9999999999.99"));
	}

	@Test
	public void testEmptyBalance() {
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("000000000000"), application(2))).isEqualTo(new BigDecimal("0.00"));
		Assertions.assertThat(PARSER.parseOfflineBalance(null, application(2))).isNull();
		// A value that is not packed BCD is not an amount
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("0000000123AB"), application(2))).isNull();
		// Both tags are numbered in the range reserved for the payment systems,
		// a value that is not encoded as n12 is not a balance
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("00012345"), application(2))).isNull();
		Assertions.assertThat(PARSER.parseOfflineBalance(BytesUtils.fromString("00000000012345"), application(2))).isNull();
	}

	/**
	 * A value that is not an amount is still kept raw, with the tag it came
	 * from: the reader says what it saw.
	 */
	@Test
	public void testRawValueIsKeptWhenItIsNotAnAmount() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F5000", "9F50 05 0000012345 9000");
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());
		Application app = application(2);
		app.setAid(BytesUtils.fromString("A0000000041010"));

		Assertions.assertThat(parser.getOfflineBalance(app)).isNull();

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawOfflineBalance())).isEqualTo("0000012345");
		Assertions.assertThat(app.getOfflineBalanceTag()).isEqualTo("9F50");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F50")).isEqualTo("9000");
		// A MasterCard is not asked for the Electronic Cash Balance
		Assertions.assertThat(app.getGetDataStatusWords().containsKey("9F79")).isFalse();

		// The 6 bytes value of a UnionPay purse, decoded and kept
		ScriptedProvider unionPay = new ScriptedProvider().on("80CA9F7900", "9F79 06 000000012345 9000");
		Application purse = application(2);
		purse.setAid(BytesUtils.fromString("A000000333010101"));
		Assertions.assertThat(new EmvParser(EmvTemplate.Builder().setProvider(unionPay).build()).getOfflineBalance(purse))
				.isEqualTo(new BigDecimal("123.45"));
		Assertions.assertThat(purse.getOfflineBalanceTag()).isEqualTo("9F79");
		Assertions.assertThat(purse.getRawOfflineBalance()).hasSize(6);
	}

}
