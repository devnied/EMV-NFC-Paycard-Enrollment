package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderVisaNulTransactionsTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderVisaNulTransactionsTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {

		case 0:
			response = "6F 3B 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 29 BF 0C 26 61 10 4F 07 A0 00 00 00 42 10 10 50 02 43 42 87 01 01 61 12 4F 07 A0 00 00 00 03 10 10 50 04 56 49 53 41 87 01 02 90 00";
			break;
		case 1:
			response = "6F 37 84 07 A0 00 00 00 42 10 10 A5 2C 9F 38 18 9F 66 04 9F 02 06 9F 03 06 9F 1A 02 95 05 5F 2A 02 9A 03 9C 01 9F 37 04 BF 0C 0E DF 60 02 0B 1E DF 61 01 03 9F 4D 02 0B 1E 90 00";
			break;
		case 2:
			response = "67 00";
			break;
		case 3:
			response = "77 38 9F 10 07 06 01 1A 23 80 40 04 57 13 49 99 99 99 99 99 99 99 D1 50 92 FF FF FF FF FF FF FF 0F 82 02 20 00 9F 36 02 02 8F 9F 26 08 FF FF FF FF FF FF FF FF 9F 6C 02 10 00 90 00";
			break;
		case 4:
			response = "9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00";
			break;
		default:
			response = "00 00 00 00 00 00 40 02 50 09 78 14 03 16 20 90 00";
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
