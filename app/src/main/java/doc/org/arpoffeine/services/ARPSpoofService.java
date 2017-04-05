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
package doc.org.arpoffeine.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.SystemService;

import java.io.IOException;

import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.helper.SystemHelper;
import doc.org.arpoffeine.util.Constants;
import doc.org.arpoffeine.util.ExecuteCommand;

/**
 * Service class with purpose to run spoofing library and set up the firewall
 */

@EIntentService
@SuppressWarnings("FieldCanBeLocal")
public class ARPSpoofService extends IntentService implements Constants {

    private static final String TAG = "ARPSpoofService";

    // Commands to stop firewall and allow everything
    private final String IPTABLES_CLEAR_FX =          "iptables -F && iptables -X";
    private final String IPTABLES_CLEAR_NAT_FX =      "iptables -t nat -F && iptables -t nat -X";
    private final String IPTABLES_MANGLE_CLEAR_FX =   "iptables -t mangle -F && iptables -t mangle -X";
    private final String IPTABLES_ACCEPT_FORWARDING = "iptables -P INPUT ACCEPT && iptables -P FORWARD ACCEPT && iptables -P OUTPUT ACCEPT";
    private final String IPV4_FILEPATH =              "echo '1' > /proc/sys/net/ipv4/ip_forward";
    private final String IPV6_FILEPATH =              "echo '1' > /proc/sys/net/ipv6/conf/all/forwarding";
    private final String IPTABLES_POSTROUTING =       "iptables -t nat -I POSTROUTING -s 0/0 -j MASQUERADE";

    @SystemService protected PowerManager   mPowerManager;
    @SystemService protected WifiManager    mWifiManager;
    @Bean          protected SystemHelper   mSystemHelper;
    @Bean          protected ExecuteCommand mExecuteCommand;

    private static volatile WifiManager.WifiLock  sWifiLock;
    private static volatile PowerManager.WakeLock sWakeLock;
    private volatile Thread mThread;

    @ServiceAction
    void startSpoofingService(Bundle bundle) {
        if (DEBUG) Log.d(TAG, "startSpoofingService");
        String localBin      = bundle.getString("localBin");
        String gateway       = bundle.getString("gateway");
        String wifiInterface = bundle.getString("interface");
        String command = localBin + " -i " + wifiInterface + " " + gateway;

        mSystemHelper.executeCommand(IPTABLES_CLEAR_FX);
        mSystemHelper.executeCommand(IPTABLES_CLEAR_NAT_FX);
        mSystemHelper.executeCommand(IPTABLES_MANGLE_CLEAR_FX);
        mSystemHelper.executeCommand(IPTABLES_ACCEPT_FORWARDING);
        mSystemHelper.executeCommand(IPV4_FILEPATH);
        mSystemHelper.executeCommand(IPV6_FILEPATH);
        mSystemHelper.executeCommand(IPTABLES_POSTROUTING);
        mSystemHelper.executeCommand("chmod 777 " + mSystemHelper.getBinaryPath(false, false));

        sWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "sWifiLock");
        sWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sWakeLock");
        sWifiLock.acquire();
        sWakeLock.acquire();

        try {
            mThread = mExecuteCommand;
            mExecuteCommand.executeCommand(command, false);
            mThread.setDaemon(true);
            mThread.start();
            mThread.join();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing ARPSpoof command", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "was interrupted", e);
        } finally {
            Log.d(TAG, "finally");
            if (mThread != null)  mThread = null;
            if (sWifiLock.isHeld()) sWifiLock.release();
            if (sWakeLock.isHeld()) sWakeLock.release();
            stopForeground(true);
            MainActivity.sSpoofing = false;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mThread != null) {
            Thread tmpThread = mThread;
            mThread = null;
            tmpThread.interrupt();
        }
        mSystemHelper.executeCommand(CLEANUP_COMMAND_ARPSPOOF);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // AA requires empty method
    }

    public ARPSpoofService() {
        super(TAG);
    }
}
