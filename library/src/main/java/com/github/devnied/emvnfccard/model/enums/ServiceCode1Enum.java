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
 * Service code, position 1 values.
 */
public enum ServiceCode1Enum implements IKeyEnum {

	INTERNATIONNAL(1, "International interchange", "None"),
	INTERNATIONNAL_ICC(2, "International interchange", "Integrated circuit card"),
	NATIONAL(5, "National interchange", "None"),
	NATIONAL_ICC(6, "National interchange", "Integrated circuit card"),
	PRIVATE(7, "Private", "None");

	private final int value;
	private final String interchange;
	private final String technology;

	/**
	 * Constructor using fields
	 * 
	 * @param value
	 * @param interchange
	 * @param technology
	 */
	private ServiceCode1Enum(final int value, final String interchange, final String technology) {
		this.value = value;
		this.interchange = interchange;
		this.technology = technology;
	}

	/**
	 * Method used to get the field interchange
	 * 
	 * @return the interchange
	 */
	public String getInterchange() {
		return interchange;
	}

	/**
	 * Method used to get the field technology
	 * 
	 * @return the technology
	 */
	public String getTechnology() {
		return technology;
	}

	@Override
	public int getKey() {
		return value;
	}

}