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
	 * Enum for crouton color
	 *
	 * @author MILLAU Julien
	 *
	 */
	public static enum CoutonColor {
		BLACK, ORANGE, GREEN;
	}

	/**
	 * Method used to display message in an activity
	 *
	 * @param pActivity
	 *            activity
	 * @param msg
	 * @param success
	 */
	public static void display(final Activity pActivity, final CharSequence msg, final CoutonColor coutonColor) {

		int color = 0;
		switch (coutonColor) {
		case GREEN:
			color = pActivity.getResources().getColor(R.color.green_success);
			break;
		case ORANGE:
			color = pActivity.getResources().getColor(R.color.orange);
			break;
		case BLACK:
		default:
			color = pActivity.getResources().getColor(R.color.black_error);
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
