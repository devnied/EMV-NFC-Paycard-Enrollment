package com.github.devnied.emvnfccard.utils;

import java.text.SimpleDateFormat;

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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("02/2017");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("02/2017");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("08/2016");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track1.getExpireDate())).isEqualTo("08/2016");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track2.getExpireDate())).isEqualTo("06/2015");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track2.getExpireDate())).isEqualTo("05/2011");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(track2.getExpireDate())).isEqualTo("05/2011");
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

}
