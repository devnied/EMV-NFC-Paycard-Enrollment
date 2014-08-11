package com.github.devnied.emvnfccard.utils;

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class AtrTest {

	@Test
	public void testEnum() {

		Assertions.assertThat(AtrUtils.getATR("3B 02 14 50")).isEqualTo(Arrays.asList("Schlumberger Multiflex 3k"));
		Assertions.assertThat(AtrUtils.getATR(null)).isEqualTo(null);
		Assertions
				.assertThat(AtrUtils.getATR("3B 07 64 11.........."))
				.isEqualTo(
						Arrays.asList("HID Corporate 1000 Format",
								"http://www.hidglobal.com/sites/hidglobal.com/files/resource_files/omnikey_contactless_developer_guide.pdf"));
	}
}
