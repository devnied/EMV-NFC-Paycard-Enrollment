package com.pro100svitlo.creditCardNfcReader.utils;

import java.math.BigInteger;
import java.util.Locale;

public final class BytesUtils {
    private static final int MAX_BIT_INTEGER = 31;
    private static final int HEXA = 16;
    private static final String FORMAT_NOSPACE = "%02x";
    private static final String FORMAT_SPACE = "%02x ";
    private static final int DEFAULT_MASK = 255;

    public static int byteArrayToInt(byte[] byteArray) {
        if(byteArray == null) {
            throw new IllegalArgumentException("Parameter \'byteArray\' cannot be null");
        } else {
            return byteArrayToInt(byteArray, 0, byteArray.length);
        }
    }

    public static int byteArrayToInt(byte[] byteArray, int startPos, int length) {
        if(byteArray == null) {
            throw new IllegalArgumentException("Parameter \'byteArray\' cannot be null");
        } else if(length > 0 && length <= 4) {
            if(startPos >= 0 && byteArray.length >= startPos + length) {
                int value = 0;

                for(int i = 0; i < length; ++i) {
                    value += (byteArray[startPos + i] & 255) << 8 * (length - i - 1);
                }

                return value;
            } else {
                throw new IllegalArgumentException("Length or startPos not valid");
            }
        } else {
            throw new IllegalArgumentException("Length must be between 1 and 4. Length = " + length);
        }
    }

    public static String bytesToString(byte[] pBytes) {
        return formatByte(pBytes, "%02x ", false);
    }

    public static String bytesToString(byte[] pBytes, boolean pTruncate) {
        return formatByte(pBytes, "%02x ", pTruncate);
    }

    public static String bytesToStringNoSpace(byte pByte) {
        return formatByte(new byte[]{pByte}, "%02x", false);
    }

    public static String bytesToStringNoSpace(byte[] pBytes) {
        return formatByte(pBytes, "%02x", false);
    }

    public static String bytesToStringNoSpace(byte[] pBytes, boolean pTruncate) {
        return formatByte(pBytes, "%02x", pTruncate);
    }

    private static String formatByte(byte[] pByte, String pFormat, boolean pTruncate) {
        StringBuffer sb = new StringBuffer();
        if(pByte == null) {
            sb.append("");
        } else {
            boolean t = false;
            byte[] arr$ = pByte;
            int len$ = pByte.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                byte b = arr$[i$];
                if(b != 0 || !pTruncate || t) {
                    t = true;
                    sb.append(String.format(pFormat, new Object[]{Integer.valueOf(b & 255)}));
                }
            }
        }

        return sb.toString().toUpperCase(Locale.getDefault()).trim();
    }

    public static byte[] fromString(String pData) {
        if(pData == null) {
            throw new IllegalArgumentException("Argument can\'t be null");
        } else {
            String text = pData.replace(" ", "");
            if(text.length() % 2 != 0) {
                throw new IllegalArgumentException("Hex binary needs to be even-length :" + pData);
            } else {
                byte[] commandByte = new byte[Math.round((float)text.length() / 2.0F)];
                int j = 0;

                for(int i = 0; i < text.length(); i += 2) {
                    Integer val = Integer.valueOf(Integer.parseInt(text.substring(i, i + 2), 16));
                    commandByte[j++] = val.byteValue();
                }

                return commandByte;
            }
        }
    }

    public static boolean matchBitByBitIndex(int pVal, int pBitIndex) {
        if(pBitIndex >= 0 && pBitIndex <= 31) {
            return (pVal & 1 << pBitIndex) != 0;
        } else {
            throw new IllegalArgumentException("parameter \'pBitIndex\' must be between 0 and 31. pBitIndex=" + pBitIndex);
        }
    }

    public static byte setBit(byte pData, int pBitIndex, boolean pOn) {
        if(pBitIndex >= 0 && pBitIndex <= 7) {
            byte ret;
            if(pOn) {
                ret = (byte)(pData | 1 << pBitIndex);
            } else {
                ret = (byte)(pData & ~(1 << pBitIndex));
            }

            return ret;
        } else {
            throw new IllegalArgumentException("parameter \'pBitIndex\' must be between 0 and 7. pBitIndex=" + pBitIndex);
        }
    }

    public static String toBinary(byte[] pBytes) {
        String ret = null;
        if(pBytes != null && pBytes.length > 0) {
            BigInteger val = new BigInteger(bytesToStringNoSpace(pBytes), 16);
            StringBuilder build = new StringBuilder(val.toString(2));

            for(int i = build.length(); i < pBytes.length * 8; ++i) {
                build.insert(0, 0);
            }

            ret = build.toString();
        }

        return ret;
    }

    public static byte[] toByteArray(int value) {
        return new byte[]{(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value};
    }

    private BytesUtils() {
    }
}
