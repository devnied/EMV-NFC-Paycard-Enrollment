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
package com.github.devnied.emvnfccard.iso7816emv;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Reader of the BER-TLV encoding EMV uses, over a byte array.
 * <p>
 * The rules implemented here are those of EMV 4.3 Book 3 Annex B and of
 * ISO/IEC 7816-4:
 * </p>
 * <ul>
 * <li>a tag is one byte, or several when the five low order bits of the first
 * one are all set, the following bytes carrying a continuation bit; the bytes
 * are returned as they were read, so a tag such as 'DF8104' keeps its
 * continuation bit and can be looked up in the dictionary</li>
 * <li>a length is one byte below 128, otherwise the low order bits of the
 * first byte give the number of bytes the length itself takes</li>
 * <li>'00' bytes may appear before, between or after the data objects and
 * carry no meaning (Annex B1). '<b>FF</b>' is <b>not</b> skipped: EMV assigns
 * real tags beginning with 'FF', and a stream that skipped them could never
 * report one</li>
 * </ul>
 * <p>
 * A malformed encoding is reported as an {@link IOException} rather than an
 * unchecked exception, so that a caller reading a card can degrade instead of
 * failing.
 * </p>
 * <p>
 * The array given to the constructor is <b>not copied</b>: the caller must not
 * modify it while the stream is being read. An instance holds a position and
 * is <b>not thread safe</b>; a card response being parsed by two threads needs
 * two instances.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public class BerTlvInputStream {

	/**
	 * Mask of the five low order bits of a tag first byte, all set when the
	 * tag takes more than one byte
	 */
	private static final int MULTI_BYTE_TAG = 0x1F;

	/**
	 * Continuation bit of a tag byte, and long form marker of a length byte
	 */
	private static final int CONTINUATION = 0x80;

	/**
	 * Highest number of bytes a tag may take. ISO/IEC 7816-4 supports tag
	 * fields of one, two and three bytes, and the longest tag of the EMV
	 * dictionary takes three: a fourth byte is accepted so that an unknown tag
	 * is reported rather than rejected, a fifth is refused.
	 */
	private static final int MAX_TAG_LENGTH = 4;

	/**
	 * Highest number of bytes the long form of a length may take. ISO/IEC
	 * 7816-4 stops at '84' followed by four bytes, which is also the widest
	 * value an int can hold. EMV itself only ever uses one or two, three for
	 * the issuer script templates.
	 */
	private static final int MAX_LENGTH_BYTES = 4;

	/**
	 * Data being read
	 */
	private final byte[] data;

	/**
	 * Position of the next byte to read
	 */
	private int index;

	/**
	 * Position saved by {@link #mark()}
	 */
	private int mark = -1;

	/**
	 * Constructor using the data to read
	 *
	 * @param pData
	 *            data to read, may be null
	 */
	public BerTlvInputStream(final byte[] pData) {
		data = pData != null ? pData : new byte[0];
	}

	/**
	 * Number of bytes left to read
	 *
	 * @return the number of bytes still available
	 */
	public int available() {
		return data.length - index;
	}

	/**
	 * Read the next byte
	 *
	 * @return the byte read, or -1 at the end of the data
	 */
	public int read() {
		if (index >= data.length) {
			return -1;
		}
		return data[index++] & 0xFF;
	}

	/**
	 * Read the requested number of bytes
	 *
	 * @param pLength
	 *            number of bytes to read
	 * @return the bytes read
	 * @throws IOException
	 *             when fewer bytes are available
	 */
	public byte[] read(final int pLength) throws IOException {
		if (pLength < 0) {
			throw new IllegalArgumentException("A negative number of bytes cannot be read: " + pLength);
		}
		if (pLength > available()) {
			throw new EOFException("Requested " + pLength + " bytes, " + available() + " available");
		}
		byte[] ret = new byte[pLength];
		System.arraycopy(data, index, ret, 0, pLength);
		index += pLength;
		return ret;
	}

	/**
	 * Save the current position, to be restored by {@link #reset()}
	 */
	public void mark() {
		mark = index;
	}

	/**
	 * Restore the position saved by {@link #mark()}
	 *
	 * @throws IOException
	 *             when no position was saved
	 */
	public void reset() throws IOException {
		if (mark < 0) {
			throw new IOException("reset() without a mark()");
		}
		index = mark;
	}

	/**
	 * Read a tag, skipping the '00' bytes that may pad the data.
	 *
	 * @return the bytes of the tag, as they were read
	 * @throws IOException
	 *             at the end of the data, or when the tag never ends
	 */
	public byte[] readTag() throws IOException {
		int first = read();
		// A '00' byte before, between or after a data object carries no
		// meaning (EMV 4.3 Book 3 Annex B1)
		while (first == 0x00) {
			first = read();
		}
		if (first < 0) {
			throw new EOFException("No tag left to read");
		}
		byte[] tag = new byte[MAX_TAG_LENGTH];
		tag[0] = (byte) first;
		int length = 1;
		if ((first & MULTI_BYTE_TAG) == MULTI_BYTE_TAG) {
			int next;
			do {
				next = read();
				if (next < 0) {
					throw new EOFException("Truncated tag");
				}
				if (length >= MAX_TAG_LENGTH) {
					throw new IOException("A tag of more than " + MAX_TAG_LENGTH + " bytes is not an EMV tag");
				}
				tag[length++] = (byte) next;
			} while ((next & CONTINUATION) == CONTINUATION);
		}
		return Arrays.copyOf(tag, length);
	}

	/**
	 * Read a length.
	 *
	 * @return the length read
	 * @throws IOException
	 *             at the end of the data, when the indefinite form is used,
	 *             when the long form announces more bytes than a length may
	 *             take, or when the result does not fit a positive int. A
	 *             length that is merely implausible for a card is returned as
	 *             it was read, the caller decides what to do with it.
	 */
	public int readLength() throws IOException {
		int first = read();
		if (first < 0) {
			throw new EOFException("No length left to read");
		}
		if ((first & CONTINUATION) == 0) {
			// Short form, the byte is the length
			return first;
		}
		int count = first & 0x7F;
		if (count == 0 || count > MAX_LENGTH_BYTES) {
			// '80' is the indefinite form, which EMV does not use, and a
			// longer count would not fit an int
			throw new IOException("Unsupported length form: " + count + " bytes");
		}
		int length = 0;
		for (int i = 0; i < count; i++) {
			int next = read();
			if (next < 0) {
				throw new EOFException("Truncated length");
			}
			length = length << 8 | next;
		}
		if (length < 0) {
			throw new IOException("Negative length");
		}
		return length;
	}

	/**
	 * Encode a length the way a card would, so that a value read can be
	 * rendered with the bytes it was carried in.
	 *
	 * @param pLength
	 *            length to encode
	 * @return the encoded length
	 */
	public static byte[] encodeLength(final int pLength) {
		if (pLength < 0) {
			throw new IllegalArgumentException("A length cannot be negative: " + pLength);
		}
		if (pLength < CONTINUATION) {
			return new byte[] { (byte) pLength };
		}
		if (pLength <= 0xFF) {
			return new byte[] { (byte) 0x81, (byte) pLength };
		}
		if (pLength <= 0xFFFF) {
			return new byte[] { (byte) 0x82, (byte) (pLength >> 8), (byte) pLength };
		}
		if (pLength <= 0xFFFFFF) {
			return new byte[] { (byte) 0x83, (byte) (pLength >> 16), (byte) (pLength >> 8), (byte) pLength };
		}
		return new byte[] { (byte) 0x84, (byte) (pLength >> 24), (byte) (pLength >> 16), (byte) (pLength >> 8),
				(byte) pLength };
	}

}
