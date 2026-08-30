package com.github.devnied.emvnfccard.parser.impl;

import java.util.List;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ProviderSelectPaymentEnvTest;
import com.github.devnied.emvnfccard.utils.reflect.ReflectionTestUtils;

import fr.devnied.bitlib.BytesUtils;

/**
 * The Application File Locator is a list of 4 bytes entries (EMV 4.3 Book 3
 * section 10.2):
 *
 * <pre>
 * byte 1 : SFI in its 5 high order bits
 * byte 2 : first record
 * byte 3 : last record
 * byte 4 : number of records, from the first one, taking part in the offline
 *          data authentication
 * </pre>
 */
public class AflTest {

	private final EmvParser parser = new EmvParser(
			EmvTemplate.Builder().setProvider(new ProviderSelectPaymentEnvTest()).build());

	private List<Afl> extract(final String pAfl) {
		return ReflectionTestUtils.invokeMethod(parser, "extractAfl", BytesUtils.fromString(pAfl));
	}

	@Test
	public void testMasterCardAfl() {
		// AFL of the MasterCardPpse trace
		List<Afl> afl = extract("10 01 01 01 18 01 01 00 20 01 02 00");
		Assertions.assertThat(afl).hasSize(3);

		Assertions.assertThat(afl.get(0).getSfi()).isEqualTo(2);
		Assertions.assertThat(afl.get(0).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(afl.get(0).getLastRecord()).isEqualTo(1);
		Assertions.assertThat(afl.get(0).getOfflineAuthenticationRecords()).isEqualTo(1);
		Assertions.assertThat(afl.get(0).isOfflineAuthentication()).isTrue();

		Assertions.assertThat(afl.get(1).getSfi()).isEqualTo(3);
		Assertions.assertThat(afl.get(1).getOfflineAuthenticationRecords()).isZero();
		Assertions.assertThat(afl.get(1).isOfflineAuthentication()).isFalse();

		Assertions.assertThat(afl.get(2).getSfi()).isEqualTo(4);
		Assertions.assertThat(afl.get(2).getFirstRecord()).isEqualTo(1);
		Assertions.assertThat(afl.get(2).getLastRecord()).isEqualTo(2);
	}

	@Test
	public void testVisaAfl() {
		// AFL of the VisaCardPse and VisaCardAid traces
		List<Afl> afl = extract("08 02 02 00 10 01 02 00 10 04 04 00 18 01 05 03");
		Assertions.assertThat(afl).hasSize(4);
		Assertions.assertThat(afl.get(0).getSfi()).isEqualTo(1);
		Assertions.assertThat(afl.get(0).getFirstRecord()).isEqualTo(2);
		Assertions.assertThat(afl.get(2).getSfi()).isEqualTo(2);
		Assertions.assertThat(afl.get(2).getFirstRecord()).isEqualTo(4);
		// The last entry has 3 of its 5 records in the offline data
		// authentication
		Assertions.assertThat(afl.get(3).getSfi()).isEqualTo(3);
		Assertions.assertThat(afl.get(3).getLastRecord()).isEqualTo(5);
		Assertions.assertThat(afl.get(3).getOfflineAuthenticationRecords()).isEqualTo(3);
	}

	/**
	 * The boolean setter is deprecated but must stay consistent with the count
	 * it now feeds.
	 */
	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedOfflineAuthenticationSetter() {
		Afl afl = new Afl();
		Assertions.assertThat(afl.isOfflineAuthentication()).isFalse();
		Assertions.assertThat(afl.getOfflineAuthenticationRecords()).isZero();

		afl.setOfflineAuthentication(true);
		Assertions.assertThat(afl.isOfflineAuthentication()).isTrue();
		Assertions.assertThat(afl.getOfflineAuthenticationRecords()).isEqualTo(1);

		afl.setOfflineAuthentication(false);
		Assertions.assertThat(afl.isOfflineAuthentication()).isFalse();
		Assertions.assertThat(afl.getOfflineAuthenticationRecords()).isZero();

		// The exact count is what the card sent, the flag follows it
		afl.setOfflineAuthenticationRecords(3);
		Assertions.assertThat(afl.getOfflineAuthenticationRecords()).isEqualTo(3);
		Assertions.assertThat(afl.isOfflineAuthentication()).isTrue();
	}

	@Test
	public void testInvalidEntriesAreSkipped() {
		// The SFI 0 is reserved and a record range cannot be empty, such an
		// entry must not trigger a READ RECORD
		Assertions.assertThat(extract("00 00 00 00")).isEmpty();
		Assertions.assertThat(extract("10 00 01 00")).isEmpty();
		Assertions.assertThat(extract("10 05 01 00")).isEmpty();
		// The SFI 31 is reserved too
		Assertions.assertThat(extract("F8 01 01 00")).isEmpty();
		// A valid entry surrounded by invalid ones is kept
		Assertions.assertThat(extract("00 00 00 00 10 02 04 00 10 09 01 00")).hasSize(1);
		// A truncated entry is ignored, the AFL is a multiple of 4 bytes
		Assertions.assertThat(extract("10 02 04 00 18 01")).hasSize(1);
		Assertions.assertThat(extract("")).isEmpty();
	}

	/**
	 * The locator is also reported as the card sent it: parseAflEntries keeps
	 * the invalid entries, and extractAfl still drives the reading with the
	 * valid ones alone.
	 */
	@Test
	public void testParseAflEntriesKeepsInvalidOnes() {
		List<Afl> all = ReflectionTestUtils.invokeMethod(parser, "parseAflEntries",
				BytesUtils.fromString("00 00 00 00 10 02 04 00 10 09 01 00 F8 01 01 00 18 01"));
		Assertions.assertThat(all).hasSize(4);
		Assertions.assertThat(all.get(0).isValid()).isFalse();
		Assertions.assertThat(all.get(0).getSfi()).isZero();
		Assertions.assertThat(all.get(1).isValid()).isTrue();
		Assertions.assertThat(all.get(1).getSfi()).isEqualTo(2);
		Assertions.assertThat(all.get(1).getFirstRecord()).isEqualTo(2);
		Assertions.assertThat(all.get(1).getLastRecord()).isEqualTo(4);
		Assertions.assertThat(all.get(2).isValid()).isFalse();
		Assertions.assertThat(all.get(2).getFirstRecord()).isEqualTo(9);
		Assertions.assertThat(all.get(3).isValid()).isFalse();
		Assertions.assertThat(all.get(3).getSfi()).isEqualTo(31);
		// The valid entries are the same objects, in the same order
		Assertions.assertThat(extract("00 00 00 00 10 02 04 00 10 09 01 00 F8 01 01 00 18 01")).hasSize(1);
		Assertions.assertThat(ReflectionTestUtils.<List<Afl>> invokeMethod(parser, "parseAflEntries", (Object) null)).isEmpty();
		Assertions.assertThat(ReflectionTestUtils.<List<Afl>> invokeMethod(parser, "parseAflEntries", new byte[0])).isEmpty();
	}

}
