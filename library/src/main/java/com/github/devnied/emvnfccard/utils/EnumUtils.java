package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.model.enums.IKeyEnum;

/**
 * Utils class which provided methods to manipulate Enum
 * 
 * @author julien Millau
 */
public abstract class EnumUtils {

	/**
	 * Get the value of and enum from his key
	 * 
	 * @param pKey
	 *            key to find
	 * @param pClass
	 *            Enum class
	 * @return Enum instance of the specified key or null otherwise
	 */
	public static IKeyEnum getValue(final int pKey, final Class<? extends IKeyEnum> pClass) {
		for (IKeyEnum val : pClass.getEnumConstants()) {
			if (val.getKey() == pKey) {
				return val;
			}
		}
		return null;
	}
}
