/*
 * Copyright (C) 2013 MILLAU Julien
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
package com.github.devnied.emvnfccard.parser.apdu.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.parser.apdu.IFile;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationUtils;

import fr.devnied.bitlib.BitUtils;

/**
 * Abstract class for all object to parse
 * 
 * @author MILLAU Julien
 */
public abstract class AbstractByteBean<T> extends AbstractData implements IFile {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -2016039522844322383L;

	/**
	 * Logger of the class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractByteBean.class.getName());

	/**
	 * Method to get the annotation set from the current class
	 * 
	 * @return An annotation set which contain all annotation data
	 */
	private Collection<AnnotationData> getAnnotationSet(final List<TagAndLength> pTags) {
		Collection<AnnotationData> ret = null;
		if (pTags != null) {
			Map<ITag, AnnotationData> data = AnnotationUtils.getInstance().getMap().get(getClass().getName());
			ret = new ArrayList<AnnotationData>(data.size());
			for (TagAndLength tal : pTags) {
				AnnotationData ann = data.get(tal.getTag());
				if (ann != null) {
					ann.setSize(tal.getLength() * BitUtils.BYTE_SIZE);
				} else {
					ann = new AnnotationData();
					ann.setSkip(true);
					ann.setSize(tal.getLength() * BitUtils.BYTE_SIZE);
				}
				ret.add(ann);
			}
		} else {
			ret = AnnotationUtils.getInstance().getMapSet().get(getClass().getName());
		}
		return ret;
	}

	/**
	 * Method to parse byte data
	 * 
	 * @param pData
	 *            byte to parse
	 * @param pTags
	 */
	@Override
	public void parse(final byte[] pData, final List<TagAndLength> pTags) {
		Collection<AnnotationData> set = getAnnotationSet(pTags);
		BitUtils bit = new BitUtils(pData);
		Iterator<AnnotationData> it = set.iterator();
		while (it.hasNext()) {
			AnnotationData data = it.next();
			if (data.isSkip()) {
				bit.addCurrentBitIndex(data.getSize());
			} else {
				Object obj = DataFactory.getObject(data, bit);
				setField(data.getField(), this, obj);
			}
		}
	}

	/**
	 * Method used to set the value of a field
	 * 
	 * @param field
	 *            the field to set
	 * @param pData
	 *            Object containing the field
	 * @param pValue
	 *            the value of the field
	 */
	protected void setField(final Field field, final IFile pData, final Object pValue) {
		if (field != null) {
			try {
				field.set(pData, pValue);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Parameters of fied.set are not valid", e);
			} catch (IllegalAccessException e) {
				LOGGER.error("Impossible to set the Field :" + field.getName(), e);
			}
		}
	}
}
