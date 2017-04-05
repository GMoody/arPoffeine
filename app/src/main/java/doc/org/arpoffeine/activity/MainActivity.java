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
package doc.org.arpoffeine.activity;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.adapter.SessionAdapter;
import doc.org.arpoffeine.fragment.HelpFragment;
import doc.org.arpoffeine.fragment.HelpFragment_;
import doc.org.arpoffeine.fragment.SessionsFragment;
import doc.org.arpoffeine.fragment.SessionsFragment_;
import doc.org.arpoffeine.fragment.WebViewFragment;
import doc.org.arpoffeine.fragment.WebViewFragment_;
import doc.org.arpoffeine.helper.ConfigHelper;
import doc.org.arpoffeine.helper.NotificationHelper;
import doc.org.arpoffeine.helper.SystemHelper;
import doc.org.arpoffeine.services.ARPSpoofService_;
import doc.org.arpoffeine.services.ListenService_;
import doc.org.arpoffeine.util.Constants;
import doc.org.arpoffeine.util.ISharedPreferences_;
import doc.org.arpoffeine.util.RefreshHandler;
import lombok.Getter;

@OptionsMenu(R.menu.main)
@EActivity(R.layout.activity_main)
@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ContextMenu.ContextMenuInfo,
        Constants {

    // Views DI start
    @Getter @ViewById(R.id.fab)           protected FloatingActionButton mFab;
    @Getter @ViewById(R.id.nav_view)      protected NavigationView       mNavigationView;
    @Getter @ViewById(R.id.drawer_layout) protected DrawerLayout         mDrawer;
    @Getter @ViewById(R.id.toolbar)       protected Toolbar              mToolbar;
    // Views DI end

    // Fragments DI start
    @Getter @FragmentById(R.id.sessions_fragment) protected SessionsFragment mSessionsFragment;
    @Getter @FragmentById(R.id.webView_fragment)  protected WebViewFragment  mWebViewFragment;
    @Getter @FragmentById(R.id.help_fragment)     protected HelpFragment     mHelpFragment;
    // Fragments DI end

    // Resources DI start
    @DrawableRes(R.drawable.ic_stop_white_24dp)       protected Drawable mStop;
    @DrawableRes(R.drawable.ic_play_arrow_white_24dp) protected Drawable mPlay;

    @StringRes(R.string.title_idle)                                 protected String mTitleIdle;
    @StringRes(R.string.tv_no_ssid)                   protected String mNoSsid;
    @StringRes(R.string.title_working)                              protected String mTitleWorking;
    @StringRes(R.string.title_spoofing)                             protected String mTitleSpoofing;
    @StringRes(R.string.sb_msg_no_wifi)         protected String mMsgNoWiFi;
    @StringRes(R.string.title_starting)                             protected String mTitleStarting;
    @StringRes(R.string.title_stopping)                             protected String mTitleStopping;
    @StringRes(R.string.tv_not_spoofing)                  protected String mNotSpoofing;
    @StringRes(R.string.title_listening)                       protected String mTitleListening;
    @StringRes(R.string.tv_not_connected)                        protected String mNotConnected;
    @StringRes(R.string.notification_text)              protected String mNotificationText;
    @StringRes(R.string.subtitle_sessions)                            protected String mSubtitleSessions;
    @StringRes(R.string.notification_title_captured)        protected String mNotificationCaptured;
    // Resources DI end

    // Beans DI start
    @Bean protected SessionAdapter     mSessionAdapter;
    @Bean protected ConfigHelper       mConfigHelper;
    @Bean protected SystemHelper       mSystemHelper;
    @Bean protected RefreshHandler     mRefreshHandler;
    @Bean protected NotificationHelper mNotificationHelper;
    // Beans DI end

    // System services DI start
    @SystemService protected ConnectivityManager mConnectivityManager;
    @SystemService protected NotificationManager mNotificationManager;
    @SystemService protected ActivityManager     mActivityManager;
    @SystemService protected WifiManager         mWifiManager;
    // System services DI end

    // Fields start
    @Getter private TextView mTvNetworkName;
    @Getter private TextView mTvSpoofingIP;

    public static boolean sHelp      = false;
    public static boolean sRooted    = false;
    public static boolean sNoTitle   = false;
    public static boolean sSpoofing  = false;
    public static boolean sListening = false;

    private Bundle  mBundle;
    private String  mGatewayIP;
    // Fields end

    // Fragments afterInject start
    @FragmentById(R.id.sessions_fragment)
    void setAuthListFragmentUp(SessionsFragment sessionsFragment) {
        this.mSessionsFragment = new SessionsFragment_();
        FragmentTransaction fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.addToBackStack("mSessionsFragment");
        fTrans.replace(R.id.main_frame_container, this.mSessionsFragment).commit();
    }

    @FragmentById(R.id.webView_fragment)
    void setWebViewFragmentUp(WebViewFragment webViewFragment) {
        this.mWebViewFragment = new WebViewFragment_();
    }

    @FragmentById(R.id.help_fragment)
    void setHelpFragmentUp(HelpFragment helpFragment) {
        this.mHelpFragment = new HelpFragment_();
    }
    // Fragments afterInject end

    // OptionsMenuItems actions start
    @OptionsItem(R.id.action_clear_blackList)
    void clearBlackListSelected() {
        mNotificationHelper.clearBlackList();
        mConfigHelper.loadDomainBL();
    }

    @OptionsItem(R.id.action_clear_saved)
    void clearSavedListSelected() {
        mNotificationHelper.clearSavedList();
    }

    @OptionsItem(R.id.action_clear_list)
    void clearListSelected() {
        mNotificationHelper.clearList();
    }

    @OptionsItem(R.id.action_reload_saved)
    void reloadSavedSelected() {
        mSystemHelper.readSessionFiles();
        mNotificationHelper.notifySavedReloadCompleted();
    }

    @OptionsItem(R.id.action_search)
    void searchSelected(MenuItem menuSearch) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSessionAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @OptionsItem(R.id.action_exit)
    void exitSelected() {
        mNotificationHelper.askToCloseWebView();
    }

    @OptionsItem(R.id.action_back)
    void backSelected() {
        if (mWebViewFragment.getMWebView().canGoBack())
            mWebViewFragment.getMWebView().goBack();
        else mNotificationHelper.askToCloseWebView();
    }

    @OptionsItem(R.id.action_reload)
    void reloadSelected() {
        mWebViewFragment.getMWebView().reload();
    }

    @OptionsItem(R.id.action_edit_url)
    void editURLSelected() {
        mNotificationHelper.askEditURL(mWebViewFragment.getMUrl());
    }
    // OptionsMenuItems actions end

    @Pref
    ISharedPreferences_ preferences;

    @AfterViews
    void afterViews() {
        if (DEBUG) Log.d(APPLICATION_TAG, "afterViews");
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        mToolbar.setTitle(mTitleIdle);

        // Get & Set up mDrawer header TextViews
        View headerView = mNavigationView.getHeaderView(0);
        mTvNetworkName = (TextView) headerView.findViewById(R.id.tv_connected_network_name);
        mTvSpoofingIP  = (TextView) headerView.findViewById(R.id.tv_spoofing_ip_value);
    }

    @AfterInject
    void afterInject() {
        if (DEBUG) Log.d(APPLICATION_TAG, "afterInject");
        mNotificationHelper.showDisclaimer();
        mSystemHelper.init(this);
        mConfigHelper.init(this);
        mSystemHelper.readSessionFiles();
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.d(APPLICATION_TAG, "onBackPressed");
        if (mDrawer.isDrawerOpen(GravityCompat.START))
            mDrawer.closeDrawer (GravityCompat.START);
        else if (sHelp) getSupportFragmentManager().popBackStack();
        else if (this.isTaskRoot())
            mNotificationHelper.askToStay();
        else super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebViewFragment.getMWebView() != null){
            if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebViewFragment.getMWebView().canGoBack())
                mWebViewFragment.getMWebView().goBack();
            else mNotificationHelper.askToCloseWebView();
            return true;
        } else return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (DEBUG) Log.d(APPLICATION_TAG, "onNavigationItemSelected");

        switch (item.getItemId()){
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity_.class);
                startActivity(intent);
                break;
            case R.id.nav_help:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("helpFragment");
                transaction.replace(R.id.main_frame_container, mHelpFragment).commit();
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Click(R.id.fab)
    void onFabClick() {
        if (DEBUG) Log.d(APPLICATION_TAG, "onFabClick");
        if (!mWifiManager.isWifiEnabled())
            mNotificationHelper.notifyNoConnection(true);
        else if (sListening) stopWorking();
        else startWorking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshHandler.startAutoRefresh();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRefreshHandler.stopAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRefreshHandler.stopAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.d(APPLICATION_TAG, "onDestroy");
        mRefreshHandler.stopAutoRefresh();
        mSessionAdapter.clear();
        mNotificationManager.cancelAll();
        stopWorking();
        finish();
        super.onDestroy();
    }

    @Background(serial = "start")
    public void startSpoofing() {
        if (DEBUG) Log.d(APPLICATION_TAG, "startSpoofing");
        int localhost = mWifiManager.getConnectionInfo().getIpAddress();
        mGatewayIP = Formatter.formatIpAddress(mWifiManager.getDhcpInfo().gateway);
        String localhostIP = Formatter.formatIpAddress(localhost);
        String interfaceName = null;

        try {
            InetAddress localInet = InetAddress.getByName(localhostIP);
            NetworkInterface wifiInterface = NetworkInterface.getByInetAddress(localInet);
            interfaceName = wifiInterface.getDisplayName();
        } catch (UnknownHostException e) {
            Log.e(APPLICATION_TAG, "error getting localhost's InetAddress");
        } catch (SocketException e) {
            Log.e(APPLICATION_TAG, "error getting wifi network interface");
        }

        mBundle = new Bundle();
        mBundle.putString("gateway",   mGatewayIP);
        mBundle.putString("localBin",  mSystemHelper.getBinaryPath(false, false));
        mBundle.putString("interface", interfaceName);

        try {
            Thread.sleep(500);
            ARPSpoofService_.intent(getApplication()).startSpoofingService(mBundle).start();
            sSpoofing = true;
        } catch (InterruptedException e) {
            if (DEBUG) Log.d(APPLICATION_TAG, "startSpoofing");
        }
    }

    @Background(serial = "stopWorking")
    public void stopSpoofing() {
        if (DEBUG) Log.d(APPLICATION_TAG, "stopSpoofing");
        try {
            ARPSpoofService_.intent(getApplication()).stop();
            Thread.sleep(200);
            sSpoofing = false;
        } catch (InterruptedException e) {
            Log.e(APPLICATION_TAG, "stopSpoofing: InterruptedException");
        }
    }

    @Background(serial = "start")
    public void startListening() {
        if (DEBUG) Log.d(APPLICATION_TAG, "startListening");
        try {
            Thread.sleep(500);
            ListenService_.intent(getApplication()).startListenService().start();
            sListening = true;
        } catch (InterruptedException e) {
            if (DEBUG) Log.e(APPLICATION_TAG, "startListening - InterruptedException");
        }
    }

    @Background(serial = "stopWorking")
    public void stopListening() {
        if (DEBUG) Log.d(APPLICATION_TAG, "stopListening");
        try {
            sListening = false;
            ListenService_.intent(getApplication()).stop();
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Log.e(APPLICATION_TAG, "stopListening: InterruptedException");
        }
    }

    @Background
    public void refresh() {
        if (DEBUG) Log.d(APPLICATION_TAG, "refresh");
        if (!sNoTitle) {
            refreshStatus();
            if (!mFab.isShown()) mFab.show();
        }
        if (!sListening) mNotificationManager.cancelAll();
        updateNetworkSettings();
    }

    @UiThread
    public void refreshStatus() {
        if (DEBUG) Log.d(APPLICATION_TAG, "refreshStatus");
        int sessionsSize = mSessionAdapter.getItemCount();
        if (sListening && !sSpoofing) mToolbar.setTitle(mTitleListening);
        else if (sListening)          mToolbar.setTitle(mTitleWorking);
        else if (sSpoofing)           mToolbar.setTitle(mTitleSpoofing);
        else                          mToolbar.setTitle(mTitleIdle);
        mToolbar.setSubtitle(mSubtitleSessions + " " + String.valueOf(sessionsSize));
    }

    @UiThread
    public void updateNetworkSettings() {
        if (DEBUG) Log.d(APPLICATION_TAG, "updateNetworkSettings");
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        if (!mWifiManager.isWifiEnabled())   mTvNetworkName.setText(mNotConnected);
        else if (wifiInfo.getSSID() == null) mTvNetworkName.setText(mNoSsid);
        else                                 mTvNetworkName.setText(wifiInfo.getSSID());

        if (sSpoofing) mTvSpoofingIP.setText(mGatewayIP);
        else           mTvSpoofingIP.setText(mNotSpoofing);
    }

    @Background
    public void notifyUser() {
        if (DEBUG) Log.d(APPLICATION_TAG, "notifyUser");

        Intent notificationIntent = new Intent(this, MainActivity_.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle(mNotificationCaptured);
        builder.setContentText (mNotificationText);

        if (preferences.isVibration().get())
            builder.setVibrate(new long[] { 100, 100, 1000 });

        if (preferences.isLights().get())
            builder.setLights(Color.rgb(126, 72, 16), 3000, 3000);

        if (preferences.isSounds().get()) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(uri);
        }

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.cookies);
        builder.setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Receiver(actions = WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
    protected void wifiStateChangedAction(Intent intent) {
        if (DEBUG) Log.d(APPLICATION_TAG, "wifiStateChangedAction");
        // Update network settings anyway
        updateNetworkSettings();

        // Offers to reconnect and turns off all services
        if (!sListening) return;
        mNotificationHelper.askToReconnect();
        stopWorking();
    }

    @UiThread
    protected void startWorking() {
        if (DEBUG) Log.d(APPLICATION_TAG, "startWorking");
        mToolbar.setTitle(mTitleStarting);
        mFab.setImageDrawable(mStop);
        mFab.setEnabled(false);

        if (!mSystemHelper.checkSu()) {
            mNotificationHelper.showUnRooted();
            mFab.setImageDrawable(mPlay);
            mFab.setEnabled(true);
            return;
        } else
            mSystemHelper.checkLibraries();

        mRefreshHandler.startAutoRefresh();
        if (!sListening && sSpoofing) stopSpoofing();
        if (!sListening) {
            startSpoofing();
            startListening();
        }
        mFab.setEnabled(true);
    }

    @UiThread
    public void stopWorking() {
        if (DEBUG) Log.d(APPLICATION_TAG, "stopWorking");
        mToolbar.setTitle(mTitleStopping);
        mFab.setEnabled(false);
        mFab.setImageDrawable(mPlay);
        stopSpoofing();
        stopListening();
        if (sRooted) {
            mSystemHelper.executeCommand(CLEANUP_COMMAND_ARPSPOOF);
            mSystemHelper.executeCommand(CLEANUP_COMMAND_ARPOFFEINE);
        }
        mFab.setEnabled(true);
    }

}
