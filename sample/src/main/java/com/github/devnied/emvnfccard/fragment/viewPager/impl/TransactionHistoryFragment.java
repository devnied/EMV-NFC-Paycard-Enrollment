package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.TransactionsAdapter;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;

/**
 * View pager fragment used to display transaction history
 * 
 * @author Millau Julien
 * 
 */
public class TransactionHistoryFragment extends AbstractFragment implements OnChildClickListener {

	/**
	 * Transaction list
	 */
	private List<EmvTransactionRecord> mTransactionList = new ArrayList<EmvTransactionRecord>();

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
	public TransactionHistoryFragment(final List<EmvTransactionRecord> pTransactionList, final String pTitle,
			final boolean pEnable) {
		super(pTitle, pEnable);
		if (pTransactionList != null && !pTransactionList.isEmpty()) {
			mTransactionList.addAll(pTransactionList);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.transaction_list_history, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		mExpandableListView = (ExpandableListView) view.findViewById(R.id.list_event);
		mAdapter = new TransactionsAdapter(mTransactionList);
		mExpandableListView.setOnChildClickListener(this);
		mExpandableListView.setAdapter(mAdapter);
	}

	/**
	 * Method used to update transaction history
	 * 
	 * @param pBuff
	 */
	public void update(final List<EmvTransactionRecord> pTransactionRecords) {
		mTransactionList.clear();
		if (pTransactionRecords != null && !pTransactionRecords.isEmpty()) {
			mTransactionList.addAll(pTransactionRecords);
		}
		setEnable(mTransactionList.size() > 0);
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onChildClick(final ExpandableListView parent, final View v, final int groupPosition, final int childPosition,
			final long id) {
		parent.collapseGroup(groupPosition);
		return true;
	}

}
