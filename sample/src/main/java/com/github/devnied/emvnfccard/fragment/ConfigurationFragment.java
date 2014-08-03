package com.github.devnied.emvnfccard.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.github.devnied.emvnfccard.R;

/**
 * Configuration fragment
 * 
 * @author Millau Julien
 * 
 */
public class ConfigurationFragment extends PreferenceFragment {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

}
