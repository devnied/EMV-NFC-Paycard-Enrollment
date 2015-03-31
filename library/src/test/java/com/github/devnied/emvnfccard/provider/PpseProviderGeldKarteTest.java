package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderGeldKarteTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderGeldKarteTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {

		case 0:
			response = "6F 25 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 13 BF 0C 10 61 0E 4F 09 D2 76 00 00 25 45 50 02 00 87 01 01 90 00";
			break;
		case 1:
			response = "6F 18 84 09 D2 76 00 00 25 45 50 02 00 A5 0B 50 09 47 65 6C 64 4B 61 72 74 65 90 00";
			break;
		case 2:
			response = "69 85";
			break;
		case 3:
			response = "69 85";
			break;
		case 4:
			response = "69 85";
			break;
		case 5:
			response = "69 85";
			break;
		case 6:
			response = "69 85";
			break;
		case 7:
			response = "6F 59 84 07 A0 00 00 00 04 30 60 A5 4E 50 07 4D 61 65 73 74 72 6F 87 01 07 9F 38 09 9F 33 02 9F 35 01 9F 40 01 5F 2D 04 64 65 65 6E BF 0C 2C 9F 4D 02 19 0A 5F 54 0B 53 50 4B 48 44 45 32 48 58 58 58 5F 53 16 44 45 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 90 00";
			break;
		case 8:
			response = "70 81 8C 9F 6C 02 00 01 9F 62 06 00 00 00 00 07 00 9F 63 06 00 00 00 00 00 FE 56 41 42 33 31 34 34 34 34 00 30 30 35 33 31 39 00 37 34 00 20 2F 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 5E 31 38 30 31 31 30 31 30 31 30 30 30 30 30 30 30 30 30 30 33 9F 64 01 04 9F 65 02 07 00 9F 66 02 00 FE 9F 6B 13 52 00 00 00 00 00 00 00 D1 91 11 01 01 00 00 00 00 00 3F 9F 67 01 04 9F 6E 07 02 21 00 00 30 30 00 90 00";
			break;
		case 9:
			response = "9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00";
			break;
		default:
			response = "00 00 00 00 46 00 40 02 50 09 78 14 03 16 20 90 00";
		}

		// Pin try left
		if (BytesUtils.bytesToStringNoSpace(pCommand).equals("80CA9F1700")) {
			response = "9F 17 01 03 90 00";
		}

		// ATC
		if (BytesUtils.bytesToStringNoSpace(pCommand).equals("80CA9F3600")) {
			response = "9F 36 02 06 2C 90 00";
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
