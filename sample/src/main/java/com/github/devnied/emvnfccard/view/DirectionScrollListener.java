package com.github.devnied.emvnfccard.view;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Scroll listener for scrollview
 * 
 * @author Millau Julien
 * 
 */
public class DirectionScrollListener implements OnTouchListener {

	/**
	 * Action button
	 */
	private final FloatingActionButton mFloatingActionButton;

	DirectionScrollListener(final FloatingActionButton floatingActionButton) {
		mFloatingActionButton = floatingActionButton;
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			mFloatingActionButton.hide(true);
			break;
		default:
			mFloatingActionButton.hide(false);
		}
		return false;
	}

}