package com.github.devnied.emvnfccard.provider;

import org.fest.assertions.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class ProviderVisaCardAidTest implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProviderVisaCardAidTest.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));

		switch (step++) {

		case 0:
			response = "6F 37 84 07 A0 00 00 00 03 10 10 A5 2C 50 0A 56 49 53 41 20 44 45 42 49 54 87 01 02 9F 11 01 01 9F 12 04 56 49 53 41 5F 2D 02 66 72 BF 0C 0A DF 60 02 0B 1E 9F 4D 02 0B 1E 90 00";
			break;
		case 1:
			Assertions.assertThat(BytesUtils.bytesToString(pCommand)).isEqualTo("80 A8 00 00 02 83 00 00");
			response = "80 12 3C 00 08 02 02 00 10 01 02 00 10 04 04 00 18 01 05 03 90 00";
			break;
		case 2:
			Assertions.assertThat(BytesUtils.bytesToString(pCommand)).isEqualTo("00 B2 02 0C 00");
			response = "70 35 57 13 40 00 00 00 00 00 00 00 D1 40 92 01 74 12 57 75 80 00 0F 9F 1F 18 37 34 31 32 35 37 37 35 38 30 30 30 30 30 30 37 35 38 30 30 30 30 30 30 5F 20 02 20 2F 90 00";
			break;
		case 3:
			Assertions.assertThat(BytesUtils.bytesToString(pCommand)).isEqualTo("80 CA 9F 4F 00");
			response = "9F 4F 10 9F 02 06 9F 27 01 9F 1A 02 5F 2A 02 9A 03 9C 01 90 00";
			break;
		default:
			response = "00 00 00 00 00 01 40 02 50 09 78 14 07 25 00 90 00";
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
