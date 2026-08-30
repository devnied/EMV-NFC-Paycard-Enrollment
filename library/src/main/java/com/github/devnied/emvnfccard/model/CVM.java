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

import com.github.devnied.emvnfccard.model.enums.CvmConditionEnum;
import com.github.devnied.emvnfccard.model.enums.CvmMethodEnum;

/**
 * A single Cardholder Verification Rule (CV Rule) as found in the CVM List (tag
 * '8E').<br>
 * A CV Rule is 2 bytes long:
 *
 * <pre>
 * byte 1: b7    -&gt; apply succeeding CV Rule if this CVM is unsuccessful
 *         b6-b1 -&gt; CVM code ({@link CvmMethodEnum})
 * byte 2:       -&gt; CVM condition code ({@link CvmConditionEnum})
 * </pre>
 *
 * @author MILLAU Julien
 *
 */
public class CVM extends AbstractData {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 1093484754754899147L;

	/**
	 * Cardholder verification method
	 */
	private CvmMethodEnum method;

	/**
	 * Condition under which the method applies
	 */
	private CvmConditionEnum condition;

	/**
	 * Indicates whether the terminal must apply the next rule when this one
	 * fails
	 */
	private boolean applyNextIfUnsuccessful;

	/**
	 * Raw CVM code (bits 6 to 1 of the first byte)
	 */
	private int rawMethod = UNKNOWN;

	/**
	 * Raw condition code (second byte)
	 */
	private int rawCondition = UNKNOWN;

	/**
	 * Get the field method
	 *
	 * @return the method
	 */
	public CvmMethodEnum getMethod() {
		return method;
	}

	/**
	 * Setter for the field method
	 *
	 * @param method
	 *            the method to set
	 */
	public void setMethod(final CvmMethodEnum method) {
		this.method = method;
	}

	/**
	 * Get the field condition
	 *
	 * @return the condition
	 */
	public CvmConditionEnum getCondition() {
		return condition;
	}

	/**
	 * Setter for the field condition
	 *
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(final CvmConditionEnum condition) {
		this.condition = condition;
	}

	/**
	 * Get the field applyNextIfUnsuccessful
	 *
	 * @return true if the next rule must be applied when this one fails
	 */
	public boolean isApplyNextIfUnsuccessful() {
		return applyNextIfUnsuccessful;
	}

	/**
	 * Setter for the field applyNextIfUnsuccessful
	 *
	 * @param applyNextIfUnsuccessful
	 *            the applyNextIfUnsuccessful to set
	 */
	public void setApplyNextIfUnsuccessful(final boolean applyNextIfUnsuccessful) {
		this.applyNextIfUnsuccessful = applyNextIfUnsuccessful;
	}

	/**
	 * Get the field rawMethod
	 *
	 * @return the rawMethod
	 */
	public int getRawMethod() {
		return rawMethod;
	}

	/**
	 * Setter for the field rawMethod
	 *
	 * @param rawMethod
	 *            the rawMethod to set
	 */
	public void setRawMethod(final int rawMethod) {
		this.rawMethod = rawMethod;
	}

	/**
	 * Get the field rawCondition
	 *
	 * @return the rawCondition
	 */
	public int getRawCondition() {
		return rawCondition;
	}

	/**
	 * Setter for the field rawCondition
	 *
	 * @param rawCondition
	 *            the rawCondition to set
	 */
	public void setRawCondition(final int rawCondition) {
		this.rawCondition = rawCondition;
	}

}
