/*
 * Copyright (C) 2017 Grigory Tureev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package doc.org.arpoffeine.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.adapter.SessionAdapter;
import doc.org.arpoffeine.util.Constants;
import lombok.NoArgsConstructor;

/**
 * Class that deals with any kind notifications
 */

@EBean
@NoArgsConstructor
@SuppressWarnings("WeakerAccess")
public class NotificationHelper implements Constants {

    private final String TAG = "NotificationHelper";

    @Bean protected ConfigHelper   mConfigHelper;
    @Bean protected SystemHelper   mSystemHelper;
    @Bean protected SessionAdapter mSessionAdapter;

    @RootContext
    protected MainActivity        mContext;
    private   AlertDialog.Builder mBuilder;
    private   AlertDialog         mAlert;
    private   View                mView;

    @StringRes(R.string.sb_msg_blackList_cleared)
    protected String mBlackListCleared;
    @StringRes(R.string.sb_msg_added_blacklist)
    protected String mAddedToBlackList;
    @StringRes(R.string.sb_msg_session_removed)
    protected String mSessionRemoved;
    @StringRes(R.string.sb_msg_saved_reloaded)
    protected String mSavedReloaded;
    @StringRes(R.string.sb_msg_saved_cleared)
    protected String mSavedCleared;
    @StringRes(R.string.sb_msg_session_saved)
    protected String mSessionSaved;
    @StringRes(R.string.sb_msg_list_cleared)
    protected String mListCleared;
    @StringRes(R.string.sb_action_reconnect)
    protected String mReconnect;
    @StringRes(R.string.sb_msg_no_network)
    protected String mNoNetwork;
    @StringRes(R.string.sb_msg_lost_wifi)
    protected String mLostWiFi;
    @StringRes(R.string.message_ask_url)
    protected String mAskUrl;
    @StringRes(R.string.sb_msg_no_wifi)
    protected String mNoWiFi;
    @StringRes(R.string.toast_accept_text)
    protected String mAccept;

    @AfterViews
    protected void afterViews(){
        mView = mContext.findViewById(R.id.content_main);
    }

