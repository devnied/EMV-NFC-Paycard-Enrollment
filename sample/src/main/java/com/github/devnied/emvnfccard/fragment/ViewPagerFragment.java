package com.github.devnied.emvnfccard.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.activity.IContentActivity;
import com.github.devnied.emvnfccard.adapter.ViewPagerAdapter;
import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;
import com.github.devnied.emvnfccard.fragment.viewPager.impl.CardDetailFragment;
import com.github.devnied.emvnfccard.fragment.viewPager.impl.LogFragment;
import com.github.devnied.emvnfccard.fragment.viewPager.impl.TransactionHistoryFragment;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.view.SlidingTabLayout;
import com.joanzapata.android.iconify.Iconify;

/**
 * View pager
 *
 * @author Millau Julien
 *
 */
public class ViewPagerFragment extends Fragment implements IRefreshable {

	/**
	 * Tab layout
	 */
	private SlidingTabLayout mTabLayout;
	/**
	 * View pager
	 */
	private ViewPager mViewPager;

	/**
	 * View pager Adapter
	 */
	private ViewPagerAdapter mViewPagerAdapter;

	/**
	 * Fragment list
	 */
	private List<IFragment> fragments = new ArrayList<IFragment>(3);

	/**
	 * Content activity
	 */
	private IContentActivity mContentActivity;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.viewpager, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		List<EmvTransactionRecord> transactions = null;
		if (mContentActivity.getCard() != null) {
			transactions = getAllTransactions(mContentActivity.getCard());
		}

		// Add fragments
		fragments.add(CardDetailFragment.newInstance(mContentActivity.getCard(), getString(R.string.viewpager_carddetail)));
		fragments.add(TransactionHistoryFragment.newInstance(transactions, getString(R.string.viewpager_transactions)));
		fragments.add(LogFragment.newInstance(mContentActivity.getLog(), getString(R.string.viewpager_log)));
		// View pager
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), fragments);
		mViewPager.setAdapter(mViewPagerAdapter);

		// banner
		View banner = view.findViewById(R.id.banner);
		banner.setOnClickListener((View.OnClickListener) getActivity());
		Iconify.addIcons((TextView) view.findViewById(R.id.bannerArrow));

		// Tab layout
		mTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
		mTabLayout.setViewPager(mViewPager);
		mTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

			@Override
			public int getIndicatorColor(final int position) {
				return Color.parseColor("#b3e5fc");
			}

			@Override
			public int getDividerColor(final int position) {
				return Color.parseColor("#03a9f4");
			}

		});
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mContentActivity = (IContentActivity) activity;
		mContentActivity.setRefreshableContent(this);
	}

	@Override
	public void update() {
		for (IFragment frag : fragments) {
			if (frag instanceof LogFragment) {
				((LogFragment) frag).updateLog(mContentActivity.getLog());
			} else if (frag instanceof CardDetailFragment) {
				((CardDetailFragment) frag).update(mContentActivity.getCard());
			} else if (frag instanceof TransactionHistoryFragment) {
				((TransactionHistoryFragment) frag).update(mContentActivity.getCard() != null ? getAllTransactions(mContentActivity
						.getCard()) : null);
			}
		}
		if (mViewPagerAdapter != null) {
			if (getActivity() != null) {
				mViewPagerAdapter.notifyDataSetChanged();
			}
			mTabLayout.updateTabs();
		}
	}

	private List<EmvTransactionRecord> getAllTransactions(final EmvCard pCard) {
		List<EmvTransactionRecord> ret = null;

		if (pCard != null && pCard.getApplications() != null) {
			for (Application app : pCard.getApplications()) {
				if (app.getListTransactions() != null) {
					if (ret == null) {
						ret = app.getListTransactions();
					} else {
						ret.addAll(app.getListTransactions());
					}
				}
			}
		}

		return ret;
	}

}
