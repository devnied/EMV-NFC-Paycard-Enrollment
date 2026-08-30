package com.github.devnied.emvnfccard.utils;

/*
 * Copyright 2010 sasc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import fr.devnied.bitlib.BytesUtils;
import com.github.devnied.emvnfccard.iso7816emv.BerTlvInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * List of utils methods to manipulate TLV
 *
 * @author MILLAU Julien
 *
 */
public final class TlvUtil {

	// Class logger
	private static final Logger LOGGER = LoggerFactory.getLogger(TlvUtil.class);

	/**
	 * Deepest nesting of constructed data objects the parsing follows. A card
	 * is free to answer a template that contains a template, and nothing in
	 * the encoding bounds that: without this, a response built for it exhausts
	 * the stack, and the resulting error escapes the public reading method.
	 */
	public static final int MAX_DEPTH = 16;

	/**
	 * Method used to find Tag with ID
	 *
	 * @param tagIdBytes
	 *            the tag to find
	 * @return the tag found
	 */
	private static ITag searchTagById(final byte[] tagBytes) {
		return EmvTags.getNotNull(tagBytes);
	}

	/**
	 * Method used to render a data object list (a PDOL, a CDOL, the log
	 * format). The entries are read by {@link #parseTagAndLength(byte[])} so
	 * that the two never disagree on the encoding.
	 *
	 * @param data
	 *            raw data object list
	 * @param indentLength
	 *            number of spaces to indent each line with
	 * @return the rendered list
	 */
	public static String getFormattedTagAndLength(final byte[] data, final int indentLength) {
		StringBuilder buf = new StringBuilder();
		String indent = getSpaces(indentLength);
		boolean firstLine = true;
		for (TagAndLength tagAndLength : parseTagAndLength(data)) {
			if (firstLine) {
				firstLine = false;
			} else {
				buf.append("\n");
			}
			buf.append(indent);
			buf.append(prettyPrintHex(tagAndLength.getTag().getTagBytes()));
			buf.append(" ");
			buf.append(String.format("%02x", tagAndLength.getLength()));
			buf.append(" -- ");
			buf.append(tagAndLength.getTag().getName());
		}
		return buf.toString();
	}

