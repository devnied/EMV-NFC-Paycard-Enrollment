package com.github.devnied.emvnfccard.iso7816emv;

import java.io.IOException;
import java.util.Random;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

/**
 * The BER-TLV reader, asserted on the encodings the specification defines.
 */
public class BerTlvInputStreamTest {

	private static String tag(final String pData) throws IOException {
		return BytesUtils.bytesToStringNoSpace(new BerTlvInputStream(BytesUtils.fromString(pData)).readTag());
	}

	/**
	 * A tag whose five low order bits are all set takes several bytes, each
	 * following one carrying a continuation bit. The bytes have to be reported
	 * as they were read: dropping the continuation bit turns 'DF8104' into
	 * 'DF0104', a tag no dictionary holds.
	 */
	@Test
	public void testMultiByteTagKeepsItsBytes() throws IOException {
		Assertions.assertThat(tag("5A")).isEqualTo("5A");
		Assertions.assertThat(tag("9F6B")).isEqualTo("9F6B");
		Assertions.assertThat(tag("DF8104")).isEqualTo("DF8104");
		Assertions.assertThat(tag("FF8105")).isEqualTo("FF8105");
	}

	/**
	 * A '00' byte before a data object carries no meaning (EMV 4.3 Book 3
	 * Annex B1). 'FF' does: EMV assigns tags beginning with it.
	 */
	@Test
	public void testPaddingIsSkippedButNotTheTagsBeginningWithFF() throws IOException {
		Assertions.assertThat(tag("0000 5A")).isEqualTo("5A");
		Assertions.assertThat(tag("FF8106")).isEqualTo("FF8106");
	}

