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
 * Nature of a File reference data element (tag '51'), which is given by its
 * length alone.
 * <p>
 * ISO/IEC 7816-4:2005 section 5.3.1.2 "File reference data element": "Referenced
 * by tag '51', this interindustry data element references a file. It may have
 * any length. - An empty data object references the MF. - If the length is one
 * and if bits 8 to 4 of the data element are not all equal and if bits 3 to 1
 * are set to 000, then bits 8 to 4 encode a number from one to thirty that is a
 * short EF identifier. - If the length is two, then the data element is a file
 * identifier. - If the length is more than two, then the data element is a path.
 * If the length is even and if the first two bytes are set to '3F00', then the
 * path is absolute. [...] If the length is even and if the first two bytes are
 * not set to '3F00', then the path is relative. [...] If the length is odd, then
 * the path is qualified. The data element is either an absolute path without
 * '3F00', or a relative path without the identifier of the current DF, followed
 * by a byte to use as P1 in one or more SELECT commands".
 * </p>
 * <p>
 * The tag '51' is not part of EMV Book 1 (neither v4.3 nor v4.4): it appears in
 * none of its File Control Information tables, in none of its directory entry
 * tables and in none of its data element tables. Only the ISO reading above is
 * used here.
 * </p>
 *
 * @author MILLAU Julien
 *
 */
public enum FileReferenceEnum {

	/**
	 * Empty value field, the reference designates the Master File
	 */
	MASTER_FILE("Master File"),

	/**
	 * One byte, bits 8 to 4 hold a short EF identifier in the range 1 to 30
	 */
	SHORT_FILE_IDENTIFIER("Short EF identifier"),

	/**
	 * Two bytes, the value field is a file identifier
	 */
	FILE_IDENTIFIER("File identifier"),

	/**
	 * An even number of bytes greater than two, beginning with '3F00': a
	 * concatenation of file identifiers starting with the MF identifier
	 */
	ABSOLUTE_PATH("Absolute path"),

	/**
	 * An even number of bytes greater than two, not beginning with '3F00': a
	 * concatenation of file identifiers starting with the identifier of the
	 * current DF
	 */
	RELATIVE_PATH("Relative path"),

	/**
	 * An odd number of bytes greater than two: a path whose last byte is to be
	 * used as P1 in one or more SELECT commands
	 */
	QUALIFIED_PATH("Qualified path");

	/**
	 * Name of the reference, as ISO/IEC 7816-4 Table 10 words it
	 */
	private final String name;

	/**
	 * Constructor using fields
	 *
	 * @param pName
	 *            name of the reference
	 */
	private FileReferenceEnum(final String pName) {
		name = pName;
	}

	/**
	 * Method used to get the name of the reference
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
