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
package com.github.devnied.emvnfccard.parser.apdu.annotation;

import java.lang.reflect.Field;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;

import fr.devnied.bitlib.BytesUtils;

/**
 * Bean which manage all annotation data
 * 
 * @author MILLAU Julien
 * 
 */
public class AnnotationData implements Comparable<AnnotationData>, Cloneable {

	/**
	 * The size of the field
	 */
	private int size;

	/**
	 * Index of the field
	 */
	private int index;

	/**
	 * read String as hexa value
	 */
	private boolean readHexa;

	/**
	 * Field to modify
	 */
	private Field field;

	/**
	 * The date standard
	 */
	private int dateStandard;

	/**
	 * Date format
	 */
	private String format;

	/**
	 * Tag
	 */
	private ITag tag;

	/**
	 * Skip data
	 */
	private boolean skip;

	/**
	 * Comparable method {@inheritDoc}
	 * 
	 */
	@Override
	public int compareTo(final AnnotationData pO) {
		return Integer.valueOf(index).compareTo(pO.getIndex());
	}

	/**
	 * Equals method {@inheritDoc}
	 * 
	 */
	@Override
	public boolean equals(final Object pObj) {
		boolean ret = false;
		if (pObj instanceof AnnotationData) {
			ret = index == ((AnnotationData) pObj).getIndex();
		}
		return ret;
	}

	/**
	 * Method used to get the field size
	 * 
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Setter for the field size
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(final int size) {
		this.size = size;
	}

	/**
	 * Method used to get the field index
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Method used to get the field readHexa
	 * 
	 * @return the readHexa
	 */
	public boolean isReadHexa() {
		return readHexa;
	}

	/**
	 * Method used to get the field field
	 * 
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	/**
	 * Method used to get the field dateStandard
	 * 
	 * @return the dateStandard
	 */
	public int getDateStandard() {
		return dateStandard;
	}

	/**
	 * Method used to get the field format
	 * 
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Setter for the field field
	 * 
	 * @param field
	 *            the field to set
	 */
	public void setField(final Field field) {
		this.field = field;
	}

	/**
	 * Method used to get the field tag
	 * 
	 * @return the tag
	 */
	public ITag getTag() {
		return tag;
	}

	/**
	 * Initialization from annotation
	 * 
	 * @param pData
	 *            annotation data
	 */
	public void initFromAnnotation(final Data pData) {
		dateStandard = pData.dateStandard();
		format = pData.format();
		index = pData.index();
		readHexa = pData.readHexa();
		size = pData.size();
		if (pData.tag() != null) {
			tag = EmvTags.find(BytesUtils.fromString(pData.tag()));
		}
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		AnnotationData data = new AnnotationData();
		data.dateStandard = dateStandard;
		data.field = field;
		data.format = new String(format);
		data.index = index;
		data.readHexa = readHexa;
		data.size = size;
		data.tag = tag;
		return data;
	}

	/**
	 * Method used to get the field skip
	 * 
	 * @return the skip
	 */
	public boolean isSkip() {
		return skip;
	}

	/**
	 * Setter for the field skip
	 * 
	 * @param skip
	 *            the skip to set
	 */
	public void setSkip(final boolean skip) {
		this.skip = skip;
	}

}
