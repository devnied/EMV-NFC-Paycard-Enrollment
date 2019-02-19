/*
 * Copyright (C) 2019 MILLAU Julien
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
package com.github.devnied.emvnfccard.model;

import java.io.Serializable;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

	/**
	 * Unknown value
	 */
	public static final int UNKNOWN = -1;

	/**
	 * Custom style
	 */
	private static final ToStringStyle CUSTOM_STYLE = new CustomToStringStyle();

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, CUSTOM_STYLE);
	}

	/**
	 * <p>
	 * <code>CustomToStringStyle</code> that outputs on multiple lines with s.
	 * </p>
	 *
	 * <p>
	 * This is an inner class rather than using
	 * <code>StandardToStringStyle</code> to ensure its immutability.
	 * </p>
	 */
	private static final class CustomToStringStyle extends ToStringStyle {

		private static final long serialVersionUID = 1L;

		/**
		 * <p>
		 * Constructor.
		 * </p>
		 */
		CustomToStringStyle() {
			super();
			setUseShortClassName(true);
			setUseIdentityHashCode(false);
			setContentStart("[");
			setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
			setFieldSeparatorAtStart(true);
			setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
		}

	}

}
