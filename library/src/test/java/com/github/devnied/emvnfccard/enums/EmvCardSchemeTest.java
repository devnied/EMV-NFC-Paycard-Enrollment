package com.github.devnied.emvnfccard.enums;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;

public class EmvCardSchemeTest {

	@Test
	public void testCardType() throws Exception {
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("4000000000000000")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5100000000000000")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("6200000000000000")).isEqualTo(EmvCardScheme.UNIONPAY);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber(null)).isEqualTo(null);
	}

	@Test
	public void testAid() throws Exception {
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 03 ")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 03 9989")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 04")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 00 05")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0 00 00 03 33")).isEqualTo(EmvCardScheme.UNIONPAY);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid(null)).isEqualTo(null);
	}

	@Test
	public void testDomesticAidWinsOverTheRid() throws Exception {
		// The German card of the GeldKartePpse trace announces the girocard AID
		// D27600002547410100, it must not be reported as a plain GeldKarte
		// whose RID D276000025 also matches
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("D27600002547410100")).isEqualTo(EmvCardScheme.GIROCARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("D27600002545500200")).isEqualTo(EmvCardScheme.GELDKARTE);
		// InterSwitch is declared with a full AID built on the Verve RID
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000003710001")).isEqualTo(EmvCardScheme.INTER_SWITCH);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000003719999")).isEqualTo(EmvCardScheme.VERVE);
		// Dankort and PBS declare the very same AID, the tie must keep
		// resolving the way it always did so the reported scheme of a Danish
		// card does not change from one release to the next
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000001211010")).isEqualTo(EmvCardScheme.PBS);
	}

	@Test
	public void testDomesticSchemes() throws Exception {
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000421010")).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(EmvCardScheme.CB.getCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000002771010")).isEqualTo(EmvCardScheme.INTERAC);
		Assertions.assertThat(EmvCardScheme.INTERAC.getCountry()).isEqualTo(CountryCodeEnum.CA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000006723010")).isEqualTo(EmvCardScheme.TROY);
		Assertions.assertThat(EmvCardScheme.TROY.getCountry()).isEqualTo(CountryCodeEnum.TR);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000005321010")).isEqualTo(EmvCardScheme.ELO);
		Assertions.assertThat(EmvCardScheme.ELO.getCountry()).isEqualTo(CountryCodeEnum.BR);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("D8040000013010")).isEqualTo(EmvCardScheme.PROSTIR);
		Assertions.assertThat(EmvCardScheme.PROSTIR.getCountry()).isEqualTo(CountryCodeEnum.UA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000006581010")).isEqualTo(EmvCardScheme.MIR);
		Assertions.assertThat(EmvCardScheme.MIR.getCountry()).isEqualTo(CountryCodeEnum.RU);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000003156020")).isEqualTo(EmvCardScheme.CHIPKNIP);
		Assertions.assertThat(EmvCardScheme.CHIPKNIP.getCountry()).isEqualTo(CountryCodeEnum.NL);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0860001000001")).isEqualTo(EmvCardScheme.HUMO);
		Assertions.assertThat(EmvCardScheme.HUMO.getCountry()).isEqualTo(CountryCodeEnum.UZ);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A000000732100123")).isEqualTo(EmvCardScheme.MEEZA);
		Assertions.assertThat(EmvCardScheme.MEEZA.getCountry()).isEqualTo(CountryCodeEnum.EG);
		// International schemes are not tied to a country
		Assertions.assertThat(EmvCardScheme.VISA.getCountry()).isNull();
		Assertions.assertThat(EmvCardScheme.VISA.isDomestic()).isFalse();
		Assertions.assertThat(EmvCardScheme.MASTER_CARD.isDomestic()).isFalse();
	}

	@Test
	public void testAdditionalProductAids() throws Exception {
		// Products of the international schemes that are now matched
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000032010")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000032020")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000980840")).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000043060")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000046000")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000000042203")).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(EmvCardScheme.getCardTypeByAid("A0000001524010")).isEqualTo(EmvCardScheme.DINERS_CLUB);
	}

	@Test
	public void testDomesticCardNumberRanges() throws Exception {
		// Mir owns 2200-2204, cards are 16 to 19 digit long
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("2200000000000000")).isEqualTo(EmvCardScheme.MIR);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("2204000000000000000")).isEqualTo(EmvCardScheme.MIR);
		// 2205 is BORICA, not Mir
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("2205000000000000")).isNull();
		// Humo cards start with 9860, 860 being the country code of Uzbekistan
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("9860000000000000")).isEqualTo(EmvCardScheme.HUMO);
		// Uzcard, the other Uzbek scheme, is not declared
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("8600000000000000")).isNull();
		// Meeza is issued in the 5078xx range of the Egyptian Banks Company
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5078030000000000")).isEqualTo(EmvCardScheme.MEEZA);
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5078100000000000")).isEqualTo(EmvCardScheme.MEEZA);
		// The neighbours of that range must not be swallowed by Meeza: 5018,
		// 5020 and 5038 are Maestro, 5019 is Dankort and 507865-507964 is Verve
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5018000000000000")).isNull();
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5019000000000000")).isNull();
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5038000000000000")).isNull();
		Assertions.assertThat(EmvCardScheme.getCardTypeByCardNumber("5078650000000000")).isNull();
	}

	@Test
	public void testMethod() throws Exception {
		Assertions.assertThat(EmvCardScheme.VISA.getAid()[0]).isEqualTo("A0 00 00 00 03");
		Assertions.assertThat(EmvCardScheme.VISA.getAidByte()[0]).isEqualTo(new byte[] { (byte) 0xA0, 0, 0, 0, 0x03 });
		Assertions.assertThat(EmvCardScheme.VISA.getName()).isEqualTo("VISA");
	}

}
