package com.github.devnied.emvnfccard.provider;

import java.io.IOException;

import android.nfc.tech.IsoDep;
import android.util.Log;

import com.github.devnied.emvnfccard.BuildConfig;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

import fr.devnied.bitlib.BytesUtils;

/**
 * Provider used to communicate with EMV card
 * 
 * @author Millau Julien
 * 
 */
public class Provider implements IProvider {

	/**
	 * TAG for logger
	 */
	private static final String TAG = Provider.class.getName();

	/**
	 * Tag comm
	 */
	private IsoDep mTagCom;

	/**
	 * Constructor
	 * 
	 * @param pTagCom
	 */
	public Provider(final IsoDep pTagCom) {
		mTagCom = pTagCom;
	}

	@Override
	public byte[] transceive(final byte[] pCommand) throws CommunicationException {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "send: " + BytesUtils.bytesToString(pCommand));
		}

		byte[] response = null;
		try {
			// send command to emv card
			response = mTagCom.transceive(pCommand);
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage());
		}

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "resp: " + BytesUtils.bytesToString(response));
		}
		return response;
	}

}
