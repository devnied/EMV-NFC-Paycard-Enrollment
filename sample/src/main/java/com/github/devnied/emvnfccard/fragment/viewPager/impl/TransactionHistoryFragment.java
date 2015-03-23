package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.TransactionsAdapter;
import com.github.devnied.emvnfccard.data.ItemWrapper;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;
import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;

/**
 * View pager fragment used to display transaction history
 *
 * @author Millau Julien
 *
 */
public class TransactionHistoryFragment extends AbstractFragment implements OnItemClickListener {

	/**
	 * Transaction list
	 */
	private List<ItemWrapper<EmvTransactionRecord>> mTransactionList = new ArrayList<ItemWrapper<EmvTransactionRecord>>();

	/**
	 * List adapter
	 */
	private BaseAdapter mAdapter;

	/**
	 * Expandable list view
	 */
	private ListView mListView;

	/**
	 * Method used to create a new instance of the fragment
	 *
	 * @param pTransactionList
	 *            transactions list
	 * @param pTitle
	 *            fragment title
	 * @return fragment
	 */
	public static IFragment newInstance(final List<EmvTransactionRecord> pTransactionList, final String pTitle) {
		TransactionHistoryFragment ret = new TransactionHistoryFragment();
		ret.setEnable(pTransactionList != null && !pTransactionList.isEmpty());
		ret.setTitle(pTitle);
		if (pTransactionList != null && !pTransactionList.isEmpty()) {
			ret.update(pTransactionList);
		}
		return ret;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.transaction_list_history, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		mListView = (ListView) view.findViewById(R.id.list_event);
		mListView.setEmptyView(view.findViewById(R.id.emptyHisto));
		mAdapter = new TransactionsAdapter(mTransactionList);
		mListView.setOnItemClickListener(this);
		mListView.setAdapter(mAdapter);
	}

	/**
	 * Method used to update transaction history
	 *
	 * @param pBuff
	 */
	public void update(final List<EmvTransactionRecord> pTransactionRecords) {
		mTransactionList.clear();
		if (pTransactionRecords != null && !pTransactionRecords.isEmpty()) {
			for (EmvTransactionRecord transaction : pTransactionRecords) {
				mTransactionList.add(new ItemWrapper<EmvTransactionRecord>(transaction));
			}
		}
		setEnable(pTransactionRecords != null);
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mTransactionList.get(position).setClicked(!mTransactionList.get(position).isClicked());
		mAdapter.notifyDataSetChanged();
	}

}
