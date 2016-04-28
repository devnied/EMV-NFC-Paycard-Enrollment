package com.pro100svitlo.creditCardNfcReader.iso7816emv;

import java.util.Arrays;

public class TagAndLength {
	private ITag tag;
	private int length;

	public TagAndLength(final ITag tag, final int length) {
		this.tag = tag;
		this.length = length;
	}

	public ITag getTag() {
		return tag;
	}

	public int getLength() {
		return length;
	}

	public byte[] getBytes() {
		byte[] tagBytes = tag.getTagBytes();
		byte[] tagAndLengthBytes = Arrays.copyOf(tagBytes, tagBytes.length + 1);
		tagAndLengthBytes[tagAndLengthBytes.length - 1] = (byte) length;
		return tagAndLengthBytes;
	}

	@Override
	public String toString() {
		return tag.toString() + " length: " + length;
	}

}
