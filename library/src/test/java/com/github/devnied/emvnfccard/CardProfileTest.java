package com.github.devnied.emvnfccard;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.OfflineDataAuthentication;
import com.github.devnied.emvnfccard.model.enums.KernelEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Profile of the card beyond its account: which contactless kernel it asks
 * for, which credentials it carries for the offline data authentication, and
 * the issuer personalisation. Asserted against the recorded traces.
 */
public class CardProfileTest {

	private EmvCard read(final String pTrace, final boolean pContactLess) throws CommunicationException {
		return EmvTemplate.Builder() //
				.setProvider(new TestProvider(pTrace)) //
				.setConfig(EmvTemplate.Config().setContactLess(pContactLess).setReadCplc(true)) //
				.build().readEmvCard();
	}

	/**
	 * The Kernel Identifier is one byte, or three and more for a domestic
	 * kernel, never two: the two high order bits of the first byte are the
	 * kernel type and the six others the Short Kernel Identifier.
	 * <p>
	 * No recorded trace carries the tag inside an application template, so the
	 * decoding itself is asserted on the encoding the specification defines
	 * rather than on captured card content.
	 * </p>
	 */
	@Test
	public void testKernelIdentifierDecoding() {
		Application app = new Application();
		// Eight bytes, the length the FailErrorCard trace shows a real card
		// using, Short Kernel Identifier 3
		app.setKernelIdentifier(BytesUtils.fromString("0300000000000000"));
		Assertions.assertThat(app.getShortKernelId()).isEqualTo(3);
		Assertions.assertThat(app.getKernel()).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(app.isKernelDerived()).isFalse();

		// A domestic kernel is named by the first three bytes as a whole, so
		// its Short Kernel Identifier must not be read as an international one
		// and reported as Kernel 2
		Application domestic = new Application();
		domestic.setKernelIdentifier(BytesUtils.fromString("C2FFFF"));
		Assertions.assertThat(domestic.getShortKernelId()).isEqualTo(-1);
		Assertions.assertThat(domestic.getKernel()).isNull();
		// The raw identifier is still reported, the caller can read it
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(domestic.getKernelIdentifier())).isEqualTo("C2FFFF");

		// An identifier is one byte, or three and more, but never two
		Application malformed = new Application();
		malformed.setKernelIdentifier(BytesUtils.fromString("8202"));
		Assertions.assertThat(malformed.getShortKernelId()).isEqualTo(-1);

