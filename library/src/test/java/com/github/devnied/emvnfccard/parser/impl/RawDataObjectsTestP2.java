package com.github.devnied.emvnfccard.parser.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Every primitive data object the card answers for an application (SELECT
 * FCI, GPO response, AFL records) is kept raw, keyed by its tag, known or
 * unknown: the typed getters are the decoded view, the raw map is the whole
 * one.
 */
public class RawDataObjectsTestP2 {

	/**
	 * File Control Information of the MasterCardPpse3 card, status word removed
	 */
	private static final String MASTERCARD_FCI = "6F 32 84 07 A0 00 00 00 04 10 10 A5 27 50 0A 4D 61 73 74 65 72 43 61 72 64 87 01 01 5F 2D 02 66 72 BF 0C 10 9F 4D 02 0B 0A 5F 56 03 43 41 4E DF 62 02 40 80";

	private EmvCard read(final String pTrace) throws CommunicationException {
		return EmvTemplate.Builder().setProvider(new TestProvider(pTrace))
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadCplc(true)).build().readEmvCard();
	}

	private static String hex(final byte[] pValue) {
		return pValue == null ? null : BytesUtils.bytesToStringNoSpace(pValue);
	}

	/**
	 * The templates are walked through and never stored themselves, the first
	 * occurrence of a tag is kept, in reading order.
	 */
	@Test
	public void testTemplatesAreWalkedAndFirstOccurrenceKept() {
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(new ScriptedProvider()).build());
		Application app = new Application();

		parser.extractExtendedData(BytesUtils.fromString(MASTERCARD_FCI), app);

		Map<String, byte[]> raw = app.getRawDataObjects();
		Assertions.assertThat(new ArrayList<String>(raw.keySet())).containsExactly("84", "50", "87", "5F2D", "9F4D", "5F56",
				"DF62");
		Assertions.assertThat(hex(raw.get("84"))).isEqualTo("A0000000041010");
		Assertions.assertThat(hex(raw.get("5F56"))).isEqualTo("43414E");
		Assertions.assertThat(hex(raw.get("DF62"))).isEqualTo("4080");
		Assertions.assertThat(raw.containsKey("6F")).isFalse();
		Assertions.assertThat(raw.containsKey("A5")).isFalse();
		Assertions.assertThat(raw.containsKey("BF0C")).isFalse();

		// The GPO response adds its objects, a tag already read is not replaced
		parser.extractExtendedData(BytesUtils.fromString("77 0A 82 02 00 00 94 04 08 01 01 00"), app);
		parser.extractExtendedData(BytesUtils.fromString("77 04 82 02 20 00"), app);
		Assertions.assertThat(raw).hasSize(9);
		Assertions.assertThat(hex(raw.get("82"))).isEqualTo("0000");
		Assertions.assertThat(hex(raw.get("94"))).isEqualTo("08010100");
	}

	/**
	 * Data that is not BER-TLV, or nested beyond reason, is not collected and
	 * raises nothing.
	 */
	@Test
	public void testTolerance() {
		Map<String, byte[]> map = new LinkedHashMap<String, byte[]>();
		AbstractParser.collectRawDataObjects(null, map);
		AbstractParser.collectRawDataObjects(new byte[0], map);
		AbstractParser.collectRawDataObjects(BytesUtils.fromString("FF FF"), map);
		AbstractParser.collectRawDataObjects(BytesUtils.fromString("00"), map);
		// A record whose template announces more than it holds
		AbstractParser.collectRawDataObjects(BytesUtils.fromString("70 10 5A 02 49 99"), map);
		Assertions.assertThat(hex(map.get("5A"))).isEqualTo("4999");
		AbstractParser.collectRawDataObjects(BytesUtils.fromString("5F"), null);

		// Nested 3 deep: read. Nested beyond the bound: left out
		Assertions.assertThat(collect(nest("5A 01 01", 3))).containsExactly("5A");
		Assertions.assertThat(collect(nest("5A 01 01", AbstractParser.MAX_RAW_DATA_OBJECT_DEPTH + 4))).isEmpty();
	}

	private static java.util.List<String> collect(final String pData) {
		Map<String, byte[]> map = new LinkedHashMap<String, byte[]>();
		AbstractParser.collectRawDataObjects(BytesUtils.fromString(pData), map);
		return new ArrayList<String>(map.keySet());
	}

	/**
	 * Wrap a data object in the constructed private tag 'E1' the number of
	 * times asked
	 */
	private static String nest(final String pData, final int pLevels) {
		String data = pData;
		for (int i = 0; i < pLevels; i++) {
			byte[] value = BytesUtils.fromString(data);
			String length = value.length > 127 ? String.format("81%02X", value.length) : String.format("%02X", value.length);
			data = "E1" + length + BytesUtils.bytesToStringNoSpace(value);
		}
		return data;
	}

	/**
	 * The MasterCardPpse3 card: FCI, GPO and the single record of its AFL, the
	 * kernel 2 specific tags included.
	 */
	@Test
	public void testMasterCardPpse3() throws CommunicationException {
		Map<String, byte[]> raw = read("MasterCardPpse3").getApplications().get(0).getRawDataObjects();

		Assertions.assertThat(hex(raw.get("84"))).isEqualTo("A0000000041010");
		Assertions.assertThat(hex(raw.get("5F56"))).isEqualTo("43414E");
		Assertions.assertThat(hex(raw.get("82"))).isEqualTo("0000");
		Assertions.assertThat(hex(raw.get("94"))).isEqualTo("08010100");
		Assertions.assertThat(hex(raw.get("9F6C"))).isEqualTo("0001");
		Assertions.assertThat(hex(raw.get("9F62"))).isEqualTo("000000000700");
		Assertions.assertThat(raw.get("56")).hasSize(65);
		Assertions.assertThat(hex(raw.get("9F66"))).isEqualTo("00FE");
		Assertions.assertThat(hex(raw.get("9F6E"))).isEqualTo("02210000303000");
		Assertions.assertThat(raw.containsKey("70")).isFalse();
		Assertions.assertThat(raw.containsKey("77")).isFalse();
	}

	/**
	 * The MasterCardPpse2 card carries proprietary tags in its FCI and record.
	 */
	@Test
	public void testMasterCardPpse2() throws CommunicationException {
		Map<String, byte[]> raw = read("MasterCardPpse2").getApplications().get(0).getRawDataObjects();

		Assertions.assertThat(hex(raw.get("9F5D"))).isEqualTo("010300");
		Assertions.assertThat(hex(raw.get("9F5E"))).isEqualTo("520000000000000001");
		Assertions.assertThat(raw.get("DF64")).hasSize(32);
		Assertions.assertThat(hex(raw.get("DF65"))).isEqualTo("01");
		Assertions.assertThat(hex(raw.get("9F6C"))).isEqualTo("0001");
		// "DEBIT MASTERCARD"
		Assertions.assertThat(hex(raw.get("50"))).isEqualTo("4445424954204D415354455243415244");
		Assertions.assertThat(raw.containsKey("56")).isTrue();
		Assertions.assertThat(raw.containsKey("9F62")).isTrue();
	}

	/**
	 * The VisaCardPpse3 card answers its cryptogram and its Signed Dynamic
	 * Application Data in the GPO, and its offline authentication data in the
	 * records.
	 */
	@Test
	public void testVisaCardPpse3() throws CommunicationException {
		Map<String, byte[]> raw = read("VisaCardPpse3").getApplications().get(0).getRawDataObjects();

		Assertions.assertThat(hex(raw.get("9F27"))).isEqualTo("40");
		Assertions.assertThat(raw.get("9F4B")).hasSize(112);
		Assertions.assertThat(hex(raw.get("8F"))).isEqualTo("07");
		Assertions.assertThat(hex(raw.get("9F32"))).isEqualTo("03");
		Assertions.assertThat(hex(raw.get("9F47"))).isEqualTo("03");
		Assertions.assertThat(hex(raw.get("5F34"))).isEqualTo("00");
		Assertions.assertThat(hex(raw.get("5A"))).isEqualTo("4999999999999999");
	}

	/**
	 * The VisaCardPpse card: the GPO response of the first application holds
	 * the cryptogram, and the objects of one application never leak into the
	 * other.
	 */
	@Test
	public void testVisaCardPpse() throws CommunicationException {
		EmvCard card = read("VisaCardPpse");
		Map<String, byte[]> cb = card.getApplications().get(0).getRawDataObjects();
		Map<String, byte[]> visa = card.getApplications().get(1).getRawDataObjects();

		Assertions.assertThat(hex(cb.get("82"))).isEqualTo("2000");
		Assertions.assertThat(hex(cb.get("9F36"))).isEqualTo("028F");
		Assertions.assertThat(raw(cb, "9F26")).hasSize(8);
		Assertions.assertThat(hex(cb.get("9F10"))).isEqualTo("06011A23804004");
		Assertions.assertThat(hex(cb.get("84"))).isEqualTo("A0000000421010");
		Assertions.assertThat(hex(visa.get("84"))).isEqualTo("A0000000031010");
		Assertions.assertThat(hex(visa.get("9F6C"))).isEqualTo("1000");
	}

	private static byte[] raw(final Map<String, byte[]> pMap, final String pTag) {
		return pMap.get(pTag);
	}

}