	public static TLV getNextTLV(final BerTlvInputStream stream) {
		TLV tlv = null;
		try {
			// The shortest data object is a one byte tag followed by a length
			// of '00' (EMV 4.3 Book 3 Annex B: "If L = '00', the value field is
			// not present"), so two bytes are enough to read one
			int left = stream.available();
			if (left < 2) {
				return tlv;
			}
			byte[] tagBytes = stream.readTag();
			// ISO/IEC 7816-4 Annex D.1 lets '00' and 'FF' bytes appear before,
			// between or after data objects without any meaning. They are not
			// a tag, and a response that is not BER-TLV at all usually begins
			// with one of them
			if (isPadding(tagBytes)) {
				return tlv;
			}
			ITag tag = searchTagById(tagBytes);
			int length = stream.readLength();
			int available = stream.available();
			if (available >= length) {
				tlv = new TLV(tag, length, BerTlvInputStream.encodeLength(length), stream.read(length));
			} else if (tag.isConstructed() && available > 0) {
				// A card that announces a template longer than what it sent
				// still lets the objects it did send be read: the length of a
				// constructed object is redundant with the lengths of the
				// objects it holds. Only the constructed case is clamped, a
				// truncated primitive value would be handed back as if the
				// card had sent it, a shortened card number for instance.
				// The content is clamped only when it does begin with a
				// readable object, so that a response which is not BER-TLV at
				// all is still reported as such rather than parsed as a
				// truncated template
				byte[] value = stream.read(available);
				if (holdsReadableObject(value)) {
					LOGGER.warn("The template " + tag.getName() + " announces " + length + " bytes, " + available + " were sent");
					tlv = new TLV(tag, available, BerTlvInputStream.encodeLength(available), value);
				}
			}
		} catch (EOFException eof) {
			LOGGER.debug(eof.getMessage(), eof);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return tlv;
	}

	/**
	 * Method used to know if the bytes read as a tag are the padding a card is
	 * free to insert between data objects
	 *
	 * @param pTagBytes
	 *            bytes read as a tag
	 * @return true when every byte is '00' or every byte is 'FF'
	 */
	private static boolean isPadding(final byte[] pTagBytes) {
		if (pTagBytes == null || pTagBytes.length == 0) {
			return true;
		}
		return allEqual(pTagBytes, (byte) 0x00) || allEqual(pTagBytes, (byte) 0xFF);
	}

	/**
	 * Method used to know if every byte of an array holds the same value
	 *
	 * @param pData
	 *            array to test
	 * @param pValue
	 *            value to compare to
	 * @return true when every byte equals the parameter
	 */
	private static boolean allEqual(final byte[] pData, final byte pValue) {
		for (byte element : pData) {
			if (element != pValue) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method used to know if the parameter begins with a data object that can be
	 * read, which tells a template the card truncated from a response that is
	 * not BER-TLV encoded at all.
	 *
	 * @param pData
	 *            candidate content of a constructed data object
	 * @return true when the first tag and length of the parameter are readable
	 *         and the value they announce is present
	 */
	private static boolean holdsReadableObject(final byte[] pData) {
		BerTlvInputStream stream = new BerTlvInputStream(pData);
		try {
			stream.readTag();
			return stream.readLength() <= stream.available();
		} catch (IOException e) {
			LOGGER.debug(e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Method used get Tag value as String
	 *
	 * @param tag
	 *            tag type
	 * @param value
	 *            tag value
	 * @return
	 */
	private static String getTagValueAsString(final ITag tag, final byte[] value) {
		StringBuilder buf = new StringBuilder();

		switch (tag.getTagValueType()) {
		case TEXT:
			buf.append("=");
			buf.append(new String(value));
			break;
		case NUMERIC:
			buf.append("NUMERIC");
			break;
		case BINARY:
			buf.append("BINARY");
			break;
		case MIXED:
			buf.append("=");
			buf.append(getSafePrintChars(value));
			break;
		case DOL:
			break;
		default:
			break;
		}

		return buf.toString();
	}

	/**
	 * Method used to parser Tag and length
	 *
	 * @param data
	 *            data to parse
	 * @return tag and length
	 */
	public static List<TagAndLength> parseTagAndLength(final byte[] data) {
		List<TagAndLength> tagAndLengthList = new ArrayList<TagAndLength>();
		if (data != null) {
			BerTlvInputStream stream = new BerTlvInputStream(data);

			try {
				while (stream.available() > 0) {
					// Every entry of a DOL is a tag followed by a length, so
					// it is at least 2 bytes long (EMV 4.3 Book 3 section
					// 5.4). A card padding its list with a trailing byte is
					// not a reason to give up the whole reading: keep the
					// entries already parsed and stop there
					if (stream.available() < 2) {
						LOGGER.warn("Malformed data object list, " + stream.available() + " byte left: " + BytesUtils.bytesToStringNoSpace(data));
						break;
					}

					ITag tag = searchTagById(stream.readTag());
					// The length of a data object list entry is a single byte
					// (EMV 4.3 Book 3 section 5.4), it is not a BER length: a
					// value of '82' asks for 130 bytes of data, it does not
					// introduce a two bytes long form
					int tagValueLength = stream.read();
					if (tagValueLength < 0) {
						LOGGER.warn("Truncated data object list: " + BytesUtils.bytesToStringNoSpace(data));
						break;
					}

					tagAndLengthList.add(new TagAndLength(tag, tagValueLength));
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return tagAndLengthList;
	}

	public static String prettyPrintAPDUResponse(final byte[] data) {
		return prettyPrintAPDUResponse(data, 0);
	}

	/**
	 * Method used to get the list of TLV inside the parameter tag specified in parameter
	 *
	 * @param pData
	 *            data to parse
	 * @param pTag
	 *            tag to find
	 * @param pAdd
	 *           boolean to indicate if we nned to add the tlv to the return list
	 * @return the list of TLV tag inside
	 */
	public static List<TLV> getlistTLV(final byte[] pData, final ITag pTag, final boolean pAdd) {
		return getlistTLV(pData, pTag, pAdd, 0);
	}

	private static List<TLV> getlistTLV(final byte[] pData, final ITag pTag, final boolean pAdd, final int pDepth) {

		List<TLV> list = new ArrayList<TLV>();
		if (pDepth > MAX_DEPTH) {
			LOGGER.warn("Data objects nested deeper than " + MAX_DEPTH + ", the rest is not parsed");
			return list;
		}

		BerTlvInputStream stream = new BerTlvInputStream(pData);

		while (stream.available() > 0) {
			TLV tlv = TlvUtil.getNextTLV(stream);
			if (tlv == null) {
				break;
			}
			if (pAdd) {
				list.add(tlv);
			} else if (tlv.getTag().isConstructed()) {
				list.addAll(getlistTLV(tlv.getValueBytes(), pTag, tlv.getTag() == pTag, pDepth + 1));
			}
		}

		return list;
	}

	/**
	 * Method used to get the list of TLV corresponding to tags specified in parameters
	 *
	 * @param pData
	 *            data to parse
	 * @param pTag
	 *            tags to find
	 * @return the list of TLV
	 */
	public static List<TLV> getlistTLV(final byte[] pData, final ITag... pTag) {
		return getlistTLV(pData, 0, pTag);
	}

	private static List<TLV> getlistTLV(final byte[] pData, final int pDepth, final ITag... pTag) {

		List<TLV> list = new ArrayList<TLV>();
		if (pDepth > MAX_DEPTH) {
			LOGGER.warn("Data objects nested deeper than " + MAX_DEPTH + ", the rest is not parsed");
			return list;
		}

		BerTlvInputStream stream = new BerTlvInputStream(pData);

		while (stream.available() > 0) {
			TLV tlv = TlvUtil.getNextTLV(stream);
			if (tlv == null) {
				break;
			}
			if (ArrayUtils.contains(pTag, tlv.getTag())) {
				list.add(tlv);
			} else if (tlv.getTag().isConstructed()) {
				list.addAll(getlistTLV(tlv.getValueBytes(), pDepth + 1, pTag));
			}
		}

		return list;
	}

	/**
	 * Method used to get Tag value
	 *
	 * @param pData
	 *            data
	 * @param pTag
	 *            tag to find
	 * @return tag value or null
	 */
	public static byte[] getValue(final byte[] pData, final ITag... pTag) {
		// The traversal of getlistTLV is depth first and in document order, so
		// its first match is the one this method used to walk the tree for
		List<TLV> list = getlistTLV(pData, pTag);
		return list.isEmpty() ? null : list.get(0).getValueBytes();
	}

	public static String prettyPrintAPDUResponse(final byte[] data, final int indentLength) {
		return prettyPrintAPDUResponse(data, indentLength, 0);
	}

	private static String prettyPrintAPDUResponse(final byte[] data, final int indentLength, final int pDepth) {
		StringBuilder buf = new StringBuilder();
		// True once a data object the dictionary knows has been rendered. It
		// tells a response the reader stopped reading half way from bytes that
		// were never BER-TLV: a transaction log record follows the Log Format
		// and holds no tag at all, so it parses into nothing but unknown tags
		// and must print nothing rather than a plausible looking tree
		boolean recognised = false;
		if (pDepth > MAX_DEPTH) {
			// Saying nothing here would read as an empty template, when the
			// card did send something the reader chose not to follow
			LOGGER.warn("Data objects nested deeper than " + MAX_DEPTH + ", the rest is not printed");
			return "\n" + getSpaces(indentLength) + "> nested deeper than " + MAX_DEPTH
					+ " data objects, the rest is not shown";
		}
		BerTlvInputStream stream = new BerTlvInputStream(data);

		try {
			while (stream.available() > 0) {
				// Where this data object starts in the output, so that a
				// reading error rolls back its own line alone
				int mark = buf.length();
				buf.append("\n");
				if (stream.available() == 2) {
					stream.mark();
					byte[] value;
					try {
						value = stream.read(2);
					} catch (IOException e) {
						break;
					}
					SwEnum sw = SwEnum.getSW(value);
					if (sw != null) {
						buf.append(getSpaces(0));
						buf.append(BytesUtils.bytesToString(value)).append(" -- ");
						buf.append(sw.getDetail());
						appendDecoded(buf, TagValueDecoder.decodeStatusWord(value), 0);
						continue;
					}
					stream.reset();
				}

				buf.append(getSpaces(indentLength));

				TLV tlv = TlvUtil.getNextTLV(stream);

				if (tlv == null) {
					// A response that held at least one known data object keeps
					// what was read of it, the line of the unreadable object
					// alone is dropped. Bytes that never looked like BER-TLV
					// lose their tree, but the status word that closes the
					// response is still worth showing
					buf.setLength(recognised ? mark : 0);
					if (!recognised) {
						appendStatusWord(buf, data, pDepth);
					}
					LOGGER.debug("TLV format error");
					break;
				}
				recognised = recognised || EmvTags.find(tlv.getTagBytes()) != null;

				byte[] tagBytes = tlv.getTagBytes();
				byte[] lengthBytes = tlv.getRawEncodedLengthBytes();
				byte[] valueBytes = tlv.getValueBytes();

				ITag tag = tlv.getTag();

				buf.append(prettyPrintHex(tagBytes));
				buf.append(" ");
				buf.append(prettyPrintHex(lengthBytes));
				buf.append(" -- ");
				buf.append(tag.getName());

				int extraIndent = (lengthBytes.length + tagBytes.length) * 3;

				if (tag.isConstructed()) {
					// indentLength += extraIndent; //TODO check this
					// Recursion
					buf.append(prettyPrintAPDUResponse(valueBytes, indentLength + extraIndent, pDepth + 1));
				} else {
					buf.append("\n");
					if (tag.getTagValueType() == TagValueTypeEnum.DOL) {
						buf.append(TlvUtil.getFormattedTagAndLength(valueBytes, indentLength + extraIndent));
					} else {
						buf.append(getSpaces(indentLength + extraIndent));
						buf.append(prettyPrintHex(BytesUtils.bytesToStringNoSpace(valueBytes), indentLength + extraIndent));
						buf.append(" (");
						buf.append(TlvUtil.getTagValueAsString(tag, valueBytes));
						buf.append(")");
						appendDecoded(buf, TagValueDecoder.decode(tag, valueBytes), indentLength + extraIndent);
					}
				}
			}
		} catch (IOException e) {
			// reset() reports a missing mark this way, it cannot happen here
			LOGGER.error(e.getMessage(), e);
		}
		return buf.toString();
	}

	/**
	 * Method used to append the status word closing a response whose data field
	 * could not be read as BER-TLV, a transaction log record for instance. The
	 * two last bytes of a response are its status word, which is the very
	 * assumption the reading of a two bytes response already makes.
	 *
	 * @param pBuf
	 *            response being rendered
	 * @param pData
	 *            the whole response
	 * @param pDepth
	 *            nesting of the data object being rendered, the status word
	 *            closes the response and not a template
	 */
	private static void appendStatusWord(final StringBuilder pBuf, final byte[] pData, final int pDepth) {
		if (pDepth != 0 || pData == null || pData.length <= 2) {
			return;
		}
		byte[] value = new byte[] { pData[pData.length - 2], pData[pData.length - 1] };
		SwEnum sw = SwEnum.getSW(value);
		if (sw != null) {
			pBuf.append("\n").append(BytesUtils.bytesToString(value)).append(" -- ").append(sw.getDetail());
			appendDecoded(pBuf, TagValueDecoder.decodeStatusWord(value), 0);
		}
	}

	/**
	 * Method used to append the decoded value of a data object under its
	 * hexadecimal dump. Each line begins with "&gt; " so that it is told apart
	 * from the continuation lines of a value long enough to be wrapped, which
	 * start in the very same column.
	 *
	 * @param pBuf
	 *            response being rendered
	 * @param pLines
	 *            decoded lines, may be empty
	 * @param pIndent
	 *            number of spaces to indent each line with
	 */
	private static void appendDecoded(final StringBuilder pBuf, final List<String> pLines, final int pIndent) {
		String indent = getSpaces(pIndent);
		for (String line : pLines) {
			pBuf.append("\n").append(indent).append("> ").append(line);
		}
	}

	public static String getSpaces(final int length) {
		return StringUtils.leftPad(StringUtils.EMPTY, length);
	}

	public static String prettyPrintHex(final String in, final int indent) {
		return prettyPrintHex(in, indent, true);
	}

	public static String prettyPrintHex(final byte[] data) {
		return prettyPrintHex(BytesUtils.bytesToStringNoSpace(data), 0, true);
	}

	public static String prettyPrintHex(final String in, final int indent, final boolean wrapLines) {
		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			buf.append(c);

			int nextPos = i + 1;
			if (wrapLines && nextPos % 32 == 0 && nextPos != in.length()) {
				buf.append("\n").append(getSpaces(indent));
			} else if (nextPos % 2 == 0 && nextPos != in.length()) {
				buf.append(" ");
			}
		}
		return buf.toString();
	}

	public static String getSafePrintChars(final byte[] byteArray) {
		if (byteArray == null) {
			return "";
		}
		return getSafePrintChars(byteArray, 0, byteArray.length);
	}

	public static String getSafePrintChars(final byte[] byteArray, final int startPos, final int length) {
		if (byteArray == null) {
			return "";
		}
		if (byteArray.length < startPos + length) {
			throw new IllegalArgumentException("startPos(" + startPos + ")+length(" + length + ") > byteArray.length("
					+ byteArray.length + ")");
		}
		StringBuilder buf = new StringBuilder();
		for (int i = startPos; i < startPos + length; i++) {
			if (byteArray[i] >= (byte) 0x20 && byteArray[i] < (byte) 0x7F) {
				buf.append((char) byteArray[i]);
			} else {
				buf.append(".");
			}
		}
		return buf.toString();
	}

	/**
	 * Method used to get length of all Tags
	 *
	 * @param pList
	 *            tag length list
	 * @return the sum of tag length
	 */
	public static int getLength(final List<TagAndLength> pList) {
		int ret = 0;
		if (pList != null) {
			for (TagAndLength tl : pList) {
				ret += tl.getLength();
			}
		}
		return ret;
	}

	/**
	 * Private constructor
	 */
	private TlvUtil() {
	}

}
