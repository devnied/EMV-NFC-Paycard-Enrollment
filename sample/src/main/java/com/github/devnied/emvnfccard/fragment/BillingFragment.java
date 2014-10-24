package com.github.devnied.emvnfccard.fragment;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.ActionBar.LayoutParams;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.github.devnied.emvnfccard.R;
import com.github.devnied.emvnfccard.billing.SkuDetails;
import com.github.devnied.emvnfccard.utils.ConstantUtils;
import com.github.devnied.emvnfccard.utils.CroutonUtils;
import com.github.devnied.emvnfccard.utils.SimpleAsyncTask;

/**
 * Billing fragment
 * 
 * @author Millau Julien
 * 
 */
public class BillingFragment extends Fragment implements OnClickListener {

	/**
	 * InApp Billing service
	 */
	private IInAppBillingService mService;

	/**
	 * Linear layout
	 */
	private LinearLayout layout;

	/**
	 * Async task
	 */
	private SimpleAsyncTask asyncTask;

	/**
	 * Service bind
	 */
	private boolean serviceBind;

	/**
	 * Service connection
	 */
	private ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			mService = null;

		}

		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			mService = IInAppBillingService.Stub.asInterface(service);
			// Create Async task
			asyncTask = new SimpleAsyncTask() {

				/**
				 * response list
				 */
				private ArrayList<String> responseList;

				@Override
				protected void doInBackground() {
					Bundle querySkus = new Bundle();
					ArrayList<String> itemId = new ArrayList<String>();
					for (int i = 0; i < 10; i++) {
						itemId.add("donate_" + i);
					}
					querySkus.putStringArrayList("ITEM_ID_LIST", itemId);
					try {
						Bundle skuDetails = mService.getSkuDetails(3, getActivity().getPackageName(), "inapp", querySkus);
						int response = skuDetails.getInt("RESPONSE_CODE");
						if (response == 0) {
							responseList = skuDetails.getStringArrayList("DETAILS_LIST");
						}
					} catch (RemoteException e) {
						Log.e(BillingFragment.class.getName(), "Remote exception", e);
					}

				}

				@Override
				protected void onPostExecute(final Object result) {
					if (responseList != null) {
						for (String thisResponse : responseList) {
							try {
								SkuDetails sku = new SkuDetails(thisResponse);
								View v = View.inflate(getActivity(), R.layout.inapp_listview_item, null);
								v.setOnClickListener(BillingFragment.this);
								v.setTag(sku);
								// Add layout param
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
										LayoutParams.WRAP_CONTENT);
								params.setMargins(0, 10, 0, 0);
								v.setLayoutParams(params);
								// Add inApp text
								TextView text = (TextView) v.findViewById(R.id.inapp_text);
								text.setText(sku.getDescription().trim() + " " + sku.getPrice());
								// Add view
								layout.addView(v);
							} catch (JSONException e) {
								Log.e(BillingFragment.class.getName(), "Billing response error", e);
							}
						}
					}
				}
			};
			asyncTask.execute();
		}
	};

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.donate, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		layout = (LinearLayout) view.findViewById(R.id.inAppList);
		if (!serviceBind) {
			((View) getActivity().findViewById(R.id.about_inapp_content).getParent()).setVisibility(View.GONE);
		}
	}

	@Override
	public void onAttach(final android.app.Activity activity) {
		super.onAttach(activity);
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		serviceBind = activity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mService != null) {
			getActivity().unbindService(mServiceConn);
		}
		if (asyncTask != null) {
			asyncTask.cancel(true);
		}
	}

	@Override
	public void onClick(final View v) {
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, getActivity().getPackageName(), ((SkuDetails) v.getTag()).getSku(),
					"inapp", null);
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

			getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(), ConstantUtils.INTENT_RESULT_CODE,
					new Intent(), 0, 0, 0);
		} catch (Exception e) {
			Log.e(BillingFragment.class.getName(), "On click error", e);
		}
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == ConstantUtils.INTENT_RESULT_CODE) {
			CroutonUtils.display(getActivity(), getText(R.string.billing_success), true);
		}
	}
}
