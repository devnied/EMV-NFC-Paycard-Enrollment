package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderMasterCard3Test implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderMasterCard3Test.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {

		case 0:
			response = "6F 23 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 11 BF 0C 0E 61 0C 4F 07 A0 00 00 00 04 10 10 87 01 01 90 00";
			break;
		case 1:
			response = "6F 32 84 07 A0 00 00 00 04 10 10 A5 27 50 0A 4D 61 73 74 65 72 43 61 72 64 87 01 01 5F 2D 02 66 72 BF 0C 10 9F 4D 02 0B 0A 5F 56 03 43 41 4E DF 62 02 40 80 90 00";
			break;
		case 2:
			response = "77 0A 82 02 00 00 94 04 08 01 01 00 90 00";
			break;
		case 3:
			response = "70 81 8C 9F 6C 02 00 01 9F 62 06 00 00 00 00 07 00 9F 63 06 00 00 00 00 00 FE 56 41 42 33 31 34 34 34 34 00 30 30 35 33 31 39 00 37 34 00 20 2F 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 5E 31 38 30 31 31 30 31 30 31 30 30 30 30 30 30 30 30 30 30 33 9F 64 01 04 9F 65 02 07 00 9F 66 02 00 FE 9F 6B 13 52 00 00 00 00 00 00 00 D1 91 11 01 01 00 00 00 00 00 3F 9F 67 01 04 9F 6E 07 02 21 00 00 30 30 00 90 00";
			break;
		case 4:
			response = "9F 4F 1A 9F 27 01 9F 02 06 5F 2A 02 9A 03 9F 36 02 9F 52 06 DF 3E 01 9F 21 03 9F 7C 14 90 00";
			break;

		default:
			response = "40 00 00 00 00 22 00 09 49 11 01 12 00 E0 61 90 22 21 00 00 00 00 50 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 90 00";
		}

		if (BytesUtils.bytesToStringNoSpace(pCommand).equals("80CA9F1700")) {
			response = "9F 17 01 02 90 00";
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
