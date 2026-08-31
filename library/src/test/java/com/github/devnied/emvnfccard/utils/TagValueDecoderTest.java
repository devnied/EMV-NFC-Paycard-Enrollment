package com.github.devnied.emvnfccard.utils;

import java.util.List;
import java.util.Random;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.model.Afl;

import fr.devnied.bitlib.BytesUtils;

/**
 * Every value used here is either taken from a capture of
 * library/src/test/resources/data (the file is named in the javadoc of the
 * test) or is an encoding given by the specification the test cites. No card
 * data is made up.
 */
public class TagValueDecoderTest {

	/**
	 * Method used to decode a value the way the pretty printer does
	 *
	 * @param pTag
	 *            tag of the data object
	 * @param pValue
	 *            raw value, as an hexadecimal string
	 * @return the decoded lines
	 */
	private static List<String> decode(final ITag pTag, final String pValue) {
		return TagValueDecoder.decode(pTag, BytesUtils.fromString(pValue));
	}

	/**
	 * data/MasterCardPpse, response to the selection of the AID
	 */
	@Test
	public void testDecodeAid() {
		List<String> lines = decode(EmvTags.AID_CARD, "A0 00 00 00 42 10 10");

		Assertions.assertThat(lines).hasSize(1);
		Assertions.assertThat(lines.get(0)).isEqualTo("RID A0 00 00 00 42, PIX 10 10, CB");
	}

	/**
	 * data/UnionPayPseDirectory, an AID of a scheme the library knows by its
	 * full identifier
	 */
	@Test
	public void testDecodeAidOfUnknownScheme() {
		List<String> lines = decode(EmvTags.AID_CARD, "A0 00 00 03 33 01 01 02");

		Assertions.assertThat(lines).hasSize(1);
		Assertions.assertThat(lines.get(0)).startsWith("RID A0 00 00 03 33, PIX 01 01 02");
	}

	/**
	 * data/VisaCardPpse, the name of the payment system directory the reader
	 * selects first
	 */
	@Test
	public void testDecodeDedicatedFileName() {
		Assertions.assertThat(decode(EmvTags.DEDICATED_FILE_NAME, "32 50 41 59 2E 53 59 53 2E 44 44 46 30 31")).containsExactly(
				"name 2PAY.SYS.DDF01");
	}

	/**
	 * data/MasterCardPpse, response to the GET PROCESSING OPTIONS
	 */
	@Test
	public void testDecodeApplicationFileLocator() {
		List<String> lines = decode(EmvTags.APPLICATION_FILE_LOCATOR, "10 01 01 01 18 01 01 00 20 01 02 00");

		Assertions.assertThat(lines).containsExactly(//
				"SFI 2, record 1, 1 record(s) used for offline data authentication", //
				"SFI 3, record 1", //
				"SFI 4, records 1 to 2");
	}

	/**
	 * data/VisaCardPpse3, an entry covering several records
	 */
	@Test
	public void testDecodeApplicationFileLocatorOfSeveralRecords() {
		Assertions.assertThat(decode(EmvTags.APPLICATION_FILE_LOCATOR, "10 02 04 00")).containsExactly("SFI 2, records 2 to 4");
	}

	/**
	 * A locator whose length is not a multiple of 4 is read as far as it goes,
	 * and the printer says how many bytes it left out
	 */
	@Test
	public void testDecodeTruncatedApplicationFileLocator() {
		List<String> lines = decode(EmvTags.APPLICATION_FILE_LOCATOR, "10 02 04 00 08");

		Assertions.assertThat(lines).hasSize(2);
		Assertions.assertThat(lines.get(1)).isEqualTo("the locator ends with 1 byte(s) that are not an entry");
	}

	/**
	 * The entries the parser reads are the very same ones the printer shows
	 */
	@Test
	public void testParseAflEntries() {
		List<Afl> list = TagValueDecoder.parseAflEntries(BytesUtils.fromString("10 01 01 01 18 01 01 00 20 01 02 00"));

		Assertions.assertThat(list).hasSize(3);
		Assertions.assertThat(list.get(0).getSfi()).isEqualTo(2);
		Assertions.assertThat(list.get(0).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(0).getLastRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(0).getOfflineAuthenticationRecords()).isEqualTo(1);
		Assertions.assertThat(list.get(2).getSfi()).isEqualTo(4);
		Assertions.assertThat(list.get(2).getLastRecord()).isEqualTo(2);
		Assertions.assertThat(TagValueDecoder.parseAflEntries(null)).isEmpty();
	}

