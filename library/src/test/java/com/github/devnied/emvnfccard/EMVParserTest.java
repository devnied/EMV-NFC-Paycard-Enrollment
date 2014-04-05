package com.github.devnied.emvnfccard;

import java.text.SimpleDateFormat;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.EMVCardTypeEnum;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.parser.impl.DefaultEmvParser;
import com.github.devnied.emvnfccard.provider.PpseProviderTest;
import com.github.devnied.emvnfccard.provider.ProviderAidTest;
import com.github.devnied.emvnfccard.provider.PseProviderTest;

import fr.devnied.bitlib.BytesUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DefaultEmvParser.class })
public class EMVParserTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EMVParserTest.class);

	@Test
	public void test() {

		IProvider prov = new ProviderAidTest();

		EMVParser parser = new EMVParser(prov, true);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5772829193253472");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardTypeEnum.VISA);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("VISA");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("08/2014");

	}

	@Test
	public void testPPSE() {

		IProvider prov = new PpseProviderTest();

		EMVParser parser = new EMVParser(prov, true);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4979670123453600");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardTypeEnum.VISA);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("CB");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("02/2016");
	}

	@Test
	public void testPSE() {

		IProvider prov = new PseProviderTest();

		EMVParser parser = new EMVParser(prov, false);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4979670123453600");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardTypeEnum.VISA);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("VISACREDIT");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("02/2016");
	}

	@Test
	public void testAfl() throws Exception {

		@SuppressWarnings("unchecked")
		List<Afl> list = (List<Afl>) Whitebox.invokeMethod(DefaultEmvParser.getInstance(), DefaultEmvParser.class, "extractAfl",
				BytesUtils.fromString("80 0E 7C 00 08 01 01 00 10 01 05 00 18 01 02 01"));

		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(list.get(0).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(0).getLastRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(0).isOfflineAuthentication()).isEqualTo(false);
		Assertions.assertThat(list.get(1).getSfi()).isEqualTo(2);
		Assertions.assertThat(list.get(1).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(1).getLastRecord()).isEqualTo(5);
		Assertions.assertThat(list.get(1).isOfflineAuthentication()).isEqualTo(false);
		Assertions.assertThat(list.get(2).getSfi()).isEqualTo(3);
		Assertions.assertThat(list.get(2).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(2).getLastRecord()).isEqualTo(2);
		Assertions.assertThat(list.get(2).isOfflineAuthentication()).isEqualTo(true);

	}

	@Test
	public void tesCardType() throws Exception {
		Assertions.assertThat(EMVCardTypeEnum.getCardTypeByCardNumber("4000000000000000")).isEqualTo(EMVCardTypeEnum.VISA);
		Assertions.assertThat(EMVCardTypeEnum.getCardTypeByCardNumber("5000000000000000"))
				.isEqualTo(EMVCardTypeEnum.MASTER_CARD1);
		Assertions.assertThat(EMVCardTypeEnum.getCardTypeByCardNumber("6200000000000000")).isEqualTo(EMVCardTypeEnum.UNIONPAY);
	}

}
