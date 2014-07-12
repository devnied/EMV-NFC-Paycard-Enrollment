package com.github.devnied.emvnfccard;

import java.text.SimpleDateFormat;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.enums.EMVCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.EMVParser;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.provider.ExceptionProviderTest;
import com.github.devnied.emvnfccard.provider.PpseProviderMasterCardTest;
import com.github.devnied.emvnfccard.provider.PpseProviderVisa2Test;
import com.github.devnied.emvnfccard.provider.PpseProviderVisaTest;
import com.github.devnied.emvnfccard.provider.ProviderAidTest;
import com.github.devnied.emvnfccard.provider.PseProviderTest;

import fr.devnied.bitlib.BytesUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EMVParser.class })
public class EMVParserTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EMVParserTest.class);

	@Test
	public void testPPSEVisa() throws CommunicationException {

		IProvider prov = new PpseProviderVisaTest();

		EMVParser parser = new EMVParser(prov, true);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(card.getFisrtName()).isEqualTo(null);
		Assertions.assertThat(card.getLastName()).isEqualTo(null);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("CB");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
	}

	@Test
	public void testPPSEMasterCard() throws CommunicationException {

		IProvider prov = new PpseProviderMasterCardTest();

		EMVParser parser = new EMVParser(prov, true);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5599999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardScheme.MASTER_CARD1);
		Assertions.assertThat(card.getFisrtName()).isEqualTo(null);
		Assertions.assertThat(card.getLastName()).isEqualTo(null);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("CB");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
	}

	@Test
	public void testPPSEVisa2() throws CommunicationException {

		IProvider prov = new PpseProviderVisa2Test();

		EMVParser parser = new EMVParser(prov, true);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(card.getFisrtName()).isEqualTo(null);
		Assertions.assertThat(card.getLastName()).isEqualTo(null);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("CB");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
	}

	@Test
	public void testPSE() throws CommunicationException {

		IProvider prov = new PseProviderTest();

		EMVParser parser = new EMVParser(prov, false);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4979670123453600");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(card.getFisrtName()).isEqualTo(null);
		Assertions.assertThat(card.getLastName()).isEqualTo(null);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("VISACREDIT");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("02/2016");
		Assertions.assertThat(card.getListPayment()).isNotNull();
		EMVPaymentRecord record = card.getListPayment().get(0);
		Assertions.assertThat(record.getAmount()).isEqualTo(4600);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.REFUND);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getTransactionDate()).isNotNull();
	}

	@Test
	public void testAid() throws CommunicationException {

		IProvider prov = new ProviderAidTest();

		EMVParser parser = new EMVParser(prov, true);
		EMVCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getAid()).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5772829193253472");
		Assertions.assertThat(card.getType()).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(card.getCardLabel()).isEqualTo("VISA");
		Assertions.assertThat(card.getFisrtName()).isEqualTo(null);
		Assertions.assertThat(card.getLastName()).isEqualTo(null);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("08/2014");

	}

	@Test
	public void testException() throws CommunicationException {

		IProvider prov = new ExceptionProviderTest();

		EMVParser parser = new EMVParser(prov, true);
		try {
			parser.readEmvCard();
			Assert.fail();
		} catch (CommunicationException e) {
			Assert.assertTrue(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAfl() throws Exception {

		List<Afl> list = (List<Afl>) Whitebox.invokeMethod(new EMVParser(null, true), EMVParser.class, "extractAfl",
				BytesUtils.fromString("10020301 18010500 20010200"));
		Assertions.assertThat(list.size()).isEqualTo(3);
		Assertions.assertThat(list.get(0).getSfi()).isEqualTo(2);
		Assertions.assertThat(list.get(0).getFirstRecord()).isEqualTo(2);
		Assertions.assertThat(list.get(0).getLastRecord()).isEqualTo(3);
		Assertions.assertThat(list.get(0).isOfflineAuthentication()).isEqualTo(true);
		Assertions.assertThat(list.get(1).getSfi()).isEqualTo(3);
		Assertions.assertThat(list.get(1).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(1).getLastRecord()).isEqualTo(5);
		Assertions.assertThat(list.get(1).isOfflineAuthentication()).isEqualTo(false);
		Assertions.assertThat(list.get(2).getSfi()).isEqualTo(4);
		Assertions.assertThat(list.get(2).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(list.get(2).getLastRecord()).isEqualTo(2);
		Assertions.assertThat(list.get(2).isOfflineAuthentication()).isEqualTo(false);
	}

	@Test
	public void tesCardType() throws Exception {
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("4000000000000000")).isEqualTo(EMVCardScheme.VISA);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("5000000000000000")).isEqualTo(EMVCardScheme.MASTER_CARD1);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber("6200000000000000")).isEqualTo(EMVCardScheme.UNIONPAY);
		Assertions.assertThat(EMVCardScheme.getCardTypeByCardNumber(null)).isEqualTo(null);
	}

}
