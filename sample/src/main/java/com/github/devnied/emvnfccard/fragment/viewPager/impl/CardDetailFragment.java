package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;
import com.github.devnied.emvnfccard.model.EMVCard;

public class CardDetailFragment extends AbstractFragment {

	/**
	 * Card to display
	 */
	private EMVCard mCard;

	/**
	 * Empty view
	 */
	private LinearLayout mEmptyView;

	/**
	 * Card view
	 */
	private ScrollView mScrollView;

	/**
	 * Constructor using fields
	 * 
	 * @param pTitle
	 *            fragment title
	 * @param pEnable
	 *            fragment visibility
	 */
	public CardDetailFragment(final EMVCard pCard, final String pTitle) {
		super(pTitle, true);
		mCard = pCard;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.card_detail, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		mEmptyView = (LinearLayout) view.findViewById(R.id.emptyView);
		mScrollView = (ScrollView) view.findViewById(R.id.card_detail);
		updateContent();
	}

	private void updateContent() {
		if (mCard != null) {
			mEmptyView.setVisibility(View.GONE);
			mScrollView.setVisibility(View.VISIBLE);
		}
	}

	public void update(final EMVCard pCard) {
		mCard = pCard;
		updateContent();
	}

}
