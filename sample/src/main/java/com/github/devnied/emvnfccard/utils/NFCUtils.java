package com.github.devnied.emvnfccard.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;

/**
 * 
 * Utils class used to manager NFC Adapter
 * 
 * @author MILLAU julien
 * 
 */
public class NFCUtils {

	/**
	 * Check if the NFCAdapter is enable
	 * 
	 * @return true if the NFCAdapter is available enable
	 */
	public static boolean isNfcEnable(final Context pContext) {
		boolean ret = true;
		try {
			NfcAdapter adpater = NfcAdapter.getDefaultAdapter(pContext);
			ret = adpater != null && adpater.isEnabled();
		} catch (UnsupportedOperationException e) {
			ret = false;
		}
		return ret;
	}

	/**
	 * NFC adapter
	 */
	private final NfcAdapter mNfcAdapter;
	/**
	 * Intent sent
	 */
	private final PendingIntent mPendingIntent;

	/**
	 * Parent Activity
	 */
	private final Activity mActivity;

	/**
	 * Inetnt filter
	 */
	private static final IntentFilter[] INTENT_FILTER = new IntentFilter[] { new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED) };

	/**
	 * Tech List
	 */
	private static final String[][] TECH_LIST = new String[][] { { IsoDep.class.getName() } };

	/**
	 * Constructor of this class
	 * 
	 * @param pActivity
	 *            activity context
	 */
	public NFCUtils(final Activity pActivity) {
		mActivity = pActivity;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(mActivity);
		mPendingIntent = PendingIntent.getActivity(mActivity, 0,
				new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	/**
	 * Disable dispacher Remove the most important priority for foreground application
	 */
	public void disableDispatch() {
		if (mNfcAdapter != null) {
			mNfcAdapter.disableForegroundDispatch(mActivity);
		}
	}

	/**
	 * Activate NFC dispacher to read NFC Card Set the most important priority to the foreground application
	 */
	public void enableDispatch() {
		if (mNfcAdapter != null) {
			mNfcAdapter.enableForegroundDispatch(mActivity, mPendingIntent, INTENT_FILTER, TECH_LIST);
		}
	}

	/**
	 * Getter mNfcAdapter
	 * 
	 * @return the mNfcAdapter
	 */
	public NfcAdapter getmNfcAdapter() {
		return mNfcAdapter;
	}
}
