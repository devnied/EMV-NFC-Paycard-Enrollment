package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {
		case 0:
			response = "6F 3B 54 0E 78 50 41 88 2E 53 59 00 2E 44 44 78 30 31 A5 29 BF 0C 26 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01 61 12 4F 07 A0 00 00 00 03 10 10 50 04 56 49 53 41 87 01 02 90 00";
			break;
		case 1:
			response = "6F 37 84 07 A0 00 00 00 42 10 10 A5 2C 9F 38 18 99 66 DF 34 02 06 9F 03 06 56 1A 02 00 05 5F 2A 34 9A 24 9C 01 56 66 04 67 DD 0E 45 34 02 43 1E DF 61 00 03 9F 4D 02 48 1E 90 00";
			break;
		case 2:
			response = "67 00";
			break;
		case 3:
			response = "70 35 57 13 49 79 67 01 23 45 36 00 D1 60 22 01 73 39 97 58 30 00 0F 9F 1F 18 37 33 33 39 39 37 35 38 33 30 30 30 30 30 30 35 38 33 30 30 30 30 30 30 5F 20 02 20 2F 90 00";
			break;
		default:
			response = "00 00 00 00 46 00 40 02 50 09 78 14 03 16 00 90 00";
		}
		LOGGER.debug("resp: " + response);
		return BytesUtils.fromString(response);
	}

}
