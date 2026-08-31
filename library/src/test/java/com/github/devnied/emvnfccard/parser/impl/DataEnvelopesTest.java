package com.github.devnied.emvnfccard.parser.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.DataEnvelopes;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Reading of the Standalone Data Storage data envelopes (tags '9F70' to '9F79')
 * with the GET DATA command.
 * <p>
 * EMV Contactless Book C-2 v2.12 section 3.5.2: "It introduces a range of
 * payment system tags ('9F70' to '9F79') for the reading and writing of
 * non-payment data [...]. The whole range is freely readable using the GET DATA
 * command. [...] The length of the data is variable. The maximum length is
 * implementation specific, and is between 32 and 192 bytes."
 * </p>
 * <p>
 * No trace of src/test/resources/data holds a data envelope: none of the cards
 * recorded there implements Standalone Data Storage. The responses scripted
 * below therefore carry <b>no card content at all</b>. Their tags come from
 * Annex A of Book C-2 v2.12 (A.1.107 to A.1.111 and A.1.159 to A.1.163), their
 * lengths from the SDS Scheme Indicator of the Application Capabilities
 * Information (tag '9F5D' byte 3), whose value '00001000' reads "All SDS tags
 * 32 bytes except '9F78' which is 64 bytes", and their value is the unwritten
 * envelope of a card that was personalised with the storage and never had
 * anything put in it.
 * </p>
 */
public class DataEnvelopesTest {

	/**
	 * Length of an envelope under the SDS Scheme Indicator '00001000', "All SDS
	 * tags 32 bytes except '9F78' which is 64 bytes"
	 */
	private static final int SDS_LENGTH = 32;

	/**
	 * Length of the envelope '9F78' under the same scheme indicator
	 */
	private static final int SDS_LENGTH_9F78 = 64;

	/**
	 * Application Identifier of a MasterCard application, taken from the
	 * MasterCardPpse3 trace
	 */
	private static final String MASTERCARD_AID = "A0000000041010";

	/**
	 * Registered Application Provider Identifier of the UnionPay (PBOC)
	 * applications, the ones that read '9F79' as the Electronic Cash Balance
	 */
	private static final String UNIONPAY_RID = "A000000333";

	/**
	 * Application Identifier of a Visa application, taken from the
	 * VisaCardPpse3 trace
	 */
	private static final String VISA_AID = "A0000000031010";

	/**
	 * Template kept for the whole test, the parser only holds a weak reference
	 * on it
	 */
	private EmvTemplate template;

	/**
	 * Build a parser reading the card through the scripted provider
	 *
	 * @param pProvider
	 *            provider answering the GET DATA commands
	 * @return the parser
	 */
	private EmvParser parser(final ScriptedProvider pProvider) {
		template = EmvTemplate.Builder().setProvider(pProvider).build();
		return new EmvParser(template);
	}

	/**
	 * Build an application announcing the kernel and selected with the AID
	 *
	 * @param pAid
	 *            Application Identifier, hexadecimal
	 * @param pKernel
	 *            kernel the card asks for
	 * @return the application
	 */
	private Application application(final String pAid, final KernelEnum pKernel) {
		Application application = new Application();
		application.setAid(BytesUtils.fromString(pAid));
		if (pKernel != null) {
			application.setShortKernelId(pKernel.getKey());
		}
		return application;
	}

	/**
	 * Build the response of a card holding an envelope that was never written
	 * to: the tag, its length and as many '00' bytes, then '9000'
	 *
	 * @param pTag
	 *            tag of the envelope, hexadecimal
	 * @param pLength
	 *            length of the envelope
	 * @return the response APDU, hexadecimal
	 */
	private static String envelope(final String pTag, final int pLength) {
		StringBuilder response = new StringBuilder(pTag);
		// EMV 4.4 Book 3 Annex B2, a length of 128 bytes and more is coded on
		// the long form: '81' then the length itself
		if (pLength > 127) {
			response.append("81");
		}
		response.append(String.format("%02X", pLength));
		for (int i = 0; i < pLength; i++) {
			response.append("00");
		}
		return response.append("9000").toString();
	}

