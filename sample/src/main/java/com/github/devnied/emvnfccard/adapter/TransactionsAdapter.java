package com.github.devnied.emvnfccard.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.github.devnied.emvnfccard.model.EMVTransactionRecord;

public class TransactionsAdapter extends BaseExpandableListAdapter {

	/**
	 * Transaction records
	 */
	private List<EMVTransactionRecord> mTransactionRecord;

	/**
	 * Constructor using fields
	 * 
	 * @param pTransactions
	 */
	public TransactionsAdapter(final List<EMVTransactionRecord> pTransactions) {
		mTransactionRecord = pTransactions;
	}

	@Override
	public int getGroupCount() {
		return mTransactionRecord.size();
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		return mTransactionRecord.size();
	}

	@Override
	public Object getGroup(final int groupPosition) {
		return mTransactionRecord.get(groupPosition);
	}

	@Override
	public Object getChild(final int groupPosition, final int childPosition) {
		return mTransactionRecord.get(groupPosition);
	}

	@Override
	public long getGroupId(final int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(final int groupPosition, final int childPosition) {
		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, final View convertView,
			final ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChildSelectable(final int groupPosition, final int childPosition) {
		return true;
	}

}
