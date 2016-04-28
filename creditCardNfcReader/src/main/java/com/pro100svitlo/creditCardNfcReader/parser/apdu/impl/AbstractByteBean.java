package com.pro100svitlo.creditCardNfcReader.parser.apdu.impl;

import com.pro100svitlo.creditCardNfcReader.iso7816emv.ITag;
import com.pro100svitlo.creditCardNfcReader.iso7816emv.TagAndLength;
import com.pro100svitlo.creditCardNfcReader.model.AbstractData;
import com.pro100svitlo.creditCardNfcReader.parser.apdu.IFile;
import com.pro100svitlo.creditCardNfcReader.parser.apdu.annotation.AnnotationData;
import com.pro100svitlo.creditCardNfcReader.parser.apdu.annotation.AnnotationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.devnied.bitlib.BitUtils;

/**
 * Abstract class for all object to parse
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
