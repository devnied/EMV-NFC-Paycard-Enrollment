package com.github.devnied.emvnfccard.enums;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;

public class CurrencyEnumTest {

	@Test
	public void test() throws Exception {
		Assertions.assertThat(CurrencyEnum.EUR.getCode()).isEqualTo("EUR");
		Assertions.assertThat(CurrencyEnum.EUR.getISOCodeAlpha()).isEqualTo("EUR");
		Assertions.assertThat(CurrencyEnum.EUR.getISOCodeNumeric()).isEqualTo(978);
		Assertions.assertThat(CurrencyEnum.EUR.getKey()).isEqualTo(978);
		Assertions.assertThat(CurrencyEnum.EUR.getName()).isEqualTo("Euro");
		Assertions.assertThat(CurrencyEnum.ALL.getCountries()).isEqualTo(new CountryCodeEnum[] { CountryCodeEnum.AL });

		// Find currency
		Assertions.assertThat(CurrencyEnum.find("EUR")).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(CurrencyEnum.find("dTEST")).isEqualTo(null);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.FR)).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.US, CurrencyEnum.USD)).isEqualTo(CurrencyEnum.USD);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.US, null)).isEqualTo(CurrencyEnum.USD);
		Assertions.assertThat(CurrencyEnum.find(null, CurrencyEnum.USD)).isEqualTo(CurrencyEnum.USD);
		Assertions.assertThat(CurrencyEnum.find(null, null)).isEqualTo(null);

		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.BO)).isEqualTo(CurrencyEnum.BOB);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.CH)).isEqualTo(CurrencyEnum.CHF);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.CL)).isEqualTo(CurrencyEnum.CLP);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.MX)).isEqualTo(CurrencyEnum.MXN);
		Assertions.assertThat(CurrencyEnum.find(CountryCodeEnum.UY)).isEqualTo(CurrencyEnum.UYU);

		// Format
		Assertions.assertThat(CurrencyEnum.EUR.format(100)).isEqualTo("EUR 1.00");
		Assertions.assertThat(CurrencyEnum.BIF.format(10)).isEqualTo("BIF 10");
		Assertions.assertThat(CurrencyEnum.JOD.format(10)).isEqualTo("JOD 0.010");
		Assertions.assertThat(CurrencyEnum.XBB.format(10)).isEqualTo("XBB 10");
		Assertions.assertThat(CurrencyEnum.MRO.format(10)).isEqualTo("MRO 10");
	}

}
