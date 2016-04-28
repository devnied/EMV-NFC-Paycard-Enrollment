package com.pro100svitlo.creditCardNfcReader.parser.apdu;

import com.pro100svitlo.creditCardNfcReader.iso7816emv.TagAndLength;

import java.util.List;

/**
 * Interface for File to parse
 */
public interface IFile {

	/**
	 * Method to parse byte data
	 * 
	 * @param pData
	 *            byte to parse
	 * @param pList
	 *            Tag and length
	 */
	void parse(final byte[] pData, final List<TagAndLength> pList);

}
