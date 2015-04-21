package com.github.devnied.emvnfccard.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import android.text.Html;
import android.util.Log;

import com.github.devnied.emvnfccard.fragment.viewPager.impl.LogFragment;

/**
 * Library exception
 *
 * @author MILLAU Julien
 *
 */
public class LibraryException extends RuntimeException {

	/**
	 * Generated serial UID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Library exception
	 *
	 * @param pError
	 * @param pTh
	 */
	public LibraryException(final StringBuffer pBuff, final Throwable pTh) {
		super(StringUtils.join(getLines(Html.fromHtml(pBuff.toString()).toString().getBytes()), "-"), pTh);
	}

	private static List<String> getLines(final byte[] pData) {
		List<String> lines = new ArrayList<String>();
		InputStream is = new ByteArrayInputStream(pData);
		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("resp:") || line.startsWith("send:")) {
					lines.add(line.replaceAll("[^a-zA-Z0-9:]", ""));
				}
			}
		} catch (IOException e) {
			Log.e(LogFragment.class.getName(), e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		return lines;
	}

}
