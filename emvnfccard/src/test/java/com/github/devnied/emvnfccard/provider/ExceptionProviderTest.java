package com.github.devnied.emvnfccard.provider;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

public class ExceptionProviderTest implements IProvider {
	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {

		throw new CommunicationException();
	}

}
