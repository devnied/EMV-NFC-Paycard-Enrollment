package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

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
			response = "6F 1E 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 0C 88 01 01 9F 11 01 01 5F 2D 02 66 72 90 00";
			break;
		case 1:
			response = "70 23 61 21 4F 07 A0 00 00 00 42 10 10 50 02 43 42 9F 12 0E 54 52 41 4E 53 41 43 54 49 4F 4E 20 43 42 87 01 01 90 00";
			break;
		case 2:
			response = "6F 3D 84 07 A0 00 00 00 42 10 10 A5 32 50 02 43 42 87 01 01 9F 11 01 01 9F 12 0E 54 52 41 4E 53 41 43 54 49 4F 4E 20 43 42 5F 2D 02 66 72 BF 0C 0E DF 60 02 0B 1E DF 61 01 03 9F 4D 02 0B 1E 90 00";
			break;
		case 3:
			response = "80 12 3C 00 08 02 02 00 10 01 02 00 10 04 04 00 18 01 05 03 90 00";
			break;
		case 4:
			response = "70 35 57 13 49 79 67 01 23 45 36 00 D1 60 22 01 74 12 FF FF 80 00 0F 9F 1F 18 FF FF 31 32 35 37 37 FF 3F 30 FF 30 30 30 30 37 35 FF FF 30 30 30 30 FF 5F 20 02 20 2F 90 00";
			break;
		case 5:
			response = "9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00";
			break;
		default:
			response = "00 00 00 00 40 00 80 02 50 09 78 14 07 16 00 90 00";
		}

		if (BytesUtils.bytesToStringNoSpace(pCommand).equals("80CA9F1700")) {
			response = "9F 17 01 03 90 00";
		}

		LOGGER.debug("resp: " + response);
		byte[] ret = BytesUtils.fromString(response);
		try {
			LOGGER.debug(TlvUtil.prettyPrintAPDUResponse(ret));
		} catch (Exception e) {
		}
		return ret;
	}

}
