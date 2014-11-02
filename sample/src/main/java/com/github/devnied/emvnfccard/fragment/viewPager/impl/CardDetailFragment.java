package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.github.devnied.emvnfccard.EmvApplication;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;
import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.utils.CardUtils;
import com.github.devnied.emvnfccard.utils.ViewUtils;

/**
 * View pager fragment used to display card detail
 *
 * @author Millau Julien
 *
 */
public class CardDetailFragment extends AbstractFragment {

	/**
	 * Card to display
	 */
	private EmvCard mCard;

	/**
	 * Empty view
	 */
	private LinearLayout mEmptyView;

	/**
	 * Card view
	 */
	private ScrollView mScrollView;

	/**
	 * Card number
	 */
	private TextView mCardNumber;

	/**
	 * Card validity
	 */
	private TextView mCardValidity;

	/**
	 * Image view
	 */
	private ImageView mImageView;

	/**
	 * Extended layout
	 */
	private TableLayout mExtendedLayout;

	/**
	 * Linear layout (banner)
	 */
	private LinearLayout mBanner;

	/**
	 * Method used to create a new instance of the fragment
	 *
	 * @param pCard
	 *            EmvCard
	 * @param pTitle
	 *            fragment title
	 * @return fragment
	 */
	public static IFragment newInstance(final EmvCard pCard, final String pTitle) {
		CardDetailFragment ret = new CardDetailFragment();
		ret.setEnable(true);
		ret.setTitle(pTitle);
		ret.setCard(pCard);
		return ret;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.card_detail, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		// Get views
		mEmptyView = (LinearLayout) view.findViewById(R.id.emptyView);
		mScrollView = (ScrollView) view.findViewById(R.id.card_detail);
		mCardNumber = (TextView) view.findViewById(R.id.cardNumber);
		mCardValidity = (TextView) view.findViewById(R.id.cardValidity);
		mImageView = (ImageView) view.findViewById(R.id.type);
		mExtendedLayout = (TableLayout) view.findViewById(R.id.extended_content);
		mBanner = (LinearLayout) getActivity().findViewById(R.id.banner);
		// Set OCR-A typeface
		ViewUtils.setTypeFace(EmvApplication.sTypeface, mCardNumber, mCardValidity);
		// Update content
		updateContent();
	}

	/**
	 * Method used to update card detail content
	 */
	private void updateContent() {
		if (getActivity() != null) {
			if (mCard != null) {
				mBanner.setVisibility(View.VISIBLE);
				mEmptyView.setVisibility(View.GONE);
				mScrollView.setVisibility(View.VISIBLE);
				// Update content
				mCardNumber.setText(CardUtils.formatCardNumber(mCard.getCardNumber(), mCard.getType()));
				if (mCard.getExpireDate() != null) {
					SimpleDateFormat format = new SimpleDateFormat("MM/yy", Locale.getDefault());
					mCardValidity.setText(format.format(mCard.getExpireDate()));
				}
				mImageView.setImageResource(CardUtils.getResourceIdCardType(mCard.getType()));

				// Extended card data

				// Remove all existing view
				mExtendedLayout.removeAllViews();

				// Card holder name
				if (StringUtils.isNotBlank(mCard.getHolderFirstname()) && StringUtils.isNotBlank(mCard.getHolderLastname())) {
					createRaw(getString(R.string.extended_card_holder_name),
							StringUtils.join(mCard.getHolderFirstname(), " ", mCard.getHolderLastname()));
				}

				// card AID
				if (StringUtils.isNotEmpty(mCard.getAid())) {
					createRaw(getString(R.string.extended_title_AID), CardUtils.formatAid(mCard.getAid()));
				}

				// Card Application label
				if (StringUtils.isNotEmpty(mCard.getApplicationLabel())) {
					createRaw(getString(R.string.extended_title_application_label), mCard.getApplicationLabel());
				}

				// Card type
				if (mCard.getType() != null) {
					createRaw(getString(R.string.extended_title_card_type), mCard.getType().getName());
				}

				// Pin try left
				createRaw(getString(R.string.extended_title_pin_try), mCard.getLeftPinTry() + " "
						+ getString(R.string.extended_title_times));

				// Atr desc
				if (mCard.getAtrDescription() != null && !mCard.getAtrDescription().isEmpty()) {
					createRaw(getString(R.string.extended_title_possible_bank), StringUtils.join(mCard.getAtrDescription(), "\n"));
				}

			} else {
				mBanner.setVisibility(View.GONE);
				mEmptyView.setVisibility(View.VISIBLE);
				mScrollView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Method used to create a row in the section "Extended card detail"
	 *
	 * @param pKeyName
	 *            key title
	 * @param pValue
	 *            key value
	 */
	private void createRaw(final String pKeyName, final String pValue) {
		View v = View.inflate(getActivity(), R.layout.tablelayout_raw, null);
		TextView title = (TextView) v.findViewById(R.id.extended_raw_title);
		title.setText(pKeyName);
		TextView content = (TextView) v.findViewById(R.id.extended_raw_content);
		content.setText(pValue);
		mExtendedLayout.addView(v);
	}

	public void update(final EmvCard pCard) {
		mCard = pCard;
		updateContent();
	}

	/**
	 * Setter for the field mCard
	 *
	 * @param mCard
	 *            the mCard to set
	 */
	public void setCard(final EmvCard mCard) {
		this.mCard = mCard;
	}

}
