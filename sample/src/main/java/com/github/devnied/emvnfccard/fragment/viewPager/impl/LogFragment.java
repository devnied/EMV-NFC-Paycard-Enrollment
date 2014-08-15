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
import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;

/**
 * View pager fragment used to display card log
 * 
 * @author Millau Julien
 * 
 */
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
	 * Method used to create an instance of the fragment
	 * 
	 * @param pBuf
	 *            logs
	 * @param pTitle
	 *            Fragment title
	 * @return fragment
	 */
	public static IFragment newInstance(final StringBuffer pBuf, final String pTitle) {
		LogFragment ret = new LogFragment();
		ret.setEnable(pBuf != null && pBuf.length() != 0);
		ret.setTitle(pTitle);
		ret.setBuffer(pBuf);
		return ret;
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

	/**
	 * Setter for the field mBuffer
	 * 
	 * @param mBuffer
	 *            the mBuffer to set
	 */
	public void setBuffer(final StringBuffer mBuffer) {
		this.mBuffer = mBuffer;
	}

}