	/**
	 * data/MasterCardPpse, the record read through the file locator
	 */
	@Test
	public void testDecodeTrack2EquivalentData() {
		List<String> lines = decode(EmvTags.TRACK_2_EQV_DATA, "55 99 99 99 99 99 99 99 D1 50 92 FF FF FF FF FF FF FF 0F");

		Assertions.assertThat(lines).contains("PAN 5599999999999999");
		Assertions.assertThat(lines).contains("expires 2015-09");
	}

	/**
	 * data/MasterCardPpse, the Application Transaction Counter of the record
	 */
	@Test
	public void testDecodeApplicationTransactionCounter() {
		Assertions.assertThat(decode(EmvTags.APP_TRANSACTION_COUNTER, "02 8F")).containsExactly("= 655 in decimal");
	}

	/**
	 * data/InteractPpse, answer to the GET DATA of the PIN try counter. A
	 * single digit reads the same in the hexadecimal dump the printer writes
	 * just above, so nothing is added; a counter that does not is spelled out.
	 */
	@Test
	public void testDecodePinTryCounter() {
		Assertions.assertThat(decode(EmvTags.PIN_TRY_COUNTER, "03")).isEmpty();
		Assertions.assertThat(decode(EmvTags.PIN_TRY_COUNTER, "0A")).containsExactly("= 10 in decimal");
	}

	/**
	 * data/MasterCardPpse (French then English) and data/InteractPpse (English)
	 */
	@Test
	public void testDecodeLanguagePreference() {
		Assertions.assertThat(decode(EmvTags.LANGUAGE_PREFERENCE, "66 72 65 6E")).containsExactly(
				"languages fr, en (ISO 639, most preferred first)");
		Assertions.assertThat(decode(EmvTags.LANGUAGE_PREFERENCE, "65 6E")).containsExactly(
				"languages en (ISO 639, most preferred first)");
	}

	/**
	 * data/MasterCardPpse (part 1, Latin-1) and data/MasterCardPpse2 (part 9,
	 * Latin-5, the alphabet of the Turkish cards)
	 */
	@Test
	public void testDecodeIssuerCodeTableIndex() {
		Assertions.assertThat(decode(EmvTags.ISSUER_CODE_TABLE_INDEX, "01")).containsExactly(
				"part 1 of ISO/IEC 8859, read as ISO-8859-1");
		Assertions.assertThat(decode(EmvTags.ISSUER_CODE_TABLE_INDEX, "09").get(0)).startsWith("part 9 of ISO/IEC 8859");
	}

	/**
	 * data/UnionPayPseDirectory, the short file identifier of the directory
	 */
	@Test
	public void testDecodeShortFileIdentifier() {
		Assertions.assertThat(decode(EmvTags.SFI, "01")).containsExactly("SFI 1, governed by the EMV specification");
		Assertions.assertThat(decode(EmvTags.SFI, "0B")).containsExactly("SFI 11, payment system specific");
		Assertions.assertThat(decode(EmvTags.SFI, "15")).containsExactly("SFI 21, issuer specific");
		// EMV 4.4 Book 3 Annex A1: the SFI is a binary value in the range 1 to
		// 30 with the three high order bits set to zero
		Assertions.assertThat(decode(EmvTags.SFI, "1F")).containsExactly("SFI 31, outside the range 1 to 30");
		Assertions.assertThat(decode(EmvTags.SFI, "20")).containsExactly(
				"the three high order bits of a short file identifier must be zero");
	}

	/**
	 * data/MasterCardPpse holds the three priorities of a directory
	 */
	@Test
	public void testDecodeApplicationPriorityIndicator() {
		Assertions.assertThat(decode(EmvTags.APPLICATION_PRIORITY_INDICATOR, "01")).containsExactly(
				"priority 1 of 15, 1 being the highest");
		Assertions.assertThat(decode(EmvTags.APPLICATION_PRIORITY_INDICATOR, "02")).containsExactly(
				"priority 2 of 15, 1 being the highest");
		Assertions.assertThat(decode(EmvTags.APPLICATION_PRIORITY_INDICATOR, "00")).containsExactly("no priority assigned");
		// EMV 4.4 Book 1: b8 set means the application cannot be selected
		// without the confirmation of the cardholder
		Assertions.assertThat(decode(EmvTags.APPLICATION_PRIORITY_INDICATOR, "81")).containsExactly(
				"b8 -- the application cannot be selected without the confirmation of the cardholder",
				"priority 1 of 15, 1 being the highest");
	}

