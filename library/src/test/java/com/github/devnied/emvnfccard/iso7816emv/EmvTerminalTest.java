package com.github.devnied.emvnfccard.iso7816emv;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;

import fr.devnied.bitlib.BytesUtils;

public class EmvTerminalTest {

	private ITerminal terminal = new DefaultTerminalImpl();

	@Test
	public void testTerminalCapability() {
		// EMV 4.3 Book 4 Annex A3: byte 1 declares the transaction types the
		// terminal supports, it has to cover the purchase the terminal sends
		// in the tag '9C'
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 1))).isEqualTo(
				new byte[] { 0x60 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 2))).isEqualTo(
				new byte[] { 0x60, 0x00 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 5))).isEqualTo(
				BytesUtils.fromString("6000B0B1FF"));
	}

	/**
	 * A terminal declaring the signature has to declare that it can print or
	 * capture it (EMV 4.3 Book 4 Annex A2, note of the CVM Capability table).
	 */
	@Test
	public void testSignatureImpliesPrintingCapability() {
		byte[] capabilities = terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 3));
		byte[] additional = terminal.constructValue(new TagAndLength(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, 5));
		boolean signature = (capabilities[1] & 0x20) != 0;
		boolean printOrElectronicAttendant = (additional[3] & 0x80) != 0;
		Assertions.assertThat(signature).isTrue();
		Assertions.assertThat(printOrElectronicAttendant).isTrue();
	}

	/**
	 * The Terminal Transaction Qualifiers must not announce the contactless
	 * VSDC of the legacy VCPS (byte 1 bit 7, reserved for future use) together
	 * with the qVSDC support of the bit 6.
	 */
	@Test
	public void testTerminalTransactionQualifiers() {
		byte[] ttq = terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS, 4));
		Assertions.assertThat(ttq).hasSize(4);
		Assertions.assertThat(ttq[0] & 0x40).isEqualTo(0);
		// qVSDC (EMV contactless mode) is the one the parser relies on
		Assertions.assertThat(ttq[0] & 0x20).isNotEqualTo(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(ttq)).isEqualTo("B620C000");
	}

	@Test
	public void testAmountAuthorized() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 1))).isEqualTo(
				new byte[] { (byte) 0x01 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.AMOUNT_AUTHORISED_NUMERIC, 2))).isEqualTo(
				new byte[] { 0x00, 0x01 });
	}

	@Test
	public void testTerminalCountryCode() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2))).isEqualTo(
				new byte[] { 0x02, 0x50 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 4))).isEqualTo(
				new byte[] { 0x00, 0x00, 0x02, 0x50 });
	}

	@Test
	public void testTerminalType() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TYPE, 1))).isEqualTo(new byte[] { 0x22 });
	}

	@Test
	public void testTerminalCapabilities() {
		// Byte 2 declares the enciphered PIN for online verification: the
		// Terminal Transaction Qualifiers built by the same terminal announce
		// online PIN, and a card comparing the two (EMV Contactless Book C-2
		// reads both) must not see them disagree
		// Byte 3 declares SDA and DDA: a terminal that is not online only
		// "shall be capable of performing SDA, DDA" (EMV 4.3 Book 4 section
		// 6.3.2), and the terminal type sent here is '22' (offline with
		// online capability)
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 3))).isEqualTo(
				new byte[] { (byte) 0xE0, (byte) 0xE0, (byte) 0xC0 });
		// The Terminal Capabilities are a format 'b' data element, so EMV 4.3
		// Book 3 section 5.4 asks for a value left justified and padded with
		// hexadecimal zeroes on the right, and truncated on the right
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 2))).isEqualTo(
				new byte[] { (byte) 0xE0, (byte) 0xE0 });
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 4))).isEqualTo(
				new byte[] { (byte) 0xE0, (byte) 0xE0, (byte) 0xC0, 0 });
	}

	/**
	 * The Transaction Currency Exponent (tag '5F36') is the ISO 4217 minor
	 * unit of the transaction currency, it is not 2 for every currency.
	 */
	@Test
	public void testTransactionCurrencyExponent() {
		Assertions.assertThat(new DefaultTerminalImpl(CountryCodeEnum.FR).getCurrencyExponent()).isEqualTo(2);
		// The yen has no decimal
		DefaultTerminalImpl japanese = new DefaultTerminalImpl(CountryCodeEnum.JP);
		Assertions.assertThat(japanese.getCurrency()).isEqualTo(CurrencyEnum.JPY);
		Assertions.assertThat(japanese.getCurrencyExponent()).isEqualTo(0);
		Assertions.assertThat(japanese.constructValue(new TagAndLength(EmvTags.TRANSACTION_CURRENCY_EXP, 1))).isEqualTo(
				BytesUtils.fromString("00"));
		// The Jordanian dinar has 3
		DefaultTerminalImpl jordanian = new DefaultTerminalImpl(CountryCodeEnum.JO);
		Assertions.assertThat(jordanian.getCurrency()).isEqualTo(CurrencyEnum.JOD);
		Assertions.assertThat(jordanian.constructValue(new TagAndLength(EmvTags.TRANSACTION_CURRENCY_EXP, 1))).isEqualTo(
				BytesUtils.fromString("03"));
	}

	@Test
	public void testDolPaddingRules() {
		// A format 'n' data element is right justified and padded with
		// hexadecimal zeroes on the left, its leftmost digits are truncated
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.MERCHANT_CATEGORY_CODE, 3))).isEqualTo(
				BytesUtils.fromString("005999"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.MERCHANT_CATEGORY_CODE, 1))).isEqualTo(
				BytesUtils.fromString("99"));
		// Any other format is left justified and padded on the right
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 10))).isEqualTo(
				BytesUtils.fromString("7A45123EE59C7F400000"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 2))).isEqualTo(
				BytesUtils.fromString("7A45"));
		// A DOL asking for nothing is answered with nothing
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_CAPABILITIES, 0))).isEmpty();
	}

	@Test
	public void test_DS_REQUESTED_OPERATOR_ID() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 2))).isEqualTo(
				BytesUtils.fromString("7A45"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.DS_REQUESTED_OPERATOR_ID, 8))).isEqualTo(
				BytesUtils.fromString("7A45123EE59C7F40"));
	}

	@Test
	public void test_MERCHANT_TYPE_INDICATOR() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.MERCHANT_TYPE_INDICATOR, 1))).isEqualTo(
				BytesUtils.fromString("01"));
	}

	@Test
	public void test_TERMINAL_TRANSACTION_INFORMATION() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_INFORMATION, 3))).isEqualTo(
				BytesUtils.fromString("C08000"));
	}

	@Test
	public void test_TERMINAL_TRANSACTION_TYPE() {
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_TRANSACTION_TYPE, 1))).isEqualTo(
				BytesUtils.fromString("00"));
	}

	@Test
	public void testTransactionCurrencyCode() {
		// The transaction currency follows the terminal country: France uses
		// the euro (ISO 4217 numeric code 978)
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TRANSACTION_CURRENCY_CODE, 2))).isEqualTo(
				BytesUtils.fromString("0978"));
	}

	@Test
	public void testDomesticTerminal() {
		// A card of a domestic scheme often refuses a foreign terminal, the
		// terminal country and the transaction currency have to match
		DefaultTerminalImpl canadian = new DefaultTerminalImpl(CountryCodeEnum.CA);
		Assertions.assertThat(canadian.getCountryCode()).isEqualTo(CountryCodeEnum.CA);
		Assertions.assertThat(canadian.getCurrency()).isEqualTo(CurrencyEnum.CAD);
		// Canada is 124 in ISO 3166 and the Canadian dollar is 124 in ISO 4217
		Assertions.assertThat(canadian.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2))).isEqualTo(
				BytesUtils.fromString("0124"));
		Assertions.assertThat(canadian.constructValue(new TagAndLength(EmvTags.TRANSACTION_CURRENCY_CODE, 2))).isEqualTo(
				BytesUtils.fromString("0124"));

		DefaultTerminalImpl chinese = new DefaultTerminalImpl(CountryCodeEnum.CN);
		// China is 156 in ISO 3166 and the renminbi is 156 in ISO 4217
		Assertions.assertThat(chinese.constructValue(new TagAndLength(EmvTags.TERMINAL_COUNTRY_CODE, 2))).isEqualTo(
				BytesUtils.fromString("0156"));
		Assertions.assertThat(chinese.constructValue(new TagAndLength(EmvTags.TRANSACTION_CURRENCY_CODE, 2))).isEqualTo(
				BytesUtils.fromString("0156"));

		// A null country keeps the default one
		Assertions.assertThat(new DefaultTerminalImpl(null).getCountryCode()).isEqualTo(CountryCodeEnum.FR);
	}

	@Test
	public void testAdditionalDolTags() {
		// Tags a picky card may ask for in its PDOL/CDOL and that used to be
		// answered with zeroes
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.APP_VERSION_NUMBER_TERMINAL, 2))).isEqualTo(
				BytesUtils.fromString("008C"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.POINT_OF_SERVICE_ENTRY_MODE, 1))).isEqualTo(
				BytesUtils.fromString("07"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.MERCHANT_CATEGORY_CODE, 2))).isEqualTo(
				BytesUtils.fromString("5999"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TRANSACTION_CURRENCY_EXP, 1))).isEqualTo(
				BytesUtils.fromString("02"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TRANSACTION_SEQUENCE_COUNTER, 2))).isEqualTo(
				BytesUtils.fromString("0001"));
		Assertions.assertThat(terminal.constructValue(new TagAndLength(EmvTags.TERMINAL_IDENTIFICATION, 8))).isEqualTo(
				BytesUtils.fromString("3030303030303030"));
		// The transaction time is built from the current time, only its length
		// and its BCD encoding can be asserted
		byte[] time = terminal.constructValue(new TagAndLength(EmvTags.TRANSACTION_TIME, 3));
		Assertions.assertThat(time).hasSize(3);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(time)).matches("[0-9]{6}");
	}
}
