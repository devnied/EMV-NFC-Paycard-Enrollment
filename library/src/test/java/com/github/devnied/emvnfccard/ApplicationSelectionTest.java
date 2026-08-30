package com.github.devnied.emvnfccard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.PaymentDirectory;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.provider.ProviderSelectPaymentEnvTest;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;
import com.github.devnied.emvnfccard.provider.TestProvider;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * Application selection rules of EMV 4.3 Book 1 (sections 12.2 to 12.4) and of
 * EMV Contactless Book B.
 */
public class ApplicationSelectionTest {

	private EmvTemplate template(final ProviderSelectPaymentEnvTest pProvider) {
		return EmvTemplate.Builder().setProvider(pProvider).build();
	}

	/**
	 * The Application Priority Indicator holds the priority order in its bits 4
	 * to 1 and the cardholder confirmation flag in its bit 8, the whole byte is
	 * not a priority (EMV 4.3 Book 1 section 12.2.3).
	 */
	@Test
	public void testApplicationPriorityIndicator() {
		Application app = new Application();

		// '01' and '02' are the values the cards of the MasterCardPpse and
		// VisaCardPpse traces return
		app.setApplicationPriorityIndicator((byte) 0x01);
		Assertions.assertThat(app.getPriority()).isEqualTo(1);
		Assertions.assertThat(app.isCardholderConfirmationRequired()).isFalse();

		app.setApplicationPriorityIndicator((byte) 0x02);
		Assertions.assertThat(app.getPriority()).isEqualTo(2);
		Assertions.assertThat(app.isCardholderConfirmationRequired()).isFalse();

		// Bit 8 set: the application may not be selected without the
		// confirmation of the cardholder
		app.setApplicationPriorityIndicator((byte) 0x81);
		Assertions.assertThat(app.getPriority()).isEqualTo(1);
		Assertions.assertThat(app.isCardholderConfirmationRequired()).isTrue();

		// Bits 7 to 5 are RFU and must not leak into the priority
		app.setApplicationPriorityIndicator((byte) 0xF5);
		Assertions.assertThat(app.getPriority()).isEqualTo(5);
		Assertions.assertThat(app.isCardholderConfirmationRequired()).isTrue();

		// 0 means that no priority was assigned to the application
		app.setApplicationPriorityIndicator((byte) 0x00);
		Assertions.assertThat(app.getPriority()).isZero();
	}

	/**
	 * An application without priority is selected after the ones that have one.
	 */
	@Test
	public void testApplicationsAreOrderedByPriority() {
		Application noPriority = new Application();
		noPriority.setApplicationPriorityIndicator((byte) 0x00);
		Application first = new Application();
		first.setApplicationPriorityIndicator((byte) 0x01);
		Application second = new Application();
		second.setApplicationPriorityIndicator((byte) 0x02);

		List<Application> list = Arrays.asList(noPriority, second, first);
		Collections.sort(list);
		Assertions.assertThat(list).containsExactly(first, second, noPriority);
	}

