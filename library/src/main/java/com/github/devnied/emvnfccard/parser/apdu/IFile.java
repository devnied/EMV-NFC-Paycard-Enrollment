package com.github.devnied.emvnfccard.parser.apdu;

import java.util.List;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;

/**
 * Interface for File to parse
 * 
 * @author MILLAU Julien
 * 
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

	/**
	 * Get default size
	 * 
	 * @return the size of the file
	 */
	int getDefaultSize();

}
