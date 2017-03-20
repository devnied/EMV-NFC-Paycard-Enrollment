package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class ProviderAidTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProviderAidTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {
		case 0:
			response = "67 00";
			break;
		case 1:
			response = "6F 33 84 07 A0 00 00 00 03 10 10 A5 28 9F 38 18 9F 66 04 9F 02 06 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 0A DF 60 02 0B 1E 9F 4D 02 0B 1E 90 00";
			break;
		case 2:
			response = "80 0E 7C 00 08 01 01 00 10 01 05 00 18 01 02 01 90 00";
			break;
		case 3:
			response = "6C 4F";
			break;
		case 4:
			response = "70 35 57 13 57 72 82 91 93 25 34 72 D1 40 82 01 74 12 57 75 80 00 0F 9F 1F 18 37 34 31 32 35 37 37 35 38 30 30 30 30 30 30 37 35 38 30 30 30 30 30 30 5F 20 02 20 2F 90 00";
			break;
		case 5:
			response = "9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00";
			break;
		default:
			response = "00 00 00 00 71 35 40 02 50 09 78 13 12 22 20 90 00";
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
