package com.github.devnied.emvnfccard.parser;

import org.junit.Test;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.impl.ProviderWrapper;
import com.github.devnied.emvnfccard.provider.TestProvider;

import fr.devnied.bitlib.BytesUtils;

public class ProviderWrapperTest {
	
	@Test
	public void testProvider() throws CommunicationException{
		ProviderWrapper wrapper = new ProviderWrapper(new TestProvider("wrapperTest"));
		wrapper.transceive(BytesUtils.fromString("00 A4 04 00 05 A0 00 00 00 03 00"));
	}
}
