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
package com.github.devnied.emvnfccard.model.enums;

/**
 * Service code, position 2 values.
 */
public enum ServiceCode2Enum implements IKeyEnum {

	NORMAL(0, "Normal"),
	BY_ISSUER(2, "By issuer"),
	BY_ISSUER_WIHOUT_BI_AGREEMENT(4, "By issuer unless explicit bilateral agreement applies");

	private final int value;
	private final String authorizationProcessing;

	private ServiceCode2Enum(final int value, final String authorizationProcessing) {
		this.value = value;
		this.authorizationProcessing = authorizationProcessing;
	}

	/**
	 * Gets the authorization processing rules.
	 * 
	 * @return Authorization processing rules.
	 */
	public String getAuthorizationProcessing() {
		return authorizationProcessing;
	}

	@Override
	public int getKey() {
		return value;
	}

}