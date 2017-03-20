package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderMasterCardTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderMasterCardTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {

		case 0:
			response = "6F 4D 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 3B BF 0C 38 61 16 4F 07 A0 00 00 00 42 10 10 87 01 01 50 02 43 42 9F 28 03 40 02 00 61 1E 4F 07 A0 00 00 00 04 10 10 87 01 02 50 0A 4D 41 53 54 45 52 43 41 52 44 9F 28 03 40 02 00 90 00";
			break;
		case 1:
			response = "6F 29 84 07 A0 00 00 00 42 10 10 A5 1E 50 02 43 42 87 01 01 9F 11 01 01 9F 12 02 43 42 5F 2D 04 66 72 65 6E BF 0C 04 DF 61 01 04 90 00";
			break;
		case 2:
			response = "77 12 82 02 19 80 94 0C 10 01 01 01 18 01 01 00 20 01 02 00 90 00";
			break;
		case 3:
			response = "77 38 9F 10 07 06 01 1A 23 80 40 04 57 13 55 99 99 99 99 99 99 99 D1 50 92 FF FF FF FF FF FF FF 0F 82 02 20 00 9F 36 02 02 8F 9F 26 08 FF FF FF FF FF FF FF FF 9F 6C 02 10 00 90 00";
			break;
		case 4:
			response = "9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00";
			break;
		default:
			response = "00 00 00 00 46 00 40 02 50 09 78 14 03 16 20 90 00";
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
