package com.github.devnied.emvnfccard.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.widget.TextView;

/**
 * Utils class used to manager view
 * 
 * @author Millau Julien
 * 
 */
public final class ViewUtils {

	/**
	 * Apply typeFace to all view
	 * 
	 * @param pTypeFace
	 *            the type face to apply
	 * @param pViews
	 *            all view to apply typeface
	 */
	public static void setTypeFace(final Typeface pTypeFace, final TextView... pViews) {
		for (TextView view : pViews) {
			view.setTypeface(pTypeFace);
			view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
		}
	}

	/**
	 * This method converts dp unit to equivalent pixels, depending on device density.
	 * 
	 * @param dp
	 *            A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static float convertDpToPixel(final float dp, final Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	/**
	 * This method is used to get a String ressources by name
	 * 
	 * @param pContext
	 *            application context
	 * @param pName
	 *            resource name
	 * @return
	 */
	public static int getStringRessourceByName(final Context pContext, final String pName) {
		return pContext.getResources().getIdentifier(pName, "string", pContext.getPackageName());
	}

	/**
	 * Private constructor
	 */
	private ViewUtils() {
	}

}
