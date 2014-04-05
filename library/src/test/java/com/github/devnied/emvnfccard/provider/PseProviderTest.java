package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

public class PseProviderTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PseProviderTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {
		case 0:
			response = "6F 1A 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 08 88 01 01 5F 2D 02 65 6E 90 00";
			break;
		case 1:
			response = "6C 1C";
			break;
		case 2:
			response = "70 1A 61 18 4F 07 A0 00 00 00 03 10 10 50 0A 56 49 53 41 43 52 45 44 49 54 87 01 01 90 00";
			break;
		case 3:
			response = "6F 33 84 07 A0 00 00 00 03 10 10 A5 28 9F 38 18 9F 66 04 9F 02 06 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 0A DF 60 02 0B 1E 9F 4D 02 0B 1E 90 00";
			break;
		case 4:
			response = "67 00";
			break;
		case 5:
			response = "70 35 57 13 49 79 67 01 23 45 36 00 D1 60 22 01 73 39 97 58 30 00 0F 9F 1F 18 37 33 33 39 39 37 35 38 33 30 30 30 30 30 30 35 38 33 30 30 30 30 30 30 5F 20 02 20 2F 90 00";
			break;
		default:
			response = "00 00 00 00 46 00 40 02 50 09 78 14 03 16 00 90 00";
		}
		LOGGER.debug("resp: " + response);
		return BytesUtils.fromString(response);
	}

}
