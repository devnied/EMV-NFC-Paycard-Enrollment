package com.github.devnied.emvnfccard.utils;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

public class TLVUtilsTest {

	@Test
	public void test() {

		// GetIntValue
		Assertions.assertThat(TLVUtils.getIntValue(BytesUtils.fromString("6F 1A 84 08 88 01 01 6E"), TLVUtils.SFI)).isEqualTo(1);
		Assertions.assertThat(TLVUtils.getIntValue(BytesUtils.fromString("6F 1A 84"), "FF")).isEqualTo(-1);

		// getHexaValue
		Assertions.assertThat(
				TLVUtils.getHexaValue(BytesUtils.fromString("70 1A 34 18 4F 07 A0 00 00 00 03 10 10 50 0A"), TLVUtils.AID))
				.isEqualTo("A0000000031010");

		Assertions.assertThat(TLVUtils.getHexaValue(BytesUtils.fromString("70 43 61 18 4F 07"), "AA")).isEqualTo(null);

	}
}
