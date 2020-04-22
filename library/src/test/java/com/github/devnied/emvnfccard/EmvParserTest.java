package com.github.devnied.emvnfccard;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;
import com.github.devnied.emvnfccard.model.*;
import com.github.devnied.emvnfccard.model.enums.*;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.AbstractParser;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ExceptionProviderTest;
import com.github.devnied.emvnfccard.provider.ProviderSelectPaymentEnvTest;
import com.github.devnied.emvnfccard.provider.TestProvider;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;
import fr.devnied.bitlib.BytesUtils;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;


public class EmvParserTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmvParserTest.class);

	@Test
	public void testPPSEVisa() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpse")) //
				.setConfig(EmvTemplate.Config().setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(card.getApplications().get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(16);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getApplications().get(1).getApplicationLabel()).isEqualTo("VISA");
		Assertions.assertThat(card.getApplications().get(1).getLeftPinTry()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(1).getTransactionCounter()).isEqualTo(44);
		Assertions.assertThat(card.getApplications().get(1).getPriority()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(1).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(1).getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getApplications().get(1).getListTransactions().size()).isEqualTo(1);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testUnknownCard() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("UnknownCard")) //
				.setConfig(EmvTemplate.Config().setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.UNKNOWN);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderNullProvider() throws CommunicationException {

		EmvTemplate.Builder() //
		.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(false).setReadTransactions(false)) //
		.build();
	}

	@Test
	public void testPPSEVisaNoOptions() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpseNoOptions")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(false).setReadTransactions(false).setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(card.getApplications().get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isZero();
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testPPSEVisa3() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpse3")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isNull();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(0);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		Assertions.assertThat(card.getHolderLastname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("06/2018");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testPPSEVisaNullLog() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardNullTransaction")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(card.getApplications().get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(0);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getApplications().get(1).getApplicationLabel()).isEqualTo("VISA");
		Assertions.assertThat(card.getApplications().get(1).getLeftPinTry()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(1).getTransactionCounter()).isEqualTo(44);
		Assertions.assertThat(card.getApplications().get(1).getPriority()).isEqualTo(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(1).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(1).getListTransactions().size()).isEqualTo(0);
		Assertions.assertThat(card.getApplications().get(1).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testGetApplicationTemplate() throws Exception {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardNullTransaction")) //
				.build();

		List<Application> data = ReflectionTestUtils
				.invokeMethod(
						parser,
						"getApplicationTemplate",
						BytesUtils
						.fromString("6F 57 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 45 BF 0C 42 61 1B 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01 9F 2A 08 03 00 00 00 00 00 00 00 61 23 4F 07 A0 00 00 00 03 10 10 50 0A 56 49 53 41 20 44 45 42 49 54 87 01 02 9F 2A 08 03 00 00 00 00 00 00 00"));
		Assertions.assertThat(data).isNotNull();
		Assertions.assertThat(data.size()).isEqualTo(2);
		Assertions.assertThat(data.get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(BytesUtils.bytesToString(data.get(0).getAid())).isEqualTo("A0 00 00 00 42 10 10");
		Assertions.assertThat(data.get(1).getApplicationLabel()).isEqualTo("VISA DEBIT");
		Assertions.assertThat(BytesUtils.bytesToString(data.get(1).getAid())).isEqualTo("A0 00 00 00 03 10 10");

		data = ReflectionTestUtils
				.invokeMethod(
						parser,
						"getApplicationTemplate",
						BytesUtils
						.fromString("6F57840E325041592E5359532E4444463031A545BF0C4261104F07A0000000421010500243428701019F2A08030000000000000061184F07A0000000031010500A564953412044454249548701029F2A0803000000000000009000"));
		Assertions.assertThat(data).isNotNull();
		Assertions.assertThat(data.size()).isEqualTo(2);
		Assertions.assertThat(data.get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(BytesUtils.bytesToString(data.get(0).getAid())).isEqualTo("A0 00 00 00 42 10 10");
		Assertions.assertThat(data.get(1).getApplicationLabel()).isEqualTo("VISA DEBIT");
		Assertions.assertThat(BytesUtils.bytesToString(data.get(1).getAid())).isEqualTo("A0 00 00 00 03 10 10");

		data = ReflectionTestUtils
				.invokeMethod(
						parser,
						"getApplicationTemplate",
						BytesUtils
						.fromString("6F 2C 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 1A BF 0C 17 61 15 4F 07 A0 00 00 02 77 10 10 50 07 49 6E 74 65 72 61 63 87 01 01"));
		Assertions.assertThat(data).isNotNull();
		Assertions.assertThat(data.size()).isEqualTo(1);
		Assertions.assertThat(data.get(0).getApplicationLabel()).isEqualTo("Interac");
		Assertions.assertThat(BytesUtils.bytesToString(data.get(0).getAid())).isEqualTo("A0 00 00 02 77 10 10");

		data = ReflectionTestUtils
				.invokeMethod(
						parser,
						"getApplicationTemplate",
						BytesUtils
						.fromString("6F 2C 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 1A BF 0C 17 61 15 41 07 A0 00 00 02 77 10 10 50 07 49 6E 74 65 72 61 63 87 01 01"));
		Assertions.assertThat(data).isNotNull();
		Assertions.assertThat(data.size()).isEqualTo(0);
	}

	@Test
	public void testPPSEMasterCard() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("MasterCardPpse")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(card.getApplications().get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(0);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getApplications().get(1).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(card.getApplications().get(1).getLeftPinTry()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(1).getTransactionCounter()).isEqualTo(44);
		Assertions.assertThat(card.getApplications().get(1).getPriority()).isEqualTo(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(1).getAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(card.getApplications().get(1).getListTransactions().size()).isEqualTo(0);
		Assertions.assertThat(card.getApplications().get(1).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5599999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testPPSEInteract() throws CommunicationException {
		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("InteractPpse")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getBic()).isNull();
		Assertions.assertThat(card.getIban()).isNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("INTERAC");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000002771010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(0);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5200000000000000");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.INTERAC);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("11/2019");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testPPSEGeldKarte() throws CommunicationException {

		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyy");

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("GeldKartePpse")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();


		if (card != null) {
			LOGGER.debug(card.toString());
		}

		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getBic()).isEqualTo("NOLADE21SHO");
		Assertions.assertThat(card.getIban()).isEqualTo("DE01130510300000000000");
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNull();
		Assertions.assertThat(card.getTrack2().getService()).isNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getAmount()).isEqualTo(6.83f);
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("girocard");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("D27600002547410100");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().get(0).getAmount()).isEqualTo(180);
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().get(0).getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().get(0).getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(date.format(card.getApplications().get(0).getListTransactions().get(0).getDate())).isEqualTo("06/07/2001");
		Assertions.assertThat(time.format(card.getApplications().get(0).getListTransactions().get(0).getTime())).isEqualTo("10:47:27");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("1500001111");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.GELDKARTE);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("12/2017");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testPPSEMasterCard2() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("MasterCardPpse2")) //
				.setTerminal(new DefaultTerminalImpl()) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNotNull();
		Assertions.assertThat(card.getTrack1().getFormatCode()).isEqualTo("B");
		Assertions.assertThat(card.getTrack1().getRaw()).isNotEmpty();
		Assertions.assertThat(sdf.format(card.getTrack1().getExpireDate())).isEqualTo("07/2002");
		Assertions.assertThat(card.getTrack1().getCardNumber()).isEqualTo("5200000000000000");
		Assertions.assertThat(card.getTrack1().getHolderFirstname()).isNull();
		Assertions.assertThat(card.getTrack1().getHolderLastname()).isNull();
		Assertions.assertThat(card.getTrack1().getService()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1()).isEqualTo(ServiceCode1Enum.INTERNATIONNAL_ICC);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getInterchange()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode1().getTechnology()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2()).isEqualTo(ServiceCode2Enum.NORMAL);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode2().getAuthorizationProcessing()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3()).isEqualTo(ServiceCode3Enum.GOODS_SERVICES);
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getAllowedServices()).isNotNull();
		Assertions.assertThat(card.getTrack1().getService().getServiceCode3().getPinRequirements()).isNotNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5200000000000000");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("MASTERCARD");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(10);
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("07/2002");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions()).isNotEmpty();
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(10);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		EmvTransactionRecord record = card.getApplications().get(0).getListTransactions().get(0);
		Assertions.assertThat(record.getAmount()).isEqualTo(2200);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.TRY);
		Assertions.assertThat(record.getDate()).isNotNull();
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Assertions.assertThat(sdf2.format(record.getDate())).isEqualTo("12/01/2011");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));

	}

	@Test
	public void testPPSEMasterCard3() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("MasterCardPpse3")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5200000000000000");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.MASTER_CARD);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("MasterCard");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("11/2019");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(10);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		EmvTransactionRecord record = card.getApplications().get(0).getListTransactions().get(0);
		Assertions.assertThat(record.getAmount()).isEqualTo(2200);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.TRY);
		Assertions.assertThat(record.getDate()).isNotNull();
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy");
		Assertions.assertThat(sdf2.format(record.getDate())).isEqualTo("12/01/2011");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));

	}

	@Test
	public void testPPSEVisa2() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpse2")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("CB");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(card.getApplications().get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(16);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getApplications().get(1).getApplicationLabel()).isEqualTo("VISA");
		Assertions.assertThat(card.getApplications().get(1).getLeftPinTry()).isEqualTo(2);
		Assertions.assertThat(card.getApplications().get(1).getTransactionCounter()).isEqualTo(44);
		Assertions.assertThat(card.getApplications().get(1).getPriority()).isEqualTo(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(1).getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getApplications().get(1).getListTransactions().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(1).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testPSE() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPse")) //
				.setConfig(EmvTemplate.Config().setContactLess(false).setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("TRANSACTION CB");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getTransactionCounter()).isEqualTo(1580);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4979670123453600");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("02/2016");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions()).isNotNull();
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(16);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		EmvTransactionRecord record = card.getApplications().get(0).getListTransactions().get(0);
		Assertions.assertThat(record.getAmount()).isEqualTo(4000);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("80");
		Assertions.assertThat(record.getTransactionType()).isEqualTo(TransactionTypeEnum.PURCHASE);
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.EUR);
		Assertions.assertThat(record.getTerminalCountry()).isEqualTo(CountryCodeEnum.FR);
		Assertions.assertThat(record.getDate()).isNotNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testAid() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("FailPpseVisaAid")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadAt(true).setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("5772829193253472");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getApplications().size()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("VISA");
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("08/2014");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(Arrays.asList("CB Visa Banque Populaire (France)"));
	}

	@Test
	public void testException() throws CommunicationException {
		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new ExceptionProviderTest()) //
				.setConfig(EmvTemplate.Config()) //
				.build();
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

		EmvParser parser = new EmvParser(null);

		List<Afl> list = ReflectionTestUtils
				.invokeMethod(parser, "extractAfl",
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
	public void testSelectPaymentEnvironment() throws Exception {

		ProviderSelectPaymentEnvTest prov = new ProviderSelectPaymentEnvTest();

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(prov) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		prov.setExpectedData("00A404000E325041592E5359532E444446303100");
		ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");

		parser = EmvTemplate.Builder() //
				.setProvider(prov) //
				.setConfig(EmvTemplate.Config().setContactLess(false))//
				.build();
		prov.setExpectedData("00A404000E315041592E5359532E444446303100");

		ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
	}

	@Test
	public void testExtractApplicationLabel() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new ProviderSelectPaymentEnvTest()) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();

		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);

		String value = ReflectionTestUtils.invokeMethod(
						parser,
						"extractApplicationLabel",
						BytesUtils
						.fromString("6F 3B 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 29 BF 0C 26 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01 61 12 4F 07 A0 00 00 00 03 10 10 50 04 56 49 53 41 87 01 02 90 00"));
		Assertions.assertThat(value).isEqualTo("CB");
		value = ReflectionTestUtils.invokeMethod(parser, AbstractParser.class, "extractApplicationLabel", (byte[]) null);
		Assertions.assertThat(value).isEqualTo(null);
	}

	@Test
	public void testSelectAID() throws Exception {
		ProviderSelectPaymentEnvTest prov = new ProviderSelectPaymentEnvTest();
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(prov) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();

		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);

		prov.setExpectedData("00A4040007A000000042101000");
		ReflectionTestUtils.invokeMethod(parser, "selectAID", BytesUtils.fromString("A0000000421010"));
		prov.setExpectedData("00A4040000");
		ReflectionTestUtils.invokeMethod(parser, AbstractParser.class, "selectAID", (byte[])null);
	}

	@Test
	public void testgetLeftPinTry() throws Exception {
		ProviderSelectPaymentEnvTest prov = new ProviderSelectPaymentEnvTest();
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(prov) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();

		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);

		prov.setExpectedData("80CA9F1700");
		prov.setReturnedData("9F 17 01 03 90 00");
		int val = (int) ReflectionTestUtils.invokeMethod(parser, "getLeftPinTry");
		Assertions.assertThat(val).isEqualTo(3);

		prov.setExpectedData("80CA9F1700");
		prov.setReturnedData("90 00");
		val = (int) ReflectionTestUtils.invokeMethod(parser, "getLeftPinTry");
		Assertions.assertThat(val).isEqualTo(EmvParser.UNKNOW);

		prov.setReturnedData(null);
		val = (int) ReflectionTestUtils.invokeMethod(parser, "getLeftPinTry");
		Assertions.assertThat(val).isEqualTo(EmvParser.UNKNOW);

		prov.setReturnedData("8090");
		val = (int) ReflectionTestUtils.invokeMethod(parser, "getLeftPinTry");
		Assertions.assertThat(val).isEqualTo(EmvParser.UNKNOW);
	}

	@Test
	public void testgetLogFormat() throws Exception {
		ProviderSelectPaymentEnvTest prov = new ProviderSelectPaymentEnvTest();
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(prov) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);
		prov.setExpectedData("80CA9F4F00");
		prov.setReturnedData("9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00");
		List<TagAndLength> list = (List<TagAndLength>) ReflectionTestUtils.invokeMethod(parser, "getLogFormat");
		Assertions.assertThat(list.size()).isEqualTo(6);

		prov.setExpectedData("80CA9F4F00");
		prov.setReturnedData("0000");
		list = (List<TagAndLength>) ReflectionTestUtils.invokeMethod(parser, "getLogFormat");
		Assertions.assertThat(list.size()).isEqualTo(0);

		prov.setExpectedData("80CA9F4F00");
		prov.setReturnedData("9000");
		list = (List<TagAndLength>) ReflectionTestUtils.invokeMethod(parser, "getLogFormat");
		Assertions.assertThat(list.size()).isEqualTo(0);
	}

	@Test
	public void testGetLogEntry() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new ProviderSelectPaymentEnvTest()) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);
		byte[] selectResponse = BytesUtils
				.fromString("6F 37 84 07 A0 00 00 00 42 10 10 A5 2C 9F 38 18 9F 66 04 9F 02 06 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 0E DF 62 02 0B 1E DF 61 01 03 9F 4D 02 0B 11 90 00");
		byte[] data = (byte[]) ReflectionTestUtils.invokeMethod(parser, "getLogEntry", selectResponse);

		selectResponse = BytesUtils
				.fromString("6F 32 84 07 A0 00 00 00 42 10 10 A5 27 9F 38 18 9F 66 04 9F 02 06 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 09 DF 60 02 0B 1E DF 61 01 03 90 00");
		data = (byte[]) ReflectionTestUtils.invokeMethod(parser, "getLogEntry", selectResponse);
		Assertions.assertThat(BytesUtils.bytesToString(data)).isEqualTo("0B 1E");
	}

	@Test
	public void testReadWithAid() throws Exception {
		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardAid")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadAt(false)) //
				.build();
		ReflectionTestUtils.invokeMethod(parser, "readWithAID");
		EmvCard card = parser.getCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRaw()).isNotNull();
		Assertions.assertThat(card.getTrack2().getService()).isNotNull();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4000000000000000");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(card.getApplications().get(0).getApplicationLabel()).isEqualTo("VISA");
		Assertions.assertThat(card.getApplications().get(0).getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(card.getApplications().get(0).getListTransactions()).isNotNull();
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(16);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2014");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo(null);
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(null);
	}

	@Test
	public void testReadCpcl() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardPpseNoCplc")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(false)) //
				.build();
		EmvCard card = template.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getCplc()).isNull();
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
	}

	@Test
	public void testextractCardHolderNameNull() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardAid")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);
		ReflectionTestUtils.invokeMethod(parser, "extractCardHolderName", BytesUtils.fromString("5F 20 02 20 2F"));
		EmvCard card = template.getCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
	}

	@Test
	public void testextractCardHolderNameEmpty() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardAid")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);
		ReflectionTestUtils.invokeMethod(parser, "extractCardHolderName", BytesUtils.fromString("5F 20 02 20 20"));
		EmvCard card = template.getCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
	}

	@Test
	public void testextractCardHolderName() throws Exception {
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(new TestProvider("VisaCardAid")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);
		ReflectionTestUtils.invokeMethod(parser, "extractCardHolderName", BytesUtils.fromString("5F2008446F652F4A6F686E"));
		EmvCard card = template.getCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getHolderLastname()).isEqualTo("Doe");
		Assertions.assertThat(card.getHolderFirstname()).isEqualTo("John");
	}

	@Test
	public void testTransactionCounter() throws Exception {
		ProviderSelectPaymentEnvTest prov = new ProviderSelectPaymentEnvTest();
		EmvTemplate template = EmvTemplate.Builder() //
				.setProvider(prov) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true)) //
				.build();
		EmvParser parser = (EmvParser) template.getParsers().get(template.getParsers().size() - 1);
		prov.setExpectedData("80CA9F3600");
		prov.setReturnedData("9F 36 02 06 2C 90 00");
		int ret = (int) ReflectionTestUtils.invokeMethod(parser, "getTransactionCounter");
		Assertions.assertThat(ret).isEqualTo(1580);

		prov.setReturnedData("9F 36 02 FF FF 90 00");
		ret = (int) ReflectionTestUtils.invokeMethod(parser, "getTransactionCounter");
		Assertions.assertThat(ret).isEqualTo(65535);

		prov.setReturnedData("0000");
		ret = (int) ReflectionTestUtils.invokeMethod(parser, "getTransactionCounter");
		Assertions.assertThat(ret).isEqualTo(EmvParser.UNKNOW);

		prov.setReturnedData("9000");
		ret = (int) ReflectionTestUtils.invokeMethod(parser, "getTransactionCounter");
		Assertions.assertThat(ret).isEqualTo(EmvParser.UNKNOW);
	}

	@Test
	public void testLockedCard() throws Exception {
		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("LockedCard")) //
				.setConfig(EmvTemplate.Config().setContactLess(true).setReadAllAids(true).setReadTransactions(true).setReadCplc(true)) //
				.build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.LOCKED);
	}

	@Test
	public void testFailErrorCard() throws Exception {
		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("FailErrorCard")) //
				.setConfig(EmvTemplate.Config().setReadCplc(true)).build();
		EmvCard card = parser.readEmvCard();

		if (card != null) {
			LOGGER.debug(card.toString());
		}
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.LOCKED);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.SELECTED);
		Assertions.assertThat(card.getApplications().get(1).getReadingStep()).isEqualTo(ApplicationStepEnum.NOT_SELECTED);
	}
}
