package com.github.devnied.emvnfccard.utils;

import java.text.SimpleDateFormat;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.ServiceCode1Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode2Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode3Enum;

import fr.devnied.bitlib.BytesUtils;

public class TrackUtilsTest {

	@Test
	public void track1Test() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils
				.extractTrack1Data(
						card,
						BytesUtils
								.fromString("70 75 9F 6C 02 00 01 9F 62 06 00 00 00 38 00 00 9F 63 06 00 00 00 00 E0 E0 56 34 42343131313131313131313131313131313f305e202f5e31373032323031313030333f313030313030303030303030303030303f30303030 9F 64 01 03 9F 65 02 1C 00 9F 66 02 03 F0 9F 6B 13 5566887766556677 D1 81 02 01 00 00 00 00 00 10 0F 9F 67 01 03 90 00"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4111111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("02/2017");
		Assertions.assertThat(card.getTrack1()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track1NameTest() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils
				.extractTrack1Data(
						card,
						BytesUtils
								.fromString("563A42343131313131313131313131313131313F305E446F652F4A6F686E5E31373032323031313030333F313030313030303030303030303030303F30303030"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4111111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("02/2017");
		Assertions.assertThat(card.getTrack1()).isNotNull();
		Assertions.assertThat(card.getTrack1().getHolderFirstname()).isEqualTo("John");
		Assertions.assertThat(card.getTrack1().getHolderLastname()).isEqualTo("Doe");
		Assertions.assertThat(card.getTrack1().getService()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track1FormatTest() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils
				.extractTrack1Data(
						card,
						BytesUtils
								.fromString("564C42353231313131313131313131313131315E202F2020202020202020202020202020202020202020202020205E31363038323032303030303030303030303030312020303030202020202030"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5211111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("08/2016");
		Assertions.assertThat(card.getTrack1()).isNotNull();
		Assertions.assertThat(card.getTrack1().getHolderFirstname()).isNull();
		Assertions.assertThat(card.getTrack1().getHolderLastname()).isNull();
		Assertions.assertThat(card.getTrack1().getService()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.GOODS_SERVICES);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getPinRequirements()).isNotNull();
	}
	
	@Test
	public void track1FormatNullUser() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils
				.extractTrack1Data(
						card,
						BytesUtils
								.fromString("564C42353231313131313131313131313131315E22202020202020202020202020202020202020202020202020205E31363038323032303030303030303030303030312020303030202020202030"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5211111111111111");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("08/2016");
		Assertions.assertThat(card.getTrack1()).isNotNull();
		Assertions.assertThat(card.getTrack1().getHolderFirstname()).isNull();
		Assertions.assertThat(card.getTrack1().getHolderLastname()).isNull();
		Assertions.assertThat(card.getTrack1().getService()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.GOODS_SERVICES);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track2Test() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("57 13 55 66 88 77 66 55 66 77 D1 50 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5566887766556677");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("06/2015");
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getTrack2().getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getTrack2().getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION);
		Assertions.assertThat(card.getTrack2().getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode3().getPinRequirements()).isNotNull();
	}

	@Test
	public void track2Test2() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("57 15 55 55 55 66 88 77 66 55 66 7D 11 05 62 01 69 28 07 65 90 00 0F"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5555556688776655667");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("05/2011");
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.NATIONAL_ICC);
		Assertions.assertThat(card.getTrack2().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.BY_ISSUER);
		Assertions.assertThat(card.getTrack2().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.NO_RESTRICTION_PIN_REQUIRED);
	}

	@Test
	public void track2TestNullService() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("57 15 55 55 55 66 88 77 66 55 66 7D 11 05 FF F1 69 28 07 65 90 00 0F"));

		Assertions.assertThat(ret).isTrue();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5555556688776655667");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("05/2011");
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode1()).isNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode2()).isNull();
		Assertions.assertThat(card.getTrack2().getService().getServiceCode3()).isNull();
	}
	
	@Test
	public void track2TestNull() {
		EmvCard card = new EmvCard();
		boolean ret = TrackUtils.extractTrack2Data(card,
				BytesUtils.fromString("00"));

		Assertions.assertThat(ret).isFalse();
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack2()).isNull();
	}

}
