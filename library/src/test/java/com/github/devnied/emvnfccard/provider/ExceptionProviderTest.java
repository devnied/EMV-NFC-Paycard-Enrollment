package com.github.devnied.emvnfccard.provider;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

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

	@Override
	public byte[] getAt() {
		return BytesUtils.fromString("3B 65 00 00 20 63 CB A0 00");
	}

}
