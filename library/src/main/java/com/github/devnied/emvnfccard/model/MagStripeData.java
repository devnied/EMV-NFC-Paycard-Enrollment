/*
 * Copyright (C) 2019 MILLAU Julien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.devnied.emvnfccard.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;

/**
 * Data objects a card was personalised with for the Mag-stripe Mode of EMV
 * Contactless Book C-2 (Kernel 2, the MasterCard M/Chip applications).
 * <p>
 * In that mode the Kernel does not build an EMV cryptogram: it reads Track 1
 * Data and Track 2 Data from the card and rewrites part of their discretionary
 * data field with a dynamic card verification code, an unpredictable number and
 * the application transaction counter. The card states where each of those
 * digits goes with the bitmaps of this class.
 * </p>
 * <p>
 * Book C-2 v2.11 section 4.7 fixes the meaning of a bitmap: "Each bit in the
 * bitmap refers to a position in the discretionary data. The least significant
 * bit of the bitmap, i.e. the rightmost bit b1, corresponds to position p1", p1
 * being the rightmost digit of the discretionary data field. "For Track 2 Data
 * mTRACK2 is a maximum of 13 digits, resulting in a bitmap of 16 bits or 2
 * bytes. For Track 1 Data the maximum value of mTRACK1 is 48 resulting in a
 * bitmap of length 6 bytes or 48 bits."
 * </p>
 * <p>
 * <b>Nothing here is computed.</b> The library never sends the COMPUTE
 * CRYPTOGRAPHIC CHECKSUM command that produces the CVC3, which is outside of
 * its read only scope: the two CVC3 fields are filled only when a response the
 * card sent anyway already carried them.
 * </p>
 * <p>
 * Every data object below is read with the READ RECORD command. Book C-2 v2.11
 * section 3.4.2: "If a tag refers to Card data, this data is retrieved through
 * READ RECORD commands - as part of reading the records listed in the
 * Application File Locator - or through a GET DATA command. The Kernel has a
 * list of data objects that are read using GET DATA (See Table 5.16). All other
 * data objects are read using READ RECORD commands." Table 5.16 only lists
 * '9F70' to '9F79', so none of these is a GET DATA data object.
 * </p>
 * <p>
 * Mag-stripe Mode was withdrawn from the specification by Book C-2 v2.12 (May
 * 2026), which implements Specification Bulletin No. 317 "Mag-stripe Mode
 * Implementation Option Deprecated": every reference above is to v2.11, the
 * last version defining the mode. Cards personalised for it are still in the
 * field, which is why the library keeps reading them.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class MagStripeData extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 4479327404175573471L;

	/**
	 * Number of bits held by a byte of a bitmap
	 */
	private static final int BITS_PER_BYTE = 8;

	/**
	 * Highest number of Unpredictable Number digits Book C-2 v2.11 S7.22
	 * accepts: "IF [(nUN &lt; 0) OR (nUN &gt; 8)] THEN GOTO S7.24.1"
	 */
	public static final int MAX_UNPREDICTABLE_NUMBER_DIGITS = 8;

	/**
	 * Track 1 Data (tag '56'), the bitmaps of the first track address the
	 * discretionary data field of this value.<br>
	 * Book C-2 v2.11 A.1.167: "Track 1 Data contains the data objects of the
	 * track 1 according to [ISO/IEC 7813] Structure B, excluding start
	 * sentinel, end sentinel and LRC. The Track 1 Data may be present in the
	 * file read using the READ RECORD command during a Mag-stripe Mode
	 * transaction." Format ans, length var. up to 76.
	 */
	private byte[] track1Data;

	/**
	 * Track 2 Data (tag '9F6B'), the bitmaps of the second track address the
	 * discretionary data field of this value.<br>
	 * Book C-2 v2.11 A.1.169: "Track 2 Data contains the data objects of the
	 * track 2 according to [ISO/IEC 7813], excluding start sentinel, end
	 * sentinel and LRC. The Track 2 Data has a maximum length of 37 positions
	 * and is present in the file read using the READ RECORD command during a
	 * Mag-stripe Mode transaction." Format b, length var. up to 19.
	 */
	private byte[] track2Data;

	/**
	 * PCVC3(Track1) (tag '9F62'), 6 bytes.<br>
	 * Book C-2 v2.11 A.1.112: "PCVC3(Track1) indicates to the Kernel the
	 * positions in the discretionary data field of the Track 1 Data where the
	 * CVC3 (Track1) digits must be copied."
	 */
	private byte[] pcvc3Track1;

	/**
	 * PUNATC(Track1) (tag '9F63'), 6 bytes.<br>
	 * Book C-2 v2.11 A.1.127: "PUNATC(Track1) indicates to the Kernel the
	 * positions in the discretionary data field of Track 1 Data where the
	 * Unpredictable Number (Numeric) digits and Application Transaction Counter
	 * digits have to be copied." The single bitmap flags both, split by
	 * {@link #natcTrack1}.
	 */
	private byte[] punatcTrack1;

	/**
	 * NATC(Track1) (tag '9F64'), 1 byte, a count and not a bitmap.<br>
	 * Book C-2 v2.11 A.1.105: "The value of NATC(Track1) represents the number
	 * of digits of the Application Transaction Counter to be included in the
	 * discretionary data field of Track 1 Data."
	 */
	private int natcTrack1 = UNKNOWN;

	/**
	 * PCVC3(Track2) (tag '9F65'), 2 bytes.<br>
	 * Book C-2 v2.11 A.1.113: "PCVC3(Track2) indicates to the Kernel the
	 * positions in the discretionary data field of the Track 2 Data where the
	 * CVC3 (Track2) digits must be copied." Mandatory in Mag-stripe Mode (Table
	 * 6.4).
	 */
	private byte[] pcvc3Track2;

	/**
	 * PUNATC(Track2) (tag '9F66'), 2 bytes.<br>
	 * Book C-2 v2.11 A.1.128: "PUNATC(Track2) indicates to the Kernel the
	 * positions in the discretionary data field of Track 2 Data where the
	 * Unpredictable Number (Numeric) digits and Application Transaction Counter
	 * digits have to be copied." Mandatory in Mag-stripe Mode (Table 6.4).
	 * <p>
	 * Tag '9F66' is the Terminal Transaction Qualifiers of Kernel 3, which
	 * Book C-3 v2.12 Annex A Table A-2 sources on the reader and codes on 4
	 * bytes. Only an application running on Kernel 2 fills this field.
	 * </p>
	 */
	private byte[] punatcTrack2;

	/**
	 * NATC(Track2) (tag '9F67'), 1 byte, a count and not a bitmap.<br>
	 * Book C-2 v2.11 A.1.106: "The value of NATC(Track2) represents the number
	 * of digits of the Application Transaction Counter to be included in the
	 * discretionary data field of Track 2 Data." Mandatory in Mag-stripe Mode
	 * (Table 6.4).
	 */
	private int natcTrack2 = UNKNOWN;

	/**
	 * UDOL (tag '9F69'), the raw data object list.<br>
	 * Book C-2 v2.11 A.1.178: "The UDOL is the DOL that specifies the data
	 * objects to be included in the data field of the COMPUTE CRYPTOGRAPHIC
	 * CHECKSUM command. The UDOL must at least include the Unpredictable Number
	 * (Numeric). The UDOL is not mandatory for the Card. If it is not present
	 * in the Card, then the Default UDOL is used."
	 * <p>
	 * Tag '9F69' is the Card Authentication Related Data of Kernel 3 (Book C-3
	 * v2.12 Annex A Table A-2), which the card also sends in a record: only the
	 * kernel tells the two apart. Only an application running on Kernel 2 fills
	 * this field.
	 * </p>
	 */
	private byte[] rawUdol;

	/**
	 * UDOL (tag '9F69') parsed into the list of tags and lengths it is made of.
	 * <p>
	 * The list is the command the card would have the reader build. This
	 * library reads it and never executes it: the COMPUTE CRYPTOGRAPHIC
	 * CHECKSUM command it feeds is one the library does not send.
	 * </p>
	 */
	private transient List<TagAndLength> udol = new ArrayList<TagAndLength>();

	/**
	 * CVC3 (Track1) (tag '9F60'), 2 bytes.<br>
	 * Book C-2 v2.11 A.1.31: "The CVC3 (Track1) is a 2-byte cryptogram returned
	 * by the Card in the response to the COMPUTE CRYPTOGRAPHIC CHECKSUM
	 * command." That command is outside of the read only scope of this library,
	 * so the field stays null unless a response read for another reason carried
	 * the data object.
	 */
	private byte[] cvc3Track1;

	/**
	 * CVC3 (Track2) (tag '9F61'), 2 bytes.<br>
	 * Book C-2 v2.11 A.1.32: "The CVC3 (Track2) is a 2-byte cryptogram returned
	 * by the Card in the response to the COMPUTE CRYPTOGRAPHIC CHECKSUM
	 * command." Read only, see {@link #cvc3Track1}.
	 */
	private byte[] cvc3Track2;

	/**
	 * Number of CVC3 (Track1) digits the card asks the Kernel to write, that is
	 * the number of non-zero bits of {@link #pcvc3Track1}.<br>
	 * Book C-2 v2.11 S13.21: "q := Number of non-zero bits in PCVC3(Track1)".
	 */
	private int cvc3Track1DigitCount = UNKNOWN;

	/**
	 * Number of CVC3 (Track2) digits the card asks the Kernel to write, that is
	 * the number of non-zero bits of {@link #pcvc3Track2}.<br>
	 * Book C-2 v2.11 S13.18: "q := Number of non-zero bits in PCVC3(Track2)".
	 */
	private int cvc3Track2DigitCount = UNKNOWN;

	/**
	 * Number of Unpredictable Number (Numeric) digits, nUN.<br>
	 * Book C-2 v2.11 A.1.108 defines it as "Number of non-zero bits in
	 * PUNATC(Track2) - NATC(Track2)", and S7.22 rejects a card whose value is
	 * negative or greater than {@link #MAX_UNPREDICTABLE_NUMBER_DIGITS}.
	 */
	private int unpredictableNumberDigitCount = UNKNOWN;
	/**
	 * Mag-stripe Application Version Number (Card) (tag '9F6C' on Kernel 2
	 * / MasterCard), binary value.<br>Book C-2 v2.11 A.1.6: "Version number
	 * assigned by the payment system for the Mag-stripe Mode application",
	 * format b, length 2. {@link AbstractData#UNKNOWN} when absent.
	 */
	private int applicationVersionNumber = UNKNOWN;


	/**
	 * Get the field track1Data
	 *
	 * @return the Track 1 Data (tag '56')
	 */
	public byte[] getTrack1Data() {
		return copy(track1Data);
	}

	/**
	 * Setter for the field track1Data
	 *
	 * @param track1Data
	 *            the track1Data to set
	 */
	public void setTrack1Data(final byte[] track1Data) {
		this.track1Data = copy(track1Data);
	}

	/**
	 * Get the field track2Data
	 *
	 * @return the Track 2 Data (tag '9F6B')
	 */
	public byte[] getTrack2Data() {
		return copy(track2Data);
	}

	/**
	 * Setter for the field track2Data
	 *
	 * @param track2Data
	 *            the track2Data to set
	 */
	public void setTrack2Data(final byte[] track2Data) {
		this.track2Data = copy(track2Data);
	}

	/**
	 * Get the field pcvc3Track1
	 *
	 * @return the PCVC3(Track1) bitmap (tag '9F62')
	 */
	public byte[] getPcvc3Track1() {
		return copy(pcvc3Track1);
	}

	/**
	 * Setter for the field pcvc3Track1
	 *
	 * @param pcvc3Track1
	 *            the pcvc3Track1 to set
	 */
	public void setPcvc3Track1(final byte[] pcvc3Track1) {
		this.pcvc3Track1 = copy(pcvc3Track1);
		cvc3Track1DigitCount = countBits(pcvc3Track1);
	}

	/**
	 * Get the field punatcTrack1
	 *
	 * @return the PUNATC(Track1) bitmap (tag '9F63')
	 */
	public byte[] getPunatcTrack1() {
		return copy(punatcTrack1);
	}

	/**
	 * Setter for the field punatcTrack1
	 *
	 * @param punatcTrack1
	 *            the punatcTrack1 to set
	 */
	public void setPunatcTrack1(final byte[] punatcTrack1) {
		this.punatcTrack1 = copy(punatcTrack1);
	}

	/**
	 * Get the field natcTrack1
	 *
	 * @return the NATC(Track1) count (tag '9F64') or {@link #UNKNOWN}
	 */
	public int getNatcTrack1() {
		return natcTrack1;
	}

	/**
	 * Setter for the field natcTrack1
	 *
	 * @param natcTrack1
	 *            the natcTrack1 to set
	 */
	public void setNatcTrack1(final int natcTrack1) {
		this.natcTrack1 = natcTrack1;
	}

	/**
	 * Get the field pcvc3Track2
	 *
	 * @return the PCVC3(Track2) bitmap (tag '9F65')
	 */
	public byte[] getPcvc3Track2() {
		return copy(pcvc3Track2);
	}

	/**
	 * Setter for the field pcvc3Track2
	 *
	 * @param pcvc3Track2
	 *            the pcvc3Track2 to set
	 */
	public void setPcvc3Track2(final byte[] pcvc3Track2) {
		this.pcvc3Track2 = copy(pcvc3Track2);
		cvc3Track2DigitCount = countBits(pcvc3Track2);
	}

	/**
	 * Get the field punatcTrack2
	 *
	 * @return the PUNATC(Track2) bitmap (tag '9F66')
	 */
	public byte[] getPunatcTrack2() {
		return copy(punatcTrack2);
	}

	/**
	 * Setter for the field punatcTrack2
	 *
	 * @param punatcTrack2
	 *            the punatcTrack2 to set
	 */
	public void setPunatcTrack2(final byte[] punatcTrack2) {
		this.punatcTrack2 = copy(punatcTrack2);
		refreshUnpredictableNumberDigitCount();
	}

	/**
	 * Get the field natcTrack2
	 *
	 * @return the NATC(Track2) count (tag '9F67') or {@link #UNKNOWN}
	 */
	public int getNatcTrack2() {
		return natcTrack2;
	}

	/**
	 * Setter for the field natcTrack2
	 *
	 * @param natcTrack2
	 *            the natcTrack2 to set
	 */
	public void setNatcTrack2(final int natcTrack2) {
		this.natcTrack2 = natcTrack2;
		refreshUnpredictableNumberDigitCount();
	}

	/**
	 * Get the field rawUdol
	 *
	 * @return the raw UDOL (tag '9F69')
	 */
	public byte[] getRawUdol() {
		return copy(rawUdol);
	}

	/**
	 * Get the field udol
	 *
	 * @return the UDOL parsed into tags and lengths, never null
	 */
	public List<TagAndLength> getUdol() {
		return Collections.unmodifiableList(udol != null ? udol : new ArrayList<TagAndLength>());
	}

	/**
	 * Setter for the field udol
	 *
	 * @param pRawUdol
	 *            raw value of the tag '9F69'
	 * @param pUdol
	 *            the same value parsed into tags and lengths
	 */
	public void setUdol(final byte[] pRawUdol, final List<TagAndLength> pUdol) {
		rawUdol = copy(pRawUdol);
		udol = pUdol != null ? new ArrayList<TagAndLength>(pUdol) : new ArrayList<TagAndLength>();
	}

	/**
	 * Get the field cvc3Track1
	 *
	 * @return the CVC3 (Track1) (tag '9F60')
	 */
	public byte[] getCvc3Track1() {
		return copy(cvc3Track1);
	}

	/**
	 * Setter for the field cvc3Track1
	 *
	 * @param cvc3Track1
	 *            the cvc3Track1 to set
	 */
	public void setCvc3Track1(final byte[] cvc3Track1) {
		this.cvc3Track1 = copy(cvc3Track1);
	}

	/**
	 * Get the field cvc3Track2
	 *
	 * @return the CVC3 (Track2) (tag '9F61')
	 */
	public byte[] getCvc3Track2() {
		return copy(cvc3Track2);
	}

	/**
	 * Setter for the field cvc3Track2
	 *
	 * @param cvc3Track2
	 *            the cvc3Track2 to set
	 */
	public void setCvc3Track2(final byte[] cvc3Track2) {
		this.cvc3Track2 = copy(cvc3Track2);
	}

	/**
	 * Get the field cvc3Track1DigitCount
	 *
	 * @return the number of CVC3 (Track1) digits or {@link #UNKNOWN}
	 */
	public int getCvc3Track1DigitCount() {
		return cvc3Track1DigitCount;
	}

	/**
	 * Get the field cvc3Track2DigitCount
	 *
	 * @return the number of CVC3 (Track2) digits or {@link #UNKNOWN}
	 */
	public int getCvc3Track2DigitCount() {
		return cvc3Track2DigitCount;
	}

	/**
	 * Get the field unpredictableNumberDigitCount
	 *
	 * @return nUN or {@link #UNKNOWN} when the card sent neither PUNATC(Track2)
	 *         nor NATC(Track2)
	 */
	public int getUnpredictableNumberDigitCount() {
		return unpredictableNumberDigitCount;
	}

	/**
	 * Method used to know whether the card holds anything of the Mag-stripe
	 * Mode profile.
	 *
	 * @return true if at least one data object of the profile was read
	 */
	public boolean isPresent() {
		return track1Data != null || track2Data != null || pcvc3Track1 != null || punatcTrack1 != null
				|| natcTrack1 != UNKNOWN || pcvc3Track2 != null || punatcTrack2 != null || natcTrack2 != UNKNOWN
				|| rawUdol != null || cvc3Track1 != null || cvc3Track2 != null;
	}

	/**
	 * Method used to know whether the card carries the four data objects Book
	 * C-2 v2.11 Table 6.4 makes mandatory for a Mag-stripe Mode transaction:
	 * Track 2 Data, PUNATC(Track2), PCVC3(Track2) and NATC(Track2). S7.20 tests
	 * exactly that list before going on with the transaction.
	 *
	 * @return true if the mandatory data objects are all present
	 */
	public boolean isMandatoryDataPresent() {
		return track2Data != null && track2Data.length > 0 && punatcTrack2 != null && punatcTrack2.length > 0
				&& pcvc3Track2 != null && pcvc3Track2.length > 0 && natcTrack2 != UNKNOWN;
	}

	/**
	 * Method used to replay the consistency checks Book C-2 v2.11 S7.22 runs on
	 * the mag-stripe mode data objects: "nUN := (Number of non-zero bits in
	 * PUNATC(Track2)) - NATC(Track2). IF [(nUN &lt; 0) OR (nUN &gt; 8)] THEN
	 * GOTO S7.24.1", then, when Track 1 Data is present, NATC(Track1),
	 * PCVC3(Track1) and PUNATC(Track1) must all be present and "(Number of
	 * non-zero bits in PUNATC(Track1) - NATC(Track1) &ne; nUN)" must not hold.
	 * <p>
	 * The check is reported and never acted upon: a reader says what it saw
	 * instead of refusing to read.
	 * </p>
	 *
	 * @return true when the profile read is the one the specification describes
	 */
	public boolean isConsistent() {
		if (!isMandatoryDataPresent() || unpredictableNumberDigitCount < 0
				|| unpredictableNumberDigitCount > MAX_UNPREDICTABLE_NUMBER_DIGITS) {
			return false;
		}
		if (track1Data == null || track1Data.length == 0) {
			return true;
		}
		if (natcTrack1 == UNKNOWN || pcvc3Track1 == null || pcvc3Track1.length == 0 || punatcTrack1 == null
				|| punatcTrack1.length == 0) {
			return false;
		}
		return countBits(punatcTrack1) - natcTrack1 == unpredictableNumberDigitCount;
	}

	/**
	 * Method used to get the positions of the discretionary data field of Track
	 * 1 Data the CVC3 (Track1) digits are written to, as flagged by
	 * PCVC3(Track1).
	 *
	 * @return the positions, ascending, p1 being the rightmost digit of the
	 *         discretionary data field
	 */
	public List<Integer> getCvc3Track1Positions() {
		return positions(pcvc3Track1);
	}

	/**
	 * Method used to get the positions of the discretionary data field of Track
	 * 2 Data the CVC3 (Track2) digits are written to, as flagged by
	 * PCVC3(Track2).
	 *
	 * @return the positions, ascending, p1 being the rightmost digit of the
	 *         discretionary data field
	 */
	public List<Integer> getCvc3Track2Positions() {
		return positions(pcvc3Track2);
	}

	/**
	 * Method used to get the positions of the discretionary data field of Track
	 * 1 Data the Unpredictable Number (Numeric) digits are written to.<br>
	 * Book C-2 v2.11 S13.21: "The eligible positions in the 'Discretionary Data'
	 * in Track 1 Data are indicated by the nUN least significant non-zero bits
	 * in PUNATC(Track1)."
	 *
	 * @return the positions, ascending
	 */
	public List<Integer> getUnpredictableNumberTrack1Positions() {
		return leastSignificant(positions(punatcTrack1), natcTrack1);
	}

	/**
	 * Method used to get the positions of the discretionary data field of Track
	 * 2 Data the Unpredictable Number (Numeric) digits are written to.<br>
	 * Book C-2 v2.11 S13.18: "The eligible positions in the 'Discretionary Data'
	 * in Track 2 Data are indicated by the nUN least significant non-zero bits
	 * in PUNATC(Track2)."
	 *
	 * @return the positions, ascending
	 */
	public List<Integer> getUnpredictableNumberTrack2Positions() {
		return leastSignificant(positions(punatcTrack2), natcTrack2);
	}

	/**
	 * Method used to get the positions of the discretionary data field of Track
	 * 1 Data the Application Transaction Counter digits are written to.<br>
	 * Book C-2 v2.11 S13.21: "The eligible positions in the 'Discretionary Data'
	 * in Track 1 Data are indicated by the t most significant non-zero bits in
	 * PUNATC(Track1)", t being NATC(Track1).
	 *
	 * @return the positions, ascending
	 */
	public List<Integer> getApplicationTransactionCounterTrack1Positions() {
		return mostSignificant(positions(punatcTrack1), natcTrack1);
	}

	/**
	 * Method used to get the positions of the discretionary data field of Track
	 * 2 Data the Application Transaction Counter digits are written to.<br>
	 * Book C-2 v2.11 S13.18: "The eligible positions in the 'Discretionary Data'
	 * in Track 2 Data are indicated by the t most significant non-zero bits in
	 * PUNATC(Track2)", t being NATC(Track2).
	 *
	 * @return the positions, ascending
	 */
	public List<Integer> getApplicationTransactionCounterTrack2Positions() {
		return mostSignificant(positions(punatcTrack2), natcTrack2);
	}

	/**
	 * Method used to recompute nUN, which both PUNATC(Track2) and NATC(Track2)
	 * take part in and which the card sends in two separate data objects
	 */
	private void refreshUnpredictableNumberDigitCount() {
		if (punatcTrack2 == null || natcTrack2 == UNKNOWN) {
			unpredictableNumberDigitCount = UNKNOWN;
		} else {
			unpredictableNumberDigitCount = countBits(punatcTrack2) - natcTrack2;
		}
	}

	/**
	 * Method used to count the non-zero bits of a bitmap
	 *
	 * @param pBitmap
	 *            bitmap of the discretionary data
	 * @return the number of non-zero bits, or {@link #UNKNOWN} when the card
	 *         sent no bitmap
	 */
	private static int countBits(final byte[] pBitmap) {
		if (pBitmap == null) {
			return UNKNOWN;
		}
		int ret = 0;
		for (byte current : pBitmap) {
			ret += Integer.bitCount(current & 0xFF);
		}
		return ret;
	}

	/**
	 * Method used to list the positions a bitmap flags.
	 * <p>
	 * Book C-2 v2.11 section 4.7: "The least significant bit of the bitmap, i.e.
	 * the rightmost bit b1, corresponds to position p1", so the bit of weight
	 * one of the last byte is the position 1.
	 * </p>
	 *
	 * @param pBitmap
	 *            bitmap of the discretionary data
	 * @return the flagged positions, ascending, never null
	 */
	private static List<Integer> positions(final byte[] pBitmap) {
		List<Integer> ret = new ArrayList<Integer>();
		if (pBitmap == null) {
			return ret;
		}
		for (int bit = 0; bit < pBitmap.length * BITS_PER_BYTE; bit++) {
			byte current = pBitmap[pBitmap.length - 1 - bit / BITS_PER_BYTE];
			if ((current >> bit % BITS_PER_BYTE & 0x01) == 1) {
				ret.add(bit + 1);
			}
		}
		return ret;
	}

	/**
	 * Method used to keep the least significant positions of a PUNATC bitmap,
	 * the ones the Unpredictable Number (Numeric) digits go to
	 *
	 * @param pPositions
	 *            positions the bitmap flags, ascending
	 * @param pNatc
	 *            number of Application Transaction Counter digits
	 * @return the positions left to the unpredictable number, never null
	 */
	private static List<Integer> leastSignificant(final List<Integer> pPositions, final int pNatc) {
		if (pNatc == UNKNOWN || pNatc < 0 || pNatc > pPositions.size()) {
			return new ArrayList<Integer>();
		}
		return new ArrayList<Integer>(pPositions.subList(0, pPositions.size() - pNatc));
	}

	/**
	 * Method used to keep the most significant positions of a PUNATC bitmap,
	 * the ones the Application Transaction Counter digits go to
	 *
	 * @param pPositions
	 *            positions the bitmap flags, ascending
	 * @param pNatc
	 *            number of Application Transaction Counter digits
	 * @return the positions taken by the counter, never null
	 */
	private static List<Integer> mostSignificant(final List<Integer> pPositions, final int pNatc) {
		if (pNatc == UNKNOWN || pNatc < 0 || pNatc > pPositions.size()) {
			return new ArrayList<Integer>();
		}
		return new ArrayList<Integer>(pPositions.subList(pPositions.size() - pNatc, pPositions.size()));
	}

	/**
	 * Method used to copy a value, so that neither the caller of a setter nor
	 * the caller of a getter shares the array held by this class
	 *
	 * @param pValue
	 *            value to copy
	 * @return a copy of the value, or null
	 */
	private static byte[] copy(final byte[] pValue) {
		return pValue != null ? Arrays.copyOf(pValue, pValue.length) : null;
	}

	/**
	 * Mag-stripe Application Version Number (Card) (tag '9F6C' on Kernel 2
	 * / MasterCard), binary value.<br>Book C-2 v2.11 A.1.6: "Version number
	 * assigned by the payment system for the Mag-stripe Mode application",
	 * format b, length 2. {@link AbstractData#UNKNOWN} when absent.
	 *
	 * @return the version number, or {@link AbstractData#UNKNOWN}
	 */
	public int getApplicationVersionNumber() {
		return applicationVersionNumber;
	}

	/**
	 * Setter for the field applicationVersionNumber
	 *
	 * @param applicationVersionNumber
	 *            the applicationVersionNumber to set
	 */
	public void setApplicationVersionNumber(final int applicationVersionNumber) {
		this.applicationVersionNumber = applicationVersionNumber;
	}

}
