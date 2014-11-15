package com.github.devnied.emvnfccard.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.preference.PreferenceFragment;
import android.view.View;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.activity.HomeActivity;
import com.github.devnied.emvnfccard.utils.CroutonUtils;
import com.github.devnied.emvnfccard.utils.CroutonUtils.CoutonColor;

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

		// click on clear button
		Preference clear = findPreference("clear_card");
		clear.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(final Preference preference) {
				HomeActivity activity = (HomeActivity) getActivity();
				if (activity != null) {
					activity.clear();
					CroutonUtils.display(activity, getString(R.string.card_deleted), CoutonColor.GREEN);
					activity.backToHomeScreen();
				}
				return true;
			}
		});

	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		view.setBackgroundColor(getResources().getColor(android.R.color.white));
		super.onViewCreated(view, savedInstanceState);
	}
}
