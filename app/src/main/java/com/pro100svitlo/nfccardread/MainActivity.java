package com.pro100svitlo.nfccardread;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pro100svitlo.nfccardreader.CardNfcAsyncTask;

public class MainActivity extends AppCompatActivity implements CardNfcAsyncTask.CardNfcInterface {

    private CardNfcAsyncTask mCardNfcAsyncTask;
    private Toolbar mToolbar;
    private LinearLayout mPutCardContent;
    private LinearLayout mCardReadyContent;
    private TextView mCardNumberText;
    private TextView mExpireDateText;
    private ImageView mCardLogoIcon;
    private NfcAdapter mNfcAdapter;
    private AlertDialog mTurnNfcDialog;
    private IntentFilter[] mIntentFilter = {new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)};
    private String[][] mNfcTechFilter = new String[][] { new String[] {
            NfcA.class.getName()
    } };
    private PendingIntent mNfcPIntent;
    private ProgressDialog mProgressDialog;
    private String mDoNotMoveCardMessage;
    private String mUnknownEmvCardMessage;
    private String mCardWithLockedNfcMessage;
    private boolean mIsScanNow;
    private boolean mIntentFromCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null){
            TextView noNfc = (TextView)findViewById(android.R.id.candidatesArea);
            noNfc.setVisibility(View.VISIBLE);
        } else {
            mPutCardContent = (LinearLayout) findViewById(R.id.content_putCard);
            mCardReadyContent = (LinearLayout) findViewById(R.id.content_cardReady);
            mCardNumberText = (TextView) findViewById(android.R.id.text1);
            mExpireDateText = (TextView) findViewById(android.R.id.text2);
            mCardLogoIcon = (ImageView) findViewById(android.R.id.icon);
            mNfcPIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            createProgressDialog();
            initNfcMessages();

            mIntentFromCreate = true;
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIntentFromCreate = false;
        if (mNfcAdapter != null && !mNfcAdapter.isEnabled()){
            showTurnOnNfcDialog();
            mPutCardContent.setVisibility(View.GONE);
        } else if (mNfcAdapter != null){
            if (!mIsScanNow){
                mPutCardContent.setVisibility(View.VISIBLE);
            }
            mNfcAdapter.enableForegroundDispatch(this, mNfcPIntent, mIntentFilter, mNfcTechFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCardNfcAsyncTask = new CardNfcAsyncTask.Builder(this, intent, mIntentFromCreate)
                .build();
    }

    @Override
    public void startNfcReadCard() {
        mIsScanNow = true;
        mProgressDialog.show();
    }

    @Override
    public void cardIsReadyToRead() {
        mPutCardContent.setVisibility(View.GONE);
        mCardReadyContent.setVisibility(View.VISIBLE);
        String card = mCardNfcAsyncTask.getCardNumber();
        card = getPrettyCardNumber(card);
        String expiredDate = mCardNfcAsyncTask.getCardExpireDate();
        String cardType = mCardNfcAsyncTask.getCardType();
        mCardNumberText.setText(card);
        mExpireDateText.setText(expiredDate);
        parseCardType(cardType);
    }

    @Override
    public void doNotMoveCardSoFast() {
        showSnackBar(mDoNotMoveCardMessage);
    }

    @Override
    public void unknownEmvCard() {
        showSnackBar(mUnknownEmvCardMessage);
    }

    @Override
    public void cardWithLockedNfc() {
        showSnackBar(mCardWithLockedNfcMessage);
    }

    @Override
    public void finishNfcReadCard() {
        mProgressDialog.hide();
        mCardNfcAsyncTask = null;
        mIsScanNow = false;
    }

    private void createProgressDialog(){
        String title = getString(R.string.ad_progressBar_title);
        String mess = getString(R.string.ad_progressBar_mess);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(mess);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
    }

    private void showSnackBar(String message){
        Snackbar.make(mToolbar, message, Snackbar.LENGTH_SHORT).show();
    }

    private void showTurnOnNfcDialog(){
        if (mTurnNfcDialog == null) {
            String title = getString(R.string.ad_nfcTurnOn_title);
            String mess = getString(R.string.ad_nfcTurnOn_message);
            String pos = getString(R.string.ad_nfcTurnOn_pos);
            String neg = getString(R.string.ad_nfcTurnOn_neg);
            mTurnNfcDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(mess)
                    .setPositiveButton(pos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Send the user to the settings page and hope they turn it on
                            if (android.os.Build.VERSION.SDK_INT >= 16) {
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                            } else {
                                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton(neg, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onBackPressed();
                        }
                    }).create();
        }
        mTurnNfcDialog.show();
    }

    private void initNfcMessages(){
        mDoNotMoveCardMessage = getString(R.string.snack_doNotMoveCard);
        mCardWithLockedNfcMessage = getString(R.string.snack_lockedNfcCard);
        mUnknownEmvCardMessage = getString(R.string.snack_unknownEmv);
    }


    private void parseCardType(String cardType){
        if (cardType.equals(CardNfcAsyncTask.CARD_VISA)){
            mCardLogoIcon.setImageResource(R.mipmap.visa_logo);
        } else if (cardType.equals(CardNfcAsyncTask.CARD_MASTER_CARD)){
            mCardLogoIcon.setImageResource(R.mipmap.master_logo);
        }
    }

    private String getPrettyCardNumber(String card){
        String div = " - ";
        return  card.substring(0,4) + div + card.substring(4,8) + div + card.substring(8,12)
                +div + card.substring(12,16);
    }
}
