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
import com.github.devnied.emvnfccard.exception.TlvException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import fr.devnied.bitlib.BytesUtils;
import net.sf.scuba.tlv.TLVInputStream;
import net.sf.scuba.tlv.TLVUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
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
	 * Method used to find Tag with ID
	 *
	 * @param tagIdBytes
	 *            the tag to find
	 * @return the tag found
	 */
	private static ITag searchTagById(final int tagId) {
		return EmvTags.getNotNull(TLVUtil.getTagAsBytes(tagId));
	}

	// This is just a list of Tag And Lengths (eg DOLs)
	public static String getFormattedTagAndLength(final byte[] data, final int indentLength) {
		StringBuilder buf = new StringBuilder();
		String indent = getSpaces(indentLength);
		TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(data));

		boolean firstLine = true;
		try {
			while (stream.available() > 0) {
				if (firstLine) {
					firstLine = false;
				} else {
					buf.append("\n");
				}
				buf.append(indent);

				ITag tag = searchTagById(stream.readTag());
				int length = stream.readLength();

				buf.append(prettyPrintHex(tag.getTagBytes()));
				buf.append(" ");
				buf.append(String.format("%02x", length));
				buf.append(" -- ");
				buf.append(tag.getName());
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return buf.toString();
	}

	public static TLV getNextTLV(final TLVInputStream stream) {
		TLV tlv = null;
		try {
			int left = stream.available();
			if (left <= 2) {
				return tlv;
			}
			ITag tag = searchTagById(stream.readTag());
			int length = stream.readLength();
			if (stream.available() >= length) {
				tlv = new TLV(tag, length, TLVUtil.getLengthAsBytes(length), stream.readValue());
			}
		} catch (EOFException eof) {
			LOGGER.debug(eof.getMessage(), eof);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return tlv;
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
	 * @return tag and length
	 */
	public static List<TagAndLength> parseTagAndLength(final byte[] data) {
		List<TagAndLength> tagAndLengthList = new ArrayList<TagAndLength>();
		if (data != null) {
			TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(data));

			try {
				while (stream.available() > 0) {
					if (stream.available() < 2) {
						throw new TlvException("Data length < 2 : " + stream.available());
					}

					ITag tag = searchTagById(stream.readTag());
					int tagValueLength = stream.readLength();

					tagAndLengthList.add(new TagAndLength(tag, tagValueLength));
				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(stream);
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

		List<TLV> list = new ArrayList<TLV>();

		TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(pData));

		try {
			while (stream.available() > 0) {

				TLV tlv = TlvUtil.getNextTLV(stream);
				if (tlv == null) {
					break;
				}
				if (pAdd) {
					list.add(tlv);
				} else if (tlv.getTag().isConstructed()) {
					list.addAll(TlvUtil.getlistTLV(tlv.getValueBytes(), pTag, tlv.getTag() == pTag));
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
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

		List<TLV> list = new ArrayList<TLV>();

		TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(pData));

		try {
			while (stream.available() > 0) {

				TLV tlv = TlvUtil.getNextTLV(stream);
				if (tlv == null) {
					break;
				}
				if (ArrayUtils.contains(pTag, tlv.getTag())) {
					list.add(tlv);
				} else if (tlv.getTag().isConstructed()) {
					list.addAll(TlvUtil.getlistTLV(tlv.getValueBytes(), pTag));
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
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
			TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(pData));

			try {
				while (stream.available() > 0) {

					TLV tlv = TlvUtil.getNextTLV(stream);
					if (tlv == null) {
						break;
					}
					if (ArrayUtils.contains(pTag, tlv.getTag())) {
						return tlv.getValueBytes();
					} else if (tlv.getTag().isConstructed()) {
						ret = TlvUtil.getValue(tlv.getValueBytes(), pTag);
						if (ret != null) {
							break;
						}
					}

				}
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(stream);
			}
		}

		return ret;
	}

	public static String prettyPrintAPDUResponse(final byte[] data, final int indentLength) {
		StringBuilder buf = new StringBuilder();
		TLVInputStream stream = new TLVInputStream(new ByteArrayInputStream(data));

		try {
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

				if (tlv == null) {
					buf.setLength(0);
					LOGGER.debug("TLV format error");
					break;
				}

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
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (TlvException exce) {
			buf.setLength(0);
			LOGGER.debug(exce.getMessage(), exce);
		} finally {
			IOUtils.closeQuietly(stream);
		}
		return buf.toString();
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
