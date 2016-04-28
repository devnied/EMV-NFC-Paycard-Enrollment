package com.pro100svitlo.creditCardNfcReader.parser;

import com.pro100svitlo.creditCardNfcReader.exception.CommunicationException;

/**
 * Interface for provider for transmit command to card
 */
public interface IProvider {

	/**
	 * Method used to transmit and receive card response
	 * 
	 * @param pCommand
	 *            command to send to card
	 * @return byte array returned by card
	 */
	byte[] transceive(byte[] pCommand) throws CommunicationException;

}
