package com.github.devnied.emvnfccard.model;

import org.fest.assertions.Assertions;
import org.junit.Test;

import fr.devnied.bitlib.BytesUtils;

/**
 * Bitmaps of the contactless Mag-stripe Mode, asserted against the two
 * personalisations recorded in src/test/resources/data.
 * <p>
 * Both are records a MasterCard application answered to a READ RECORD, kept
 * verbatim: MasterCardPpse2 answers "70 6B 9F 6C 02 00 01 9F 62 06 00 00 00 00
 * 0E 00 9F 63 06 00 00 00 00 00 7E 56 2A [...] 9F 64 01 03 9F 65 02 00 0E 9F 66
 * 02 0E 70 9F 6B 13 [...] 9F 67 01 03", and MasterCardPpse3 the same data
 * objects with the values asserted by {@link #testSecondPersonalisation()}.
 * </p>
 * <p>
 * The expected positions are read off the bitmaps with the rule of EMV
 * Contactless Book C-2 v2.11 section 4.7: "The least significant bit of the
 * bitmap, i.e. the rightmost bit b1, corresponds to position p1", p1 being the
 * rightmost digit of the discretionary data field.
 * </p>
 */
public class MagStripeDataTest {

	/**
	 * Build the mag-stripe data objects of a personalisation
	 *
	 * @param pPcvc3Track1
	 *            PCVC3(Track1), tag '9F62'
	 * @param pPunatcTrack1
	 *            PUNATC(Track1), tag '9F63'
	 * @param pNatcTrack1
	 *            NATC(Track1), tag '9F64'
	 * @param pPcvc3Track2
	 *            PCVC3(Track2), tag '9F65'
	 * @param pPunatcTrack2
	 *            PUNATC(Track2), tag '9F66'
	 * @param pNatcTrack2
	 *            NATC(Track2), tag '9F67'
	 * @return the data objects
	 */
	private MagStripeData build(final String pPcvc3Track1, final String pPunatcTrack1, final int pNatcTrack1,
			final String pPcvc3Track2, final String pPunatcTrack2, final int pNatcTrack2) {
		MagStripeData ret = new MagStripeData();
		ret.setPcvc3Track1(BytesUtils.fromString(pPcvc3Track1));
		ret.setPunatcTrack1(BytesUtils.fromString(pPunatcTrack1));
		ret.setNatcTrack1(pNatcTrack1);
		ret.setPcvc3Track2(BytesUtils.fromString(pPcvc3Track2));
		ret.setPunatcTrack2(BytesUtils.fromString(pPunatcTrack2));
		ret.setNatcTrack2(pNatcTrack2);
		return ret;
	}

	/**
	 * The personalisation of the MasterCardPpse2 capture.
	 * <p>
	 * PCVC3(Track2) is '000E', so its three non-zero bits b2, b3 and b4 send
	 * the three CVC3 (Track2) digits to the positions p2, p3 and p4. PUNATC
	 * (Track2) is '0E70': b5, b6, b7 then b10, b11, b12, and NATC(Track2) is 3,
	 * so nUN = 6 - 3 = 3. Book C-2 v2.11 S13.18 sends the unpredictable number
	 * to "the nUN least significant non-zero bits in PUNATC(Track2)", p5 to p7,
	 * and the counter to "the t most significant non-zero bits", p10 to p12.
	 * </p>
	 */
	@Test
	public void testFirstPersonalisation() {
		MagStripeData magStripe = build("00 00 00 00 0E 00", "00 00 00 00 00 7E", 3, "00 0E", "0E 70", 3);

		Assertions.assertThat(magStripe.getCvc3Track2DigitCount()).isEqualTo(3);
		Assertions.assertThat(magStripe.getCvc3Track2Positions()).containsExactly(2, 3, 4);
		Assertions.assertThat(magStripe.getUnpredictableNumberDigitCount()).isEqualTo(3);
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack2Positions()).containsExactly(5, 6, 7);
		Assertions.assertThat(magStripe.getApplicationTransactionCounterTrack2Positions()).containsExactly(10, 11, 12);