	/**
	 * The ten GET DATA commands a Kernel 2 application is asked, in the order
	 * of the range
	 */
	private static List<String> allCommands() {
		return Arrays.asList("80CA9F7000", "80CA9F7100", "80CA9F7200", "80CA9F7300", "80CA9F7400", "80CA9F7500",
				"80CA9F7600", "80CA9F7700", "80CA9F7800", "80CA9F7900");
	}

	/**
	 * A card implementing Standalone Data Storage answers the ten envelopes,
	 * '9F78' being the longer one under the scheme indicator '00001000'.
	 */
	@Test
	public void testEnvelopesAreReadOnKernel2() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider() //
				.on("80CA9F7000", envelope("9F70", SDS_LENGTH)) //
				.on("80CA9F7100", envelope("9F71", SDS_LENGTH)) //
				.on("80CA9F7200", envelope("9F72", SDS_LENGTH)) //
				.on("80CA9F7300", envelope("9F73", SDS_LENGTH)) //
				.on("80CA9F7400", envelope("9F74", SDS_LENGTH)) //
				.on("80CA9F7500", envelope("9F75", SDS_LENGTH)) //
				.on("80CA9F7600", envelope("9F76", SDS_LENGTH)) //
				.on("80CA9F7700", envelope("9F77", SDS_LENGTH)) //
				.on("80CA9F7800", envelope("9F78", SDS_LENGTH_9F78)) //
				.on("80CA9F7900", envelope("9F79", SDS_LENGTH));
		Application app = application(MASTERCARD_AID, KernelEnum.MASTERCARD);

		parser(provider).extractDataEnvelopes(app);

