package com.github.devnied.emvnfccard.utils;

import android.app.Activity;
import android.view.Gravity;

import com.github.devnied.emvnfccard.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Utils class used to manipulate crouton
 * 
 * @author MILLAU Julien
 * 
 */
public final class CroutonUtils {

	/**
	 * Method used to display message in an activity
	 * 
	 * @param pActivity
	 *            activity
	 * @param msg
	 * @param success
	 */
	public static void display(final Activity pActivity, final CharSequence msg, final boolean success) {

		int color = 0xFF656464;
		if (success) {
			color = 0xFF78B653;
		}
		// Remove all previous crouton
		Crouton.cancelAllCroutons();
		// Build style
		Style style = new Style.Builder().setBackgroundColorValue(color) //
				.setGravity(Gravity.CENTER) //
				.setTextAppearance(R.style.Crouton_TextApparence) //
				.setHeight((int) (26 * pActivity.getResources().getDisplayMetrics().density)) //
				.build();

		Crouton.showText(pActivity, msg, style, R.id.couton);
	}

	/**
	 * Private constructor
	 */
	private CroutonUtils() {
	}
}
