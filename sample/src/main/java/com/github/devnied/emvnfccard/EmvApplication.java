package com.github.devnied.emvnfccard;

import android.app.Application;
import android.graphics.Typeface;

public class EmvApplication extends Application {

	public static Typeface sTypeface;

	@Override
	public void onCreate() {
		super.onCreate();
		sTypeface = Typeface.createFromAsset(getAssets(), "OCR-A.ttf");
	}

}
