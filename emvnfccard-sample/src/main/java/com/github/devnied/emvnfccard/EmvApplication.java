package com.github.devnied.emvnfccard;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Application class
 * 
 * @author MILLAU Julien
 * 
 */
public class EmvApplication extends Application {

	/**
	 * Custom typeface
	 */
	public static Typeface sTypeface;

	@Override
	public void onCreate() {
		super.onCreate();
		sTypeface = Typeface.createFromAsset(getAssets(), "OCR-A.ttf");
	}

}
