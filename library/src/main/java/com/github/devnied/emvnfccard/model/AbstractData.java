package com.github.devnied.emvnfccard.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Abstract class used to provide some commons methods to bean
 * 
 * @author MILLAU Julien
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
