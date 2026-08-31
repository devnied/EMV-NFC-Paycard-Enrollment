package com.github.devnied.emvnfccard;

import java.util.ArrayList;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * A reader reads everything the card agrees to answer: an application that
 * exposes no account number is still read as far as it goes, and a card
 * without payment directory has every AID of the list tried.
 */
public class ReadEverythingTest {

	/**
	 * SELECT of the PPSE
	 */
	private static final String SELECT_PPSE = "00A404000E325041592E5359532E444446303100";

	/**
	 * GET PROCESSING OPTIONS with an empty command template
	 */
	private static final String GPO = "80A8000002830000";

	/**
	 * A payment directory listing a single MasterCard application
	 */
	private static final String PPSE_FCI = "6F 2F 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 1D BF 0C 1A"
			+ " 61 18 4F 07 A0 00 00 00 04 10 10 50 0A 4D 41 53 54 45 52 43 41 52 44 87 01 01 90 00";

	/**
	 * The File Control Information of that application: a label, a language
	 * preference, a code table and a Log Entry (SFI 11, 10 records)
	 */
	private static final String APPLICATION_FCI = "6F 28 84 07 A0 00 00 00 04 10 10 A5 1D 50 0A 4D 41 53 54 45 52 43 41 52 44"
			+ " 5F 2D 02 65 6E 9F 11 01 01 BF 0C 05 9F 4D 02 0B 0A 90 00";

	/**
	 * The application refuses the GET PROCESSING OPTIONS and holds no record
	 * 1 in the SFI 1: no account number can be read from it. Its label, its
	 * transaction log and its GET DATA are read all the same.
	 */
	@Test
	public void testAnApplicationWithoutAccountNumberIsStillRead() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on(SELECT_PPSE, PPSE_FCI)
				.on("00A4040007A000000004101000", APPLICATION_FCI)
				// Conditions of use not satisfied, then the fallback record is
				// answered by the default '6A83' of the provider
				.on(GPO, "6985")
				// Log Format: amount, currency, date and ATC
				.on("80CA9F4F00", "9F 4F 0B 9F 02 06 5F 2A 02 9A 03 9F 36 02 90 00")
				// A single transaction, 10.00 EUR on 2024-01-15
				.on("00B2015C00", "00 00 00 00 10 00 09 78 24 01 15 00 05 90 00")
				.on("80CA9F1700", "9F 17 01 03 90 00").on("80CA9F3600", "9F 36 02 00 2C 90 00");

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false)).build();
		EmvCard card = parser.readEmvCard();

		// No account number: the card is not readable, but not unknown either
		Assertions.assertThat(card.getCardNumber()).isNull();
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.LOCKED);
		Assertions.assertThat(card.getApplications()).hasSize(1);
		Application application = card.getApplications().get(0);
		Assertions.assertThat(application.getReadingStep()).isEqualTo(ApplicationStepEnum.SELECTED);
		// What the File Control Information tells
		Assertions.assertThat(application.getApplicationLabel()).isEqualTo("MASTERCARD");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(application.getAid())).isEqualTo("A0000000041010");
		Assertions.assertThat(application.getLanguagePreferences()).containsExactly("en");
		Assertions.assertThat(application.getIssuerCodeTableIndex()).isEqualTo(1);
		Assertions.assertThat(application.getAidScheme()).isEqualTo(EmvCardScheme.MASTER_CARD);
		// The transaction log, located by the Log Entry
		Assertions.assertThat(application.getTransactionLog()).isNotNull();
		Assertions.assertThat(application.getTransactionLog().isRead()).isTrue();
		Assertions.assertThat(application.getListTransactions()).hasSize(1);
		// The GET DATA
		Assertions.assertThat(application.getLeftPinTry()).isEqualTo(3);
		Assertions.assertThat(application.getTransactionCounter()).isEqualTo(44);
		Assertions.assertThat(provider.getReceived()).contains("80CA9F4F00", "00B2015C00", "80CA9F1700", "80CA9F3600");
		// The card scheme describes an account, none was read
		Assertions.assertThat(card.getType()).isNull();
	}

	/**
	 * A card without payment directory holds a Visa application and a CB one,
	 * both answering their RID. The whole list of AIDs is tried, and the AIDs
	 * of an application already read are not selected again.
	 */
	@Test
	public void testEveryAidIsTriedWithoutPaymentDirectory() throws CommunicationException {
		ScriptedProvider provider = aidOnlyCard();

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build();
		EmvCard card = parser.readEmvCard();

		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Assertions.assertThat(card.getApplications()).hasSize(2);
		Assertions.assertThat(aids(card)).contains("A0000000031010", "A0000000421010");
		// The account is the one of the first application read
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getType()).isEqualTo(EmvCardScheme.VISA);
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		// The CB RID was tried after the Visa application was found
		Assertions.assertThat(provider.getReceived()).contains("00A4040005A00000004200");
		// The full AID of the Visa application read through its RID is not
		// selected again
		Assertions.assertThat(provider.getReceived()).excludes("00A4040007A000000003101000");
	}

	/**
	 * When a single application is asked for, the lookup by AID still stops
	 * at the first one found.
	 */
	@Test
	public void testTheFirstAidIsEnoughWhenASingleApplicationIsAskedFor() throws CommunicationException {
		ScriptedProvider provider = aidOnlyCard();

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider).setConfig(
				EmvTemplate.Config().setReadAt(false).setReadTransactions(false).setReadAllAids(false)).build();
		EmvCard card = parser.readEmvCard();

		Assertions.assertThat(card.getApplications()).hasSize(1);
		Assertions.assertThat(aids(card)).containsExactly("A0000000031010");
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(provider.getReceived()).excludes("00A4040005A00000004200");
	}

	/**
	 * Build the card without payment directory: a Visa and a CB application
	 * behind their RID, both answering the same processing options and the
	 * same record
	 *
	 * @return the scripted provider
	 */
	private static ScriptedProvider aidOnlyCard() {
		return new ScriptedProvider().on(SELECT_PPSE, "6A82")
				.on("00A4040005A00000000300", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				.on("00A4040005A00000004200", "6F 0F 84 07 A0 00 00 00 42 10 10 A5 04 50 02 43 42 90 00")
				.on(GPO, "80 06 3C 00 08 01 01 00 90 00")
				.on("00B2010C00", "70 12 57 10 49 99 99 99 99 99 99 99 D1 50 92 01 00 00 00 0F 90 00");
	}

	/**
	 * Names of the applications of a card, hexadecimal
	 *
	 * @param pCard
	 *            card read
	 * @return the list of AID
	 */
	private static List<String> aids(final EmvCard pCard) {
		List<String> ret = new ArrayList<String>();
		for (Application application : pCard.getApplications()) {
			ret.add(BytesUtils.bytesToStringNoSpace(application.getAid()));
		}
		return ret;
	}

}
