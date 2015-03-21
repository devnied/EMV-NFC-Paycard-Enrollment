package com.github.devnied.emvnfccard.adapter;

import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.data.ItemWrapper;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.utils.ViewHolder;
import com.github.devnied.emvnfccard.utils.ViewUtils;
import com.joanzapata.android.iconify.Iconify;

/**
 * Transaction list adapter
 *
 * @author Millau Julien
 *
 */
public class TransactionsAdapter extends BaseAdapter {

	/**
	 * Transaction records
	 */
	private List<ItemWrapper<EmvTransactionRecord>> mTransactionRecord;

	/**
	 * Constructor using fields
	 *
	 * @param pTransactions
	 */
	public TransactionsAdapter(final List<ItemWrapper<EmvTransactionRecord>> pTransactions) {
		mTransactionRecord = pTransactions;
	}

	@Override
	public int getCount() {
		return mTransactionRecord != null ? mTransactionRecord.size() : 0;
	}

	@Override
	public Object getItem(final int position) {
		return mTransactionRecord.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.transaction_item, null);
		}

		// Get Transaction
		ItemWrapper<EmvTransactionRecord> item = (ItemWrapper<EmvTransactionRecord>) getItem(position);
		EmvTransactionRecord record = item.getData();

		// Add indicator icon
		TextView indicator = ViewHolder.get(v, R.id.transaction_indicator);
		View detail = ViewHolder.get(v, R.id.transaction_detail);

		if (item.isClicked()) {
			indicator.setText("{fa-angle-up}");
			detail.setVisibility(View.VISIBLE);
		} else {
			indicator.setText("{fa-angle-down}");
			detail.setVisibility(View.GONE);
		}
		Iconify.addIcons(indicator);

		// Get views
		TextView date = ViewHolder.get(v, R.id.transaction_date);
		TextView price = ViewHolder.get(v, R.id.transaction_price);
		TextView symbol = ViewHolder.get(v, R.id.transaction_symbol);

		// Set date
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		date.setText(format.format(record.getDate()));

		// set price amount
		String amount = null;
		if (record.getCurrency() != null) {
			amount = record.getCurrency().format(record.getAmount().longValue());
		} else {
			amount = String.valueOf(record.getAmount());
		}
		price.setText(amount);

		// Set Symbol
		if (record.getCurrency() != null) {
			Currency currency = Currency.getInstance(record.getCurrency().getISOCodeAlpha());
			if (currency != null) {
				symbol.setText(currency.getSymbol(Locale.getDefault()));
			}
		}

		// Extended data

		// Get views
		TextView country = ViewHolder.get(v, R.id.transaction_county);
		TextView cryptogram = ViewHolder.get(v, R.id.transaction_cryptogram);
		TextView type = ViewHolder.get(v, R.id.transaction_type);

		// Update content
		if (record.getTerminalCountry() != null) {
			ViewHolder.get(v, R.id.row_transaction_country).setVisibility(View.VISIBLE);
			country.setText(record.getTerminalCountry().getName());
		} else {
			ViewHolder.get(v, R.id.row_transaction_country).setVisibility(View.GONE);
		}

		// Transaction cryptogram
		if (StringUtils.isNotBlank(record.getCyptogramData())) {
			ViewHolder.get(v, R.id.row_transaction_crypto).setVisibility(View.VISIBLE);
			cryptogram.setText(record.getCyptogramData());
		} else {
			ViewHolder.get(v, R.id.row_transaction_crypto).setVisibility(View.GONE);
		}

		// transaction type
		if (record.getTransactionType() != null) {
			ViewHolder.get(v, R.id.row_transaction_type).setVisibility(View.VISIBLE);
			type.setText(ViewUtils.getStringRessourceByName(parent.getContext(), "transaction_type_" + record.getTransactionType().getKey()));
		} else {
			ViewHolder.get(v, R.id.row_transaction_type).setVisibility(View.GONE);
		}

		return v;
	}
}
