package com.github.devnied.emvnfccard.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.CardAdapter;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.parser.EMVParser;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.provider.Provider;
import com.github.devnied.emvnfccard.utils.CroutonUtils;
import com.github.devnied.emvnfccard.utils.NFCUtils;
import com.github.devnied.emvnfccard.utils.SimpleAsyncTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * First Activity for EMV card reader
 * 
 * @author MILLAU Julien
 * 
 */
public class HomeActivity extends Activity implements OnItemClickListener {

	/**
	 * Nfc utils
	 */
	private NFCUtils mNfcUtils;

	/**
	 * List view
	 */
	private ListView mListView;

	/**
	 * List adapter
	 */
	private BaseAdapter mAdapter;

	/**
	 * List of cards
	 */
	private List<EMVCard> mList = new ArrayList<EMVCard>();

	/**
	 * Waiting Dialog
	 */
	private ProgressDialog mDialog;

	/**
	 * Alert dialog
	 */
	private AlertDialog mAlertDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// init NfcUtils
		mNfcUtils = new NFCUtils(this);

		mListView = (ListView) findViewById(R.id.listCard);
		mAdapter = new CardAdapter(mList, this);
		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(findViewById(R.id.emptyView));
		mListView.setOnItemClickListener(this);
	}

	@Override
	protected void onResume() {
		mNfcUtils.enableDispatch();
		// Close
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.cancel();
		}
		// Check NFC enable
		if (!NFCUtils.isNfcEnable(getApplicationContext())) {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setTitle(getString(R.string.msg_info));
			alertbox.setMessage(getString(R.string.msg_nfc_disable));
			alertbox.setPositiveButton(getString(R.string.msg_activate_nfc), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					Intent intent = null;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						intent = new Intent(Settings.ACTION_NFC_SETTINGS);
					} else {
						intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
					}
					dialog.dismiss();
					startActivity(intent);
				}
			});
			alertbox.setCancelable(false);
			mAlertDialog = alertbox.show();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNfcUtils.disableDispatch();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (mTag != null) {

			new SimpleAsyncTask() {

				/**
				 * Tag comm
				 */
				private IsoDep mTagcomm;

				/**
				 * Emv Card
				 */
				private EMVCard mCard;

				/**
				 * Boolean to indicate exception
				 */
				private boolean mException;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					// Show dialog
					if (mDialog == null) {
						mDialog = ProgressDialog.show(HomeActivity.this, getString(R.string.card_reading),
								getString(R.string.card_reading_desc), true, false);
					} else {
						mDialog.show();
					}
				}

				@Override
				protected void doInBackground() {

					mTagcomm = IsoDep.get(mTag);
					if (mTagcomm == null) {
						CroutonUtils.display(HomeActivity.this, getText(R.string.error_communication_nfc), false);
						return;
					}
					mException = false;

					try {
						// Open connection
						mTagcomm.connect();

						// Create provider
						IProvider prov = new Provider(mTagcomm);

						EMVParser parser = new EMVParser(prov, true);
						mCard = parser.readEmvCard();

					} catch (IOException e) {
						mException = true;
					} finally {
						// close tagcomm
						if (mTagcomm != null) {
							try {
								mTagcomm.close();
							} catch (IOException e) {
								// do nothing
							}
						}
					}
				}

				@Override
				protected void onPostExecute(final Object result) {
					// close dialog
					if (mDialog != null) {
						mDialog.cancel();
					}

					if (!mException) {
						if (mCard != null && StringUtils.isNotBlank(mCard.getCardNumber())) {
							if (!mList.contains(mCard)) {
								mList.add(mCard);
								mAdapter.notifyDataSetChanged();
								CroutonUtils.display(HomeActivity.this, getText(R.string.card_added), true);
							} else {
								CroutonUtils.display(HomeActivity.this, getText(R.string.error_card_already_added), false);
								EMVCard card = mList.get(mList.indexOf(mCard));
								if (card != null && card.getListPayment() != null) {
									card.getListPayment().clear();
									card.getListPayment().addAll(mCard.getListPayment());
								}
							}
						} else {
							CroutonUtils.display(HomeActivity.this, getText(R.string.error_card_unknown), false);
						}
					} else {
						CroutonUtils.display(HomeActivity.this, getResources().getText(R.string.error_communication_nfc), false);
					}
				}

			}.execute();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);

		menu.findItem(R.id.menu_about).setIntent(new Intent(this, AboutActivity.class));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

		Intent it = new Intent(this, ListEventActivity.class);
		it.putExtra(ListEventActivity.EXTRA_RECORD, mList.get(position));
		startActivity(it);
	}
}