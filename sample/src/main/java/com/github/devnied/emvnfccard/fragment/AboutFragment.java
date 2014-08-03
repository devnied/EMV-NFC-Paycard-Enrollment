package com.github.devnied.emvnfccard.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.utils.CroutonUtils;

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
				CroutonUtils.display(getActivity(), getText(R.string.error_link), false);
			}
			return false;
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about, container, false);

		TextView content = (TextView) view.findViewById(R.id.about_content);
		if (content != null) {
			content.setMovementMethod(new MovementCheck());
		}

		return view;
	}

}
