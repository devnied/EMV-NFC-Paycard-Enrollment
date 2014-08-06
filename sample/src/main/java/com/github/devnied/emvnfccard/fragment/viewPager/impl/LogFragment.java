package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;

public class LogFragment extends AbstractFragment {

	/**
	 * TextView
	 */
	private TextView mTextView;

	/**
	 * Scroll view
	 */
	private ScrollView mScrollView;

	/**
	 * String buffer
	 */
	private StringBuffer mBuffer;

	/**
	 * Constructor using fields
	 * 
	 * @param pTitle
	 *            fragment title
	 * @param pEnable
	 *            fragment visibility
	 */
	public LogFragment(final StringBuffer pBuf, final String pTitle) {
		super(pTitle, pBuf != null && pBuf.length() != 0);
		mBuffer = pBuf;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.log, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
		mTextView = (TextView) view.findViewById(R.id.logContent);
		updateLog(mBuffer);
	}

	/**
	 * Method used to update log
	 * 
	 * @param pBuff
	 */
	public void updateLog(final StringBuffer pBuff) {
		if (pBuff != null) {
			mBuffer = pBuff;
			String text = pBuff.toString();
			if (!text.isEmpty()) {
				mEnable = true;
				if (mTextView != null) {
					mTextView.setVisibility(View.VISIBLE);
					mTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
				}
			}
			if (mScrollView != null) {
				mScrollView.post(new Runnable() {
					@Override
					public void run() {
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
			}
		}
	}

}
