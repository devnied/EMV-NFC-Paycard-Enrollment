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
 * Contactless kernel an application is meant to be processed on, identified by
 * its Short Kernel Identifier (EMV Contactless Book B, Kernel Identifier).
 * <p>
 * These are the kernels of the table that derives a kernel from an Application
 * Identifier. A card announcing another Short Kernel Identifier is not guessed
 * at: the number itself is exposed by
 * {@link com.github.devnied.emvnfccard.model.Application#getShortKernelId()}.
 * A domestic kernel is identified by the first three bytes of the Kernel
 * Identifier as a whole and is never mapped here.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public enum KernelEnum implements IKeyEnum {

	/**
	 * Kernel 2, the MasterCard applications
	 */
	MASTERCARD(2, "MasterCard"),

	/**
	 * Kernel 3, the Visa applications
	 */
	VISA(3, "Visa"),

	/**
	 * Kernel 4, the American Express applications
	 */
	AMERICAN_EXPRESS(4, "American Express"),

	/**
	 * Kernel 5, the JCB applications
	 */
	JCB(5, "JCB"),

	/**
	 * Kernel 6, the Discover applications
	 */
	DISCOVER(6, "Discover"),

	/**
	 * Kernel 7, the UnionPay applications
	 */
	UNIONPAY(7, "UnionPay");

	/**
	 * Short Kernel Identifier
	 */
	private final int shortKernelId;

	/**
	 * Kernel name
	 */
	private final String name;

	/**
	 * Constructor using fields
	 *
	 * @param pShortKernelId
	 *            short kernel identifier
	 * @param pName
	 *            kernel name
	 */
	private KernelEnum(final int pShortKernelId, final String pName) {
		shortKernelId = pShortKernelId;
		name = pName;
	}

	/**
	 * Method used to get the Short Kernel Identifier
	 *
	 * @return the short kernel identifier
	 */
	@Override
	public int getKey() {
		return shortKernelId;
	}

	/**
	 * Method used to get the kernel name
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Method used to find the kernel matching a Short Kernel Identifier
	 *
	 * @param pShortKernelId
	 *            short kernel identifier
	 * @return the kernel, or null when no international kernel bears this
	 *         identifier
	 */
	public static KernelEnum find(final int pShortKernelId) {
		for (KernelEnum kernel : values()) {
			if (kernel.shortKernelId == pShortKernelId) {
				return kernel;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

}