		// '000000' means that the kernel is the one the AID implies, so the
		// card has stated nothing
		Application unspecified = new Application();
		unspecified.setKernelIdentifier(BytesUtils.fromString("00"));
		Assertions.assertThat(unspecified.getShortKernelId()).isEqualTo(-1);
		Assertions.assertThat(unspecified.getKernel()).isNull();
	}

	/**
	 * The kernels an Application Identifier can be mapped to.
	 */
	@Test
	public void testKernelTable() {
		Assertions.assertThat(KernelEnum.find(2)).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(KernelEnum.find(3)).isEqualTo(KernelEnum.VISA);
		Assertions.assertThat(KernelEnum.find(4)).isEqualTo(KernelEnum.AMERICAN_EXPRESS);
		Assertions.assertThat(KernelEnum.find(5)).isEqualTo(KernelEnum.JCB);
		Assertions.assertThat(KernelEnum.find(6)).isEqualTo(KernelEnum.DISCOVER);
		Assertions.assertThat(KernelEnum.find(7)).isEqualTo(KernelEnum.UNIONPAY);
		Assertions.assertThat(KernelEnum.find(1)).isNull();
	}

	/**
	 * The FailErrorCard capture places its Kernel Identifier beside the
	 * application templates rather than inside them, so it belongs to no
	 * application in particular. Attributing it by position would be a guess,
	 * and the kernel is derived from the AID instead.
	 */
	@Test
	public void testKernelIdentifierOutsideTheTemplateIsNotAttributed() throws CommunicationException {
		for (Application app : read("FailErrorCard", true).getApplications()) {
			Assertions.assertThat(app.getKernelIdentifier()).isNull();
		}
	}

	/**
	 * Most cards state no Kernel Identifier at all, so the kernel is derived
	 * from the AID. The two cases have to stay distinguishable.
	 */
	@Test
	public void testKernelIsDerivedFromTheAid() throws CommunicationException {
		EmvCard card = read("MasterCardPpse", true);
		Application mastercard = null;
		for (Application app : card.getApplications()) {
			if (BytesUtils.bytesToStringNoSpace(app.getAid()).startsWith("A000000004")) {
				mastercard = app;
			}
		}

		Assertions.assertThat(mastercard).isNotNull();
		Assertions.assertThat(mastercard.getKernel()).isEqualTo(KernelEnum.MASTERCARD);
		Assertions.assertThat(mastercard.isKernelDerived()).isTrue();
	}

	/**
	 * The credentials of the offline data authentication are reported, never
	 * verified. The lengths come from the certificates themselves.
	 */
	@Test
	public void testOfflineDataAuthenticationCredentials() throws CommunicationException {
		Application app = read("VisaCardPpse3", true).getApplications().get(0);
		OfflineDataAuthentication oda = app.getOfflineDataAuthentication();

		Assertions.assertThat(oda.isPresent()).isTrue();
		// '8F 01 07': the card was personalised under the certification
		// authority key of index 7
		Assertions.assertThat(oda.getCertificationAuthorityPublicKeyIndex()).isEqualTo(7);
		// '9F 32 01 03' and '9F 47 01 03': both exponents are 3
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(oda.getIssuerPublicKeyExponent())).isEqualTo("03");
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(oda.getIccPublicKeyExponent())).isEqualTo("03");
		// The certificates give the key sizes without a line of cryptography
		Assertions.assertThat(oda.getCertificationAuthorityKeyLength()).isEqualTo(144);
		Assertions.assertThat(oda.getIssuerKeyLength()).isEqualTo(144);
		// This card holds its own key pair, so it is personalised for the
		// dynamic authentication
		Assertions.assertThat(oda.isDynamicAuthenticationPersonalised()).isTrue();
	}

	/**
	 * The Issuer Application Data is the densest card state object obtainable,
	 * and it is exposed raw because its layout is proprietary to each scheme.
	 */
	@Test
	public void testIssuerApplicationData() throws CommunicationException {
		Application app = read("VisaCardPpse", true).getApplications().get(0);

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(app.getIssuerApplicationData())).isEqualTo("06011A23804004");
	}

	/**
	 * The Application Interchange Profile decode must not drift: these are the
	 * profiles the recorded cards actually returned.
	 */
	@Test
	public void testInterchangeProfileOfRealCards() throws CommunicationException {
		// AIP '1980' of a MasterCard
		Application mastercard = read("MasterCardPpse", true).getApplications().get(0);
		Assertions.assertThat(mastercard.getInterchangeProfile()).isNotNull();
		Assertions.assertThat(mastercard.getInterchangeProfile().isCdaSupported()).isTrue();
		Assertions.assertThat(mastercard.getInterchangeProfile().isXdaSupported()).isFalse();
		Assertions.assertThat(mastercard.getInterchangeProfile().isRelayResistanceSupported()).isFalse();
	}

	/**
	 * The Dedicated File Name the card returns has to match the AID that was
	 * selected. One recorded capture does not match, which is exactly what the
	 * check exists to surface.
	 */
	@Test
	public void testDedicatedFileNameIsCheckedAgainstTheSelectedAid() throws CommunicationException {
		// This card answers its File Control Information with the name
		// A0000000421010 to the selection of A0000000031010
		boolean mismatch = false;
		for (Application app : read("VisaCardNullTransaction", true).getApplications()) {
			mismatch |= app.isDedicatedFileNameMismatch();
		}
		Assertions.assertThat(mismatch).isTrue();

		// Every application of this one answers the name it was selected with
		for (Application app : read("VisaCardPpse", true).getApplications()) {
			Assertions.assertThat(app.isDedicatedFileNameMismatch()).isFalse();
		}
	}
}