	/**
	 * data/VisaCardPpse, the log entry of the FCI issuer discretionary data
	 */
	@Test
	public void testDecodeLogEntry() {
		Assertions.assertThat(decode(EmvTags.LOG_ENTRY, "0B 1E")).containsExactly("SFI 11, 30 records at most");
	}

	/**
	 * ISO 4217 gives 978 to the euro and 826 to the pound sterling. A code the
	 * standard does not assign is reported as such rather than left out.
	 */
	@Test
	public void testDecodeCurrency() {
		Assertions.assertThat(decode(EmvTags.TRANSACTION_CURRENCY_CODE, "09 78")).containsExactly("978 EUR (Euro)");
		Assertions.assertThat(decode(EmvTags.APPLICATION_CURRENCY_CODE, "08 26")).containsExactly(
				"826 GBP (Pound sterling)");
		Assertions.assertThat(decode(EmvTags.TRANSACTION_CURRENCY_CODE, "09 99")).containsExactly(
				"999, no ISO 4217 currency with this code");
	}

	/**
	 * ISO 3166-1 gives 250 to France and 826 to the United Kingdom
	 */
	@Test
	public void testDecodeCountry() {
		Assertions.assertThat(decode(EmvTags.ISSUER_COUNTRY_CODE, "02 50")).containsExactly("250 FR (France)");
		Assertions.assertThat(decode(EmvTags.TERMINAL_COUNTRY_CODE, "08 26")).containsExactly(
				"826 GB (United Kingdom)");
		Assertions.assertThat(decode(EmvTags.ISSUER_COUNTRY_CODE_ALPHA2, "46 52")).containsExactly("FR (France)");
		Assertions.assertThat(decode(EmvTags.ISSUER_COUNTRY_CODE_ALPHA3, "46 52 41")).containsExactly("FR (France)");
	}

	/**
	 * The currency exponent tells where the decimal point of the amounts is
	 */
	@Test
	public void testDecodeCurrencyExponent() {
		Assertions.assertThat(decode(EmvTags.APP_CURRENCY_EXPONENT, "02")).containsExactly("amounts have 2 decimal digits");
		Assertions.assertThat(decode(EmvTags.TRANSACTION_CURRENCY_EXP, "00")).containsExactly("amounts have 0 decimal digits");
	}

	/**
	 * EMV 4.3 Book 3 Annex A: an amount is 12 digits of packed BCD expressed in
	 * the minor unit of the currency, its binary form 4 bytes
	 */
	@Test
	public void testDecodeAmount() {
		Assertions.assertThat(decode(EmvTags.AMOUNT_AUTHORISED_NUMERIC, "00 00 00 00 10 00")).containsExactly(
				"amount 1000 in the minor unit of the currency");
		Assertions.assertThat(decode(EmvTags.AMOUNT_OTHER_NUMERIC, "00 00 00 00 00 00")).containsExactly(
				"amount 0 in the minor unit of the currency");
		Assertions.assertThat(decode(EmvTags.AMOUNT_AUTHORISED_BINARY, "00 00 03 E8")).containsExactly(
				"amount 1000 in the minor unit of the currency");
	}

	/**
	 * EMV 4.3 Book 3 Annex A codes a date as YYMMDD and a time as HHMMSS, both
	 * packed BCD
	 */
	@Test
	public void testDecodeDateAndTime() {
		Assertions.assertThat(decode(EmvTags.APP_EXPIRATION_DATE, "25 12 31")).containsExactly("2025-12-31");
		Assertions.assertThat(decode(EmvTags.TRANSACTION_DATE, "24 03 01")).containsExactly("2024-03-01");
		Assertions.assertThat(decode(EmvTags.TRANSACTION_TIME, "14 30 05")).containsExactly("14:30:05");
		// A card padding the day with '00' keeps the month readable
		Assertions.assertThat(decode(EmvTags.APP_EXPIRATION_DATE, "25 12 00")).containsExactly("2025-12-31");
	}

	/**
	 * A value that is not a date, or a time out of range, produces nothing
	 * rather than a wrong reading
	 */
	@Test
	public void testDecodeUnreadableDateAndTime() {
		Assertions.assertThat(decode(EmvTags.APP_EXPIRATION_DATE, "25 13 31")).isEmpty();
		Assertions.assertThat(decode(EmvTags.TRANSACTION_TIME, "25 00 00")).isEmpty();
		Assertions.assertThat(decode(EmvTags.TRANSACTION_TIME, "1A 30 05")).isEmpty();
	}