    @UiThread
    public void showUnRooted() {
        if (DEBUG) Log.d(TAG, "showUnRooted");
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(APPLICATION_TAG).
                setMessage(R.string.message_error_su_check).
                setCancelable(false).
                setPositiveButton(R.string.btn_understand, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
        });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void showDisclaimer() {
        if (DEBUG) Log.d(TAG, "showDisclaimer");
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.disclaimer, (ViewGroup) mContext.findViewById(R.id.disclaimer_layout));
        final AlertDialog al = new AlertDialog.Builder(mContext).create();
        Button disclaimer_btn = (Button)layout.findViewById(R.id.disclaimer_btn);
        disclaimer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) layout.findViewById(R.id.cb_accept);
                if (cb.isChecked()) al.cancel();
                else Toast.makeText(mContext, mAccept, Toast.LENGTH_SHORT).show();
            }
        });
        al.setView(layout);
        al.setCancelable(false);
        al.show();
    }

    @UiThread
    public void clearBlackList() {
        if (DEBUG) Log.d(TAG, "clearBlackList");
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(APPLICATION_TAG).
                setMessage(R.string.message_clear_blackList).
                setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mConfigHelper.clearBL();
                        Snackbar.make(mView, mBlackListCleared, Snackbar.LENGTH_SHORT).show();
                    }
                }).
                setNegativeButton(R.string.btn_abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
        });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void askToStay() {
        if (DEBUG) Log.d(TAG, "askToStay");
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(APPLICATION_TAG).
                setMessage(R.string.message_ask_stay).
                setPositiveButton(R.string.btn_exit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mContext.moveTaskToBack(true);
                        mContext.finish();
                    }
                }).
                setNegativeButton(R.string.btn_stay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
        });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void askToCloseWebView() {
        if (DEBUG) Log.d(TAG, "askToCloseWebView");
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(APPLICATION_TAG).
                setMessage(R.string.message_ask_close_webView).
                setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // Replaces WebView fragment with Main fragment, shows FAB and changes menu to main-menu
                        mContext.getSupportFragmentManager().popBackStack();
                        mContext.getMFab().show();
                        mContext.getMToolbar().getMenu().clear();
                        mContext.getMToolbar().inflateMenu(R.menu.main);

                        // Sets sNoTitle in order titles being updated
                        MainActivity.sNoTitle = false;
                    }
                }).
                setNegativeButton(R.string.btn_stay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void clearSavedList() {
        if (DEBUG) Log.d(TAG, "clearSavedList");
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(APPLICATION_TAG).
                setMessage(R.string.message_clear_savedList).
                setCancelable(false).
                setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mSystemHelper.deleteSessionFiles();
                        Snackbar.make(mView, mSavedCleared, Snackbar.LENGTH_SHORT).show();
                        mSessionAdapter.notifyDataSetChanged();
                    }
                }).
                setNegativeButton(R.string.btn_abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
        });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void clearList() {
        if (DEBUG) Log.d(TAG, "clearList");
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setTitle(APPLICATION_TAG).
                setMessage(R.string.message_clear_list).
                setCancelable(false).
                setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mSessionAdapter.clear();
                        Snackbar.make(mView, mListCleared, Snackbar.LENGTH_SHORT).show();
                    }
                }).
                setNegativeButton(R.string.btn_abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
        });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void askEditURL(String URL) {
        if (DEBUG) Log.d(TAG, "askEditURL");
        mBuilder = new AlertDialog.Builder(mContext);
        final EditText inputURL = new EditText(mContext);
        inputURL.setText(URL);
        mBuilder.setTitle(mAskUrl).
                setView(inputURL).
                setPositiveButton(R.string.btn_go, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mContext.getMWebViewFragment().getMWebView().loadUrl(inputURL.getText().toString());
                        mContext.getMToolbar().setTitle(inputURL.getText().toString());
                    }
                }).
                setNegativeButton(R.string.btn_abort, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
        });
        mAlert = mBuilder.create();
        mAlert.show();
    }

    @UiThread
    public void askToReconnect() {
        if (DEBUG) Log.d(TAG, "askToReconnect");
        Snackbar.make(mView, mLostWiFi, Snackbar.LENGTH_LONG)
                .setAction(mReconnect, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mContext.startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                    }})
                .setActionTextColor(Color.WHITE)
                .show();
    }

    @UiThread
    public void notifyNoConnection(boolean wifi) {
        if (DEBUG) Log.d(TAG, "notifyNoConnection");
        String msg = (wifi) ? mNoWiFi : mNoNetwork;
        Snackbar.make(mView, msg, Snackbar.LENGTH_SHORT).show();
    }

    @UiThread
    public void notifySavedReloadCompleted() {
        if (DEBUG) Log.d(TAG, "notifySavedReloadCompleted");
        Snackbar.make(mView, mSavedReloaded, Snackbar.LENGTH_SHORT).show();
    }

    @UiThread
    public void notifyCookieSaved() {
        if (DEBUG) Log.d(TAG, "notifyCookieSaved");
        Snackbar.make(mView, mSessionSaved, Snackbar.LENGTH_SHORT).show();
    }

    @UiThread
    public void notifyCookieRemoved() {
        if (DEBUG) Log.d(TAG, "notifyCookieRemoved");
        Snackbar.make(mView, mSessionRemoved, Snackbar.LENGTH_SHORT).show();
    }

    @UiThread
    public void notifyAddedToBL(String domain) {
        if (DEBUG) Log.d(TAG, "notifyAddedToBL");
        String msg = mAddedToBlackList + ": " + domain;
        Snackbar.make(mView, msg, Snackbar.LENGTH_SHORT).show();
    }
}