		// The whole range is read, P1||P2 of each command is the tag itself
		Assertions.assertThat(provider.getReceived()).isEqualTo(allCommands());
		DataEnvelopes envelopes = app.getDataEnvelopes();
		Assertions.assertThat(envelopes.isPresent()).isTrue();
		Assertions.assertThat(envelopes.getProtectedDataEnvelope1()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getProtectedDataEnvelope2()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getProtectedDataEnvelope3()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getProtectedDataEnvelope4()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getProtectedDataEnvelope5()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope1()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope2()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope3()).hasSize(SDS_LENGTH);
		// "All SDS tags 32 bytes except '9F78' which is 64 bytes"
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope4()).hasSize(SDS_LENGTH_9F78);
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope5()).hasSize(SDS_LENGTH);
		// None exceeds the maximum, and every GET DATA was answered '9000'
		Assertions.assertThat(envelopes.getOversizedTags()).isEmpty();
		Assertions.assertThat(app.getGetDataStatusWords()).hasSize(10);
		Assertions.assertThat(new ArrayList<String>(app.getGetDataStatusWords().keySet())).containsExactly("9F70", "9F71",
				"9F72", "9F73", "9F74", "9F75", "9F76", "9F77", "9F78", "9F79");
		Assertions.assertThat(new HashSet<String>(app.getGetDataStatusWords().values())).containsOnly("9000");
	}

	/**
	 * A card that does not implement the storage answers a status word per tag.
	 * None of them interrupts the reading: the tags that follow are asked all
	 * the same, and the envelopes the card does hold are read.
	 */
	@Test
	public void testUnsupportedStatusWordsDoNotInterruptTheReading() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider() //
				// '6A81' function not supported
				.on("80CA9F7000", "6A81") //
				// '6A88' referenced data not found
				.on("80CA9F7100", "6A88") //
				// '6D00' instruction code not supported or invalid
				.on("80CA9F7200", "6D00") //
				// '6E00' class not supported
				.on("80CA9F7300", "6E00") //
				// The envelope the card does hold, in the middle of the range
				.on("80CA9F7500", envelope("9F75", SDS_LENGTH));
		// Every other tag is answered '6A82' (file not found) by the provider
		Application app = application(MASTERCARD_AID, KernelEnum.MASTERCARD);

		parser(provider).extractDataEnvelopes(app);

		// The reading went through the whole range
		Assertions.assertThat(provider.getReceived()).isEqualTo(allCommands());
		DataEnvelopes envelopes = app.getDataEnvelopes();
		Assertions.assertThat(envelopes.isPresent()).isTrue();
		Assertions.assertThat(envelopes.getProtectedDataEnvelope1()).isNull();
		Assertions.assertThat(envelopes.getProtectedDataEnvelope2()).isNull();
		Assertions.assertThat(envelopes.getProtectedDataEnvelope3()).isNull();
		Assertions.assertThat(envelopes.getProtectedDataEnvelope4()).isNull();
		Assertions.assertThat(envelopes.getProtectedDataEnvelope5()).isNull();
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope1()).hasSize(SDS_LENGTH);
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope2()).isNull();
		Assertions.assertThat(envelopes.getUnprotectedDataEnvelope5()).isNull();
		// The status word the card answered for every tag is kept
		Assertions.assertThat(app.getGetDataStatusWords()).hasSize(10);
		Assertions.assertThat(app.getGetDataStatusWords().get("9F70")).isEqualTo("6A81");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F71")).isEqualTo("6A88");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F72")).isEqualTo("6D00");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F73")).isEqualTo("6E00");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F74")).isEqualTo("6A82");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F75")).isEqualTo("9000");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F79")).isEqualTo("6A82");
	}

	/**
	 * A card holding no envelope at all is reported as such, and no exception
	 * is raised for it.
	 */
	@Test
	public void testCardWithoutStandaloneDataStorage() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		Application app = application(MASTERCARD_AID, KernelEnum.MASTERCARD);

		parser(provider).extractDataEnvelopes(app);

		Assertions.assertThat(provider.getReceived()).isEqualTo(allCommands());
		Assertions.assertThat(app.getDataEnvelopes().isPresent()).isFalse();
	}

	/**
	 * The range '9F50' to '9F7F' is reserved for the payment systems and is
	 * interpreted within the context of the application RID: only the kernel
	 * that defines the envelopes is asked for them.
	 */
	@Test
	public void testOnlyKernel2IsAskedForTheEnvelopes() throws CommunicationException {
		ScriptedProvider visa = new ScriptedProvider();
		parser(visa).extractDataEnvelopes(application(VISA_AID, KernelEnum.VISA));
		Assertions.assertThat(visa.getReceived()).isEmpty();

		// The kernel of a Visa AID is derived even when the card announced none
		ScriptedProvider derived = new ScriptedProvider();
		parser(derived).extractDataEnvelopes(application(VISA_AID, null));
		Assertions.assertThat(derived.getReceived()).isEmpty();

		// Neither the card nor the AID names a kernel: nothing is read
		ScriptedProvider unknown = new ScriptedProvider();
		parser(unknown).extractDataEnvelopes(new Application());
		Assertions.assertThat(unknown.getReceived()).isEmpty();
	}

	/**
	 * The tag '9F79' is the Electronic Cash Balance of the applications that
	 * implement electronic cash, where it holds an amount and not an envelope.
	 * The application is read for the nine other tags and the balance keeps the
	 * tenth, even when the card asks for Kernel 2.
	 */
	@Test
	public void testElectronicCashKeepsTheTag9F79() throws CommunicationException {
		ScriptedProvider provider = new ScriptedProvider();
		Application app = application(UNIONPAY_RID, KernelEnum.MASTERCARD);

		parser(provider).extractDataEnvelopes(app);

		Assertions.assertThat(provider.getReceived()).isEqualTo(allCommands().subList(0, 9));
		Assertions.assertThat(provider.getReceived()).excludes("80CA9F7900");
		Assertions.assertThat(app.getDataEnvelopes().getUnprotectedDataEnvelope5()).isNull();
	}

	/**
	 * An envelope longer than the 192 bytes of the specification is reported as
	 * the card answered it: a reader says what it saw.
	 */
	@Test
	public void testEnvelopeLongerThanTheSpecifiedMaximum() throws CommunicationException {
		int length = DataEnvelopes.MAX_LENGTH + 1;
		ScriptedProvider provider = new ScriptedProvider().on("80CA9F7000", envelope("9F70", length));
		Application app = application(MASTERCARD_AID, KernelEnum.MASTERCARD);

		parser(provider).extractDataEnvelopes(app);

		Assertions.assertThat(app.getDataEnvelopes().getProtectedDataEnvelope1()).hasSize(length);
		// ... and the envelope is named as exceeding the maximum
		Assertions.assertThat(app.getDataEnvelopes().getOversizedTags()).containsExactly("9F70");
	}

	/**
	 * The offline balance is kept raw with the tag it was read from, even when
	 * the value is not the n12 amount the decoded balance requires.
	 */
	@Test
	public void testOfflineBalanceKeepsItsTagAndRawValue() throws CommunicationException {
		// A UnionPay purse: tag '9F79' is the Electronic Cash Balance
		ScriptedProvider unionPay = new ScriptedProvider().on("80CA9F7900", "9F79 06 000000012345 9000");
		Application purse = application(UNIONPAY_RID, null);
		purse.setCurrencyExponent(2);
		Assertions.assertThat(parser(unionPay).getOfflineBalance(purse)).isEqualTo(new BigDecimal("123.45"));
		Assertions.assertThat(unionPay.getReceived()).isEqualTo(Arrays.asList("80CA9F7900"));
		Assertions.assertThat(purse.getOfflineBalanceTag()).isEqualTo("9F79");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(purse.getRawOfflineBalance())).isEqualTo("000000012345");
		Assertions.assertThat(purse.getGetDataStatusWords().get("9F79")).isEqualTo("9000");

		// A MasterCard answering 5 bytes at tag '9F50': not an amount, but the
		// value is reported
		ScriptedProvider mastercard = new ScriptedProvider().on("80CA9F5000", "9F50 05 0000012345 9000");
		Application app = application(MASTERCARD_AID, KernelEnum.MASTERCARD);
		Assertions.assertThat(parser(mastercard).getOfflineBalance(app)).isNull();
		Assertions.assertThat(mastercard.getReceived()).isEqualTo(Arrays.asList("80CA9F5000"));
		Assertions.assertThat(app.getOfflineBalanceTag()).isEqualTo("9F50");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getRawOfflineBalance())).isEqualTo("0000012345");
		Assertions.assertThat(app.getGetDataStatusWords().get("9F50")).isEqualTo("9000");

		// A card holding no balance: nothing is kept but the status word
		Application none = application(MASTERCARD_AID, KernelEnum.MASTERCARD);
		Assertions.assertThat(parser(new ScriptedProvider()).getOfflineBalance(none)).isNull();
		Assertions.assertThat(none.getOfflineBalanceTag()).isNull();
		Assertions.assertThat(none.getRawOfflineBalance()).isNull();
		Assertions.assertThat(none.getGetDataStatusWords().get("9F50")).isEqualTo("6A82");
	}

	/**
	 * An envelope holds up to 192 bytes, the model never hands it out by
	 * reference.
	 */
	@Test
	public void testTheEnvelopeIsNotHandedOutByReference() {
		byte[] value = new byte[SDS_LENGTH];
		DataEnvelopes envelopes = new DataEnvelopes();
		envelopes.setProtectedDataEnvelope1(value);
		value[0] = (byte) 0xFF;

		Assertions.assertThat(envelopes.getProtectedDataEnvelope1()[0]).isZero();
		envelopes.getProtectedDataEnvelope1()[0] = (byte) 0xFF;
		Assertions.assertThat(envelopes.getProtectedDataEnvelope1()[0]).isZero();
	}

}
