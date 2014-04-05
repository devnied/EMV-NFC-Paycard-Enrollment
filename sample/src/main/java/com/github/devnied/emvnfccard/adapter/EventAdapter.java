package com.github.devnied.emvnfccard.adapter;

import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.devnied.emvnfccard.EmvApplication;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.model.EMVPaymentRecord;
import com.github.devnied.emvnfccard.utils.ViewHolder;
import com.github.devnied.emvnfccard.utils.ViewUtils;

public class EventAdapter extends BaseAdapter {

	private List<EMVPaymentRecord> mListEvent;

	private Context mContext;

	public EventAdapter(final List<EMVPaymentRecord> pListEvent, final Context pContext) {
		mListEvent = pListEvent;
		mContext = pContext;
	}

	@Override
	public int getCount() {
		return mListEvent.size();
	}

	@Override
	public Object getItem(final int position) {
		return mListEvent.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return mListEvent.get(position).hashCode();
	}

	@Override
	public View getView(final int position, final View pConvertView, final ViewGroup parent) {

		View v = pConvertView;
		if (pConvertView == null) {
			v = View.inflate(mContext, R.layout.event_item, null);
		}
		// get view from holder
		TextView date = ViewHolder.get(v, R.id.event_date);
		TextView amount = ViewHolder.get(v, R.id.event_price);
		TextView symbol = ViewHolder.get(v, R.id.event_symbol);

		// get element
		EMVPaymentRecord event = (EMVPaymentRecord) getItem(position);

		// format date
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		date.setText(format.format(event.getTransactionDate()));

		// Add currency
		Currency currency = Currency.getInstance(event.getCurrency().getCode());
		symbol.setText(currency.getSymbol(Locale.getDefault()));

		// Set amount
		amount.setText(event.getCurrency().format(event.getAmount().longValue()));

		// Apply faceType
		ViewUtils.setTypeFace(EmvApplication.sTypeface, date, amount, symbol);

		return v;
	}
}
