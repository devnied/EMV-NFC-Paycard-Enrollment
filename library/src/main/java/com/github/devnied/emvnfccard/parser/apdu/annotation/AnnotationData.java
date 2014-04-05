package com.github.devnied.emvnfccard.parser.apdu.annotation;

import java.lang.reflect.Field;

/**
 * Bean which manage all annotation data
 * 
 * @author julien Millau
 * 
 */
public class AnnotationData implements Comparable<AnnotationData> {

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
	 * Setter for the field index
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(final int index) {
		this.index = index;
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
	 * Setter for the field readHexa
	 * 
	 * @param readHexa
	 *            the readHexa to set
	 */
	public void setReadHexa(final boolean readHexa) {
		this.readHexa = readHexa;
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
	 * Setter for the field field
	 * 
	 * @param field
	 *            the field to set
	 */
	public void setField(final Field field) {
		this.field = field;
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
	 * Setter for the field dateStandard
	 * 
	 * @param dateStandard
	 *            the dateStandard to set
	 */
	public void setDateStandard(final int dateStandard) {
		this.dateStandard = dateStandard;
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
	 * Setter for the field format
	 * 
	 * @param format
	 *            the format to set
	 */
	public void setFormat(final String format) {
		this.format = format;
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
	}

}
