package com.github.devnied.emvnfccard.adapter;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.devnied.emvnfccard.EmvApplication;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.model.EMVCard;
import com.github.devnied.emvnfccard.utils.CardUtils;
import com.github.devnied.emvnfccard.utils.ViewHolder;
import com.github.devnied.emvnfccard.utils.ViewUtils;

public class CardAdapter extends BaseAdapter {

	private List<EMVCard> mListCard;

	private Context mContext;

	public CardAdapter(final List<EMVCard> pListCard, final Context pContext) {
		mListCard = pListCard;
		mContext = pContext;
	}

	@Override
	public int getCount() {
		return mListCard.size();
	}

	@Override
	public Object getItem(final int position) {
		return mListCard.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return mListCard.get(position).hashCode();
	}

	@Override
	public View getView(final int position, final View pConvertView, final ViewGroup parent) {

		View v = pConvertView;
		if (pConvertView == null) {
			v = View.inflate(mContext, R.layout.card_item, null);
		}
		// get view from holder
		TextView cardNumber = ViewHolder.get(v, R.id.cardNumber);
		TextView validity = ViewHolder.get(v, R.id.cardValidity);
		TextView validityLabel = ViewHolder.get(v, R.id.cardValidityLabel);
		ImageView cardType = ViewHolder.get(v, R.id.type);

		// get element
		EMVCard card = (EMVCard) getItem(position);

		// Add card number
		cardNumber.setText(CardUtils.formatCardNumber(card.getCardNumber()));

		// format date
		SimpleDateFormat format = new SimpleDateFormat("MM/yy");
		validity.setText(format.format(card.getExpireDate()));

		// Set Image
		cardType.setImageResource(CardUtils.getResourceIdCardType(card.getType()));

		// Apply faceType
		ViewUtils.setTypeFace(EmvApplication.sTypeface, cardNumber, validity, validityLabel);

		return v;
	}
}