	/**
	 * The consequence for a caller: the dictionary holds 48 tags of three
	 * bytes, and none of them could be read back before.
	 */
	@Test
	public void testThreeByteTagsAreReachable() {
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(
				TlvUtil.getValue(BytesUtils.fromString("df81040212349000"), EmvTags.BALANCE_READ_BEFORE_GEN_AC)))
				.isEqualTo("1234");
		Assertions.assertThat(
				BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(BytesUtils.fromString("ff8105021234"), EmvTags.DATA_RECORD)))
				.isEqualTo("1234");
	}

	/**
	 * Below 128 a length is the byte itself, above it the low order bits give
	 * the number of bytes the length takes.
	 */
	@Test
	public void testLengthForms() throws IOException {
		Assertions.assertThat(new BerTlvInputStream(BytesUtils.fromString("7F")).readLength()).isEqualTo(127);
		Assertions.assertThat(new BerTlvInputStream(BytesUtils.fromString("8180")).readLength()).isEqualTo(128);
		Assertions.assertThat(new BerTlvInputStream(BytesUtils.fromString("820101")).readLength()).isEqualTo(257);
	}

	/**
	 * A malformed length is reported as a checked exception, not as an
	 * unchecked one escaping to the caller of a card read.
	 */
	@Test
	public void testMalformedLengthIsAChecked() {
		for (String malformed : new String[] { "84FFFFFFFF", "80", "8F0102030405060708090A0B0C0D0E0F", "81" }) {
			try {
				new BerTlvInputStream(BytesUtils.fromString(malformed)).readLength();
				Assert.fail("Expected an IOException for " + malformed);
			} catch (IOException expected) {
				// the caller degrades instead of failing
			}
		}
	}

	/**
	 * The encoding of a length is the inverse of its reading.
	 */
	@Test
	public void testLengthEncodingRoundTrips() throws IOException {
		for (int length : new int[] { 0, 1, 127, 128, 255, 256, 65535, 65536 }) {
			byte[] encoded = BerTlvInputStream.encodeLength(length);
			Assertions.assertThat(new BerTlvInputStream(encoded).readLength()).isEqualTo(length);
		}
	}

	/**
	 * The boundaries are part of the contract: a tag stops being read at the
	 * fifth byte, a length at a count of more than four.
	 */
	@Test
	public void testTheBoundariesAreWhereTheyAreClaimed() throws IOException {
		// Four bytes of tag are accepted, five are not
		Assertions.assertThat(tag("DF818201")).isEqualTo("DF818201");
		try {
			new BerTlvInputStream(BytesUtils.fromString("DF81828301")).readTag();
			Assert.fail("A tag of five bytes must be refused");
		} catch (IOException expected) {
			// reported, not returned truncated
		}
		// A count of four is accepted, five is not
		Assertions.assertThat(new BerTlvInputStream(BytesUtils.fromString("8400000005")).readLength()).isEqualTo(5);
		try {
			new BerTlvInputStream(BytesUtils.fromString("850000000005")).readLength();
			Assert.fail("A length count of five bytes must be refused");
		} catch (IOException expected) {
			// reported, not accumulated into an overflowing int
		}
	}

	/**
	 * The position handling: reading consumes, a mark can be returned to, and
	 * returning to a mark that was never taken is an error rather than a
	 * silent rewind to the start.
	 */
	@Test
	public void testPositionHandling() throws IOException {
		BerTlvInputStream stream = new BerTlvInputStream(BytesUtils.fromString("0102030405"));
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(stream.read(2))).isEqualTo("0102");
		stream.mark();
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(stream.read(2))).isEqualTo("0304");
		stream.reset();
		Assertions.assertThat(stream.available()).isEqualTo(3);

		try {
			new BerTlvInputStream(BytesUtils.fromString("0102")).reset();
			Assert.fail("A reset without a mark must be refused");
		} catch (IOException expected) {
			// and not rewind to the beginning of the data
		}
		try {
			new BerTlvInputStream(BytesUtils.fromString("0102")).read(3);
			Assert.fail("Reading past the end must be refused");
		} catch (IOException expected) {
			// the caller asked for more than the card sent
		}
	}

	/**
	 * No random byte string may make the parsing throw something the callers
	 * do not handle, on any of the entry points a card read goes through. The
	 * implementation this replaced threw an unchecked exception on about one
	 * input in six of this very generator.
	 */
	@Test
	public void testNoUncheckedExceptionEscapesOnRandomInput() {
		Random random = new Random(20260816L);
		for (int i = 0; i < 200000; i++) {
			byte[] data = new byte[1 + random.nextInt(12)];
			random.nextBytes(data);
			// One input in four is given a plausible template prefix, so that
			// the generator reaches the nested paths and not only the first
			// byte of a malformed tag
			if (i % 4 == 0 && data.length > 2) {
				data[0] = 0x70;
				data[1] = (byte) (data.length - 2);
			}
			try {
				TlvUtil.parseTagAndLength(data);
				TlvUtil.getValue(data, EmvTags.PAN);
				TlvUtil.getlistTLV(data, EmvTags.APPLICATION_TEMPLATE);
				TlvUtil.getlistTLV(data, EmvTags.APPLICATION_TEMPLATE, false);
				TlvUtil.prettyPrintAPDUResponse(data);
				TlvUtil.getFormattedTagAndLength(data, 0);
			} catch (RuntimeException e) {
				Assert.fail("Unchecked exception on " + BytesUtils.bytesToStringNoSpace(data) + ": " + e);
			}
		}
	}

	/**
	 * A card is free to answer a template holding a template, and nothing in
	 * the encoding bounds the nesting. The parsing must not follow it until
	 * the stack gives way: a StackOverflowError is an Error, so no caller
	 * catches it and it reaches the application reading the card.
	 */
	@Test
	public void testDeepNestingDoesNotExhaustTheStack() {
		// 2000 nested '70' templates around a PAN
		byte[] data = BytesUtils.fromString("5A0812345678901234 56");
		for (int i = 0; i < 2000; i++) {
			byte[] nested = new byte[data.length + 3];
			nested[0] = 0x70;
			nested[1] = (byte) 0x81;
			nested[2] = (byte) data.length;
			System.arraycopy(data, 0, nested, 3, data.length);
			data = nested;
		}

		// No StackOverflowError, and the parsing stops rather than pretending
		Assertions.assertThat(TlvUtil.getValue(data, EmvTags.PAN)).isNull();
		Assertions.assertThat(TlvUtil.getlistTLV(data, EmvTags.PAN)).isEmpty();
	}
}
