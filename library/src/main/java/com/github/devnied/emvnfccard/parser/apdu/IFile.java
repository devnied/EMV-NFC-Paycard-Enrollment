package com.github.devnied.emvnfccard.parser.apdu;

/**
 * Interface for File to parse
 * 
 * @author julien Millau
 * 
 */
public interface IFile {

	/**
	 * Method to parse byte data
	 * 
	 * @param pData
	 *            byte to parse
	 */
	void parse(final byte[] pData);

	/**
	 * Get default size
	 * 
	 * @return the size of the file
	 */
	int getDefaultSize();

}
