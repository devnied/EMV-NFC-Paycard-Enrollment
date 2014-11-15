package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.activity.HomeActivity;
import com.github.devnied.emvnfccard.fragment.viewPager.AbstractFragment;
import com.github.devnied.emvnfccard.fragment.viewPager.IFragment;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.utils.CroutonUtils;
import com.github.devnied.emvnfccard.utils.CroutonUtils.CoutonColor;
import com.github.devnied.emvnfccard.view.FloatingActionButton;

import fr.devnied.bitlib.BytesUtils;

/**
 * View pager fragment used to display card log
 *
 * @author Millau Julien
 *
 */
public class LogFragment extends AbstractFragment implements OnClickListener {

	/**
	 * TextView
	 */
	private TextView mTextView;

	/**
	 * Scroll view
	 */
	private ScrollView mScrollView;

	/**
	 * String buffer
	 */
	private StringBuffer mBuffer;

	/**
	 * Method used to create an instance of the fragment
	 *
	 * @param pBuf
	 *            logs
	 * @param pTitle
	 *            Fragment title
	 * @return fragment
	 */
	public static IFragment newInstance(final StringBuffer pBuf, final String pTitle) {
		LogFragment ret = new LogFragment();
		ret.setEnable(pBuf != null && pBuf.length() != 0);
		ret.setTitle(pTitle);
		ret.setBuffer(pBuf);
		return ret;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.log, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
		mTextView = (TextView) view.findViewById(R.id.logContent);
		FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.fabutton);
		button.attachTo(mScrollView);
		button.setDrawable(getResources().getDrawable(R.drawable.ic_menu_share));
		button.setOnClickListener(this);
		updateLog(mBuffer);
	}

	/**
	 * Method used to update log
	 *
	 * @param pBuff
	 */
	public void updateLog(final StringBuffer pBuff) {
		if (pBuff != null) {
			mBuffer = pBuff;
			String text = pBuff.toString();
			if (!text.isEmpty()) {
				mEnable = true;
				if (mTextView != null) {
					mTextView.setVisibility(View.VISIBLE);
					mTextView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
				}
			}
			if (mScrollView != null) {
				mScrollView.post(new Runnable() {
					@Override
					public void run() {
						mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
					}
				});
			}
		}
	}

	/**
	 * Setter for the field mBuffer
	 *
	 * @param mBuffer
	 *            the mBuffer to set
	 */
	public void setBuffer(final StringBuffer mBuffer) {
		this.mBuffer = mBuffer;
	}

	@Override
	public void onClick(final View v) {
		HomeActivity activity = (HomeActivity) getActivity();
		if (activity != null) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.mail_to) });
			i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
			i.putExtra(Intent.EXTRA_TEXT, getMailContent(activity));
			try {
				startActivity(Intent.createChooser(i, getString(R.string.mail_popup_title)));
			} catch (android.content.ActivityNotFoundException ex) {
				CroutonUtils.display(getActivity(), getResources().getText(R.string.error_email), CoutonColor.BLACK);
			}
		}
	}

	/**
	 * Get mail content
	 *
	 * @param pActivity
	 * @return
	 */
	private String getMailContent(final HomeActivity pActivity) {
		StringBuilder builder = new StringBuilder();
		EmvCard card = pActivity.getCard();
		// Add application version
		try {
			builder.append(pActivity.getPackageManager().getPackageInfo(pActivity.getPackageName(), 0).versionName).append("\n");
		} catch (NameNotFoundException e) {
			// Do nothing
		}

		// Add ATS
		builder.append("ATS: ").append(BytesUtils.bytesToString(pActivity.getLastAts())).append("\n");

		String cardNumber = null;
		if (card != null) {
			cardNumber = StringUtils.deleteWhitespace(card.getCardNumber());
			if (cardNumber != null) {
				cardNumber = cardNumber.replaceAll("\\d{2}", "$0 ").trim();
			}
		}
		String mailContent = Html.fromHtml(mBuffer.toString()).toString().replace(" ", " ");
		if (cardNumber != null && mailContent != null) {
			mailContent = mailContent.replace(cardNumber, "XX XX");
		}
		builder.append(mailContent);
		return builder.toString();
	}
}
