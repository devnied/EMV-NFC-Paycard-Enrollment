package com.github.devnied.emvnfccard.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import com.github.devnied.emvnfccard.EmvApplication;
import com.github.devnied.emvnfccard.R;

public class RateDialog {

	private final static String RATE_ALERT = "rateAlert";
	private final static String DONT_SHOW = "dontshowagain";
	private final static String LAUNCH_COUNT = "launch_count";

	private int launchesUntilPrompt = 3;

	private static Context mContext;

	public RateDialog(final Context context) {
		mContext = context;
	}

	/**
	 * Method used to reset application prefs
	 */
	public void reset() {
		SharedPreferences prefs = mContext.getSharedPreferences(RATE_ALERT, 0);
		if (prefs != null) {
			Editor editor = prefs.edit();
			editor.clear();
			editor.commit();
		}
	}

	public RateDialog init() {
		SharedPreferences prefs = mContext.getSharedPreferences(RATE_ALERT, 0);
		if (prefs.getBoolean(DONT_SHOW, false)) {
			return null;
		}

		SharedPreferences.Editor editor = prefs.edit();
		long launch_count = prefs.getLong(LAUNCH_COUNT, 0) + 1;
		editor.putLong(LAUNCH_COUNT, launch_count);

		if (launch_count >= launchesUntilPrompt) {
			showRateDialog(mContext, editor);
		}

		editor.commit();
		return this;
	}

	private RateDialog showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(mContext.getString(R.string.dialog_text));
		builder.setTitle(mContext.getResources().getString(R.string.rate));
		builder.setNegativeButton(mContext.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.dismiss();
			}
		});

		builder.setNeutralButton(mContext.getResources().getString(R.string.dontask), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				if (editor != null) {
					editor.putBoolean(DONT_SHOW, true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});

		builder.setPositiveButton(mContext.getResources().getString(R.string.rate), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
						+ EmvApplication.class.getPackage().getName())));
				if (editor != null) {
					editor.putBoolean(DONT_SHOW, true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
		return this;
	}

	public RateDialog setMinLaunches(final int minLaunches) {
		launchesUntilPrompt = minLaunches;
		return this;
	}

}