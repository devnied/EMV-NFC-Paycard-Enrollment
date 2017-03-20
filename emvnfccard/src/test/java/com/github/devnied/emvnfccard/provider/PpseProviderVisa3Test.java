package com.github.devnied.emvnfccard.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import fr.devnied.bitlib.BytesUtils;

public class PpseProviderVisa3Test implements IProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PpseProviderVisa3Test.class);

	private int step;

	@Override
	public byte[] transceive(final byte[] pCommand) {
		String response = null;
		LOGGER.debug("send: " + BytesUtils.bytesToString(pCommand));
		switch (step++) {

		case 0:
			response = "6F 2A 84 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 18 BF 0C 15 61 0C 4F 07 A0 00 00 00 42 10 10 87 01 01 BF 63 04 DF 20 01 80 90 00";
			break;
		case 1:
			response = "6F 28 84 07 A0 00 00 00 42 10 10 A5 1D 87 01 01 9F 38 0C 9F 66 04 9F 02 06 9F 37 04 5F 2A 02 BF 0C 08 9F 5A 05 31 08 26 08 26 90 00";
			break;
		case 2:
			response = "77 81 B9 82 02 20 00 94 04 10 02 04 00 57 13 49 99 99 99 99 99 99 99 D1 80 62 01 00 00 00 37 74 94 0F 5F 34 01 00 9F 10 07 FF FF FF FF FF FF FF 9F 26 08 FF FF FF FF FF FF FF FF 9F 27 01 40 9F 36 02 00 D9 9F 6C 02 16 00 9F 4B 70 FF FF FF FF BC 6E 64 DE A4 5A 5F 51 7D CA 15 D5 0E 6F D8 34 9A 08 AE 27 D5 AE 62 F8 30 DD 37 EE CE FF FF FF FF FF FF FF FF FF FF FF FF EC 97 B4 53 D2 B6 15 2F 51 26 A1 0F FC E0 17 7B 0C 56 6B CF 1D C8 A1 9F 6C C9 1C FB 30 84 AA C3 64 1A B7 92 A4 18 1A 63 62 6F 4F 0C 9D CE 3D D5 3B 48 1F 6C 7C F9 50 AC FF FF FF FF FF FF FF FF FF FF FF 90 00";
			break;
		case 3:
			response = "70 3E 8F 01 07 9F 32 01 03 92 24 E0 FF 85 7D 6C FE EB 6B F8 A4 AD 5E 2A FF 13 02 95 1A BB 2C 95 1C 4A 1B 2C 96 EB 35 AA A6 DD 6D 17 AD C8 E3 9F 47 01 03 9F 48 0A 9C 35 50 B4 83 CA EC F1 DC 23 90 00";
			break;
		case 4:
			response = "70 81 93 90 81 90 57 EA 76 DA 8D A5 4B F5 20 E4 DA 2C F1 4A 98 9D ED 97 24 FB 20 63 31 B7 99 46 AD 74 D1 80 97 D5 D1 D5 F5 BA A5 E9 0D 20 FC 90 1C 4A 94 AD A1 0E DD 09 C6 6E 58 6E F9 C6 7F B4 19 4D CC F7 05 18 7F 1C 44 02 6A F2 49 47 3A 2C 97 80 B3 6E 24 B0 7E 1F B8 0D C0 3E 4D 49 31 79 09 6D A0 B5 34 67 01 BF B4 81 07 19 6F D5 ED 00 DC 5F DF 7E 2E AA 2A 1E F4 BA F0 0B 8F 07 73 FA FC 0F AD 65 E4 5E 55 3E 83 3B 83 E9 77 7F 23 71 B8 DC F5 6B C4 A2 90 00";
			break;
		case 5:
			response = "70 81 B3 9F 46 81 90 51 4B 75 7E 06 11 C0 70 0F B5 4D 7D 6F C0 07 3C AF E6 C4 D4 CD 54 B1 6A DF 7E 6B 8D 3D 1D EC 5F D5 59 B7 A5 07 F8 B5 52 BA E8 F7 DA B5 FF FB 19 AB 10 4A FD 47 E9 A9 08 8E 9D 9A 8F 73 AC 27 3A B7 1D 5F 9B E4 57 9B A7 F8 FB 4C 96 E7 8F D0 54 A2 5E 12 52 55 65 8A 8D C0 C8 22 6D 61 C4 F7 C4 45 70 EC 02 E0 07 0F 6E CF 8D 76 A3 8F 37 96 BB FD A5 86 AE 60 5D 86 A4 89 33 06 18 DD A2 3A B2 90 0A E6 A1 C6 F3 EE 21 BE FA 55 6D E3 26 2D 33 5A 08 49 99 99 99 99 99 99 99 5F 24 03 17 06 30 5F 28 02 08 26 9F 69 07 01 B7 B9 25 27 16 00 90 00";
			break;
		case 6:
			response = "6F 10 84 08 A0 00 00 00 42 00 00 00 A5 04 9F 65 01 FF 90 00";
			break;
		case 7:
			response = "6D 00";
			break;

		default:

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
