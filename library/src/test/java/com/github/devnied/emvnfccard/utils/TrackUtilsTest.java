package com.github.devnied.emvnfccard.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.enums.ServiceCode1Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode2Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode3Enum;

import fr.devnied.bitlib.BytesUtils;

public class TrackUtilsTest {

	@Test
	public void track1Test() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42343131313131313131313131313131313F305E202F5E31373032323031313030333F313030313030303030303030303030303F"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getCardNumber()).isEqualTo("4111111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("28/02/2017");
		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getService()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(track1.getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(track1.getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(track1.getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track1NameTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42343131313131313131313131313131313F305E446F652F4A6F686E5E31373032323031313030333F313030313030303030303030303030303F"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getCardNumber()).isEqualTo("4111111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("28/02/2017");
		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getHolderFirstname()).isEqualTo("John");
		Assertions.assertThat(track1.getHolderLastname()).isEqualTo("Doe");
		Assertions.assertThat(track1.getService()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(track1.getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(track1.getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(track1.getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track1FormatTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42353231313131313131313131313131315E202F2020202020202020202020202020202020202020202020205E31363038323032303030303030303030303030312020303030202020202030"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getCardNumber()).isEqualTo("5211111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("31/08/2016");
		Assertions.assertThat(track1.getHolderFirstname()).isNull();
		Assertions.assertThat(track1.getHolderLastname()).isNull();
		Assertions.assertThat(track1.getService()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(track1.getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(track1.getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.GOODS_SERVICES);
		Assertions.assertThat(track1.getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track1FormatNullUser() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42353231313131313131313131313131315E22202020202020202020202020202020202020202020202020205E31363038323032303030303030303030303030312020303030202020202030"));

		Assertions.assertThat(track1.getCardNumber()).isEqualTo("5211111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("31/08/2016 00:00:00");
		Assertions.assertThat(track1.getHolderFirstname()).isNull();
		Assertions.assertThat(track1.getHolderLastname()).isNull();
		Assertions.assertThat(track1.getService()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(track1.getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(track1.getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.GOODS_SERVICES);
		Assertions.assertThat(track1.getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track2EquivalentTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils.fromString("55 66 88 77 66 55 66 77 D1 50 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getCardNumber()).isEqualTo("5566887766556677");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(track2.getExpireDate())).isEqualTo("30/06/2015");
		Assertions.assertThat(track2.getService()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(track2.getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(track2.getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(track2.getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track2EquivalentTest2() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils.fromString("55 55 55 66 88 77 66 55 66 7D 11 05 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getCardNumber()).isEqualTo("5555556688776655667");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(track2.getExpireDate())).isEqualTo("31/05/2011");
		Assertions.assertThat(track2.getService()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.NATIONAL_ICC);
		Assertions.assertThat(track2.getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.BY_ISSUER);
		Assertions.assertThat(track2.getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION_PIN_REQUIRED);
	}

	@Test
	public void track2EquivalentTestNullService() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils.fromString("55 55 55 66 88 77 66 55 66 7D 11 05 FF F1 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getCardNumber()).isEqualTo("5555556688776655667");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(track2.getExpireDate())).isEqualTo("31/05/2011");
		Assertions.assertThat(track2.getService()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode1()).isNull();
		Assertions.assertThat(track2.getService().getServiceCode2()).isNull();
		Assertions.assertThat(track2.getService().getServiceCode3()).isNull();
	}

	@Test
	public void track2EquivalentTestNull() {
		EmvTrack2 card = TrackUtils.extractTrack2EquivalentData(BytesUtils.fromString("00"));

		Assertions.assertThat(card).isNull();
	}


	/**
	 * The discretionary data is the balance of the characters of a track 1,
	 * everything the card wrote after the service code. ISO/IEC 7813 gives its
	 * content to nobody, EMV 4.4 Book 3 Annex A1 saying of the same field of
	 * the second track that it is "defined by individual payment systems".
	 * <p>
	 * The track read here is the one of {@link #track1Test()}, whose end
	 * sentinel closes the field after four characters.
	 * </p>
	 */
	@Test
	public void track1DiscretionaryDataTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42343131313131313131313131313131313F305E202F5E31373032323031313030333F313030313030303030303030303030303F"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getServiceCode()).isEqualTo("201");
		Assertions.assertThat(track1.getDiscretionaryData()).isEqualTo("1003");
		Assertions.assertThat(track1.getRawDiscretionaryData()).isNull();
	}

	/**
	 * A track 1 whose discretionary data holds the spaces of the common
	 * character set: they are kept inside the field, and only the padding at
	 * its ends is dropped. The track is the one of {@link #track1FormatTest()}.
	 */
	@Test
	public void track1DiscretionaryDataWithSpacesTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42353231313131313131313131313131315E202F2020202020202020202020202020202020202020202020205E31363038323032303030303030303030303030312020303030202020202030"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getServiceCode()).isEqualTo("202");
		Assertions.assertThat(track1.getDiscretionaryData()).isEqualTo("0000000000001  000     0");
	}

	/**
	 * The discretionary data of a track 2 is numeric and takes one nibble per
	 * digit. EMV 4.4 Book 3 Annex A1 closes the sub-fields of the Track 2
	 * Equivalent Data with "Discretionary Data (defined by individual payment
	 * systems) | n, var." then "Pad with one Hex 'F' if needed to ensure whole
	 * bytes | b": the pad is not a digit of the field and is dropped.
	 * <p>
	 * The track is the one of {@link #track2EquivalentTest()}, and the thirteen
	 * digits left after the pad are the most a Kernel 2 card can address, EMV
	 * Contactless Book C-2 v2.6 section 4.8 reading "For Track 2 Data mTRACK2
	 * is a maximum of 13 digits".
	 * </p>
	 */
	@Test
	public void track2DiscretionaryDataTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils
				.fromString("55 66 88 77 66 55 66 77 D1 50 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getServiceCode()).isEqualTo("201");
		Assertions.assertThat(track2.getDiscretionaryData()).isEqualTo("6928076590000");
		Assertions.assertThat(track2.getRawDiscretionaryData()).isNull();
	}

	/**
	 * A card leaving the service code unused does not shift the discretionary
	 * data: Table 2 of ISO/IEC 7813:2006 gives the field three positions
	 * whatever it holds, so the discretionary data still starts three positions
	 * after the expiration date. The track is the one of
	 * {@link #track2EquivalentTestNullService()}, whose service code holds
	 * three 'F' nibbles.
	 */
	@Test
	public void track2DiscretionaryDataWithoutServiceCodeTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils
				.fromString("55 55 55 66 88 77 66 55 66 7D 11 05 FF F1 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getServiceCode()).isNull();
		Assertions.assertThat(track2.getService()).isNotNull();
		Assertions.assertThat(track2.getService().getServiceCode1()).isNull();
		Assertions.assertThat(track2.getDiscretionaryData()).isEqualTo("16928076590000");
	}

	/**
	 * Test the discretionary data of a track 2 whose service code field the card
	 * filled with padding
	 * <p>
	 * Track 2 Equivalent Data of the real MasterCardPpse capture. The three
	 * nibbles ISO/IEC 7813 reserves for the service code hold '2FF', which is
	 * not the three digits the field asks for, so everything that follows is the
	 * 'F' padding of the data object and not discretionary data. Reporting it
	 * would hand out padding as if the card had stored something.
	 * </p>
	 */
	@Test
	public void track2DiscretionaryDataFilledWithPaddingTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils
				.fromString("55 99 99 99 99 99 99 99 D1 50 92 FF FF FF FF FF FF FF 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getServiceCode()).isNull();
		Assertions.assertThat(track2.getDiscretionaryData()).isNull();
	}

	/**
	 * Reading of the Track 1 Discretionary Data (tag '9F1F'), the card resident
	 * data object holding the discretionary field of the first track on its
	 * own. EMV 4.4 Book 3 Annex A1: "Track 1 Discretionary Data | Discretionary
	 * part of track 1 according to ISO/IEC 7813 | ICC | ans | '70' or '77' |
	 * '9F1F' | var."
	 * <p>
	 * The value read here is the discretionary field of the MasterCardPpse3
	 * capture: the record of that trace answers a Track 1 Data (tag '56') whose
	 * thirteen last bytes, "30 31 30 30 30 30 30 30 30 30 30 30 33", are the
	 * discretionary data that follows its service code "101". A card answering
	 * the same field in the tag '9F1F' answers those very bytes, the format ans
	 * of the two data objects coding one character per byte.
	 * </p>
	 */
	@Test
	public void track1DiscretionaryDataTagTest() {
		String discretionaryData = TrackUtils.extractTrack1DiscretionaryData(BytesUtils
				.fromString("30 31 30 30 30 30 30 30 30 30 30 30 33"));

		Assertions.assertThat(discretionaryData).isEqualTo("0100000000003");
	}

	/**
	 * The same data object padded by a card that answers it on a longer field.
	 * EMV 4.4 Book 3 Annex A2: "A data element in format a, an, or ans is left
	 * justified and padded with trailing hexadecimal zeroes." The padding is
	 * not part of the discretionary data and is dropped.
	 */
	@Test
	public void track1DiscretionaryDataTagPaddingTest() {
		String discretionaryData = TrackUtils.extractTrack1DiscretionaryData(BytesUtils
				.fromString("30 31 30 30 30 30 30 30 30 30 30 30 33 00 00 00"));

		Assertions.assertThat(discretionaryData).isEqualTo("0100000000003");
	}

	/**
	 * Reading of the Track 2 Discretionary Data (tag '9F20'), the card resident
	 * data object holding the discretionary field of the second track on its
	 * own. EMV 4.4 Book 3 Annex A1: "Track 2 Discretionary Data | Discretionary
	 * part of track 2 according to ISO/IEC 7813 | ICC | cn | '70' or '77' |
	 * '9F20' | var."
	 * <p>
	 * The value read here is the discretionary field of the MasterCardPpse3
	 * capture: the record of that trace answers a Track 2 Data (tag '9F6B')
	 * whose seven last bytes, "01 00 00 00 00 00 3F", are the discretionary
	 * data that follows its service code "101", pad included. A card answering
	 * the same field in the tag '9F20' answers those very bytes, the format cn
	 * of the two data objects coding two digits per byte and padding with
	 * trailing 'F' nibbles.
	 * </p>
	 * <p>
	 * The same card wrote the same thirteen digits in its two tracks, which
	 * {@link #track1DiscretionaryDataTagTest()} reads as characters.
	 * </p>
	 */
	@Test
	public void track2DiscretionaryDataTagTest() {
		String discretionaryData = TrackUtils.extractTrack2DiscretionaryData(BytesUtils
				.fromString("01 00 00 00 00 00 3F"));

		Assertions.assertThat(discretionaryData).isEqualTo("0100000000003");
	}

	/**
	 * A card that answers neither of the two data objects leaves nothing to
	 * decode.
	 */
	@Test
	public void discretionaryDataTagsNullTest() {
		Assertions.assertThat(TrackUtils.extractTrack1DiscretionaryData(null)).isNull();
		Assertions.assertThat(TrackUtils.extractTrack2DiscretionaryData(null)).isNull();
		Assertions.assertThat(TrackUtils.extractTrack1DiscretionaryData(new byte[0])).isNull();
		Assertions.assertThat(TrackUtils.extractTrack2DiscretionaryData(new byte[0])).isNull();
	}

	/**
	 * The fields of a track 1 as the card wrote them, kept next to their
	 * decoded values. The track is the one of {@link #track1Test()}: no start
	 * sentinel, an optional field '?0' between the PAN and the name, a name
	 * field " /" that holds no name, and an end sentinel closing the
	 * discretionary field.
	 */
	@Test
	public void track1RawFieldsTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42343131313131313131313131313131313F305E202F5E31373032323031313030333F313030313030303030303030303030303F"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.isStartSentinel()).isFalse();
		Assertions.assertThat(track1.isEndSentinel()).isTrue();
		Assertions.assertThat(track1.getOptionalField()).isEqualTo("?0");
		Assertions.assertThat(track1.getNameField()).isEqualTo(" /");
		Assertions.assertThat(track1.getHolderLastname()).isNull();
		Assertions.assertThat(track1.getHolderFirstname()).isNull();
		Assertions.assertThat(track1.getExpirationField()).isEqualTo("1702");
		Assertions.assertThat(track1.getServiceCodeField()).isEqualTo("201");
		Assertions.assertThat(track1.getServiceCode()).isEqualTo("201");
		Assertions.assertThat(track1.getRawDiscretionaryField()).isEqualTo("1003");
		Assertions.assertThat(track1.getDiscretionaryData()).isEqualTo("1003");
	}

	/**
	 * The name field of the track of {@link #track1NameTest()} as written,
	 * next to the two names it splits into
	 */
	@Test
	public void track1RawNameFieldTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42343131313131313131313131313131313F305E446F652F4A6F686E5E31373032323031313030333F313030313030303030303030303030303F"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getNameField()).isEqualTo("Doe/John");
		Assertions.assertThat(track1.getHolderLastname()).isEqualTo("Doe");
		Assertions.assertThat(track1.getHolderFirstname()).isEqualTo("John");
		Assertions.assertThat(track1.getOptionalField()).isEqualTo("?0");
		Assertions.assertThat(track1.getExpirationField()).isEqualTo("1702");
		Assertions.assertThat(track1.getServiceCodeField()).isEqualTo("201");
		Assertions.assertThat(track1.getRawDiscretionaryField()).isEqualTo("1003");
		Assertions.assertThat(track1.isEndSentinel()).isTrue();
	}

	/**
	 * The track of {@link #track1FormatNullUser()}: no sentinel at either
	 * end, no optional field, a name field padded with spaces that is kept
	 * untrimmed, and a discretionary field whose trailing padding is kept as
	 * written while {@link EmvTrack1#getDiscretionaryData()} drops it
	 */
	@Test
	public void track1RawFieldsWithoutSentinelsTest() {
		EmvTrack1 track1 = TrackUtils
				.extractTrack1Data(
						BytesUtils
								.fromString("42353231313131313131313131313131315E22202020202020202020202020202020202020202020202020205E31363038323032303030303030303030303030312020303030202020202030"));

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.isStartSentinel()).isFalse();
		Assertions.assertThat(track1.isEndSentinel()).isFalse();
		Assertions.assertThat(track1.getOptionalField()).isNull();
		Assertions.assertThat(track1.getNameField()).isEqualTo("\"                         ");
		Assertions.assertThat(track1.getNameField()).hasSize(26);
		Assertions.assertThat(track1.getHolderLastname()).isNull();
		Assertions.assertThat(track1.getHolderFirstname()).isNull();
		Assertions.assertThat(track1.getExpirationField()).isEqualTo("1608");
		Assertions.assertThat(track1.getServiceCodeField()).isEqualTo("202");
		Assertions.assertThat(track1.getRawDiscretionaryField()).isEqualTo("0000000000001  000     0");
		Assertions.assertThat(track1.getDiscretionaryData()).isEqualTo("0000000000001  000     0");
	}

	/**
	 * A track 1 written with both sentinels of ISO/IEC 7813 Table 1 and a
	 * name field without the '/' separator: the name is kept as written and
	 * the two holder names, which need the separator, stay null
	 */
	@Test
	public void track1SentinelsAndSingleNameTest() {
		EmvTrack1 track1 = TrackUtils.extractTrack1Data("%B4111111111111111^JOHN DOE^17022011003?".getBytes());

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.isStartSentinel()).isTrue();
		Assertions.assertThat(track1.isEndSentinel()).isTrue();
		Assertions.assertThat(track1.getFormatCode()).isEqualTo("B");
		Assertions.assertThat(track1.getCardNumber()).isEqualTo("4111111111111111");
		Assertions.assertThat(track1.getOptionalField()).isNull();
		Assertions.assertThat(track1.getNameField()).isEqualTo("JOHN DOE");
		Assertions.assertThat(track1.getHolderLastname()).isNull();
		Assertions.assertThat(track1.getHolderFirstname()).isNull();
		Assertions.assertThat(track1.getExpirationField()).isEqualTo("1702");
		Assertions.assertThat(track1.getServiceCodeField()).isEqualTo("201");
		Assertions.assertThat(track1.getRawDiscretionaryField()).isEqualTo("1003");
	}

	/**
	 * A track 1 whose service code field holds the '^' separator ISO/IEC
	 * 7813 lets a card write when the field is unused: the field is kept as
	 * written while {@link EmvTrack1#getServiceCode()} stays null
	 */
	@Test
	public void track1UnusedServiceCodeFieldTest() {
		EmvTrack1 track1 = TrackUtils.extractTrack1Data("%B4111111111111111^DOE/JOHN^1702^1003?".getBytes());

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getExpirationField()).isEqualTo("1702");
		Assertions.assertThat(track1.getServiceCodeField()).isEqualTo("^");
		Assertions.assertThat(track1.getServiceCode()).isNull();
		Assertions.assertThat(track1.getService()).isNotNull();
		Assertions.assertThat(track1.getService().getServiceCode1()).isNull();
		Assertions.assertThat(track1.getRawDiscretionaryField()).isEqualTo("1003");
	}

	/**
	 * A track 1 whose expiration field holds the '^' separator has no readable
	 * date, but the card number and the name it carries are unambiguous: the
	 * track is returned with an empty expiration date rather than thrown away.
	 * A field the card did not encode is reported as absent, never rounded
	 * into a plausible looking date.
	 */
	@Test
	public void track1UnusedExpirationFieldTest() {
		EmvTrack1 track1 = TrackUtils.extractTrack1Data("%B4111111111111111^DOE/JOHN^^2011003?".getBytes());

		Assertions.assertThat(track1).isNotNull();
		Assertions.assertThat(track1.getCardNumber()).isEqualTo("4111111111111111");
		Assertions.assertThat(track1.getHolderLastname()).isEqualTo("DOE");
		Assertions.assertThat(track1.getHolderFirstname()).isEqualTo("JOHN");
		Assertions.assertThat(track1.getExpireDate()).isNull();
	}

	/**
	 * The fields of a track 2 as the card wrote them, one character per
	 * nibble. The track is the one of {@link #track2EquivalentTest()}: the
	 * four expiration digits, then the service code positions, the
	 * discretionary data and the 'F' pad that follow them
	 */
	@Test
	public void track2RawFieldsTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils
				.fromString("55 66 88 77 66 55 66 77 D1 50 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getExpirationField()).isEqualTo("1506");
		Assertions.assertThat(track2.getServiceCodeField()).isEqualTo("201");
		Assertions.assertThat(track2.getServiceCode()).isEqualTo("201");
		Assertions.assertThat(track2.getTrailingField()).isEqualTo("2016928076590000F");
		Assertions.assertThat(track2.getDiscretionaryData()).isEqualTo("6928076590000");
	}

	/**
	 * The track of {@link #track2DiscretionaryDataWithoutServiceCodeTest()}:
	 * the three service code positions hold 'F' nibbles, kept as written
	 * while {@link EmvTrack2#getServiceCode()} stays null
	 */
	@Test
	public void track2RawFieldsWithoutServiceCodeTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils
				.fromString("55 55 55 66 88 77 66 55 66 7D 11 05 FF F1 69 28 07 65 90 00 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getExpirationField()).isEqualTo("1105");
		Assertions.assertThat(track2.getServiceCodeField()).isEqualTo("FFF");
		Assertions.assertThat(track2.getServiceCode()).isNull();
		Assertions.assertThat(track2.getTrailingField()).isEqualTo("FFF16928076590000F");
		Assertions.assertThat(track2.getDiscretionaryData()).isEqualTo("16928076590000");
	}

	/**
	 * The track of {@link #track2DiscretionaryDataFilledWithPaddingTest()}:
	 * the trailing field keeps the padding the card wrote while the
	 * discretionary data, which is nothing but padding, stays null
	 */
	@Test
	public void track2RawFieldsFilledWithPaddingTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils
				.fromString("55 99 99 99 99 99 99 99 D1 50 92 FF FF FF FF FF FF FF 0F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getExpirationField()).isEqualTo("1509");
		Assertions.assertThat(track2.getServiceCodeField()).isEqualTo("2FF");
		Assertions.assertThat(track2.getServiceCode()).isNull();
		Assertions.assertThat(track2.getTrailingField()).isEqualTo("2FFFFFFFFFFFFFF0F");
		Assertions.assertThat(track2.getDiscretionaryData()).isNull();
	}

	/**
	 * A track 2 that ends with its expiration date, or one nibble after it:
	 * the fields the card did not write stay null or are as short as the
	 * track leaves them, and nothing is thrown
	 */
	@Test
	public void track2RawFieldsShortTrackTest() {
		EmvTrack2 track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils.fromString("55 66 88 77 66 55 66 7D 15 06"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getCardNumber()).isEqualTo("556688776655667");
		Assertions.assertThat(track2.getExpirationField()).isEqualTo("1506");
		Assertions.assertThat(track2.getServiceCodeField()).isNull();
		Assertions.assertThat(track2.getTrailingField()).isNull();
		Assertions.assertThat(track2.getServiceCode()).isNull();
		Assertions.assertThat(track2.getDiscretionaryData()).isNull();

		track2 = TrackUtils.extractTrack2EquivalentData(BytesUtils.fromString("55 66 88 77 66 55 66 77 D1 50 6F"));

		Assertions.assertThat(track2).isNotNull();
		Assertions.assertThat(track2.getExpirationField()).isEqualTo("1506");
		Assertions.assertThat(track2.getServiceCodeField()).isEqualTo("F");
		Assertions.assertThat(track2.getTrailingField()).isEqualTo("F");
		Assertions.assertThat(track2.getServiceCode()).isNull();
		Assertions.assertThat(track2.getDiscretionaryData()).isNull();
	}

}