		// PCVC3(Track1) '00000000 0E00' flags b10, b11 and b12: a bitmap is
		// read across its bytes, the last one carrying the lowest positions
		Assertions.assertThat(magStripe.getCvc3Track1DigitCount()).isEqualTo(3);
		Assertions.assertThat(magStripe.getCvc3Track1Positions()).containsExactly(10, 11, 12);
		// PUNATC(Track1) '00000000 007E' flags b2 to b7, and NATC(Track1) is 3
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack1Positions()).containsExactly(2, 3, 4);
		Assertions.assertThat(magStripe.getApplicationTransactionCounterTrack1Positions()).containsExactly(5, 6, 7);
	}

	/**
	 * The personalisation of the MasterCardPpse3 capture, "9F 62 06 00 00 00 00
	 * 07 00 9F 63 06 00 00 00 00 00 FE [...] 9F 64 01 04 9F 65 02 07 00 9F 66 02
	 * 00 FE [...] 9F 67 01 04".
	 * <p>
	 * The two tracks are laid out the other way around: here PCVC3(Track2) is
	 * '0700', whose non-zero bits b9, b10 and b11 are carried by the first byte
	 * of the bitmap, and PUNATC(Track2) is '00FE', whose seven non-zero bits are
	 * carried by the second one.
	 * </p>
	 */
	@Test
	public void testSecondPersonalisation() {
		MagStripeData magStripe = build("00 00 00 00 07 00", "00 00 00 00 00 FE", 4, "07 00", "00 FE", 4);

		Assertions.assertThat(magStripe.getCvc3Track2DigitCount()).isEqualTo(3);
		Assertions.assertThat(magStripe.getCvc3Track2Positions()).containsExactly(9, 10, 11);
		// nUN = 7 non-zero bits - 4 counter digits
		Assertions.assertThat(magStripe.getUnpredictableNumberDigitCount()).isEqualTo(3);
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack2Positions()).containsExactly(2, 3, 4);
		Assertions.assertThat(magStripe.getApplicationTransactionCounterTrack2Positions()).containsExactly(5, 6, 7, 8);

		Assertions.assertThat(magStripe.getCvc3Track1Positions()).containsExactly(9, 10, 11);
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack1Positions()).containsExactly(2, 3, 4);
		Assertions.assertThat(magStripe.getApplicationTransactionCounterTrack1Positions()).containsExactly(5, 6, 7, 8);
	}

	/**
	 * Book C-2 v2.11 S7.20 checks the four data objects of Table 6.4 before
	 * going on, then S7.22 checks that both tracks agree on the number of
	 * unpredictable number digits: "(Number of non-zero bits in PUNATC(Track1) -
	 * NATC(Track1) &ne; nUN)" sends the transaction to the error state. Both
	 * captures satisfy it, six non-zero bits against three counter digits for
	 * the first one and seven against four for the second.
	 */
	@Test
	public void testConsistencyOfTheTwoTracks() {
		MagStripeData magStripe = build("00 00 00 00 0E 00", "00 00 00 00 00 7E", 3, "00 0E", "0E 70", 3);
		// Nothing is mandatory until the Track 2 Data is there
		Assertions.assertThat(magStripe.isMandatoryDataPresent()).isFalse();
		Assertions.assertThat(magStripe.isConsistent()).isFalse();

		magStripe.setTrack2Data(BytesUtils.fromString("52 00 00 00 00 00 00 00 D0 11 92 02 00 00 01 00 00 00 0F"));
		Assertions.assertThat(magStripe.isMandatoryDataPresent()).isTrue();
		// Track 1 Data is absent, so the first track is not checked
		Assertions.assertThat(magStripe.isConsistent()).isTrue();

		// The Track 1 Data of the same record, 42 bytes of ISO/IEC 7813
		// Structure B: format code '42', PAN, '5E', name, '5E', expiry date,
		// service code and discretionary data
		magStripe.setTrack1Data(BytesUtils.fromString("42 35 32 30 30 30 30 30 30 30 30 30 30 30 30 30 30 5E 20 2F 5E"
				+ " 30 32 30 37 32 30 32 30 30 30 30 30 31 30 30 30 30 30 30 30 30"));
		Assertions.assertThat(magStripe.isConsistent()).isTrue();

		// A first track that disagrees with the second one on the number of
		// unpredictable number digits: four non-zero bits left against three
		magStripe.setNatcTrack1(2);
		Assertions.assertThat(magStripe.isConsistent()).isFalse();
	}

	/**
	 * Book C-2 v2.11 S7.22 refuses a card whose bitmaps leave no room for the
	 * unpredictable number, or too much: "nUN := (Number of non-zero bits in
	 * PUNATC(Track2)) - NATC(Track2). IF [(nUN &lt; 0) OR (nUN &gt; 8)] THEN
	 * GOTO S7.24.1".
	 * <p>
	 * The data objects below are the ones of the MasterCardPpse2 capture with
	 * its NATC(Track2) alone raised to a value its PUNATC(Track2) cannot
	 * honour, the malformed card the state machine guards against.
	 * </p>
	 */
	@Test
	public void testUnpredictableNumberDigitCountOutOfRange() {
		MagStripeData magStripe = new MagStripeData();
		magStripe.setTrack2Data(BytesUtils.fromString("52 00 00 00 00 00 00 00 D0 11 92 02 00 00 01 00 00 00 0F"));
		magStripe.setPcvc3Track2(BytesUtils.fromString("00 0E"));
		// Six non-zero bits and a counter said to take nine of them
		magStripe.setPunatcTrack2(BytesUtils.fromString("0E 70"));
		magStripe.setNatcTrack2(9);

		Assertions.assertThat(magStripe.isMandatoryDataPresent()).isTrue();
		Assertions.assertThat(magStripe.getUnpredictableNumberDigitCount()).isEqualTo(-3);
		Assertions.assertThat(magStripe.isConsistent()).isFalse();
		// A count the bitmap cannot honour flags no position at all
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack2Positions()).isEmpty();
		Assertions.assertThat(magStripe.getApplicationTransactionCounterTrack2Positions()).isEmpty();
	}

	/**
	 * A card that was not personalised for the Mag-stripe Mode, and the state of
	 * the model before anything was read
	 */
	@Test
	public void testEmptyProfile() {
		MagStripeData magStripe = new MagStripeData();

		Assertions.assertThat(magStripe.isPresent()).isFalse();
		Assertions.assertThat(magStripe.isMandatoryDataPresent()).isFalse();
		Assertions.assertThat(magStripe.isConsistent()).isFalse();
		Assertions.assertThat(magStripe.getCvc3Track1DigitCount()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(magStripe.getCvc3Track2DigitCount()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(magStripe.getUnpredictableNumberDigitCount()).isEqualTo(AbstractData.UNKNOWN);
		Assertions.assertThat(magStripe.getUdol()).isEmpty();
		Assertions.assertThat(magStripe.getCvc3Track1Positions()).isEmpty();
		Assertions.assertThat(magStripe.getCvc3Track2Positions()).isEmpty();
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack1Positions()).isEmpty();
		Assertions.assertThat(magStripe.getUnpredictableNumberTrack2Positions()).isEmpty();
		Assertions.assertThat(magStripe.toString()).contains("MagStripeData");
	}

}
