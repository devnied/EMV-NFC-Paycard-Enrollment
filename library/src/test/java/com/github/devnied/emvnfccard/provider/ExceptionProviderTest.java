package com.github.devnied.emvnfccard.provider;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

/**
 * Provider used to mock card response (Always throw CommunicationException)
 *
 * @author MILLAU julien
 *
 */
public class ExceptionProviderTest implements IProvider {

	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {
		throw new CommunicationException();
	}

}
