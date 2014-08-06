package com.github.devnied.emvnfccard.fragment.viewPager;

import android.app.Fragment;

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

	public AbstractFragment(final String pTitle, final boolean pEnable) {
		mTitle = pTitle;
		mEnable = pEnable;
	}

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
