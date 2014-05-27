package com.github.devnied.emvnfccard.utils;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.SWEnum;

public class ResponseUtilsTest {

	@Test
	public void testResponse() {

		Assertions.assertThat(ResponseUtils.isSucceed(new byte[] { (byte) 0x90, 0 })).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.isSucceed(new byte[] { 0, (byte) 0x90, 0 })).isEqualTo(true);
		Assertions.assertThat(ResponseUtils.isSucceed(new byte[] { (byte) 0x00, 0 })).isEqualTo(false);
		Assertions.assertThat(ResponseUtils.isSucceed(null)).isEqualTo(false);

		Assertions.assertThat(SWEnum.getSW(new byte[] {})).isEqualTo(null);
		Assertions.assertThat(SWEnum.getSW(null)).isEqualTo(null);
		Assertions.assertThat(SWEnum.getSW(new byte[] { (byte) 0x90, 0 })).isEqualTo(SWEnum.SW_SUCCESS);
		Assertions.assertThat(SWEnum.getSW(new byte[] { (byte) 0x65, (byte) 0x81 })).isEqualTo(SWEnum.SW_MEMORY_ERROR);
		Assertions.assertThat(SWEnum.getSW(new byte[] { 0x45, 0x45, (byte) 0x90, 0 })).isEqualTo(SWEnum.SW_SUCCESS);
		Assertions.assertThat(SWEnum.getSW(new byte[] { 0x45, 0x45, (byte) 0x65, (byte) 0x81 }))
				.isEqualTo(SWEnum.SW_MEMORY_ERROR);

	}
}
