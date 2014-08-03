package com.github.devnied.emvnfccard.adapter;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

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
	private final List<Fragment> mFragments;

	/**
	 * 
	 * @param fm
	 * @param pFragments
	 */
	public ViewPagerAdapter(final FragmentManager fm, final List<Fragment> pFragments) {
		super(fm);
		mFragments = pFragments;
	}

	@Override
	public Fragment getItem(final int position) {
		return mFragments.get(position);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}

}