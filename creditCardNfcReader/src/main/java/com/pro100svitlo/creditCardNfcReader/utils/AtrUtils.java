package com.pro100svitlo.creditCardNfcReader.utils;

import android.util.Log;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Created by pro100svitlo on 15.05.16.
 */

public final class AtrUtils {

    private static String TAG = "creditCardNfcReader";
    /**
     * MultiMap containing ATR
     */
    private static final MultiMap<String, String> MAP = new MultiValueMap<String, String>();

    static {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            is = AtrUtils.class.getResourceAsStream("/smartcard_list.txt");
            isr = new InputStreamReader(is, CharEncoding.UTF_8);
            br = new BufferedReader(isr);

            int lineNumber = 0;
            String line;
            String currentATR = null;
            while ((line = br.readLine()) != null) {
                ++lineNumber;
                if (line.startsWith("#") || line.trim().length() == 0) { // comment ^#/ empty line ^$/
                    continue;
                } else if (line.startsWith("\t") && currentATR != null) {
                    MAP.put(currentATR, line.replace("\t", "").trim());
                } else if (line.startsWith("3")) { // ATR hex
                    currentATR = StringUtils.deleteWhitespace(line.toUpperCase());
                } else {
                    Log.d(TAG, "Encountered unexpected line in atr list: currentATR=" + currentATR + " Line(" + lineNumber
                            + ") = " + line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Method used to find description from ATR
     *
     * @param pAtr
     *            Card ATR
     * @return list of description
     */
    @SuppressWarnings("unchecked")
    public static final Collection<String> getDescription(final String pAtr) {
        Collection<String> ret = null;
        if (StringUtils.isNotBlank(pAtr)) {
            String val = StringUtils.deleteWhitespace(pAtr);
            for (String key : MAP.keySet()) {
                if (val.matches("^" + key + "$")) {
                    ret = (Collection<String>) MAP.get(key);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Method used to find ATR description from ATS (Answer to select)
     *
     * @param pAts
     *            EMV card ATS
     * @return card description
     */
    @SuppressWarnings("unchecked")
    public static final Collection<String> getDescriptionFromAts(final String pAts) {
        Collection<String> ret = null;
        if (StringUtils.isNotBlank(pAts)) {
            String val = StringUtils.deleteWhitespace(pAts);
            for (String key : MAP.keySet()) {
                if (key.contains(val)) { // TODO Fix this
                    ret = (Collection<String>) MAP.get(key);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Private constructor
     */
    private AtrUtils() {
    }

}