	/**
	 * data/VisaCardPpse3, the PAN sequence number of the record
	 */
	@Test
	public void testDecodePanSequenceNumber() {
		Assertions.assertThat(decode(EmvTags.PAN_SEQUENCE_NUMBER, "00")).containsExactly("PAN sequence number 00");
	}

	/**
	 * data/MasterCardPpse, answer to the GET DATA of the tag '9F7F'. The 42
	 * bytes are the card production life cycle of the GlobalPlatform Card
	 * Specification, not the 4 bytes of a DS Unpredictable Number.
	 */
	@Test
	public void testDecodeCardProductionLifeCycle() {
		List<String> lines = decode(EmvTags.DS_UNPREDICTABLE_NUMBER, "47 90 50 37 16 71 92 59 48 04 10 46 95 00 07 54 "
				+ "71 49 47 92 02 17 16 73 92 64 16 74 12 91 00 00 00 74 00 00 00 00 00 00 00 00");

		Assertions.assertThat(lines).contains("IC fabricator '4790'");
		Assertions.assertThat(lines).contains("IC type '5037'");
		Assertions.assertThat(lines).contains("IC serial number '95000754'");
	}

	/**
	 * The same tag carrying the 4 bytes of a DS Unpredictable Number is left
	 * alone: a life cycle is exactly 42 bytes long
	 */
	@Test
	public void testDecodeDsUnpredictableNumberIsNotReadAsALifeCycle() {
		Assertions.assertThat(decode(EmvTags.DS_UNPREDICTABLE_NUMBER, "01 02 03 04")).isEmpty();
	}

