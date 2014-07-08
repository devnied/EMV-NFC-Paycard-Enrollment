package com.github.devnied.emvnfccard.provider;

import java.io.IOException;

import android.nfc.tech.IsoDep;
import android.util.Log;

import com.github.devnied.emvnfccard.BuildConfig;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.TLVUtil;

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
	 * Logger
	 */
	private StringBuilder log = new StringBuilder();

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
		log.append("send: " + BytesUtils.bytesToString(pCommand)).append("\n");

		byte[] response = null;
		try {
			// send command to emv card
			response = mTagCom.transceive(pCommand);
		} catch (IOException e) {
			throw new CommunicationException(e.getMessage());
		}

		if (BuildConfig.DEBUG) {
			log.append("resp: " + BytesUtils.bytesToString(response)).append("\n");
			Log.d(TAG, "resp: " + BytesUtils.bytesToString(response));
			try {
				Log.d(TAG, "resp: " + TLVUtil.prettyPrintAPDUResponse(response));
				log.append(TLVUtil.prettyPrintAPDUResponse(response)).append("\n");
			} catch (Exception e) {
			}
		}
		return response;
	}

	/**
	 * Method used to get the field log
	 * 
	 * @return the log
	 */
	public StringBuilder getLog() {
		return log;
	}

}
