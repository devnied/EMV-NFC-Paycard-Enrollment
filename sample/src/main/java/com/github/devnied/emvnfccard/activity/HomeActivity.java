package com.github.devnied.emvnfccard.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.MenuAdapter;
import com.github.devnied.emvnfccard.fragment.AboutFragment;
import com.github.devnied.emvnfccard.fragment.ConfigurationFragment;
import com.github.devnied.emvnfccard.fragment.ViewPagerFragment;
import com.github.devnied.emvnfccard.utils.ConstantUtils;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Main Activity
 * 
 * @author MILLAU Julien
 * 
 */
public class HomeActivity extends Activity implements OnItemClickListener {

	/**
	 * Drawer layout
	 */
	private DrawerLayout mDrawerLayout;
	/**
	 * ListView drawer
	 */
	private ListView mDrawerListView;
	/**
	 * Action bar drawer toggle
	 */
	private ActionBarDrawerToggle mActionBarDrawerToggle;

	/**
	 * Menu adapter
	 */
	private MenuAdapter mMenuAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// get ListView defined in activity_main.xml
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mMenuAdapter = new MenuAdapter(this);
		mDrawerListView.setAdapter(mMenuAdapter);
		mDrawerListView.setOnItemClickListener(this);

		// 2. App Icon
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// 2.1 create ActionBarDrawerToggle
		mActionBarDrawerToggle = new ActionBarDrawerToggle(/* */
		this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
		R.string.navigation_menu_open, /* "open drawer" description */
		R.string.navigation_menu_close /* "close drawer" description */
		);

		// 2.2 Set actionBarDrawerToggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// default fragment layout
		getFragmentManager().beginTransaction().replace(R.id.content_frame, new ViewPagerFragment()).commit();
		mDrawerListView.setItemChecked(0, true);
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mActionBarDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mActionBarDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (!view.isActivated()) {
			Fragment fragment = null;
			switch (position) {
			case ConstantUtils.CARDS_DETAILS:
				fragment = new ViewPagerFragment();
				break;
			case ConstantUtils.CONFIGURATION:
				fragment = new ConfigurationFragment();
				break;
			case ConstantUtils.ABOUT:
				fragment = new AboutFragment();
				break;
			default:
				break;
			}
			if (fragment != null) {
				getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
			}
		}
		mDrawerLayout.closeDrawer(mDrawerListView);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
			Crouton.cancelAllCroutons();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}