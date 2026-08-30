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
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;
import fr.devnied.bitlib.BytesUtils;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

		// The raw ATS, the CPLC exchange and where the type came from
		Assertions.assertThat(card.getRawAt()).isEqualTo(BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00"));
		Assertions.assertThat(card.isAtLookedUp()).isTrue();
		Assertions.assertThat(card.getRawCplcResponse()).hasSize(47);
		Assertions.assertThat(card.getCplcStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.getCplc()).isNotNull();
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getTypeAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.isReadWithAid()).isFalse();
		Assertions.assertThat(card.getLockedStatusWord()).isNull();
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.getUnreadDdfNames()).isEmpty();
		Assertions.assertThat(card.getIgnoredDirectoryEntries()).isEmpty();
		Assertions.assertThat(card.getUnparsedTrack1()).isNull();
		Assertions.assertThat(card.getUnparsedTrack2()).isNull();
		// The PPSE: no directory file, its entries sit in the FCI
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(1);
		PaymentDirectory directory = card.getPaymentDirectories().get(0);
		Assertions.assertThat(new String(directory.getName())).isEqualTo("2PAY.SYS.DDF01");
		Assertions.assertThat(directory.getDepth()).isZero();
		Assertions.assertThat(directory.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(directory.getRawFci())).startsWith("6F3B840E");
		Assertions.assertThat(directory.getRawSfi()).isNull();
		Assertions.assertThat(directory.getSfi()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(directory.isSfiValid()).isFalse();
		Assertions.assertThat(directory.getRecords()).isEmpty();
		Assertions.assertThat(directory.getEndStatusWord()).isNull();
		Assertions.assertThat(directory.isRecordLimitReached()).isFalse();
		Assertions.assertThat(directory.getSubDirectoryNames()).isEmpty();
		Assertions.assertThat(directory.getLanguagePreferences()).isEmpty();
		Assertions.assertThat(directory.getIssuerCodeTableIndex()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(directory.getDataObjects().keySet()).containsOnly("84");
		// The selection and the GPO exchange of the first application
		Application cb = card.getApplications().get(0);
		Assertions.assertThat(cb.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRequestedAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getDedicatedFileName())).isEqualTo("A0000000421010");
		Assertions.assertThat(cb.getAidScheme()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRawFci())).startsWith("6F378407A0000000421010");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRawPdol()))
				.isEqualTo("9F66049F02069F03069F1A0295055F2A029A039C019F3704");
		Assertions.assertThat(cb.getPdol()).hasSize(9);
		// The first GPO answered '6700', the retry without the PDOL was parsed
		Assertions.assertThat(cb.isGpoRetriedWithoutPdol()).isTrue();
		Assertions.assertThat(cb.isGpoReplacedByReadRecord()).isFalse();
		Assertions.assertThat(cb.getGpoCommandTemplate()).isEqualTo(BytesUtils.fromString("8300"));
		Assertions.assertThat(cb.getPdolTerminalValues()).isEmpty();
		Assertions.assertThat(cb.getGpoStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRawGpoResponse())).startsWith("7738");
		Assertions.assertThat(cb.getRawAfl()).isNull();
		Assertions.assertThat(cb.getAfl()).isEmpty();
		Assertions.assertThat(cb.getRecords()).isEmpty();
		Assertions.assertThat(cb.getRecordsRead()).isZero();
		Assertions.assertThat(cb.getKernelType()).isNull();
		Assertions.assertThat(cb.isLanguagePreferencesFromDirectory()).isFalse();
		// The second application: both GPO failed, the record 1 of the SFI 1
		// was parsed instead
		Application visa = card.getApplications().get(1);
		Assertions.assertThat(visa.isGpoRetriedWithoutPdol()).isTrue();
		Assertions.assertThat(visa.isGpoReplacedByReadRecord()).isTrue();
		Assertions.assertThat(visa.getGpoStatusWord()).isEqualTo("9000");
		Assertions.assertThat(visa.getRecords()).hasSize(1);
		Assertions.assertThat(visa.getRecords().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(visa.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(visa.getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(visa.getAidScheme()).isEqualTo(EmvCardScheme.VISA);
		// The identification is persisted on the card and on its applications
		Assertions.assertThat(card.getIdentification()).isNotNull();
		Assertions.assertThat(card.getIdentification().getScheme()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(card.getIdentification().isSchemeFromAid()).isTrue();
		Assertions.assertThat(cb.getIdentification()).isNotNull();
		Assertions.assertThat(cb.getIdentification().getScheme()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(visa.getIdentification()).isNotNull();
		Assertions.assertThat(visa.getIdentification().getScheme()).isEqualTo(EmvCardScheme.VISA);
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
		// Looked up, unknown: the ATS was read and the lookup ran
		Assertions.assertThat(card.isAtLookedUp()).isTrue();
		Assertions.assertThat(card.getRawAt()).isNotNull();
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isEqualTo("6985");
		Assertions.assertThat(card.getPaymentDirectories()).isEmpty();
		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Assertions.assertThat(card.getCplcStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.getIdentification()).isNotNull();
		Assertions.assertThat(card.getIdentification().getConfidence()).isEqualTo(IdentificationConfidenceEnum.NONE);
	}

	@Test
	public void testAtNotLookedUp() throws CommunicationException {

		EmvTemplate parser = EmvTemplate.Builder() //
				.setProvider(new TestProvider("UnknownCard")) //
				.setConfig(EmvTemplate.Config().setReadCplc(true).setReadAt(false)) //
				.build();
		EmvCard card = parser.readEmvCard();

		Assertions.assertThat(card.isAtLookedUp()).isFalse();
		Assertions.assertThat(card.getRawAt()).isNull();
		Assertions.assertThat(card.getAt()).isNull();
		Assertions.assertThat(card.getAtrDescription()).isNull();
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

		// The status words of the GET DATA of the PIN Try Counter and of the
		// ATC land on the application
		Map<String, String> statusWords = card.getApplications().get(0).getGetDataStatusWords();
		Assertions.assertThat(statusWords.get("9F17")).isEqualTo("9000");
		Assertions.assertThat(statusWords.get("9F36")).isEqualTo("9000");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("06/2018");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

		// The terminal data sent for each PDOL entry, the first GPO succeeded
		Application app = card.getApplications().get(0);
		Assertions.assertThat(app.isGpoRetriedWithoutPdol()).isFalse();
		Assertions.assertThat(new java.util.ArrayList<String>(app.getPdolTerminalValues().keySet())).containsExactly("9F66", "9F02", "9F37", "5F2A");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getPdolTerminalValues().get("5F2A"))).isEqualTo("0978");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getGpoCommandTemplate())).startsWith("8310");
		Assertions.assertThat(app.getGpoCommandTemplate()).hasSize(18);
		// The FCI Issuer Discretionary Data of the PPSE holds a proprietary
		// template, its content is collected
		Assertions.assertThat(card.getPaymentDirectories().get(0).getDataObjects().keySet()).containsOnly("84", "DF20");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");
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
		// The Kernel Identifier is decoded on the application as on the entry
		Assertions.assertThat(data.get(0).getKernelType()).isEqualTo(KernelTypeEnum.INTERNATIONAL);
		Assertions.assertThat(data.get(0).getRequestedKernelId()).isEqualTo(new byte[] { 0x03 });
		Assertions.assertThat(data.get(0).getRawShortKernelId()).isEqualTo(3);
		Assertions.assertThat(data.get(0).getShortKernelId()).isEqualTo(3);
		Assertions.assertThat(data.get(0).getDirectoryEntry().getKernelType()).isEqualTo(KernelTypeEnum.INTERNATIONAL);
		Assertions.assertThat(data.get(0).getDirectoryEntry().getDataObjects().keySet()).containsOnly("4F", "50", "87", "9F2A");
		Assertions.assertThat(data.get(0).getDirectoryEntry().getRaw()).hasSize(0x1B);

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
		// The entry describes nothing, it is kept apart from the directory
		// entries
		Assertions.assertThat(parser.getCard().getIgnoredDirectoryEntries()).hasSize(1);
		Assertions.assertThat(parser.getCard().getIgnoredDirectoryEntries().get(0).getApplicationLabel()).isEqualTo("Interac");
		Assertions.assertThat(parser.getCard().getIgnoredDirectoryEntries().get(0).getDataObjects().keySet())
				.containsOnly("41", "50", "87");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

		// The type comes from the CB AID, replaced by the scheme of the PAN
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getTypeAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(card.getIdentification().getScheme()).isEqualTo(EmvCardScheme.CB);
		Assertions.assertThat(card.getIdentification().isSchemeFromAid()).isTrue();
		// The Application File Locator of the first application, and the
		// records it triggered
		Application cb = card.getApplications().get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRawAfl())).isEqualTo("100101011801010020010200");
		Assertions.assertThat(cb.getAfl()).hasSize(3);
		Assertions.assertThat(cb.getAfl().get(0).getSfi()).isEqualTo(2);
		Assertions.assertThat(cb.getAfl().get(0).getOfflineAuthenticationRecords()).isEqualTo(1);
		Assertions.assertThat(cb.getAfl().get(2).getLastRecord()).isEqualTo(2);
		Assertions.assertThat(cb.getRecordsRead()).isEqualTo(4);
		Assertions.assertThat(cb.getRecords()).hasSize(4);
		Assertions.assertThat(cb.getRecords().get(0).getSfi()).isEqualTo(2);
		Assertions.assertThat(cb.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(cb.getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRecords().get(0).getRaw())).startsWith("7738");
		Assertions.assertThat(cb.getRecords().get(1).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(cb.getRecords().get(1).getRaw()).isEmpty();
		Assertions.assertThat(cb.isAflTruncated()).isFalse();
		// The entries carry the unknown tag '9F28', it is collected
		Assertions.assertThat(card.getDirectoryEntries()).hasSize(2);
		Assertions.assertThat(card.getDirectoryEntries().get(0).getDataObjects().keySet()).containsOnly("4F", "87", "50", "9F28");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getDirectoryEntries().get(0).getDataObjects().get("9F28")))
				.isEqualTo("400200");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("11/2019");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

		// The card refused the GET DATA of the ATC, the status word says so
		Map<String, String> statusWords = card.getApplications().get(0).getGetDataStatusWords();
		Assertions.assertThat(statusWords.get("9F17")).isEqualTo("9000");
		Assertions.assertThat(statusWords.get("9F36")).isEqualTo("6985");
		// The selection of this card ended with a warning, recorded as such
		Assertions.assertThat(card.getApplications().get(0).getSelectStatusWord()).isEqualTo("6285");
		Assertions.assertThat(card.getApplications().get(0).isSelectWarning()).isTrue();
	}

	@Test
	public void testPPSEGeldKarte() throws CommunicationException {

		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss", Locale.US);
		SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyy", Locale.US);

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
		// The card announces the girocard AID D27600002547410100 and the
		// application label "girocard", it is a girocard carrying a GeldKarte
		// purse and not a plain GeldKarte
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.GIROCARD);
		Assertions.assertThat(card.getType().getCountry()).isEqualTo(CountryCodeEnum.DE);
		Assertions.assertThat(card.getType().isDomestic()).isTrue();
		Assertions.assertThat(card.getHolderLastname()).isNull();
		Assertions.assertThat(card.getHolderFirstname()).isNull();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("12/2017");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
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
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf2.format(record.getDate())).isEqualTo("12/01/2011");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("11/2019");
		Assertions.assertThat(card.getApplications().get(0).getListTransactions().size()).isEqualTo(10);
		Assertions.assertThat(card.getApplications().get(0).getReadingStep()).isEqualTo(ApplicationStepEnum.READ);
		EmvTransactionRecord record = card.getApplications().get(0).getListTransactions().get(0);
		Assertions.assertThat(record.getAmount()).isEqualTo(2200);
		Assertions.assertThat(record.getCyptogramData()).isEqualTo("40");
		Assertions.assertThat(record.getCurrency()).isEqualTo(CurrencyEnum.TRY);
		Assertions.assertThat(record.getDate()).isNotNull();
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Assertions.assertThat(sdf2.format(record.getDate())).isEqualTo("12/01/2011");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");

		// The details of the transaction log land on the application: the
		// Log Entry '0B 0A' names the SFI 11 and 10 records
		TransactionLog log = card.getApplications().get(0).getTransactionLog();
		Assertions.assertThat(log).isNotNull();
		Assertions.assertThat(log.getSfi()).isEqualTo(11);
		Assertions.assertThat(log.getDeclaredRecordCount()).isEqualTo(10);
		Assertions.assertThat(log.getRecords()).hasSize(10);
		Assertions.assertThat(log.isRead()).isTrue();
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2015");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");
	}

	/**
	 * The card of the VisaCardPse trace does not follow the specification: it
	 * serves its Payment System Directory at the record number '00', which ISO
	 * 7816-4 section 7.3.2 reserves ("Zero (encoded '00') is reserved for
	 * special purposes"), and answers '6985' at record 1.
	 * <p>
	 * EMV 4.3 and 4.4 Book 1 section 12.3.2 step 2 has the terminal read the
	 * directory "beginning with record number 1", so a conforming read receives
	 * the '6985' straight away and never reaches the directory entry. Step 1
	 * then sends the terminal to the List of AIDs, which this card does not
	 * answer either. The two responses below are the ones the card really gave,
	 * no pairing was invented, and the record '00' is deliberately left
	 * unscripted so that asking for it would fail this test.
	 * </p>
	 */
	@Test
	public void testPSE() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider() //
				.on("00 A4 04 00 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00",
						"6F 1E 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 0C 88 01 01 9F 11 01 01 5F 2D 02 66 72 90 00") //
				.on("00 B2 01 0C 00", "69 85");

		EmvCard card = EmvTemplate.Builder() //
				.setProvider(provider) //
				.setConfig(EmvTemplate.Config().setContactLess(false)) //
				.build().readEmvCard();

		Assertions.assertThat(provider.getReceived()).contains("00B2010C00");
		Assertions.assertThat(provider.getReceived()).excludes("00B2000C00");
		Assertions.assertThat(card).isNotNull();
		Assertions.assertThat(card.getApplications()).isEmpty();

		// The directory FCI carries the language preference and the code table
		// of the card, and the reading ended on an unexpected status
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(1);
		PaymentDirectory directory = card.getPaymentDirectories().get(0);
		Assertions.assertThat(new String(directory.getName())).isEqualTo("1PAY.SYS.DDF01");
		Assertions.assertThat(directory.getLanguagePreferences()).containsExactly("fr");
		Assertions.assertThat(directory.getIssuerCodeTableIndex()).isEqualTo(1);
		Assertions.assertThat(directory.getRawSfi()).isEqualTo(BytesUtils.fromString("01"));
		Assertions.assertThat(directory.getSfi()).isEqualTo(1);
		Assertions.assertThat(directory.isSfiValid()).isTrue();
		Assertions.assertThat(directory.getRecords()).hasSize(1);
		Assertions.assertThat(directory.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(directory.getRecords().get(0).getStatusWord()).isEqualTo("6985");
		Assertions.assertThat(directory.getRecords().get(0).getRaw()).isEmpty();
		Assertions.assertThat(directory.getEndStatusWord()).isEqualTo("6985");
		Assertions.assertThat(directory.isRecordLimitReached()).isFalse();
		Assertions.assertThat(directory.getDataObjects().keySet()).containsOnly("84", "88", "9F11", "5F2D");
		// No application at all: locked without a status word to blame
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.LOCKED);
		Assertions.assertThat(card.getLockedStatusWord()).isNull();
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("08/2014");

		// The PPSE was refused, the card was read with the list of AIDs
		Assertions.assertThat(card.getPaymentEnvironmentStatusWord()).isEqualTo("6700");
		Assertions.assertThat(card.getPaymentDirectories()).isEmpty();
		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Application visa = card.getApplications().get(0);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRequestedAid())).isEqualTo("A000000003");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getDedicatedFileName())).isEqualTo("A0000000031010");
		Assertions.assertThat(visa.getAidScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(visa.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getTypeAid())).isEqualTo("A0000000031010");
		// The first GPO was answered, with the terminal values of the PDOL
		Assertions.assertThat(visa.isGpoRetriedWithoutPdol()).isFalse();
		Assertions.assertThat(visa.isGpoReplacedByReadRecord()).isFalse();
		Assertions.assertThat(visa.getPdol()).hasSize(9);
		Assertions.assertThat(new java.util.ArrayList<String>(visa.getPdolTerminalValues().keySet())).containsExactly("9F66", "9F02", "9F03", "9F1A", "95",
				"5F2A", "9A", "9C", "9F37");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getPdolTerminalValues().get("9F1A"))).isEqualTo("0250");
		Assertions.assertThat(visa.getGpoCommandTemplate()).hasSize(35);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getGpoCommandTemplate())).startsWith("8321");
		Assertions.assertThat(visa.getGpoStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRawGpoResponse())).startsWith("800E");
		// Format 1: the locator follows the interchange profile
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRawAfl())).isEqualTo("080101001001050018010201");
		Assertions.assertThat(visa.getAfl()).hasSize(3);
		Assertions.assertThat(visa.getRecordsRead()).isEqualTo(8);
		Assertions.assertThat(visa.getRecords()).hasSize(8);
		Assertions.assertThat(visa.getRecords().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(visa.getRecords().get(0).getRecordNumber()).isEqualTo(1);
		Assertions.assertThat(visa.getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRecords().get(0).getRaw())).startsWith("7035");
		Assertions.assertThat(visa.getRecords().get(1).getSfi()).isEqualTo(2);
		Assertions.assertThat(visa.getRecords().get(1).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(visa.getRecords().get(7).getSfi()).isEqualTo(3);
		Assertions.assertThat(visa.getRecords().get(7).getRecordNumber()).isEqualTo(2);
		Assertions.assertThat(visa.isAflTruncated()).isFalse();
		Assertions.assertThat(card.getIdentification().getScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(visa.getIdentification().getScheme()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo("3B6500002063CBA000");
		Assertions.assertThat(card.getAtrDescription()).contains("CB Visa Banque Populaire (France)");
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
		SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy", Locale.US);
		Assertions.assertThat(sdf.format(card.getExpireDate())).isEqualTo("09/2014");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.getAt()).isEqualTo(null);
		Assertions.assertThat(card.getAtrDescription()).isEqualTo(null);

		// No PDOL: an empty command template, answered at the first GPO
		Application visa = card.getApplications().get(0);
		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRequestedAid())).isEqualTo("A000000003");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getDedicatedFileName())).isEqualTo("A0000000031010");
		Assertions.assertThat(visa.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(visa.getRawPdol()).isNull();
		Assertions.assertThat(visa.getPdol()).isEmpty();
		Assertions.assertThat(visa.getPdolTerminalValues()).isEmpty();
		Assertions.assertThat(visa.getGpoCommandTemplate()).isEqualTo(BytesUtils.fromString("8300"));
		Assertions.assertThat(visa.isGpoRetriedWithoutPdol()).isFalse();
		Assertions.assertThat(visa.isGpoReplacedByReadRecord()).isFalse();
		Assertions.assertThat(visa.getGpoStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRawGpoResponse())).startsWith("8012");
		// The four entries of the locator, and the nine records they name
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRawAfl())).isEqualTo("08020200100102001004040018010503");
		Assertions.assertThat(visa.getAfl()).hasSize(4);
		Assertions.assertThat(visa.getAfl().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(visa.getAfl().get(0).getFirstRecord()).isEqualTo(2);
		Assertions.assertThat(visa.getAfl().get(0).getLastRecord()).isEqualTo(2);
		Assertions.assertThat(visa.getAfl().get(1).getSfi()).isEqualTo(2);
		Assertions.assertThat(visa.getAfl().get(1).getLastRecord()).isEqualTo(2);
		Assertions.assertThat(visa.getAfl().get(2).getFirstRecord()).isEqualTo(4);
		Assertions.assertThat(visa.getAfl().get(3).getSfi()).isEqualTo(3);
		Assertions.assertThat(visa.getAfl().get(3).getLastRecord()).isEqualTo(5);
		Assertions.assertThat(visa.getAfl().get(3).getOfflineAuthenticationRecords()).isEqualTo(3);
		Assertions.assertThat(visa.getRecordsRead()).isEqualTo(9);
		Assertions.assertThat(visa.getRecords()).hasSize(9);
		Assertions.assertThat(visa.getRecords().get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(visa.getRecords().get(0).getRecordNumber()).isEqualTo(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(visa.getRecords().get(0).getRaw())).startsWith("7035");
		Assertions.assertThat(visa.getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(visa.getRecords().get(1).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(visa.getRecords().get(8).getSfi()).isEqualTo(3);
		Assertions.assertThat(visa.getRecords().get(8).getRecordNumber()).isEqualTo(5);
		Assertions.assertThat(visa.isAflTruncated()).isFalse();
		Assertions.assertThat(card.getAidType()).isEqualTo(EmvCardScheme.VISA);
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
		// Not read at all: neither a response nor a status word
		Assertions.assertThat(card.getRawCplcResponse()).isNull();
		Assertions.assertThat(card.getCplcStatusWord()).isNull();
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
		// The status word that made the card locked is the one the highest
		// priority application answered to its SELECT
		Assertions.assertThat(card.getLockedStatusWord()).isEqualTo("6985");
		Assertions.assertThat(card.getApplications()).hasSize(2);
		Application cb = card.getApplications().get(0);
		Assertions.assertThat(cb.getSelectStatusWord()).isEqualTo("6985");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(cb.getRequestedAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(cb.getDedicatedFileName()).isNull();
		Assertions.assertThat(cb.getAidScheme()).isNull();
		Assertions.assertThat(cb.getRawFci()).isNull();
		// The Kernel Identifier of this capture sits outside the entries (tag
		// '61'), so the application carries none
		Assertions.assertThat(cb.getKernelType()).isNull();
		Assertions.assertThat(cb.getRequestedKernelId()).isNull();
		Assertions.assertThat(cb.getRawShortKernelId()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(card.getIdentification()).isNotNull();
		Assertions.assertThat(cb.getIdentification()).isNotNull();
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
