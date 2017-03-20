package com.github.devnied.emvnfccard.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * Class to manage View Holder pattern for list view
 */
public final class ViewHolder {

	/**
	 * Class to get and store a view in the holder
	 * 
	 * @param pView
	 *            the root view
	 * @param pId
	 *            id of view to find
	 * @return The View
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T get(final View pView, final int pId) {
		SparseArray<View> viewHolder = (SparseArray<View>) pView.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			pView.setTag(viewHolder);
		}
		View childView = viewHolder.get(pId);
		if (childView == null) {
			childView = pView.findViewById(pId);
			viewHolder.put(pId, childView);
		}
		return (T) childView;
	}

	/**
	 * Private constructor
	 */
	private ViewHolder() {
	}
}
