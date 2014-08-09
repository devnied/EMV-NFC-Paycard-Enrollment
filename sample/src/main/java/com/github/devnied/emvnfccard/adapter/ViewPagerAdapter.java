package com.github.devnied.emvnfccard.adapter;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;

/**
 * View Pager Adapter
 * 
 * @author julien
 * 
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

	/**
	 * List fragments
	 */
	private final List<IFragment> mFragments;

	/**
	 * 
	 * @param fm
	 * @param pFragments
	 */
	public ViewPagerAdapter(final FragmentManager fm, final List<IFragment> pFragments) {
		super(fm);
		mFragments = pFragments;
	}

	@Override
	public Fragment getItem(final int position) {
		int val = 0;
		for (IFragment f : mFragments) {
			if (f.isEnable() && val++ == position) {
				return (Fragment) f;
			}
		}
		return null;
	}

	@Override
	public int getCount() {
		int ret = 0;
		for (IFragment f : mFragments) {
			if (f.isEnable()) {
				ret++;
			}
		}
		return ret;
	}

	@Override
	public CharSequence getPageTitle(final int position) {
		String ret = null;
		IFragment f = (IFragment) getItem(position);
		if (f != null) {
			ret = f.getTitle();
		}
		return ret;
	}
}