package com.github.devnied.emvnfccard.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.ViewPagerAdapter;
import com.github.devnied.emvnfccard.fragment.viewPager.CardDetailFragment;

/**
 * View pager
 * 
 * @author Millau Julien
 * 
 */
public class ViewPagerFragment extends Fragment {

	/**
	 * View pager
	 */
	private ViewPager mViewPager;

	/**
	 * Fragment list
	 */
	private List<Fragment> fragments = new ArrayList<Fragment>();

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.viewpager, container, false);
		fragments.add(new CardDetailFragment());
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		ViewPagerAdapter viewPager = new ViewPagerAdapter(getFragmentManager(), fragments);
		mViewPager.setAdapter(viewPager);

		return view;
	}
}
