package com.github.devnied.emvnfccard.activity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.devnied.emvnfccard.BuildConfig;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.adapter.MenuDrawerAdapter;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.fragment.AboutFragment;
import com.github.devnied.emvnfccard.fragment.BillingFragment;
import com.github.devnied.emvnfccard.fragment.ConfigurationFragment;
import com.github.devnied.emvnfccard.fragment.IRefreshable;
import com.github.devnied.emvnfccard.fragment.ViewPagerFragment;
import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvParser;
import com.github.devnied.emvnfccard.provider.Provider;
import com.github.devnied.emvnfccard.utils.AtrUtils;
import com.github.devnied.emvnfccard.utils.ConstantUtils;
import com.github.devnied.emvnfccard.utils.CroutonUtils;
import com.github.devnied.emvnfccard.utils.CroutonUtils.CoutonColor;
import com.github.devnied.emvnfccard.utils.LibraryException;
import com.github.devnied.emvnfccard.utils.NFCUtils;
import com.github.devnied.emvnfccard.utils.RateDialog;
import com.github.devnied.emvnfccard.utils.SimpleAsyncTask;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import fr.devnied.bitlib.BytesUtils;

/**
 * Main Activity
 *
 * @author MILLAU Julien
 *
 */
@SuppressLint("InlinedApi")
public class HomeActivity extends FragmentActivity implements OnItemClickListener, IContentActivity, OnClickListener {

	/**
	 * Nfc utils
	 */
	private NFCUtils mNfcUtils;

	/**
	 * Waiting Dialog
	 */
	private ProgressDialog mDialog;

	/**
	 * Alert dialog
	 */
	private AlertDialog mAlertDialog;

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
	private MenuDrawerAdapter mMenuAdapter;

	/**
	 * IsoDep provider
	 */
	private Provider mProvider = new Provider();

	/**
	 * Emv card
	 */
	private EmvCard mReadCard;

	/**
	 * Reference for refreshable content
	 */
	private WeakReference<IRefreshable> mRefreshableContent;

	/**
	 * last selected Menu
	 */
	private int mLastSelectedMenu = -1;

	/**
	 * Tint manager
	 */
	private SystemBarTintManager tintManager;

	/**
	 * Last Ats
	 */
	private byte[] lastAts;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (Build.VERSION.SDK_INT >= 19) {
			// create our manager instance after the content view is set
			tintManager = new SystemBarTintManager(this);
			// enable status bar tint
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setNavigationBarTintEnabled(true);
			tintManager.setTintColor(Color.parseColor("#03a9f4"));
		}

		// init NfcUtils
		mNfcUtils = new NFCUtils(this);

