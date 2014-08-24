package com.github.devnied.emvnfccard.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;

/**
 * View Pager Adapter
 * 
 * @author julien
 * 
 */
public class ViewPagerAdapter extends SortablePagerAdapter {

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
		if (mFragments != null) {
			for (IFragment f : mFragments) {
				if (f.isEnable()) {
					ret++;
				}
			}
		}
		return ret;
	}

	public int getRealPagerPosition(final int pPosition) {
		int ret = 0;
		int activePosition = 0;
		for (IFragment f : mFragments) {
			if (f.isEnable() && activePosition++ == pPosition) {
				break;
			}
			ret++;
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

	@Override
	public int getItemPosition(final Object object) {
		int position = 0;

		for (IFragment f : mFragments) {
			if (f == object) {
				if (!f.isEnable()) {
					position = POSITION_NONE;
				}
				break;
			} else if (f.isEnable()) {
				position++;
			}
		}
		return position;
	}

	@Override
	public long getItemId(final int position) {
		return mFragments.indexOf(getItem(position));
	}

}