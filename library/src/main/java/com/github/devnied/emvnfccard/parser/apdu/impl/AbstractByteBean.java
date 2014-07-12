package com.github.devnied.emvnfccard.parser.apdu.impl;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Set<AnnotationData> getAnnotationSet() {
		return AnnotationUtils.getInstance().getMap().get(getClass().getName());
	}

	/**
	 * Method to parse byte data
	 * 
	 * @param pData
	 *            byte to parse
	 */
	@Override
	public void parse(final byte[] pData) {
		if (pData.length < getDefaultSize() / 8) {
			throw new IllegalArgumentException("Wrong byte Size");
		}
		Set<AnnotationData> set = getAnnotationSet();
		BitUtils bit = new BitUtils(pData);
		Iterator<AnnotationData> it = set.iterator();
		while (it.hasNext()) {
			AnnotationData data = it.next();
			Object obj = DataFactory.getObject(data, bit);
			setField(data.getField(), this, obj);
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