	/**
	 * ISO/IEC 7816-4 codes the number of bytes still available in SW2 of a
	 * '61XX', the exact length in SW2 of a '6CXX' and the number of tries left
	 * in the low nibble of a '63CX'. The status word table can only write them
	 * as a placeholder.
	 */
	@Test
	public void testDecodeStatusWord() {
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("610A"))).containsExactly(
				"10 bytes are still available");
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("6100"))).containsExactly(
				"256 bytes are still available");
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("6C20"))).containsExactly(
				"the command has to be sent again with Le = 32");
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("63C1"))).containsExactly("1 try left");
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("63C2"))).containsExactly("2 tries left");
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("9000"))).isEmpty();
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(null)).isEmpty();
		Assertions.assertThat(TagValueDecoder.decodeStatusWord(BytesUtils.fromString("90"))).isEmpty();
	}

	/**
	 * A data object this class does not know, an empty value and a null are all
	 * answered the same way: nothing is added under the hexadecimal dump
	 */
	@Test
	public void testDecodeOfUnknownTagAndEmptyValue() {
		Assertions.assertThat(TagValueDecoder.decode(EmvTags.getNotNull(BytesUtils.fromString("FFFF")), new byte[] { 1 }))
				.isEmpty();
		Assertions.assertThat(TagValueDecoder.decode(EmvTags.APP_TRANSACTION_COUNTER, new byte[0])).isEmpty();
		Assertions.assertThat(TagValueDecoder.decode(EmvTags.APP_TRANSACTION_COUNTER, null)).isEmpty();
		Assertions.assertThat(TagValueDecoder.decode(null, new byte[] { 1 })).isEmpty();
	}

	/**
	 * data/MasterCardPpse, the Application Interchange Profile of the GET
	 * PROCESSING OPTIONS. Byte 2 b8 is reserved for the contactless kernels by
	 * EMV 4.4 Book 3 Annex C1 and the Kernel 2 and the Kernel 3 read it the
	 * other way round, so the line has to name both.
	 */
	@Test
	public void testDecodeApplicationInterchangeProfile() {
		List<String> lines = decode(EmvTags.APPLICATION_INTERCHANGE_PROFILE, "19 80");

		Assertions.assertThat(lines).containsExactly(//
				"byte 1 b5 -- cardholder verification is supported", //
				"byte 1 b4 -- terminal risk management is to be performed", //
				"byte 1 b1 -- CDA supported", //
				"byte 2 b8 -- [K2] EMV mode supported, [K3] mag-stripe mode supported, [K4] EMV and mag-stripe modes");
	}

	/**
	 * data/VisaCardPpse3, the Application Interchange Profile of a Visa card
	 */
	@Test
	public void testDecodeApplicationInterchangeProfileOfVisaCard() {
		Assertions.assertThat(decode(EmvTags.APPLICATION_INTERCHANGE_PROFILE, "20 00")).containsExactly(
				"byte 1 b6 -- DDA supported");
	}

	/**
	 * A bitmap where nothing is set says so, rather than saying nothing, which
	 * would be indistinguishable from a data object that is not decoded
	 */
	@Test
	public void testDecodeBitmapWithNoBitSet() {
		Assertions.assertThat(decode(EmvTags.TERMINAL_VERIFICATION_RESULTS, "00 00 00 00 00")).containsExactly(
				"all bits clear");
	}

	/**
	 * A bit the specification reserves is reported as an anomaly when it is
	 * set: only the bits that are set are printed, so the line can only ever
	 * appear on a card that does not follow the specification
	 */
	@Test
	public void testDecodeReservedBitThatIsSet() {
		Assertions.assertThat(decode(EmvTags.TRANSACTION_STATUS_INFORMATION, "00 01")).containsExactly(
				"byte 2 b1 -- RFU bit set, the specification requires it to be 0");
	}

	/**
	 * A bitmap shorter than the data object is defined to be is not decoded at
	 * all: half of a Terminal Verification Result says nothing about the bytes
	 * the terminal did not send. A longer one is decoded and the extra bytes
	 * are reported.
	 */
	@Test
	public void testDecodeBitmapOfTheWrongLength() {
		Assertions.assertThat(decode(EmvTags.TERMINAL_VERIFICATION_RESULTS, "00 00 00")).isEmpty();
		Assertions.assertThat(decode(EmvTags.APPLICATION_INTERCHANGE_PROFILE, "19")).isEmpty();
		Assertions.assertThat(decode(EmvTags.APPLICATION_INTERCHANGE_PROFILE, "19 80 00")).contains(
				"the value is 3 bytes, this data object is defined as 2");
	}

	/**
	 * EMV 4.4 Book 3 Annex C6: the Issuer Action Codes have the very same
	 * layout as the Terminal Verification Results
	 */
	@Test
	public void testDecodeTerminalVerificationResults() {
		Assertions.assertThat(decode(EmvTags.TERMINAL_VERIFICATION_RESULTS, "80 00 00 00 00")).containsExactly(
				"byte 1 b8 -- offline data authentication was not performed");
		Assertions.assertThat(decode(EmvTags.ISSUER_ACTION_CODE_DENIAL, "00 00 00 80 00")).containsExactly(
				"byte 4 b8 -- transaction exceeds the floor limit");
	}

	/**
	 * The two low order bits of the last byte of the Terminal Verification
	 * Results are a field of the Kernel 2, so the outcome of the relay
	 * resistance protocol is named on top of the bits themselves
	 */
	@Test
	public void testDecodeRelayResistanceOfTheTerminalVerificationResults() {
		List<String> lines = decode(EmvTags.TERMINAL_VERIFICATION_RESULTS, "00 00 00 00 02");

		Assertions.assertThat(lines).contains("[K2] byte 5 b2-b1 = 2 -- relay resistance protocol performed");
	}

	/**
	 * EMV 4.4 Book 3 Annex C2, every geographic and service restriction set
	 */
	@Test
	public void testDecodeApplicationUsageControl() {
		List<String> lines = decode(EmvTags.APP_USAGE_CONTROL, "FF 00");

		Assertions.assertThat(lines).hasSize(8);
		Assertions.assertThat(lines.get(0)).isEqualTo("byte 1 b8 -- valid for domestic cash transactions");
		Assertions.assertThat(lines.get(7)).isEqualTo("byte 1 b1 -- valid at terminals other than ATMs");
	}

	/**
	 * data/VisaCardPpse3, the Cryptogram Information Data of the record.
	 * EMV 4.4 Book 3 table 15 codes the type of cryptogram in b8-b7, so '40' is
	 * a transaction certificate and not an authorisation request.
	 */
	@Test
	public void testDecodeCryptogramInformationData() {
		Assertions.assertThat(decode(EmvTags.CRYPTOGRAM_INFORMATION_DATA, "40")).containsExactly(//
				"b8-b7 -- TC, transaction certificate, the transaction is approved offline", //
				"b3-b1 -- no information given");
		Assertions.assertThat(decode(EmvTags.CRYPTOGRAM_INFORMATION_DATA, "80").get(0)).startsWith(
				"b8-b7 -- ARQC, authorisation request cryptogram");
		Assertions.assertThat(decode(EmvTags.CRYPTOGRAM_INFORMATION_DATA, "00").get(0)).startsWith(
				"b8-b7 -- AAC, application authentication cryptogram");
		Assertions.assertThat(decode(EmvTags.CRYPTOGRAM_INFORMATION_DATA, "12")).contains("b4 -- advice required",
				"b3-b1 -- PIN try limit exceeded");
	}

	/**
	 * The CVM list of EMV 4.4 Book 3 Annex C3: amounts X and Y then the rules,
	 * '42' being the enciphered PIN verified online with the succeeding rule
	 * applied if it is unsuccessful, and '4201' its pairing with the unattended
	 * cash condition
	 */
	@Test
	public void testDecodeCvmList() {
		List<String> lines = decode(EmvTags.CVM_LIST, "00 00 00 00 00 00 00 00 42 01 1E 03 1F 00");

		Assertions.assertThat(lines).containsExactly(//
				"amount X 0, amount Y 0", //
				"rule 1: enciphered PIN verified online, if unattended cash, "
						+ "apply the next rule if this one is unsuccessful", //
				"rule 2: signature, if the terminal supports the CVM, "
						+ "fail the cardholder verification if this one is unsuccessful", //
				"rule 3: no CVM required, always, fail the cardholder verification if this one is unsuccessful");
	}

	/**
	 * EMV 4.4 Book 3 Annex C3 table 43 added the biometric methods, which the
	 * typed CvmMethodEnum of the library does not carry, and reserves the
	 * ranges '20' to '2F' and '30' to '3E'
	 */
	@Test
	public void testDecodeCvmListOfMethodsOutsideTheTypedEnum() {
		List<String> lines = decode(EmvTags.CVM_LIST, "00 00 00 00 00 00 00 00 08 00 25 00 35 00");

		Assertions.assertThat(lines.get(1)).startsWith("rule 1: finger biometric verified offline by the card");
		Assertions.assertThat(lines.get(2)).startsWith("rule 2: reserved for use by the individual payment systems");
		Assertions.assertThat(lines.get(3)).startsWith("rule 3: reserved for use by the issuer");
	}

	/**
	 * EMV 4.4 Book 3 Annex C3: the CVM Results carry the rule that was applied
	 * and its outcome
	 */
	@Test
	public void testDecodeCvmResults() {
		Assertions.assertThat(decode(EmvTags.CVM_RESULTS, "1E 03 02")).containsExactly(//
				"method: signature", //
				"condition: if the terminal supports the CVM", //
				"result: successful");
		Assertions.assertThat(decode(EmvTags.CVM_RESULTS, "3F 00 01")).containsExactly(//
				"method: no CVM performed", //
				"condition: always", //
				"result: failed");
		// The Kernel 2 gives the code '01' to the verification a consumer
		// device performs on its own, where the Book 3 gives it to a plaintext
		// PIN, so the line has to carry both
		Assertions.assertThat(decode(EmvTags.CVM_RESULTS, "01 00 02").get(0)).isEqualTo(
				"method: [Book 3] plaintext PIN verified by the card, [K2] on device CVM performed");
		// The very same code inside a CVM list is the plaintext PIN alone
		Assertions.assertThat(decode(EmvTags.CVM_LIST, "00 00 00 00 00 00 00 00 01 00").get(1)).startsWith(
				"rule 1: plaintext PIN verification performed by the card");
	}

	/**
	 * EMV 4.4 Book 4 Annex A2 and A3
	 */
	@Test
	public void testDecodeTerminalCapabilities() {
		Assertions.assertThat(decode(EmvTags.TERMINAL_CAPABILITIES, "E0 00 00")).containsExactly(//
				"byte 1 b8 -- manual key entry", //
				"byte 1 b7 -- magnetic stripe", //
				"byte 1 b6 -- IC with contacts");
		Assertions.assertThat(decode(EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES, "00 00 00 00 01")).containsExactly(
				"byte 5 b1 -- code table 1 (ISO/IEC 8859-1)");
	}

	/**
	 * EMV 4.4 Book 4 table 24
	 */
	@Test
	public void testDecodeTerminalType() {
		Assertions.assertThat(decode(EmvTags.TERMINAL_TYPE, "22")).containsExactly(
				"terminal type 22, attended, merchant controlled, offline with online capability");
		Assertions.assertThat(decode(EmvTags.TERMINAL_TYPE, "99")).containsExactly("terminal type 99, not defined by EMV");
	}

	/**
	 * The values of the tag '9C' are the first two digits of the ISO 8583
	 * processing code, EMV leaving them to the payment systems
	 */
	@Test
	public void testDecodeTransactionType() {
		Assertions.assertThat(decode(EmvTags.TRANSACTION_TYPE, "00")).containsExactly(
				"transaction type 00, purchase of goods or services");
		Assertions.assertThat(decode(EmvTags.TRANSACTION_TYPE, "01")).containsExactly("transaction type 01, cash withdrawal");
		Assertions.assertThat(decode(EmvTags.TRANSACTION_TYPE, "77")).containsExactly(
				"transaction type 77, defined by the payment system, not by EMV");
	}

	/**
	 * EMV 4.4 Book 4 Annex A4 defines the offline codes alone
	 */
	@Test
	public void testDecodeAuthorisationResponseCode() {
		Assertions.assertThat(decode(EmvTags.AUTHORISATION_RESPONSE_CODE, "59 31")).containsExactly(
				"Y1, offline approved");
		Assertions.assertThat(decode(EmvTags.AUTHORISATION_RESPONSE_CODE, "30 30")).containsExactly(
				"00, response of the payment system, not defined by EMV");
	}

	/**
	 * The value is not taken from a capture: no card of
	 * library/src/test/resources/data answers a 4 bytes '9F66'. It is the
	 * encoding of the bits EMV Contactless Book A gives to byte 1 (b6 EMV mode,
	 * b5 EMV contact chip, b3 online PIN, b2 signature) and Book C-3 to byte 3
	 * (b7 consumer device CVM).
	 */
	@Test
	public void testDecodeTerminalTransactionQualifiers() {
		List<String> lines = decode(EmvTags.TERMINAL_TRANSACTION_QUALIFIERS, "36 00 40 00");

		Assertions.assertThat(lines).containsExactly(//
				"byte 1 b6 -- EMV mode supported", //
				"byte 1 b5 -- EMV contact chip supported", //
				"byte 1 b3 -- online PIN supported", //
				"byte 1 b2 -- signature supported", //
				"byte 3 b7 -- consumer device CVM supported");
	}

	/**
	 * data/MasterCardPpse, the tag '9F6C' of the record. The Kernel 3 reads it
	 * as the Card Transaction Qualifiers and the Kernel 7 reads four of its
	 * bits differently, so the ambiguous ones name both kernels.
	 */
	@Test
	public void testDecodeCardTransactionQualifiers() {
		Assertions.assertThat(decode(EmvTags.MAG_STRIPE_APP_VERSION_NUMBER_CARD, "10 00")).containsExactly(
				"[K2] mag-stripe application version number 1000",
				"byte 1 b5 -- [K3] switch interface if offline data authentication fails, "
						+ "[K7] terminate the transaction");
		// data/MasterCardPpse2 and data/InteractPpse, where the record around
		// the tag is a Kernel 2 mag-stripe one: the reserved bits of the
		// Kernel 3 reading must not be reported as an anomaly of the card
		Assertions.assertThat(decode(EmvTags.MAG_STRIPE_APP_VERSION_NUMBER_CARD, "00 01")).containsExactly(
				"[K2] mag-stripe application version number 0001");
	}

	/**
	 * EMV Contactless Book B: the first byte of the kernel identifier holds the
	 * kind of kernel and the short kernel identifier
	 */
	@Test
	public void testDecodeKernelIdentifier() {
		Assertions.assertThat(decode(EmvTags.KERNEL_IDENTIFIER, "02")).contains("short kernel identifier 2 (MasterCard)");
		Assertions.assertThat(decode(EmvTags.KERNEL_IDENTIFIER, "03")).contains("short kernel identifier 3 (Visa)");
		Assertions.assertThat(decode(EmvTags.KERNEL_IDENTIFIER, "00")).contains(
				"no kernel preference, the one of the application identifier is used");
	}

	/**
	 * The tag '9F5D' is 3 bytes of capabilities for the Kernel 2 and a 6 bytes
	 * amount otherwise, so the length tells them apart
	 */
	@Test
	public void testDecodeApplicationCapabilitiesInformation() {
		// The bits of the second byte must be reported as the second byte: the
		// reader cross-references them with the hexadecimal printed above
		Assertions.assertThat(decode(EmvTags.APPLICATION_CAPABILITIES_INFORMATION, "01 04 00")).contains(
				"[K2] application capabilities information version 0", "[K2] data storage version 1",
				"byte 2 b3 -- [K2] support for field off detection");
		Assertions.assertThat(decode(EmvTags.APPLICATION_CAPABILITIES_INFORMATION, "01 00 00")).excludes(
				"all bits clear");
		Assertions.assertThat(decode(EmvTags.APPLICATION_CAPABILITIES_INFORMATION, "00 00 00 00 10 00")).contains(
				"amount 1000 in the minor unit of the currency",
				"read as the Available Offline Spending Amount, which is 6 bytes long");
	}

	/**
	 * data/VisaCardPpse, where the proprietary 'DF60' carries the very same two
	 * bytes as the '9F4D' of the same response
	 */
	@Test
	public void testDecodeVisaLogEntry() {
		Assertions.assertThat(decode(EmvTags.VISA_LOG_ENTRY, "0B 1E")).containsExactly("SFI 11, 30 records at most");
	}

	/**
	 * A value shorter or longer than the data object is defined to be must not
	 * make the printer fail: the raw hexadecimal is already written and the
	 * decoded lines are all that can be lost.
	 */
	@Test
	public void testDecodeNeverThrowsOnRandomInput() {
		Random random = new Random(20260830L);
		for (ITag tag : tags()) {
			for (int length = 0; length <= 48; length++) {
				byte[] value = new byte[length];
				random.nextBytes(value);
				try {
					Assertions.assertThat(TagValueDecoder.decode(tag, value)).isNotNull();
				} catch (RuntimeException e) {
					Assert.fail("Decoding the tag " + BytesUtils.bytesToStringNoSpace(tag.getTagBytes()) + " of " + length
							+ " bytes threw " + e);
				}
			}
		}
	}

	/**
	 * @return every tag the decoder knows about
	 */
	private static ITag[] tags() {
		return new ITag[] { EmvTags.AID_CARD, EmvTags.DEDICATED_FILE_NAME, EmvTags.PAN, EmvTags.PAN_SEQUENCE_NUMBER,
				EmvTags.APP_EXPIRATION_DATE, EmvTags.TRANSACTION_TIME, EmvTags.AMOUNT_AUTHORISED_NUMERIC,
				EmvTags.AMOUNT_AUTHORISED_BINARY, EmvTags.TRANSACTION_CURRENCY_CODE, EmvTags.APP_CURRENCY_EXPONENT,
				EmvTags.ISSUER_COUNTRY_CODE, EmvTags.ISSUER_COUNTRY_CODE_ALPHA2, EmvTags.ISSUER_COUNTRY_CODE_ALPHA3,
				EmvTags.SERVICE_CODE, EmvTags.TRACK1_DATA, EmvTags.TRACK_2_EQV_DATA, EmvTags.LANGUAGE_PREFERENCE,
				EmvTags.ISSUER_CODE_TABLE_INDEX, EmvTags.APP_TRANSACTION_COUNTER, EmvTags.APPLICATION_FILE_LOCATOR,
				EmvTags.SFI, EmvTags.APPLICATION_PRIORITY_INDICATOR, EmvTags.CVM_LIST, EmvTags.LOG_ENTRY,
				EmvTags.APP_VERSION_NUMBER_CARD, EmvTags.DS_UNPREDICTABLE_NUMBER, EmvTags.APPLICATION_INTERCHANGE_PROFILE,
				EmvTags.APP_USAGE_CONTROL, EmvTags.TRANSACTION_STATUS_INFORMATION, EmvTags.TERMINAL_VERIFICATION_RESULTS,
				EmvTags.TERMINAL_CAPABILITIES, EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES,
				EmvTags.TERMINAL_TRANSACTION_QUALIFIERS, EmvTags.MAG_STRIPE_APP_VERSION_NUMBER_CARD,
				EmvTags.CRYPTOGRAM_INFORMATION_DATA, EmvTags.CVM_RESULTS, EmvTags.TERMINAL_TYPE,
				EmvTags.POINT_OF_SERVICE_ENTRY_MODE, EmvTags.TRANSACTION_TYPE, EmvTags.AUTHORISATION_RESPONSE_CODE,
				EmvTags.KERNEL_IDENTIFIER, EmvTags.APPLICATION_CAPABILITIES_INFORMATION,
				EmvTags.VLP_ISSUER_AUTHORISATION_CODE, EmvTags.ISSUER_IDENTIFICATION_NUMBER,
				EmvTags.MERCHANT_CATEGORY_CODE, EmvTags.TERMINAL_FLOOR_LIMIT, EmvTags.VISA_LOG_ENTRY };
	}
}
