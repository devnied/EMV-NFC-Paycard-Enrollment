package com.github.devnied.emvnfccard.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.impl.ProviderWrapper;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

public class ProviderWrapperTest {

	/**
	 * FCI answered by the card of the wrapperTestSW_6C trace, used to build
	 * the chained responses instead of making up a card content
	 */
	private static final String CAPTURED_FCI = "6F37" + "8407A0000000031010" + "A52C" + "500A56495341204445424954"
			+ "870102" + "9F110101" + "9F120456495341" + "5F2D026672" + "BF0C0ADF60020B1E9F4D020B1E" + "9000";

	/**
	 * Provider answering the responses it was built with, in order, and
	 * recording the commands it received.<br>
	 * The exchanges it replays are ISO 7816-4 procedure bytes built for the
	 * rule under test, they are not a capture of a real card.
	 */
	private static final class RecordingProvider implements IProvider {

		private final Queue<String> responses = new LinkedList<String>();

		private final List<String> received = new ArrayList<String>();

		private RecordingProvider(final String... pResponses) {
			for (String response : pResponses) {
				responses.add(response);
			}
		}

		@Override
		public byte[] transceive(final byte[] pCommand) throws CommunicationException {
			received.add(BytesUtils.bytesToStringNoSpace(pCommand));
			String response = responses.poll();
			if (response == null) {
				throw new CommunicationException("No response left");
			}
			return BytesUtils.fromString(response);
		}

		@Override
		public byte[] getAt() {
			return BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00");
		}
	}

	@Test
	public void testProviderSW_6C() throws CommunicationException{
		ProviderWrapper wrapper = new ProviderWrapper(new TestProvider("wrapperTestSW_6C"));
		wrapper.transceive(BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00"));
	}

	@Test
	public void testProviderSW_61() throws CommunicationException{
		ProviderWrapper wrapper = new ProviderWrapper(new TestProvider("wrapperTestSW_61"));
		wrapper.transceive(BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00"));
	}

	/**
	 * The GET RESPONSE command is CLA '00' INS 'C0' (EMV 4.3 Book 1 section
	 * 9.3.1.3, table 32), a card answering '61xx' is asked for the exact
	 * number of bytes it announced.
	 */
	@Test
	public void testGetResponseCommand() throws CommunicationException {
		RecordingProvider provider = new RecordingProvider("61 05", "5F 2D 02 66 72 90 00");
		new ProviderWrapper(provider).transceive(BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00"));

		Assertions.assertThat(provider.received.get(1)).isEqualTo("00C0000005");
	}

	/**
	 * A card is allowed to answer data followed by '61xx' and to answer '61xx'
	 * again to the GET RESPONSE (EMV 4.3 Book 1 sections 9.3.1.2.1 and
	 * 9.3.1.2.2): every data field read has to be kept.
	 */
	@Test
	public void testChainedGetResponse() throws CommunicationException {
		// The FCI of the wrapperTestSW_6C trace, cut in three by the card:
		// 25 bytes then '61 20', 16 bytes then '61 10', the last 16 bytes and
		// '90 00'
		RecordingProvider provider = new RecordingProvider(//
				"6F 37 84 07 A0 00 00 00 03 10 10 A5 2C 50 0A 56 49 53 41 20 44 45 42 49 54 61 20", //
				"87 01 02 9F 11 01 01 9F 12 04 56 49 53 41 5F 2D 61 10", //
				"02 66 72 BF 0C 0A DF 60 02 0B 1E 9F 4D 02 0B 1E 90 00");
		byte[] response = new ProviderWrapper(provider).transceive(BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00"));

		// The concatenation is the response the card returned in one go in the
		// wrapperTestSW_6C trace
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(response)).isEqualTo(CAPTURED_FCI);
		Assertions.assertThat(provider.received).hasSize(3);
		Assertions.assertThat(provider.received.get(1)).isEqualTo("00C0000020");
		Assertions.assertThat(provider.received.get(2)).isEqualTo("00C0000010");
	}

	/**
	 * A card answering '61xx' forever must not hang the reading.
	 */
	@Test
	public void testChainedGetResponseIsBounded() throws CommunicationException {
		String[] responses = new String[ProviderWrapper.MAX_CHAINED_RESPONSES + 1];
		for (int i = 0; i < responses.length; i++) {
			responses[i] = "00 61 01";
		}
		RecordingProvider provider = new RecordingProvider(responses);
		new ProviderWrapper(provider).transceive(BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00"));

		Assertions.assertThat(provider.received).hasSize(ProviderWrapper.MAX_CHAINED_RESPONSES + 1);
	}

	/**
	 * The command belongs to the caller, answering '6Cxx' must not modify it.
	 */
	@Test
	public void testCommandIsNotModified() throws CommunicationException {
		RecordingProvider provider = new RecordingProvider("6C 01", CAPTURED_FCI);
		byte[] command = BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00");
		new ProviderWrapper(provider).transceive(command);

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(command)).isEqualTo("00A4040005A00000000300");
		// The card is answered with the length it asked for
		Assertions.assertThat(provider.received.get(1)).isEqualTo("00A4040005A00000000301");
	}
}
