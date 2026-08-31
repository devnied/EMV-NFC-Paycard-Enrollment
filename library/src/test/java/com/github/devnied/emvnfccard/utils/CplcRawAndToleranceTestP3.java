package com.github.devnied.emvnfccard.utils;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.CPLC;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * The raw bytes of the Card Production Life Cycle data (tag '9F7F') are kept
 * on the {@link CPLC} bean, and a date field the card filled with an invalid
 * day of year no longer loses the whole CPLC: the fields decoded before it are
 * kept and the error recorded.
 *
 * @author MILLAU Julien
 *
 */
public class CplcRawAndToleranceTestP3 {

	/**
	 * The 42 bytes of the CPLC of {@code CPLCUtilsTest#testRawDataCPLC}
	 */
	private static final String CPLC_VALUE = "47 90 50 40 47 91 81 02 31 00 83 58 00 11 68 91 45 81 48 12 83 65 00 00 00 00 01 2F 31 30 31 31 36 38 00 00 00 00 00 00 00 00";

	/**
	 * The same CPLC whose os_release_date (bytes 6 and 7) reads '03 99', day
	 * 399 of the year, which Global Platform cannot code (YDDD, DDD at most
	 * 366)
	 */
	private static final String CPLC_INVALID_DATE = "47 90 50 40 47 91 03 99 31 00 83 58 00 11 68 91 45 81 48 12 83 65 00 00 00 00 01 2F 31 30 31 31 36 38 00 00 00 00 00 00 00 00";

	/**
	 * The raw form (value followed by the status word) keeps the 42 bytes of
	 * the value, the status word left out, and records no error
	 */
	@Test
	public void testRawDataKeepsRawBytes() {
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString(CPLC_VALUE + " 90 00"));

		Assertions.assertThat(cplc).isNotNull();
		Assertions.assertThat(cplc.getRaw()).isEqualTo(BytesUtils.fromString(CPLC_VALUE));
		Assertions.assertThat(cplc.getRaw()).hasSize(CPLC.SIZE);
		Assertions.assertThat(cplc.getParseError()).isNull();
		Assertions.assertThat(cplc.getIcFabricator()).isEqualTo(0x4790);
		Assertions.assertThat(cplc.getPersoEquipment()).isEqualTo(0x0000);
	}

	/**
	 * The TLV form keeps the value of the tag '9F7F', the tag, the length and
	 * the status word left out
	 */
	@Test
	public void testPrependedKeepsRawBytes() {
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString("9F 7F 2A " + CPLC_VALUE + " 90 00"));

		Assertions.assertThat(cplc).isNotNull();
		Assertions.assertThat(cplc.getRaw()).isEqualTo(BytesUtils.fromString(CPLC_VALUE));
		Assertions.assertThat(cplc.getParseError()).isNull();
		Assertions.assertThat(cplc.getIcFabricator()).isEqualTo(0x4790);
	}

	/**
	 * An invalid day of year in a date field does not escape: the CPLC is
	 * returned with the fields decoded before it, the failing field and the
	 * ones after it null, the raw bytes still holding them, and the error
	 * recorded
	 */
	@Test
	public void testInvalidDateIsTolerated() {
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString(CPLC_INVALID_DATE + " 90 00"));

		Assertions.assertThat(cplc).isNotNull();
		Assertions.assertThat(cplc.getParseError()).isNotNull();
		Assertions.assertThat(cplc.getRaw()).isEqualTo(BytesUtils.fromString(CPLC_INVALID_DATE));
		// fields before the failing date
		Assertions.assertThat(cplc.getIcFabricator()).isEqualTo(0x4790);
		Assertions.assertThat(cplc.getIcType()).isEqualTo(0x5040);
		Assertions.assertThat(cplc.getOs()).isEqualTo(0x4791);
		// the failing date and the fields after it
		Assertions.assertThat(cplc.getOsReleaseDate()).isNull();
		Assertions.assertThat(cplc.getOsReleaseLevel()).isNull();
		Assertions.assertThat(cplc.getIcFabricDate()).isNull();
		Assertions.assertThat(cplc.getIcSerialNumber()).isNull();
		Assertions.assertThat(cplc.getPersoEquipment()).isNull();
	}

	/**
	 * The same invalid date in the TLV form
	 */
	@Test
	public void testInvalidDateIsToleratedInTlvForm() {
		CPLC cplc = CPLCUtils.parse(BytesUtils.fromString("9F 7F 2A " + CPLC_INVALID_DATE + " 90 00"));

		Assertions.assertThat(cplc).isNotNull();
		Assertions.assertThat(cplc.getParseError()).isNotNull();
		Assertions.assertThat(cplc.getRaw()).hasSize(CPLC.SIZE);
		Assertions.assertThat(cplc.getIcFabricator()).isEqualTo(0x4790);
		Assertions.assertThat(cplc.getOsReleaseDate()).isNull();
	}

	/**
	 * The length and tag rejections are unchanged: no CPLC, no raw bytes
	 */
	@Test
	public void testRejectionsUnchanged() {
		Assertions.assertThat(CPLCUtils.parse(null)).isNull();
		Assertions.assertThat(CPLCUtils.parse(BytesUtils.fromString("69 85"))).isNull();
		// right length, wrong tag
		Assertions.assertThat(CPLCUtils.parse(BytesUtils.fromString("9F 7E 2A " + CPLC_VALUE + " 90 00"))).isNull();
	}

	/**
	 * The CPLC of the VisaCardPpse capture, whose GET DATA answers "9F 7F 2A
	 * 47 90 50 37 ...", reaches the card with its 42 raw bytes
	 */
	@Test
	public void testVisaCardPpseCplcRaw() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpse")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)) //
				.build();
		EmvCard card = template.readEmvCard();

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCplc()).isNotNull();
		Assertions.assertThat(card.getCplc().getRaw()).hasSize(CPLC.SIZE);
		Assertions.assertThat(BytesUtils.bytesToString(card.getCplc().getRaw())).startsWith("47 90 50 37 16 71 92 59");
		Assertions.assertThat(card.getCplc().getParseError()).isNull();
		Assertions.assertThat(card.getCplc().getIcFabricator()).isEqualTo(0x4790);
	}

}
