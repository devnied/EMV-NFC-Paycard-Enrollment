/**
 * 
 */
package com.github.devnied.emvnfccard.parser;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.EMVCard;

/**
 * Interface for parser implementation
 * 
 * @author julien Millau
 */
public interface IParser {

	/**
	 * Method used to parse and extract data from EMV card
	 * 
	 * @param pSelectResponse
	 *            select response
	 * @param pProvider
	 *            provider used to send other command to the card
	 * @return EMVCard object containing data read from card
	 * @throws CommunicationException
	 */
	EMVCard parse(byte[] pSelectResponse, IProvider pProvider) throws CommunicationException;

}
