package com.github.devnied.emvnfccard.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.devnied.emvnfccard.BuildConfig;

public abstract class SortablePagerAdapter extends PagerAdapter {
	private static final String TAG = "SortablePagerAdapter";

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;

	public SortablePagerAdapter(final FragmentManager fm) {
		mFragmentManager = fm;
	}

	/**
	 * Return the Fragment associated with a specified position.
	 */
	public abstract Fragment getItem(int position);

	/**
	 * Return a unique identifier for the item at the given position.
	 */
	public abstract long getItemId(int position);

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		Fragment fragment = getItem(position);
		if (BuildConfig.DEBUG) {
			Log.v(TAG, "Adding item #" + position + ": f=" + fragment);
		}
		mCurTransaction.add(container.getId(), fragment);

		return fragment;
	}

	@Override
	public void destroyItem(final ViewGroup container, final int position, final Object object) {
		Fragment fragment = (Fragment) object;

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		if (BuildConfig.DEBUG) {
			Log.v(TAG, "Removing item #" + position + ": f=" + object + " v=" + ((Fragment) object).getView());
		}
		mCurTransaction.remove(fragment);
	}

	@Override
	public void finishUpdate(final ViewGroup container) {
		if (mCurTransaction != null) {
			mCurTransaction.commitAllowingStateLoss();
			mCurTransaction = null;
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return ((Fragment) object).getView() == view;
	}

}