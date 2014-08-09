package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.TransactionsAdapter;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;
import com.github.devnied.emvnfccard.model.EMVTransactionRecord;

/**
 * View pager fragment used to display transaction history
 * 
 * @author Millau Julien
 * 
 */
public class TransactionHistoryFragment extends AbstractFragment {

	/**
	 * Transaction list
	 */
	private List<EMVTransactionRecord> mTransactionList = new ArrayList<EMVTransactionRecord>();

	/**
	 * List adapter
	 */
	private BaseExpandableListAdapter mAdapter;

	/**
	 * Expandable list view
	 */
	private ExpandableListView mExpandableListView;

	/**
	 * Constructor using field
	 * 
	 * @param pTransactionList
	 *            transaction list
	 * @param pTitle
	 *            fragment title
	 * @param pEnable
	 *            boolean to enable or disable fragment
	 */
	public TransactionHistoryFragment(final List<EMVTransactionRecord> pTransactionList, final String pTitle,
			final boolean pEnable) {
		super(pTitle, pEnable);
		if (pTransactionList != null && !pTransactionList.isEmpty()) {
			mTransactionList.addAll(pTransactionList);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_transaction_history, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		mExpandableListView = (ExpandableListView) view.findViewById(R.id.list_event);
		mAdapter = new TransactionsAdapter(mTransactionList);
		mExpandableListView.setEmptyView(view.findViewById(R.id.emptyView_event));
		mExpandableListView.setAdapter(mAdapter);
	}

	/**
	 * Method used to update transaction history
	 * 
	 * @param pBuff
	 */
	public void update(final List<EMVTransactionRecord> pTransactionRecords) {
		mTransactionList.clear();
		if (pTransactionRecords != null && !pTransactionRecords.isEmpty()) {
			mTransactionList.addAll(pTransactionRecords);
		}
		setEnable(mTransactionList.size() > 0);
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

}
