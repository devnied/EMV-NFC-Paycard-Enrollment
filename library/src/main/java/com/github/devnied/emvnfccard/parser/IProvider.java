package com.github.devnied.emvnfccard.parser;

/**
 * Interface for provider for transmit command to card
 * 
 * @author julien Millau
 * 
 */
public interface IProvider {

	/**
	 * Method used to transmit and receive card response
	 * 
	 * @param pCommand
	 *            command to send to card
	 * @return byte array returned by card
	 */
	byte[] transceive(byte[] pCommand);

}
