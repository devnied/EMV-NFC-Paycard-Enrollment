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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.exception.TlvException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;

import fr.devnied.bitlib.BytesUtils;

/**
 * List of utils methods to manipulate TLV
 * 
 * @author MILLAU Julien
 * 
 */
public final class TlvUtil {

	/**
	 * Method used to find Tag with ID
	 * 
	 * @param tagIdBytes
	 *            the tag to find
	 * @return the tag found
	 */
	private static ITag searchTagById(final byte[] tagIdBytes) {
		return EmvTags.getNotNull(tagIdBytes); // TODO take app (IIN or RID) into consideration
	}

	/**
	 * Method used to search tag with id
	 * 
	 * @param stream
	 *            Byte array in a stream
	 * @return Tag found
	 */
	private static ITag searchTagById(final ByteArrayInputStream stream) {
		return searchTagById(TlvUtil.readTagIdBytes(stream));
	}

	// This is just a list of Tag And Lengths (eg DOLs)
	public static String getFormattedTagAndLength(final byte[] data, final int indentLength) {
		StringBuilder buf = new StringBuilder();
		String indent = getSpaces(indentLength);
		ByteArrayInputStream stream = new ByteArrayInputStream(data);

		boolean firstLine = true;
		while (stream.available() > 0) {
			if (firstLine) {
				firstLine = false;
			} else {
				buf.append("\n");
			}
			buf.append(indent);

			ITag tag = searchTagById(stream);
			int length = TlvUtil.readTagLength(stream);

			buf.append(prettyPrintHex(tag.getTagBytes()));
			buf.append(" ");
			buf.append(String.format("%02x", length));
			buf.append(" -- ");
			buf.append(tag.getName());
		}
		return buf.toString();
	}

	public static byte[] readTagIdBytes(final ByteArrayInputStream stream) {
		ByteArrayOutputStream tagBAOS = new ByteArrayOutputStream();
		byte tagFirstOctet = (byte) stream.read();
		tagBAOS.write(tagFirstOctet);

		// Find TAG bytes
		byte MASK = (byte) 0x1F;
		if ((tagFirstOctet & MASK) == MASK) { // EMV book 3, Page 178 or Annex B1 (EMV4.3)
			// Tag field is longer than 1 byte
			do {
				int nextOctet = stream.read();
				if (nextOctet < 0) {
					break;
				}
				byte tlvIdNextOctet = (byte) nextOctet;

				tagBAOS.write(tlvIdNextOctet);

				if (!BytesUtils.matchBitByBitIndex(tlvIdNextOctet, 7) || BytesUtils.matchBitByBitIndex(tlvIdNextOctet, 7)
						&& (tlvIdNextOctet & 0x7f) == 0) {
					break;
				}
			} while (true);
		}
		return tagBAOS.toByteArray();
	}

	public static int readTagLength(final ByteArrayInputStream stream) {
		// Find LENGTH bytes
		int length;
		int tmpLength = stream.read();

		if (tmpLength < 0) {
			throw new TlvException("Negative length: " + tmpLength);
		}

		if (tmpLength <= 127) { // 0111 1111
			// short length form
			length = tmpLength;
		} else if (tmpLength == 128) { // 1000 0000
			// length identifies indefinite form, will be set later
			// indefinite form is not specified in ISO7816-4, but we include it here for completeness
			length = tmpLength;
		} else {
			// long length form
			int numberOfLengthOctets = tmpLength & 127; // turn off 8th bit
			tmpLength = 0;
			for (int i = 0; i < numberOfLengthOctets; i++) {
				int nextLengthOctet = stream.read();
				if (nextLengthOctet < 0) {
					throw new TlvException("EOS when reading length bytes");
				}
				tmpLength <<= 8;
				tmpLength |= nextLengthOctet;
			}
			length = tmpLength;
		}
		return length;
	}