	/**
	 * The Kernel Identifier (tag '9F2A') of a directory entry is kept, it tells
	 * on which kernel the card wants the application to be processed.
	 */
	@Test
	public void testKernelIdentifierIsRead() {
		EmvTemplate parser = template(new ProviderSelectPaymentEnvTest());
		// PPSE FCI holding a Kernel Identifier for both applications
		List<Application> data = ReflectionTestUtils.invokeMethod(parser, "getApplicationTemplate", BytesUtils.fromString(
				"6F 57 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 45 BF 0C 42 61 1B 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01 9F 2A 08 03 00 00 00 00 00 00 00 61 23 4F 07 A0 00 00 00 03 10 10 50 0A 56 49 53 41 20 44 45 42 49 54 87 01 02 9F 2A 08 03 00 00 00 00 00 00 00"));

		Assertions.assertThat(data).hasSize(2);
		Assertions.assertThat(data.get(0).getKernelIdentifier()).isEqualTo(BytesUtils.fromString("0300000000000000"));
		Assertions.assertThat(data.get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(data.get(1).getKernelIdentifier()).isEqualTo(BytesUtils.fromString("0300000000000000"));
		Assertions.assertThat(data.get(1).getPriority()).isEqualTo(2);
		// No Extended Selection in this directory
		Assertions.assertThat(data.get(0).getExtendedSelection()).isNull();
	}

	/**
	 * A directory entry may carry an Extended Selection (tag '9F29') whose
	 * value has to be appended to the ADF Name of the SELECT command.
	 */
	@Test
	public void testExtendedSelectionIsRead() {
		EmvTemplate parser = template(new ProviderSelectPaymentEnvTest());
		// Directory entry built with the structure of EMV Contactless Book B:
		// 61 (Application Template) holding 4F (AID), 50 (label), 87 (priority)
		// and 9F29 (Extended Selection)
		List<Application> data = ReflectionTestUtils.invokeMethod(parser, "getApplicationTemplate", BytesUtils.fromString(
				"6F 2C 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 1A BF 0C 17 61 15 4F 07 A0 00 00 00 04 10 10 50 02 4D 43 87 01 01 9F 29 02 12 34"));

		Assertions.assertThat(data).hasSize(1);
		Assertions.assertThat(data.get(0).getExtendedSelection()).isEqualTo(BytesUtils.fromString("1234"));
		Assertions.assertThat(data.get(0).getApplicationLabel()).isEqualTo("MC");
	}

	/**
	 * When the Extended Selection is present, the SELECT command carries the
	 * ADF Name followed by that value.
	 */
	@Test
	public void testExtendedSelectionIsAppendedToTheSelect() throws CommunicationException {
		// 00 A4 04 00 <Lc=09> A0000000041010 1234 00
		ScriptedProvider provider = new ScriptedProvider().on("00A4040009A00000000410101234 00", "6F0A8407A00000000410109000");
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000041010"));
		app.setExtendedSelection(BytesUtils.fromString("1234"));
		ReflectionTestUtils.invokeMethod(parser, "selectApplication", app);

		// The card accepted it, there is no second SELECT
		Assertions.assertThat(provider.getReceived()).containsExactly("00A4040009A0000000041010123400");

		// Without an Extended Selection the SELECT carries the ADF Name alone
		app.setExtendedSelection(null);
		ReflectionTestUtils.invokeMethod(parser, "selectApplication", app);
		Assertions.assertThat(provider.getReceived().get(1)).isEqualTo("00A4040007A000000004101000");
	}

	/**
	 * A card may advertise an Extended Selection and still refuse the
	 * concatenated ADF Name, the terminal has to fall back on the bare name
	 * instead of declaring the application unreadable.
	 */
	@Test
	public void testExtendedSelectionFallsBackToThePlainAid() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				// The concatenated name is refused
				.on("00A4040009A0000000041010123400", "6A82")
				// The ADF Name alone works
				.on("00A4040007A000000004101000", "6F0A8407A00000000410109000");
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider).build());

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000041010"));
		app.setExtendedSelection(BytesUtils.fromString("1234"));
		byte[] response = ReflectionTestUtils.invokeMethod(parser, "selectApplication", app);

		Assertions.assertThat(provider.getReceived()).containsExactly("00A4040009A0000000041010123400",
				"00A4040007A000000004101000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(response)).endsWith("9000");
	}

	/**
	 * The applications matching a partial AID are enumerated with the "next
	 * occurrence" option, P2 = '02' (EMV 4.3 Book 1 sections 11.3.2 and
	 * 12.3.2).
	 */
	@Test
	public void testNextOccurrenceUsesP2() throws CommunicationException {
		ProviderSelectPaymentEnvTest provider = new ProviderSelectPaymentEnvTest();
		EmvParser parser = new EmvParser(template(provider));

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000031010"));
		app.setNextOccurrence(true);

		provider.setExpectedData("00A4040207A000000003101000");
		provider.setReturnedData("6A82");
		ReflectionTestUtils.invokeMethod(parser, "selectApplication", app);
	}

	/**
	 * The records of a payment system directory are read from the record 1
	 * until the card answers '6A83' (EMV 4.3 Book 1 section 12.3.2). Replayed
	 * against a real capture of a UnionPay contact card: the provider asserts
	 * every command, so the test fails if the parser does not emit the very
	 * sequence the terminal of the capture sent.
	 */
	@Test
	public void testPaymentDirectoryIsReadFromTheFirstRecord() throws CommunicationException {
		EmvTemplate parser = EmvTemplate.Builder().setProvider(new TestProvider("UnionPayPseDirectory"))
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();
		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		Assertions.assertThat(apps).hasSize(3);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(0).getAid())).isEqualTo("A000000333010102");
		Assertions.assertThat(apps.get(0).getApplicationLabel()).isEqualTo("UICC CREDIT");
		Assertions.assertThat(apps.get(0).getPriority()).isEqualTo(1);
		Assertions.assertThat(apps.get(1).getApplicationLabel()).isEqualTo("UICC DEBIT");
		Assertions.assertThat(apps.get(2).getApplicationLabel()).isEqualTo("UICC QUASICREDIT");

		// The directory file and every READ RECORD sent to it, the one that
		// ended the reading included
		Assertions.assertThat(parser.getCard().getPaymentDirectories()).hasSize(1);
		PaymentDirectory directory = parser.getCard().getPaymentDirectories().get(0);
		Assertions.assertThat(new String(directory.getName())).isEqualTo("1PAY.SYS.DDF01");
		Assertions.assertThat(directory.getDepth()).isZero();
		Assertions.assertThat(directory.getSfi()).isEqualTo(1);
		Assertions.assertThat(directory.isSfiValid()).isTrue();
		Assertions.assertThat(directory.getRecords()).hasSize(4);
		for (int i = 0; i < 4; i++) {
			Assertions.assertThat(directory.getRecords().get(i).getSfi()).isEqualTo(1);
			Assertions.assertThat(directory.getRecords().get(i).getRecordNumber()).isEqualTo(i + 1);
		}
		Assertions.assertThat(directory.getRecords().get(0).getStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(directory.getRecords().get(0).getRaw())).startsWith("701C611A");
		Assertions.assertThat(directory.getRecords().get(3).getStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(directory.getRecords().get(3).getRaw()).isEmpty();
		Assertions.assertThat(directory.getEndStatusWord()).isEqualTo("6A83");
		Assertions.assertThat(directory.isRecordLimitReached()).isFalse();
		Assertions.assertThat(directory.getSubDirectoryNames()).isEmpty();
	}

	/**
	 * A record of the payment directory may reference a sub directory through a
	 * DDF Name (tag '9D'): its own records list applications that are invisible
	 * otherwise (EMV 4.3 Book 1 section 12.2.2).
	 */
	@Test
	public void testDirectoryDefinitionFileIsFollowed() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				// SELECT 1PAY.SYS.DDF01 -> the directory is in the SFI 1
				.on("00A404000E315041592E5359532E444446303100",
						"6F 15 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 03 88 01 01 90 00")
				// Record 1 of the SFI 1: one application and one sub directory
				.on("00B2010C00",
						"70 20 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01"
								+ " 61 0C 9D 07 A0 00 00 00 03 00 00 87 01 02 90 00")
				// SELECT of the DDF -> its directory is in the SFI 2
				.on("00A4040007A00000000300 0000", "6F 0E 84 07 A0 00 00 00 03 00 00 A5 03 88 01 02 90 00")
				// Record 1 of the SFI 2: the application only reachable there
				.on("00B2011400", "70 14 61 12 4F 07 A0 00 00 00 03 10 10 50 04 56 49 53 41 87 01 03 90 00");

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();
		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		Assertions.assertThat(apps).hasSize(2);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(0).getAid())).isEqualTo("A0000000421010");
		Assertions.assertThat(apps.get(1).getApplicationLabel()).isEqualTo("VISA");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(apps.get(1).getAid())).isEqualTo("A0000000031010");
		Assertions.assertThat(apps.get(1).getPriority()).isEqualTo(3);

		// The sub directory is selected only once every record of the parent
		// directory has been read, otherwise the following READ RECORD would
		// apply to the sub directory
		List<String> received = provider.getReceived();
		Assertions.assertThat(received.get(0)).startsWith("00A40400");
		// The records are numbered from 1 (EMV 4.3 Book 1 section 12.3.2)
		Assertions.assertThat(received.get(1)).isEqualTo("00B2010C00");
		Assertions.assertThat(received.get(2)).isEqualTo("00B2020C00");
		Assertions.assertThat(received.get(3)).isEqualTo("00A4040007A000000003000000");

		// Both directories are reported, in selection order
		EmvCard card = parser.getCard();
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(2);
		PaymentDirectory pse = card.getPaymentDirectories().get(0);
		Assertions.assertThat(pse.getDepth()).isZero();
		Assertions.assertThat(pse.getSfi()).isEqualTo(1);
		Assertions.assertThat(pse.getSubDirectoryNames()).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(pse.getSubDirectoryNames().get(0))).isEqualTo("A0000000030000");
		Assertions.assertThat(pse.getEndStatusWord()).isEqualTo("6A83");
		PaymentDirectory ddf = card.getPaymentDirectories().get(1);
		Assertions.assertThat(ddf.getDepth()).isEqualTo(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(ddf.getName())).isEqualTo("A0000000030000");
		Assertions.assertThat(ddf.getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(ddf.getRawFci())).isEqualTo("6F0E8407A0000000030000A503880102");
		Assertions.assertThat(ddf.getSfi()).isEqualTo(2);
		Assertions.assertThat(ddf.getRecords()).hasSize(2);
		Assertions.assertThat(ddf.getSubDirectoryNames()).isEmpty();
		Assertions.assertThat(card.getUnreadDdfNames()).isEmpty();
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isFalse();
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isFalse();
	}

	/**
	 * A directory tree that references itself must not loop forever.
	 */
	@Test
	public void testDirectoryDepthIsBounded() throws CommunicationException {
		// The sub directory of this card is its own parent
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E315041592E5359532E444446303100",
						"6F 15 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 03 88 01 01 90 00")
				.on("00B2010C00", "70 0E 61 0C 9D 07 A0 00 00 00 03 00 00 87 01 02 90 00")
				.on("00A4040007A000000003000000", "6F 0E 84 07 A0 00 00 00 03 00 00 A5 03 88 01 01 90 00");

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();
		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		List<Application> apps = ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		Assertions.assertThat(apps).isEmpty();
		int selects = 0;
		for (String command : provider.getReceived()) {
			if (command.startsWith("00A40400")) {
				selects++;
			}
		}
		// The directory is only selected once: bounding the depth alone would
		// still let mutually referencing directories be walked over and over
		Assertions.assertThat(selects).isEqualTo(2);
		// The second reference was not followed, and it is reported
		EmvCard card = parser.getCard();
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(2);
		Assertions.assertThat(card.getUnreadDdfNames()).hasSize(1);
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getUnreadDdfNames().get(0))).isEqualTo("A0000000030000");
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isFalse();
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isFalse();
	}

	/**
	 * A directory referenced by several records, or by several sub directories,
	 * is only read once and the total number of directories is bounded.
	 */
	@Test
	public void testDirectoryBreadthIsBounded() throws CommunicationException {
		// Every one of the 3 sub directories references the 3 of them
		String ddfRecord = "70 21 61 09 9D 07 A0 00 00 00 03 00 01 61 09 9D 07 A0 00 00 00 03 00 02"
				+ " 61 09 9D 07 A0 00 00 00 03 00 03 90 00";
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E315041592E5359532E444446303100",
						"6F 15 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 03 88 01 01 90 00")
				.on("00B2010C00", ddfRecord)
				.on("00A4040007A000000003000100", "6F 0E 84 07 A0 00 00 00 03 00 01 A5 03 88 01 01 90 00")
				.on("00A4040007A000000003000200", "6F 0E 84 07 A0 00 00 00 03 00 02 A5 03 88 01 01 90 00")
				.on("00A4040007A000000003000300", "6F 0E 84 07 A0 00 00 00 03 00 03 A5 03 88 01 01 90 00");

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setContactLess(false)).build();
		byte[] fci = ReflectionTestUtils.invokeMethod(parser, "selectPaymentEnvironment");
		ReflectionTestUtils.invokeMethod(parser, "parseFCIProprietaryTemplate", fci);

		int selects = 0;
		for (String command : provider.getReceived()) {
			if (command.startsWith("00A40400")) {
				selects++;
			}
		}
		// Without the visited set this would be 3 + 9 + 27 + 81 selects
		Assertions.assertThat(selects).isEqualTo(1 + 3);
		// Every reference that was not followed is reported: 2 at the PSE, 2
		// in the first DDF, 2 in the second, 3 in the third
		EmvCard card = parser.getCard();
		Assertions.assertThat(card.getPaymentDirectories()).hasSize(1 + 3);
		Assertions.assertThat(card.getUnreadDdfNames()).hasSize(9);
		Assertions.assertThat(card.isDirectoryCountLimitReached()).isFalse();
		Assertions.assertThat(card.isDirectoryDepthLimitReached()).isFalse();
		Assertions.assertThat(card.getPaymentDirectories().get(3).getDepth()).isEqualTo(3);
	}

	/**
	 * ISO 7816-4 only asks the card to answer '6A82' when it manages
	 * occurrences: a card that ignores P2='02' answers the same ADF forever and
	 * must not be read again and again.
	 */
	@Test
	public void testNextOccurrenceStopsOnADuplicateApplication() throws CommunicationException {
		// This card answers the very same FCI whatever the P2 is, and has no
		// PPSE so the AID list is used
		String fci = "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00";
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100", "6A82")
				.on("00A4040005A000000003 00", fci)
				.on("00A4040205A000000003 00", fci)
				// GPO, then the single record holding the track
				.on("80A80000028300 00", "80 06 3C 00 08 01 01 00 90 00")
				.on("00B2010C00", "70 12 57 10 49 99 99 99 99 99 99 99 D1 50 92 01 00 00 00 0F 90 00")
				.on("80CA9F1700", "9F 17 01 03 90 00").on("80CA9F3600", "9F 36 02 00 2C 90 00");

		EmvTemplate parser = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build();
		EmvCard card = parser.readEmvCard();

		// The application is reported once, not MAX_AID_OCCURRENCES + 1 times
		Assertions.assertThat(card.getApplications()).hasSize(1);
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		int selects = 0;
		for (String command : provider.getReceived()) {
			if (command.startsWith("00A40402")) {
				selects++;
			}
		}
		// A single "next occurrence" attempt is enough to notice the card does
		// not manage them
		Assertions.assertThat(selects).isEqualTo(1);
	}

	/**
	 * An application the issuer invalidated answers '6283' to the SELECT and
	 * has to be skipped (EMV 4.3 Book 1 section 12.4). '6A81' is read as a
	 * blocked card and skipped too, but without terminating the session: this
	 * library only extracts public data and a card may answer '6A81' for other
	 * reasons.
	 */
	@Test
	public void testBlockedApplicationIsDetected() throws CommunicationException {
		for (String sw : new String[] { "6A81", "6283" }) {
			ProviderSelectPaymentEnvTest provider = new ProviderSelectPaymentEnvTest();
			EmvParser parser = new EmvParser(template(provider));

			Application app = new Application();
			app.setAid(BytesUtils.fromString("A0000000031010"));
			provider.setExpectedData("00A4040007A000000003101000");
			provider.setReturnedData(sw);

			Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "parse", app)).isFalse();
			Assertions.assertThat(app.isBlocked()).isTrue();
			// The status word is reported with the AID that was asked for
			Assertions.assertThat(app.getSelectStatusWord()).isEqualTo(sw);
			Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRequestedAid())).isEqualTo("A0000000031010");
			Assertions.assertThat(app.getDedicatedFileName()).isNull();
		}

		// The two status words are told apart
		EmvParser parser = new EmvParser(template(new ProviderSelectPaymentEnvTest()));
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "isApplicationBlocked",
				BytesUtils.fromString("6283"))).isTrue();
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "isApplicationBlocked",
				BytesUtils.fromString("6A81"))).isFalse();
		Assertions.assertThat((Boolean) ReflectionTestUtils.invokeMethod(parser, "isCardBlocked",
				BytesUtils.fromString("6A81"))).isTrue();
		Assertions.assertThat(ResponseUtils.contains(BytesUtils.fromString("9000"), SwEnum.SW_6A81, SwEnum.SW_6283))
				.isFalse();
	}

	/**
	 * A '6A81' answered to a "next occurrence" SELECT only means that the card
	 * does not manage occurrences, the enumeration stops and the applications
	 * already read are kept.
	 */
	@Test
	public void testNextOccurrenceNotSupportedIsNotABlockedCard() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider()
				.on("00A404000E325041592E5359532E444446303100", "6A82")
				.on("00A4040005A00000000300", "6F 10 84 07 A0 00 00 00 03 10 10 A5 05 50 03 56 49 53 90 00")
				// The card does not know the "next occurrence" option
				.on("00A4040205A00000000300", "6A81")
				.on("80A8000002830000", "80 06 3C 00 08 01 01 00 90 00")
				.on("00B2010C00", "70 12 57 10 49 99 99 99 99 99 99 99 D1 50 92 01 00 00 00 0F 90 00")
				.on("80CA9F1700", "9F 17 01 03 90 00").on("80CA9F3600", "9F 36 02 00 2C 90 00");

		EmvCard card = EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setReadAt(false).setReadTransactions(false)).build().readEmvCard();

		// The application read before the failed enumeration is kept
		Assertions.assertThat(card.getApplications()).hasSize(1);
		Assertions.assertThat(card.getApplications().get(0).isBlocked()).isFalse();
		Assertions.assertThat(card.getCardNumber()).isEqualTo("4999999999999999");
		Assertions.assertThat(card.getState()).isEqualTo(CardStateEnum.ACTIVE);
		Assertions.assertThat(card.isReadWithAid()).isTrue();
		Assertions.assertThat(card.getLockedStatusWord()).isNull();
		Assertions.assertThat(card.getApplications().get(0).getSelectStatusWord()).isEqualTo("9000");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getApplications().get(0).getRequestedAid()))
				.isEqualTo("A000000003");
	}

	/**
	 * A reader that does not support the Extended Selection never sends the
	 * concatenated ADF Name.
	 */
	@Test
	public void testExtendedSelectionCanBeDisabled() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider().on("00A4040007A000000004101000", "6F0A8407A00000000410109000");
		EmvParser parser = new EmvParser(EmvTemplate.Builder().setProvider(provider)
				.setConfig(EmvTemplate.Config().setExtendedSelectionSupported(false)).build());

		Application app = new Application();
		app.setAid(BytesUtils.fromString("A0000000041010"));
		app.setExtendedSelection(BytesUtils.fromString("1234"));
		ReflectionTestUtils.invokeMethod(parser, "selectApplication", app);

		Assertions.assertThat(provider.getReceived()).containsExactly("00A4040007A000000004101000");
	}

}
