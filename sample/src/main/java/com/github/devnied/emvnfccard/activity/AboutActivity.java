package com.github.devnied.emvnfccard.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.utils.CroutonUtils;

/**
 * About activity
 * 
 * @author MILLAU Julien
 * 
 */
public class AboutActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		TextView content = (TextView) findViewById(R.id.about_content);
		content.setMovementMethod(new MovementCheck());
	}

	/**
	 * Custom LinkMovementMethod for catching impossible action
	 * 
	 * @author MILLAU Julien
	 * 
	 */
	private class MovementCheck extends LinkMovementMethod {

		@Override
		public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {
			try {
				return super.onTouchEvent(widget, buffer, event);
			} catch (Exception e) {
				CroutonUtils.display(AboutActivity.this, getText(R.string.error_link), false);
			}
			return false;
		}
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