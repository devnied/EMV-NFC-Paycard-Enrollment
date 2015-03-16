package com.github.devnied.emvnfccard.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.devnied.emvnfccard.EmvApplication;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.utils.ViewHolder;
import com.github.devnied.emvnfccard.utils.ViewUtils;
import com.joanzapata.android.iconify.Iconify;

/**
 * Menu Adapter to display menu item
 *
 * @author julien
 *
 */
public class MenuDrawerAdapter extends BaseAdapter {

	/**
	 * Icon
	 */
	private String mIcons[] = { "{fa-credit-card}", "{fa-gear}", "{fa-info-circle}" };

	/**
	 * Application context
	 */
	private Context mContext;

	public MenuDrawerAdapter(final Context pContext) {
		mContext = pContext;
	}

	@Override
	public int getCount() {
		return mIcons.length;
	}

	@Override
	public Object getItem(final int position) {
		return null;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			v = View.inflate(mContext, R.layout.drawer_listview_item, null);
		}
		// get view from holder
		TextView menuText = ViewHolder.get(v, R.id.menu_item_text);
		TextView menuIcon = ViewHolder.get(v, R.id.menu_item_icon);

		// Get element
		menuIcon.setText(mIcons[position]);
		Iconify.addIcons(menuIcon);
		menuText.setText(mContext.getResources().getStringArray(R.array.navigation_items)[position]);

		// Apply faceType
		ViewUtils.setTypeFace(EmvApplication.sTypeface, menuText);

		return v;
	}

}
