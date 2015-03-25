package com.github.devnied.emvnfccard.fragment.viewPager.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify.IconValue;

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
		// Create icon
		IconDrawable icon = new IconDrawable(view.getContext(), IconValue.fa_share_alt).sizeDp(25).color(Color.WHITE);
		icon.setStyle(Style.FILL);
		// Update action button
		FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.fabutton);
		button.attachTo(mScrollView);
		button.setColor(getResources().getColor(R.color.blue));
		button.setDrawable(icon.getCurrent());
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
		String cardEncoded = null;
		if (card != null) {
			cardNumber = StringUtils.deleteWhitespace(card.getCardNumber());
			if (cardNumber != null) {
				cardEncoded = BytesUtils.bytesToString(cardNumber.getBytes()).trim();
				cardNumber = cardNumber.replaceAll("\\d{2}", "$0 ").trim();
			}
		}
		String mailContent = StringUtils.join(getLines(Html.fromHtml(mBuffer.toString()).toString().getBytes()), "\n");
		if (cardNumber != null && mailContent != null) {
			mailContent = mailContent.replace(cardNumber, "XX XX").replace(cardEncoded, "YY YY");
		}
		builder.append(mailContent);
		return builder.toString();
	}

	private List<String> getLines(final byte[] pData) {
		List<String> lines = new ArrayList<String>();
		InputStream is = new ByteArrayInputStream(pData);
		// read it with BufferedReader
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("resp:") || line.startsWith("send:")) {
					lines.add(line.replaceAll("[^a-zA-Z0-9:]", " "));
				}
			}
		} catch (IOException e) {
			Log.e(LogFragment.class.getName(), e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(br);
		}
		return lines;
	}
}
