package com.github.devnied.emvnfccard.utils;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class AtrTest {

	@Test
	public void testDescriptionFromATR() {

		Assertions.assertThat(AtrUtils.getDescription("3B 02 14 50")).isEqualTo(Arrays.asList("Schlumberger Multiflex 3k"));
		Assertions.assertThat(AtrUtils.getDescription(null)).isEqualTo(null);
		Assertions
				.assertThat(AtrUtils.getDescription("3B 07 64 11.........."))
				.isEqualTo(
						Arrays.asList("HID Corporate 1000 Format",
								"http://www.hidglobal.com/sites/hidglobal.com/files/resource_files/omnikey_contactless_developer_guide.pdf"));
	}

	@Test
	public void testDescriptionFromATS() {

		Assertions.assertThat(AtrUtils.getDescriptionFromAts("20 63 CB A3 A0")).contains(
				"VISA card from Banque Populaire");
		Assertions.assertThat(AtrUtils.getDescription(null)).isEqualTo(null);
	}
	
	@Test
	public void testDescriptionFromATS2() {

		Assertions.assertThat(AtrUtils.getDescriptionFromAts("0F 55 B0 11 69 FF 4A 50 D0 80 00 49 54 03")).contains(
				"Sky (Italy) VideoGuard CAM card");
	}
}
