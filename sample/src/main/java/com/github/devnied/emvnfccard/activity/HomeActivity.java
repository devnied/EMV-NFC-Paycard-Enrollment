package com.github.devnied.emvnfccard.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.github.devnied.emvnfccard.BuildConfig;
import com.github.devnied.emvnfccard.EMVParser;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.CardAdapter;
import com.github.devnied.emvnfccard.enums.EMVCardTypeEnum;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.provider.Provider;
import com.github.devnied.emvnfccard.utils.NFCUtils;
import com.github.devnied.emvnfccard.utils.SimpleAsyncTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

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
	public void onBackPressed() {
		if (BuildConfig.DEBUG) {
			EMVCard card = new EMVCard();
			card.setCardNumber("1234567891880782");
			card.setExpireDate(new Date());
			card.setType(EMVCardTypeEnum.VISA);

			List<EMVPaymentRecord> list = new ArrayList<EMVPaymentRecord>();
			EMVPaymentRecord payment = new EMVPaymentRecord();
			payment.setAmount(new Float(234.698));
			payment.setCurrency(CurrencyEnum.EUR);
			payment.setTransactionDate(new Date());
			list.add(payment);
			payment = new EMVPaymentRecord();
			payment.setAmount(new Float(2398));
			payment.setCurrency(CurrencyEnum.USD);
			payment.setTransactionDate(new Date());

			list.add(payment);
			card.setListPayment(list);
			mList.add(card);
			mAdapter.notifyDataSetChanged();
			display("Card added", true);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
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
			alertbox.show();
		}

		mNfcUtils.enableDispatch();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNfcUtils.disableDispatch();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		new SimpleAsyncTask() {

			/**
			 * Tag
			 */
			private Tag mTag;
			/**
			 * Tag comm
			 */
			private IsoDep mTagcomm;

			/**
			 * Emv Card
			 */
			private EMVCard mCard;

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
				mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				mTagcomm = IsoDep.get(mTag);
				if (mTagcomm == null) {
					display(getText(R.string.error_communication_nfc), false);
					return;
				}

				try {
					// Open connection
					mTagcomm.connect();

					// Create provider
					IProvider prov = new Provider(mTagcomm);

					EMVParser parser = new EMVParser(prov, true);
					mCard = parser.readEmvCard();

				} catch (IOException e) {
					display(getResources().getText(R.string.error_communication_nfc), false);
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
				if (mCard == null) {
					display(getText(R.string.error_card_unknown), false);
				} else {
					if (!mList.contains(mCard)) {
						mList.add(mCard);
						mAdapter.notifyDataSetChanged();
						display(getText(R.string.card_added), true);
					} else {
						display(getText(R.string.error_card_already_added), false);
					}
				}
			}

		}.execute();

	}

	protected void display(final CharSequence msg, final boolean success) {

		int color = 0xFF656464;
		if (success) {
			color = 0xFF78B653;
		}
		Crouton.cancelAllCroutons();
		Style style = new Style.Builder().setBackgroundColorValue(color) //
				.setGravity(Gravity.CENTER) //
				.setTextAppearance(R.style.Crouton_TextApparence) //
				.build();
		Crouton.showText(this, msg, style);
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