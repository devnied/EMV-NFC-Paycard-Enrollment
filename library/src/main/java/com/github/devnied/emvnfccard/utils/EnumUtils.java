package com.github.devnied.emvnfccard.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.model.enums.IKeyEnum;

/**
 * Utils class which provided methods to manipulate Enum
 * 
 * @author MILLAU Julien
 */
public final class EnumUtils {

	/**
	 * Class logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(EnumUtils.class);

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
		LOGGER.error("Unknow value:" + pKey + " for Enum:" + pClass.getName());
		return null;
	}

	/**
	 * Private constructor
	 */
	private EnumUtils() {
	}
}
