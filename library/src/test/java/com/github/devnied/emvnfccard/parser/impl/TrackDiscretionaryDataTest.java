package com.github.devnied.emvnfccard.parser.impl;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.provider.ScriptedProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Reading of the discretionary data objects a card holds next to its tracks:
 * the Track 1 Discretionary Data (tag '9F1F') and the Track 2 Discretionary
 * Data (tag '9F20').
 * <p>
 * EMV 4.4 Book 3 Annex A1 describes them as the "Discretionary part of track 1
 * according to ISO/IEC 7813" and the "Discretionary part of track 2 according
 * to ISO/IEC 7813", read from the card with the READ RECORD command in the
 * template '70' or '77'.
 * </p>
 * <p>
 * No trace of src/test/resources/data holds either of them: none of the cards
 * recorded there answers the discretionary field on its own, they all send it
 * inside a track. The records below are therefore <b>built</b> as the BER-TLV
 * structures of EMV 4.4 Book 3 Annex B, but every byte of card content they
 * carry comes from the MasterCardPpse3 capture: its record answers a Track 2
 * Data (tag '9F6B') of value "52 00 00 00 00 00 00 00 D1 91 11 01 01 00 00 00
 * 00 00 3F", whose seven last bytes are the discretionary field followed by the
 * 'F' pad, and a Track 1 Data (tag '56') whose thirteen last bytes are the same
 * field written as characters. A card answering those fields in the tags '9F20'
 * and '9F1F' answers those very bytes, the formats cn and ans of the data
 * objects being the ones of the sub-fields they are taken from.
 * </p>
 *
 * @author MILLAU julien
 *
 */
public class TrackDiscretionaryDataTest {

	/**
	 * Track 2 Data (tag '9F6B') of the MasterCardPpse3 capture
	 */
	private static final String TRACK2_DATA = "9F6B135200000000000000D19111010100000000003F";

	/**
	 * Track 2 Discretionary Data (tag '9F20') holding the discretionary field
	 * of the track above, pad included: seven bytes of compressed numeric
	 */
	private static final String TRACK2_DISCRETIONARY_DATA = "9F20070100000000003F";

	/**
	 * Track 1 Discretionary Data (tag '9F1F') holding the same field written as
	 * characters: thirteen bytes of the common character set
	 */
	private static final String TRACK1_DISCRETIONARY_DATA = "9F1F0D30313030303030303030303033";

	/**
	 * The thirteen digits the card wrote in the discretionary field of both of
	 * its tracks
	 */
	private static final String DISCRETIONARY_DATA = "0100000000003";

	/**
	 * Template kept for the whole test, the parser only holds a weak reference
	 * on it
	 */
	private EmvTemplate template;

	/**
	 * Build a parser reading nothing: every method exercised here is given the
	 * response of the card instead of asking for it
	 *
	 * @return the parser
	 */
	private EmvParser parser() {
		template = EmvTemplate.Builder().setProvider(new ScriptedProvider()).build();
		return new EmvParser(template);
	}

	/**
	 * Wrap data objects in the template '70' of a READ RECORD response
	 *
	 * @param pContent
	 *            data objects, hexadecimal
	 * @return the record, hexadecimal
	 */
	private static byte[] record(final String pContent) {
		return BytesUtils.fromString(String.format("70%02X%s", pContent.length() / 2, pContent));
	}

	/**
	 * A card answering both the track and the discretionary data object keeps
	 * the two: the digits read inside the track, and the raw value of the data
	 * object the card holds them in.
	 */
	@Test
	public void testTrack2DiscretionaryDataIsReadNextToTheTrack() {
		EmvCard card = new EmvCard();

		boolean read = parser().extractTrackData(card, record(TRACK2_DATA + TRACK2_DISCRETIONARY_DATA));

		Assertions.assertThat(read).isTrue();
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getCardNumber()).isEqualTo("5200000000000000");
		Assertions.assertThat(card.getTrack2().getServiceCode()).isEqualTo("101");
		// The field read inside the track, without the 'F' pad
		Assertions.assertThat(card.getTrack2().getDiscretionaryData()).isEqualTo(DISCRETIONARY_DATA);
		// The card resident data object, pad included
		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getTrack2().getRawDiscretionaryData()))
				.isEqualTo("0100000000003F");
	}

	/**
	 * A card answering the discretionary data object alone, in a record holding
	 * no track, leaves nothing to attach it to: the data object says nothing of
	 * the account it belongs to, and no track is made up out of it.
	 */
	@Test
	public void testDiscretionaryDataWithoutTrackIsDropped() {
		EmvCard card = new EmvCard();

		boolean read = parser().extractTrackData(card,
				record(TRACK1_DISCRETIONARY_DATA + TRACK2_DISCRETIONARY_DATA));

		Assertions.assertThat(read).isFalse();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNull();
	}

	/**
	 * The card is free to answer the data object in another record than the one
	 * the track was read in: the track read first is the one kept, and the
	 * record that follows completes it.
	 */
	@Test
	public void testDiscretionaryDataIsReadFromAnotherRecord() {
		EmvCard card = new EmvCard();
		EmvParser parser = parser();

		parser.extractTrackData(card, record(TRACK2_DATA));
		Assertions.assertThat(card.getTrack2()).isNotNull();
		Assertions.assertThat(card.getTrack2().getRawDiscretionaryData()).isNull();

		parser.extractTrackData(card, record(TRACK2_DISCRETIONARY_DATA));

		Assertions.assertThat(BytesUtils.bytesToStringNoSpace(card.getTrack2().getRawDiscretionaryData()))
				.isEqualTo("0100000000003F");
		Assertions.assertThat(card.getTrack2().getDiscretionaryData()).isEqualTo(DISCRETIONARY_DATA);
	}

	/**
	 * A record holding neither a track nor a discretionary data object is read
	 * without raising anything.
	 */
	@Test
	public void testRecordWithoutAnyTrackData() {
		EmvCard card = new EmvCard();

		// Application Primary Account Number Sequence Number (tag '5F34')
		boolean read = parser().extractTrackData(card, record("5F340100"));

		Assertions.assertThat(read).isFalse();
		Assertions.assertThat(card.getTrack1()).isNull();
		Assertions.assertThat(card.getTrack2()).isNull();
	}

}
