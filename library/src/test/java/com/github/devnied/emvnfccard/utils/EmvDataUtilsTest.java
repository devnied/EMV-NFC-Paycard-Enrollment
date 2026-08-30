package com.github.devnied.emvnfccard.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collections;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class EmvDataUtilsTest {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

	@Test
	public void testCharsetFromIssuerCodeTableIndex() {
		// The Issuer Code Table Index is the part number of ISO/IEC 8859
		Assertions.assertThat(EmvDataUtils.getCharsetName(1)).isEqualTo("ISO-8859-1");
		// Cyrillic, used by the cards issued in Russia and Bulgaria
		Assertions.assertThat(EmvDataUtils.getCharsetName(5)).isEqualTo("ISO-8859-5");
		// Greek
		Assertions.assertThat(EmvDataUtils.getCharsetName(7)).isEqualTo("ISO-8859-7");
		// Latin-5, used by the cards issued in Turkey
		Assertions.assertThat(EmvDataUtils.getCharsetName(9)).isEqualTo("ISO-8859-9");
		// Out of range values fall back on the EMV common character set
		Assertions.assertThat(EmvDataUtils.getCharsetName(0)).isEqualTo(EmvDataUtils.DEFAULT_CHARSET);
		Assertions.assertThat(EmvDataUtils.getCharsetName(99)).isEqualTo(EmvDataUtils.DEFAULT_CHARSET);
		Assertions.assertThat(EmvDataUtils.getCharsetName((byte[]) null)).isEqualTo(EmvDataUtils.DEFAULT_CHARSET);
		// Value of the tag '9F11' read from the Turkish MasterCard of the
		// MasterCardPpse2 trace
		Assertions.assertThat(EmvDataUtils.getCharsetName(new byte[] { 0x09 })).isEqualTo("ISO-8859-9");
	}

	@Test
	public void testDecodeText() {
		// "MASTERCARD", value of the tag '9F12' of the MasterCardPpse2 trace
		Assertions.assertThat(EmvDataUtils.decodeText(BytesUtils.fromString("4D4153544552434152 44"), null))
				.isEqualTo("MASTERCARD");
		// A card pads the Application Label with spaces
		Assertions.assertThat(EmvDataUtils.decodeText(BytesUtils.fromString("434220202020"), null)).isEqualTo("CB");
		Assertions.assertThat(EmvDataUtils.decodeText(null, null)).isNull();
		Assertions.assertThat(EmvDataUtils.decodeText(new byte[0], null)).isNull();
		// 0xDD is 'Ý' in ISO-8859-1 but 'İ' (dotted capital I) in ISO-8859-9,
		// which is what a Turkish issuer means with the code table index 9
		Assertions.assertThat(EmvDataUtils.decodeText(new byte[] { (byte) 0xDD }, new byte[] { 0x09 })).isEqualTo("İ");
		Assertions.assertThat(EmvDataUtils.decodeText(new byte[] { (byte) 0xDD }, new byte[] { 0x01 })).isEqualTo("Ý");
	}

	@Test
	public void testGetNumericValue() {
		// Application PAN (tag '5A') of the VisaCardPpse3 trace
		Assertions.assertThat(EmvDataUtils.getNumericValue(BytesUtils.fromString("4999999999999999")))
				.isEqualTo("4999999999999999");
		// A PAN shorter than the field is right padded with 'F' nibbles, only
		// those are stripped
		Assertions.assertThat(EmvDataUtils.getNumericValue(BytesUtils.fromString("4999999999999FFF")))
				.isEqualTo("4999999999999");
		Assertions.assertThat(EmvDataUtils.getNumericValue(null)).isNull();
	}

	@Test
	public void testGetNumericIntValue() {
		// Issuer Country Code (tag '5F28') of the VisaCardPpse3 trace
		Assertions.assertThat(EmvDataUtils.getNumericIntValue(BytesUtils.fromString("0826"), -1)).isEqualTo(826);
		// Application Currency Code (tag '9F42'), euro
		Assertions.assertThat(EmvDataUtils.getNumericIntValue(BytesUtils.fromString("0978"), -1)).isEqualTo(978);
		Assertions.assertThat(EmvDataUtils.getNumericIntValue(null, -1)).isEqualTo(-1);
		// A value that is not packed BCD cannot be read as a number
		Assertions.assertThat(EmvDataUtils.getNumericIntValue(BytesUtils.fromString("AB"), -1)).isEqualTo(-1);
	}

	@Test
	public void testGetBinaryValue() {
		// Application Transaction Counter (tag '9F36') of the VisaCardPpse3
		// trace
		Assertions.assertThat(EmvDataUtils.getBinaryValue(BytesUtils.fromString("00D9"), -1)).isEqualTo(217);
		Assertions.assertThat(EmvDataUtils.getBinaryValue(null, -1)).isEqualTo(-1);
		Assertions.assertThat(EmvDataUtils.getBinaryValue(new byte[9], -1)).isEqualTo(-1);
	}

	@Test
	public void testParseDate() {
		// Application Expiration Date (tag '5F24') of the VisaCardPpse3 trace
		Assertions.assertThat(DATE_FORMAT.format(EmvDataUtils.parseDate(BytesUtils.fromString("170630")))).isEqualTo("30/06/2017");
		// A card that pads the day with '00' expires the last day of the month
		Assertions.assertThat(DATE_FORMAT.format(EmvDataUtils.parseDate(BytesUtils.fromString("250200")))).isEqualTo("28/02/2025");
		// The two digits year uses the [1950, 2049] sliding window
		Assertions.assertThat(DATE_FORMAT.format(EmvDataUtils.parseDate(BytesUtils.fromString("991231")))).isEqualTo("31/12/1999");
		Assertions.assertThat(EmvDataUtils.parseDate(BytesUtils.fromString("171380"))).isNull();
		Assertions.assertThat(EmvDataUtils.parseDate(BytesUtils.fromString("1706"))).isNull();
		Assertions.assertThat(EmvDataUtils.parseDate(null)).isNull();
	}

	@Test
	public void testParseLanguagePreference() {
		// Language Preference (tag '5F2D') of the GeldKartePpse trace
		Assertions.assertThat(EmvDataUtils.parseLanguagePreference(BytesUtils.fromString("6465656E")))
				.isEqualTo(Arrays.asList("de", "en"));
		// Language Preference of the MasterCardPpse trace
		Assertions.assertThat(EmvDataUtils.parseLanguagePreference(BytesUtils.fromString("6672656E")))
				.isEqualTo(Arrays.asList("fr", "en"));
		// Language Preference of the InteractPpse trace
		Assertions.assertThat(EmvDataUtils.parseLanguagePreference(BytesUtils.fromString("656E")))
				.isEqualTo(Collections.singletonList("en"));
		// Unused entries are padded with hexadecimal zeroes
		Assertions.assertThat(EmvDataUtils.parseLanguagePreference(BytesUtils.fromString("667200000000")))
				.isEqualTo(Collections.singletonList("fr"));
		Assertions.assertThat(EmvDataUtils.parseLanguagePreference(null)).isEmpty();
	}

}
