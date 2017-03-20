package com.github.devnied.emvnfccard.fragment;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.utils.CroutonUtils;
import com.github.devnied.emvnfccard.utils.CroutonUtils.CoutonColor;

/**
 * About Fragment
 *
 * @author Millau Julien
 *
 */
public class AboutFragment extends Fragment {

	/**
	 * Custom LinkMovementMethod for catching impossible action
	 *
	 * @author MILLAU Julien
	 *
	 */
	private class MovementCheck extends LinkMovementMethod {

		@Override
		public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {
			try {
				return super.onTouchEvent(widget, buffer, event);
			} catch (Exception e) {
				CroutonUtils.display(getActivity(), getText(R.string.error_link), CoutonColor.BLACK);
			}
			return false;
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.about, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {

		TextView githubContent = (TextView) view.findViewById(R.id.about_github_content);
		if (githubContent != null) {
			githubContent.setMovementMethod(new MovementCheck());
		}

		// Add version
		TextView aboutversion = (TextView) view.findViewById(R.id.about_version);
		try {
			aboutversion.setText(getText(R.string.activity_name) + " v"
					+ getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			Log.e(AboutFragment.class.getName(), "Application version not found");
		}

		// Add inApp fragment
		View billingView = view.findViewById(R.id.about_inapp_content);
		if (billingView != null) {
			billingView.setVisibility(View.VISIBLE);
			getChildFragmentManager().beginTransaction().replace(R.id.about_inapp_content, new BillingFragment()).commit();
		}
	}
}
