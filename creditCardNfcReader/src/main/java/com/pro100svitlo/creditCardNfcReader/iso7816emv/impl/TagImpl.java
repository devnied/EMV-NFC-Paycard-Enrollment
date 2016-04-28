package com.pro100svitlo.creditCardNfcReader.iso7816emv.impl;

import com.pro100svitlo.creditCardNfcReader.utils.BytesUtils;
import com.pro100svitlo.creditCardNfcReader.enums.TagTypeEnum;
import com.pro100svitlo.creditCardNfcReader.enums.TagValueTypeEnum;
import com.pro100svitlo.creditCardNfcReader.iso7816emv.ITag;

import java.util.Arrays;


public final class TagImpl implements ITag {

	private final byte[] idBytes;
	public final String name;
	private final String description;
	private final TagValueTypeEnum tagValueType;
	private final Class tagClass;
	private final TagTypeEnum type;

	public TagImpl(final String id, final TagValueTypeEnum tagValueType, final String name, final String description) {
		this(BytesUtils.fromString(id), tagValueType, name, description);
	}

	public TagImpl(final byte[] idBytes, final TagValueTypeEnum tagValueType, final String name, final String description) {
		if (idBytes == null) {
			throw new IllegalArgumentException("Param id cannot be null");
		}
		if (idBytes.length == 0) {
			throw new IllegalArgumentException("Param id cannot be empty");
		}
		if (tagValueType == null) {
			throw new IllegalArgumentException("Param tagValueType cannot be null");
		}
		this.idBytes = idBytes;
		this.name = name;
		this.description = description;
		this.tagValueType = tagValueType;

		if (BytesUtils.matchBitByBitIndex(this.idBytes[0], 5)) {
			type = TagTypeEnum.CONSTRUCTED;
		} else {
			type = TagTypeEnum.PRIMITIVE;
		}
		// Bits 8 and 7 of the first byte of the tag field indicate a class.
		// The value 00 indicates a data object of the universal class.
		// The value 01 indicates a data object of the application class.
		// The value 10 indicates a data object of the context-specific class.
		// The value 11 indicates a data object of the private class.
		byte classValue = (byte) (this.idBytes[0] >>> 6 & 0x03);
		switch (classValue) {
		case (byte) 0x01:
			tagClass = Class.APPLICATION;
			break;
		case (byte) 0x02:
			tagClass = Class.CONTEXT_SPECIFIC;
			break;
		case (byte) 0x03:
			tagClass = Class.PRIVATE;
			break;
		default:
			tagClass = Class.UNIVERSAL;
			break;
		}

	}

	@Override
	public boolean isConstructed() {
		return type == TagTypeEnum.CONSTRUCTED;
	}

	@Override
	public byte[] getTagBytes() {
		return idBytes;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public TagValueTypeEnum getTagValueType() {
		return tagValueType;
	}

	@Override
	public TagTypeEnum getType() {
		return type;
	}

	@Override
	public Class getTagClass() {
		return tagClass;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ITag)) {
			return false;
		}
		ITag that = (ITag) other;
		if (getTagBytes().length != that.getTagBytes().length) {
			return false;
		}

		return Arrays.equals(getTagBytes(), that.getTagBytes());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + Arrays.hashCode(idBytes);
		return hash;
	}

	@Override
	public int getNumTagBytes() {
		return idBytes.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tag[");
		sb.append(BytesUtils.bytesToString(getTagBytes()));
		sb.append("] Name=");
		sb.append(getName());
		sb.append(", TagType=");
		sb.append(getType());
		sb.append(", ValueType=");
		sb.append(getTagValueType());
		sb.append(", Class=");
		sb.append(tagClass);
		return sb.toString();
	}
}