package com.github.devnied.emvnfccard.utils;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.SwEnum;

public class ResponseUtilsTest {

	@Test
	public void testResponseIsSucceed() {
		Assertions.assertThat(ResponseUtils.isSucceed(new byte[] { (byte) 0x90, 0 })).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.isSucceed(new byte[] { 0, (byte) 0x90, 0 })).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.isSucceed(new byte[] { (byte) 0x00, 0 })).isEqualTo(false);
		Assertions.assertThat(ResponseUtils.isSucceed(null)).isEqualTo(false);
	}

	@Test
	public void testResponseGetSW() {
		Assertions.assertThat(SwEnum.getSW(new byte[] {})).isEqualTo(null);
		Assertions.assertThat(SwEnum.getSW(null)).isEqualTo(null);
		Assertions.assertThat(SwEnum.getSW(new byte[] { (byte) 0x90, 0 })).isEqualTo(SwEnum.SW_9000);
		Assertions.assertThat(SwEnum.getSW(new byte[] { (byte) 0x65, (byte) 0x81 })).isEqualTo(SwEnum.SW_6581);
		Assertions.assertThat(SwEnum.getSW(new byte[] { 0x45, 0x45, (byte) 0x90, 0 })).isEqualTo(SwEnum.SW_9000);
		Assertions.assertThat(SwEnum.getSW(new byte[] { 0x45, 0x45, (byte) 0x65, (byte) 0x81 })).isEqualTo(SwEnum.SW_6581);
		Assertions.assertThat(SwEnum.getSW(new byte[] { 0x45, 0x45, (byte) 0x62, (byte) 0xFF })).isEqualTo(SwEnum.SW_62);
	}

	@Test
	public void testResponseIsEquals() {
		Assertions.assertThat(ResponseUtils.isEquals(new byte[] { (byte) 0x90, 0 }, SwEnum.SW_9000)).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.isEquals(new byte[] { (byte) 0x6D, 18 }, SwEnum.SW_6D)).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.isEquals(new byte[] { (byte) 0x00, 0 }, SwEnum.SW_6D)).isEqualTo(false);
		Assertions.assertThat(ResponseUtils.isEquals(null, SwEnum.SW_6D)).isEqualTo(false);
	}

	@Test
	public void testResponseContains() {
		Assertions.assertThat(ResponseUtils.contains(new byte[] { (byte) 0x90, 0 }, SwEnum.SW_6D, SwEnum.SW_9000)).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.contains(new byte[] { (byte) 0x6D, 18 }, SwEnum.SW_6D, SwEnum.SW_9000)).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.contains(new byte[] { (byte) 0x00, 0 }, SwEnum.SW_6D, SwEnum.SW_9000)).isEqualTo(false);
		Assertions.assertThat(ResponseUtils.contains(null, SwEnum.SW_6D)).isEqualTo(false);
	}

}
