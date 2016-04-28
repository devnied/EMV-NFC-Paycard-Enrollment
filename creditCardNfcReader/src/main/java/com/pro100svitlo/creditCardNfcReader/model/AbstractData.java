package com.pro100svitlo.creditCardNfcReader.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Abstract class used to provide some commons methods to bean
 * 
 */
public abstract class AbstractData implements Serializable {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = -456811026151402726L;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
