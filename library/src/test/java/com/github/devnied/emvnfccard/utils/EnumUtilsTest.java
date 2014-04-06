package com.github.devnied.emvnfccard.utils;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;

public class EnumUtilsTest {

	@Test
	public void testEnum() {

		Assertions.assertThat(EnumUtils.getValue(100000, CountryCodeEnum.class)).isNull();
		Assertions.assertThat(EnumUtils.getValue(250, CountryCodeEnum.class)).isEqualTo(CountryCodeEnum.FR);

	}
}
