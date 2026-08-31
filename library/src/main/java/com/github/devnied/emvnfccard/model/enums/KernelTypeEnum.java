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
package com.github.devnied.emvnfccard.model.enums;

/**
 * Type of kernel a card asks for, bits 8 and 7 of the first byte of the Kernel
 * Identifier (tag '9F2A').
 * <p>
 * EMV Contactless Book B v2.12, Table 3-4 "Format of the Kernel Identifier (Tag
 * '9F2A') - Byte 1": "b8 b7 = Type of kernel ; 0 0 : An international kernel,
 * with a Kernel Identifier assigned by EMVCo and coded in the Short Kernel ID ;
 * 0 1 : RFU ; 1 0 : A domestic kernel, with Kernel Identifier in EMVCo format,
 * coded by the concatenation of the Short Kernel ID and the Extended Kernel ID
 * ; 1 1 : A domestic kernel, with the Kernel Identifier in proprietary format,
 * coded by the concatenation of the Short Kernel ID and the Extended Kernel
 * ID".
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public enum KernelTypeEnum implements IKeyEnum {

	/**
	 * '00b', an international kernel whose Kernel Identifier is assigned by
	 * EMVCo and coded in the Short Kernel ID alone
	 */
	INTERNATIONAL(0, "International kernel"),

	/**
	 * '01b', reserved for future use. The Requested Kernel ID is still the
	 * whole first byte, so the Short Kernel ID does not name a kernel on its
	 * own.
	 */
	RFU(1, "RFU"),

	/**
	 * '10b', a domestic kernel whose Kernel Identifier is in the EMVCo format:
	 * the Short Kernel ID concatenated with the Extended Kernel ID, whose byte
	 * 2 is a currency code as defined by ISO 4217
	 */
	DOMESTIC_EMVCO_FORMAT(2, "Domestic kernel (EMVCo format)"),

	/**
	 * '11b', a domestic kernel whose Kernel Identifier is in a proprietary
	 * format: the Short Kernel ID concatenated with the Extended Kernel ID
	 */
	DOMESTIC_PROPRIETARY_FORMAT(3, "Domestic kernel (proprietary format)");

	/**
	 * Value of the bits 8 and 7 of the first byte of the Kernel Identifier,
	 * shifted down to the two low order bits
	 */
	private final int key;

	/**
	 * Kernel type name
	 */
	private final String name;

	/**
	 * Constructor using fields
	 *
	 * @param pKey
	 *            value of the bits 8 and 7 of the first byte
	 * @param pName
	 *            kernel type name
	 */
	private KernelTypeEnum(final int pKey, final String pName) {
		key = pKey;
		name = pName;
	}

	/**
	 * Method used to get the value of the bits 8 and 7 of the first byte of the
	 * Kernel Identifier
	 *
	 * @return the key value
	 */
	@Override
	public int getKey() {
		return key;
	}

	/**
	 * Method used to get the kernel type name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Method used to find the kernel type matching the bits 8 and 7 of the
	 * first byte of the Kernel Identifier
	 *
	 * @param pKey
	 *            value of the bits 8 and 7, shifted down to the two low order
	 *            bits
	 * @return the kernel type, or null when the parameter is not a two bits
	 *         value
	 */
	public static KernelTypeEnum find(final int pKey) {
		for (KernelTypeEnum type : values()) {
			if (type.key == pKey) {
				return type;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

}
