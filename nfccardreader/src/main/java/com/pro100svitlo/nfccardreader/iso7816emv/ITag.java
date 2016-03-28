package com.pro100svitlo.nfccardreader.iso7816emv;

import com.pro100svitlo.nfccardreader.enums.TagTypeEnum;
import com.pro100svitlo.nfccardreader.enums.TagValueTypeEnum;


public interface ITag {

	enum Class {
		UNIVERSAL, APPLICATION, CONTEXT_SPECIFIC, PRIVATE
	}

	boolean isConstructed();

	byte[] getTagBytes();

	String getName();

	String getDescription();

	TagTypeEnum getType();

	TagValueTypeEnum getTagValueType();

	Class getTagClass();

	int getNumTagBytes();

}