		// get ListView defined in activity_main.xml
		mDrawerListView = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mMenuAdapter = new MenuDrawerAdapter(this);
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
		getActionBar().setDisplayShowHomeEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);

		// Display home screen
		backToHomeScreen();

		// Read card on launch
		if (getIntent().getAction() == NfcAdapter.ACTION_TECH_DISCOVERED) {
			onNewIntent(getIntent());
		}
	}

	/**
	 * Method used to back to home screen
	 */
	public void backToHomeScreen() {
		// Select first menu
		mDrawerListView.performItemClick(mDrawerListView, 0, mDrawerListView.getItemIdAtPosition(0));
		// Close Drawer
		mDrawerLayout.closeDrawer(mDrawerListView);
	}

	@Override
	protected void onResume() {
		mNfcUtils.enableDispatch();
		// Close
		if (mAlertDialog != null && mAlertDialog.isShowing()) {
			mAlertDialog.cancel();
		}
		// Check NFC enable
		if (!NFCUtils.isNfcEnable(getApplicationContext())) {
			backToHomeScreen();

			AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			alertbox.setTitle(getString(R.string.msg_info));
			alertbox.setMessage(getString(R.string.msg_nfc_disable));
			alertbox.setPositiveButton(getString(R.string.msg_activate_nfc), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					Intent intent = null;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						intent = new Intent(Settings.ACTION_NFC_SETTINGS);
					} else {
						intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
					}
					dialog.dismiss();
					startActivity(intent);
				}
			});
			alertbox.setCancelable(true);
			alertbox.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(final DialogInterface arg0) {
					finish();
				}
			});
			mAlertDialog = alertbox.show();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNfcUtils.disableDispatch();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		final Tag mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (mTag != null) {

			new SimpleAsyncTask() {

				/**
				 * Tag comm
				 */
				private IsoDep mTagcomm;

				/**
				 * Emv Card
				 */
				private EmvCard mCard;

				/**
				 * Boolean to indicate exception
				 */
				private boolean mException;

				/**
				 * Unknown error
				 */
				private Throwable unknownError;

				/**
				 * Terminal
				 */
				private DefaultTerminalImpl terminal = new DefaultTerminalImpl();

				@Override
				protected void onPreExecute() {
					super.onPreExecute();

					backToHomeScreen();
					mProvider.getLog().setLength(0);
					// Show dialog
					if (mDialog == null) {
						mDialog = ProgressDialog.show(HomeActivity.this, getString(R.string.card_reading),
								getString(R.string.card_reading_desc), true, false);
					} else {
						mDialog.show();
					}
				}

				@Override
				protected void doInBackground() {

					mTagcomm = IsoDep.get(mTag);
					if (mTagcomm == null) {
						CroutonUtils.display(HomeActivity.this, getText(R.string.error_communication_nfc), CoutonColor.BLACK);
						return;
					}
					mException = false;

					try {
						mTagcomm.setTimeout(2000);
						mReadCard = null;
						// Open connection
						mTagcomm.connect();
						lastAts = getAts(mTagcomm);

						mProvider.setmTagCom(mTagcomm);

						// Define country from device
						terminal.setCountryCode(CountryCodeEnum.getCountry(Locale.getDefault().getISO3Country()));

						EmvParser parser = EmvParser.Builder() //
								.setProvider(mProvider) //
								.setTerminal(terminal) //
								.setContactLess(true) //
								.setReadAllAids(true) //
								.setReadTransactions(true) //
								.build();
						mCard = parser.readEmvCard();

						if (mCard != null) {
							mCard.setAtrDescription(extractAtsDescription(lastAts));
						}

					} catch (IOException e) {
						mException = true;
					} catch (Throwable e) {
						unknownError = e;
					} finally {
						// close tagcomm
						IOUtils.closeQuietly(mTagcomm);
					}
				}

				@Override
				protected void onPostExecute(final Object result) {

					// check destroyed Activity
					if (HomeActivity.this.isFinishing()) {
						return;
					}

					// close dialog
					if (mDialog != null) {
						mDialog.cancel();
					}

					if (unknownError != null) {
						throw new LibraryException(mProvider.getLog(), unknownError);
					}

					if (!mException) {
						if (mCard != null && mCard.getState() != CardStateEnum.UNKNOWN) {
							if (StringUtils.isNotBlank(mCard.getCardNumber())) {
								CroutonUtils.display(HomeActivity.this, getText(R.string.card_read), CoutonColor.GREEN);
								// rate dialog
								new RateDialog(HomeActivity.this).init();
								mReadCard = mCard;
							} else if (mCard.getState() == CardStateEnum.LOCKED) {
								CroutonUtils.display(HomeActivity.this, getText(R.string.nfc_locked), CoutonColor.ORANGE);
								
								// Throw exception on selected locked application
								if (ApplicationStepEnum.isAtLeast(mCard.getApplications(), ApplicationStepEnum.SELECTED)) {
									throw new LibraryException(mProvider.getLog(), unknownError);
								}
							}
						} else {
							CroutonUtils.display(HomeActivity.this, getText(R.string.error_card_unknown), CoutonColor.BLACK);
						}
					} else {
						CroutonUtils
						.display(HomeActivity.this, getResources().getText(R.string.error_communication_nfc), CoutonColor.BLACK);
					}

					refreshContent();
				}

			}.execute();
		}

	}

	/**
	 * Get ATS from isoDep
	 *
	 * @param pIso
	 *            isodep
	 * @return ATS byte array
	 */
	private byte[] getAts(final IsoDep pIso) {
		byte[] ret = null;
		if (pIso.isConnected()) {
			// Extract ATS from NFC-A
			ret = pIso.getHistoricalBytes();
			if (ret == null) {
				// Extract ATS from NFC-B
				ret = pIso.getHiLayerResponse();
			}
		}
		return ret;
	}

	/**
	 * Method used to get description from ATS
	 *
	 * @param pAts
	 *            ATS byte
	 */
	public Collection<String> extractAtsDescription(final byte[] pAts) {
		return AtrUtils.getDescriptionFromAts(BytesUtils.bytesToString(pAts));
	}

	@Override
	public void onBackPressed() {
		if (BuildConfig.DEBUG) {
			if (mReadCard == null) {
				StringBuffer buff = mProvider.getLog();
				for (int i = 0; i < 60; i++) {
					buff.append("=============<br/>");
				}
				mReadCard = new EmvCard();
				Application app = new Application();
				app.setAid(BytesUtils.fromString("A0 00 00 00 03 10 10"));
				app.setLeftPinTry(3);
				app.setApplicationLabel("CB");
				mReadCard.getApplications().add(app);
				mReadCard.setAtrDescription(Arrays.asList("CB Visa Banque Populaire (France)"));
				mReadCard.setHolderFirstname("John");
				mReadCard.setHolderFirstname("Doe");
				mReadCard.setType(EmvCardScheme.UNIONPAY);
				EmvTrack2 track2 = new EmvTrack2();
				track2.setCardNumber("4123456789012345");
				track2.setExpireDate(new Date());
				mReadCard.setTrack2(track2);

				List<EmvTransactionRecord> records = new ArrayList<EmvTransactionRecord>();
				
				for (int i=0; i<100 ; i++){
					// payment
					EmvTransactionRecord payment = new EmvTransactionRecord();
					payment.setAmount((float) i);
					payment.setCurrency(CurrencyEnum.EUR);
					payment.setCyptogramData("12");
					payment.setTerminalCountry(CountryCodeEnum.FR);
					payment.setDate(DateUtils.addDays(new Date(), i));
					payment.setTransactionType(TransactionTypeEnum.REFUND);
					records.add(payment);
				}
				

				app.setListTransactions(records);
				refreshContent();
				CroutonUtils.display(HomeActivity.this, getText(R.string.card_read), CoutonColor.GREEN);
			} else if (mReadCard != null) {
				mReadCard = null;
				refreshContent();
				CroutonUtils.display(HomeActivity.this, getText(R.string.card_read), CoutonColor.GREEN);
			}
		} else if (mLastSelectedMenu != ConstantUtils.CARDS_DETAILS) {
			backToHomeScreen();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		Crouton.cancelAllCroutons();
		super.onDestroy();
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

	private void refreshContent() {
		if (mRefreshableContent != null && mRefreshableContent.get() != null) {
			mRefreshableContent.get().update();
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (mLastSelectedMenu != position) {
			Fragment fragment = null;
			switch (position) {
			case ConstantUtils.CARDS_DETAILS:
				fragment = new ViewPagerFragment();
				refreshContent();
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
				getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
			}
			mLastSelectedMenu = position;
		}
		mDrawerLayout.closeDrawer(mDrawerListView);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (mLastSelectedMenu == ConstantUtils.ABOUT) {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
			if (fragment != null) {
				BillingFragment billing = (BillingFragment) fragment.getChildFragmentManager().findFragmentById(R.id.about_inapp_content);
				if (billing != null) {
					billing.onActivityResult(requestCode, resultCode, data);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
			Crouton.cancelAllCroutons();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public StringBuffer getLog() {
		return mProvider.getLog();
	}

	@Override
	public EmvCard getCard() {
		return mReadCard;
	}

	@Override
	public void setRefreshableContent(final IRefreshable pRefreshable) {
		mRefreshableContent = new WeakReference<IRefreshable>(pRefreshable);
	}

	/**
	 * Method used to clear data
	 */
	public void clear() {
		mReadCard = null;
		mProvider.getLog().setLength(0);
		IRefreshable content = mRefreshableContent.get();
		if (content != null) {
			content.update();
		}
	}

	@Override
	public void onClick(final View v) {
		if (mDrawerListView != null) {
			// Animate banner
			ObjectAnimator animator = ObjectAnimator.ofObject(v, "backgroundColor", new ArgbEvaluator(), Color.parseColor("#C5E1A5"),
					Color.parseColor("#AED581")).setDuration(70);
			animator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(final Animator animation) {
				}

				@Override
				public void onAnimationRepeat(final Animator animation) {
				}

				@Override
				public void onAnimationEnd(final Animator animation) {
					v.setBackgroundColor(Color.parseColor("#C5E1A5"));
					mDrawerListView.performItemClick(mDrawerListView, 2, mDrawerListView.getItemIdAtPosition(2));
				}

				@Override
				public void onAnimationCancel(final Animator animation) {
				}
			});
			animator.start();

		}
	}

	/**
	 * Get the last ATS
	 *
	 * @return the last card ATS
	 */
	public byte[] getLastAts() {
		return lastAts;
	}

}