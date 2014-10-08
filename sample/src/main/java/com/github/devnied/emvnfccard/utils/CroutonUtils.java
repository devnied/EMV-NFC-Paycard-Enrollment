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

		int color = pActivity.getResources().getColor(R.color.black_error);
		if (success) {
			color = pActivity.getResources().getColor(R.color.green_success);
		}
		// Remove all previous crouton
		Crouton.cancelAllCroutons();
		// Build style
		Style style = new Style.Builder().setBackgroundColorValue(color) //
				.setPaddingDimensionResId(R.dimen.crouton_padding) //
				.setGravity(Gravity.CENTER) //
				.setTextAppearance(R.style.Crouton_TextApparence) //
				.build();

		Crouton.showText(pActivity, msg, style, R.id.crouton);
	}

	/**
	 * Private constructor
	 */
	private CroutonUtils() {
	}
}
