package com.github.devnied.emvnfccard.fragment.viewPager;

import android.support.v4.app.Fragment;

/**
 * Abstract fragment
 * 
 * @author Millau Julien
 * 
 */
public abstract class AbstractFragment extends Fragment implements IFragment {

	/**
	 * Fragment title
	 */
	private String mTitle;

	/**
	 * Enable or not the fragment
	 */
	protected boolean mEnable;

	/**
	 * Setter for the field mTitle
	 * 
	 * @param mTitle
	 *            the mTitle to set
	 */
	public void setTitle(final String mTitle) {
		this.mTitle = mTitle;
	}

	/**
	 * Setter for the field mEnable
	 * 
	 * @param mEnable
	 *            the mEnable to set
	 */
	public void setEnable(final boolean mEnable) {
		this.mEnable = mEnable;
	}

	@Override
	public String getTitle() {
		return mTitle;
	}

	@Override
	public boolean isEnable() {
		return mEnable;
	}

}