	public static TLV getNextTLV(final ByteArrayInputStream stream) {
		if (stream.available() < 2) {
			throw new TlvException("Error parsing data. Available bytes < 2 . Length=" + stream.available());
		}

		// ISO/IEC 7816 uses neither '00' nor 'FF' as tag value.
		// Before, between, or after TLV-coded data objects,
		// '00' or 'FF' bytes without any meaning may occur
		// (for example, due to erased or modified TLV-coded data objects).

		stream.mark(0);
		int peekInt = stream.read();
		byte peekByte = (byte) peekInt;
		// peekInt == 0xffffffff indicates EOS
		while (peekInt != -1 && (peekByte == (byte) 0xFF || peekByte == (byte) 0x00)) {
			stream.mark(0); // Current position
			peekInt = stream.read();
			peekByte = (byte) peekInt;
		}
		stream.reset(); // Reset back to the last known position without 0x00 or 0xFF

		if (stream.available() < 2) {
			throw new TlvException("Error parsing data. Available bytes < 2 . Length=" + stream.available());
		}

		byte[] tagIdBytes = TlvUtil.readTagIdBytes(stream);

		// We need to get the raw length bytes.
		// Use quick and dirty workaround
		stream.mark(0);
		int posBefore = stream.available();
		// Now parse the lengthbyte(s)
		// This method will read all length bytes. We can then find out how many bytes was read.
		int length = TlvUtil.readTagLength(stream); // Decoded
		// Now find the raw (encoded) length bytes
		int posAfter = stream.available();
		stream.reset();
		byte[] lengthBytes = new byte[posBefore - posAfter];

		if (lengthBytes.length < 1 || lengthBytes.length > 4) {
			throw new TlvException("Number of length bytes must be from 1 to 4. Found " + lengthBytes.length);
		}

		stream.read(lengthBytes, 0, lengthBytes.length);

		int rawLength = BytesUtils.byteArrayToInt(lengthBytes);

		byte[] valueBytes;

		ITag tag = searchTagById(tagIdBytes);

		// Find VALUE bytes
		if (rawLength == 128) { // 1000 0000
			// indefinite form
			stream.mark(0);
			int prevOctet = 1;
			int curOctet;
			int len = 0;
			while (true) {
				len++;
				curOctet = stream.read();
				if (curOctet < 0) {
					throw new TlvException("Error parsing data. TLV " + "length byte indicated indefinite length, but EOS "
							+ "was reached before 0x0000 was found" + stream.available());
				}
				if (prevOctet == 0 && curOctet == 0) {
					break;
				}
				prevOctet = curOctet;
			}
			len -= 2;
			valueBytes = new byte[len];
			stream.reset();
			stream.read(valueBytes, 0, len);
			length = len;
		} else {
			if (stream.available() < length) {
				throw new TlvException("Length byte(s) indicated " + length + " value bytes, but only " + stream.available()
						+ " " + (stream.available() > 1 ? "are" : "is") + " available");
			}
			// definite form
			valueBytes = new byte[length];
			stream.read(valueBytes, 0, length);
		}

		// Remove any trailing 0x00 and 0xFF
		stream.mark(0);
		peekInt = stream.read();
		peekByte = (byte) peekInt;
		while (peekInt != -1 && (peekByte == (byte) 0xFF || peekByte == (byte) 0x00)) {
			stream.mark(0);
			peekInt = stream.read();
			peekByte = (byte) peekInt;
		}
		stream.reset(); // Reset back to the last known position without 0x00 or 0xFF

		return new TLV(tag, length, lengthBytes, valueBytes);
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
			buf.append("");
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
	 * @return list of tag and length
	 */
	public static List<TagAndLength> parseTagAndLength(final byte[] data) {
		List<TagAndLength> tagAndLengthList = new ArrayList<TagAndLength>();
		if (data != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(data);

			while (stream.available() > 0) {
				if (stream.available() < 2) {
					throw new TlvException("Data length < 2 : " + stream.available());
				}

				ITag tag = searchTagById(TlvUtil.readTagIdBytes(stream));
				int tagValueLength = TlvUtil.readTagLength(stream);

				tagAndLengthList.add(new TagAndLength(tag, tagValueLength));
			}
		}
		return tagAndLengthList;
	}

	public static String prettyPrintAPDUResponse(final byte[] data) {
		return prettyPrintAPDUResponse(data, 0);
	}

	public static String prettyPrintAPDUResponse(final byte[] data, final int startPos, final int length) {
		byte[] tmp = new byte[length - startPos];
		System.arraycopy(data, startPos, tmp, 0, length);
		return prettyPrintAPDUResponse(tmp, 0);
	}

	/**
	 * Method used to get the list of TLV inside the parameter tag specified in parameter
	 * 
	 * @param pData
	 *            data to parse
	 * @param pTag
	 *            tag to find
	 * @param pAdd
	 * @return the list of TLV tag inside
	 */
	public static List<TLV> getlistTLV(final byte[] pData, final ITag pTag, final boolean pAdd) {

		List<TLV> list = new ArrayList<TLV>();

		ByteArrayInputStream stream = new ByteArrayInputStream(pData);

		while (stream.available() > 0) {

			TLV tlv = TlvUtil.getNextTLV(stream);
			if (pAdd) {
				list.add(tlv);
			} else if (tlv.getTag().isConstructed()) {
				list.addAll(TlvUtil.getlistTLV(tlv.getValueBytes(), pTag, tlv.getTag() == pTag));
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
	 * @param pAdd
	 * @return the list of TLV
	 */
	public static List<TLV> getlistTLV(final byte[] pData, final ITag... pTag) {

		List<TLV> list = new ArrayList<TLV>();

		ByteArrayInputStream stream = new ByteArrayInputStream(pData);

		while (stream.available() > 0) {

			TLV tlv = TlvUtil.getNextTLV(stream);
			if (ArrayUtils.contains(pTag, tlv.getTag())) {
				list.add(tlv);
			} else if (tlv.getTag().isConstructed()) {
				list.addAll(TlvUtil.getlistTLV(tlv.getValueBytes(), pTag));
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

		byte[] ret = null;

		if (pData != null) {
			ByteArrayInputStream stream = new ByteArrayInputStream(pData);

			while (stream.available() > 0) {

				TLV tlv = TlvUtil.getNextTLV(stream);
				if (ArrayUtils.contains(pTag, tlv.getTag())) {
					return tlv.getValueBytes();
				} else if (tlv.getTag().isConstructed()) {
					ret = TlvUtil.getValue(tlv.getValueBytes(), pTag);
					if (ret != null) {
						break;
					}
				}
			}
		}

		return ret;
	}

	public static String prettyPrintAPDUResponse(final byte[] data, final int indentLength) {
		StringBuilder buf = new StringBuilder();

		ByteArrayInputStream stream = new ByteArrayInputStream(data);

		while (stream.available() > 0) {
			buf.append("\n");
			if (stream.available() == 2) {
				stream.mark(0);
				byte[] value = new byte[2];
				try {
					stream.read(value);
				} catch (IOException e) {
				}
				SwEnum sw = SwEnum.getSW(value);
				if (sw != null) {
					buf.append(getSpaces(0));
					buf.append(BytesUtils.bytesToString(value)).append(" -- ");
					buf.append(sw.getDetail());
					continue;
				}
				stream.reset();
			}

			buf.append(getSpaces(indentLength));

			TLV tlv = TlvUtil.getNextTLV(stream);

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
				buf.append(prettyPrintAPDUResponse(valueBytes, indentLength + extraIndent));
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
				}
			}
		}
		return buf.toString();
	}

	public static String getSpaces(final int length) {
		return StringUtils.leftPad(StringUtils.EMPTY, length);
	}

	public static String prettyPrintHex(final String in, final int indent) {
		return prettyPrintHex(in, indent, true);
	}

	public static String prettyPrintHex(final byte[] data, final int indent) {
		return prettyPrintHex(BytesUtils.bytesToStringNoSpace(data), indent, true);
	}

	public static String prettyPrintHex(final String in) {
		return prettyPrintHex(in, 0, true);
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