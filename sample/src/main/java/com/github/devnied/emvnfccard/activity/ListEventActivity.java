package com.github.devnied.emvnfccard.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.EventAdapter;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;

/**
 * List event activity
 * 
 * @author MILLAU Julien
 * 
 */
public class ListEventActivity extends Activity {

	/**
	 * Extra recors constants
	 */
	public static final String EXTRA_RECORD = "com.github.devnied.emvnfccard.activity.ListEventActivity.extra";

	/**
	 * EmvCard
	 */
	private EMVCard mCard;

	/**
	 * List of payment records
	 */
	private List<EMVPaymentRecord> mPaymentrecord = new ArrayList<EMVPaymentRecord>();

	/**
	 * Adapter
	 */
	private EventAdapter mAdapter;
	/**
	 * List view
	 */
	private ListView mListView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_event);
		// Get list view
		mListView = (ListView) findViewById(R.id.list_event);
		mListView.setEmptyView(findViewById(R.id.emptyView_event));
		// Extract Extra
		mCard = (EMVCard) getIntent().getSerializableExtra(EXTRA_RECORD);
		if (mCard != null && mCard.getListPayment() != null) {
			mPaymentrecord.addAll(mCard.getListPayment());
		}
		// Add adapter
		mAdapter = new EventAdapter(mPaymentrecord, this);
		mListView.setAdapter(mAdapter);

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}