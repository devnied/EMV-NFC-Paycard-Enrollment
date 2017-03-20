package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderMasterCard2Test implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderMasterCard2Test.class);

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
			response = "6F 7D 84 07 A0 00 00 00 04 10 10 A5 72 50 10 44 45 42 49 54 20 4D 41 53 54 45 52 43 41 52 44 9F 12 0A 4D 41 53 54 45 52 43 41 52 44 87 01 01 9F 11 01 09 9F 38 03 9F 5C 08 BF 0C 43 42 03 52 82 08 9F 5D 03 01 03 00 9F 5E 09 52 00 00 00 00 00 00 00 01 9F 4D 02 0B 0A DF 64 20 02 50 48 43 45 4E 42 4B 52 54 20 41 4E 43 30 30 31 4F 4E 20 01 01 4E 4E 50 00 00 00 00 00 00 00 DF 65 01 01 90 00";
			break;
		case 2:
			response = "77 12 82 02 19 80 94 0C 08 01 01 00 10 01 01 01 20 02 03 00 90 00";
			break;
		case 3:
			response = "70 6B 9F 6C 02 00 01 9F 62 06 00 00 00 00 0E 00 9F 63 06 00 00 00 00 00 7E 56 2A 42 35 30 30 32 30 30 30 30 30 35 34 35 37 37 32 36 5E 20 2F 5E 32 31 31 32 32 30 32 30 30 30 30 30 31 30 30 30 30 30 30 30 30 9F 64 01 03 9F 65 02 00 0E 9F 66 02 0E 70 9F 6B 13 52 00 00 00 00 00 00 00 D0 11 92 02 00 00 01 00 00 00 0F 9F 67 01 03 90 00";
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
