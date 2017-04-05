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
import android.os.PowerManager;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.SystemService;

import java.io.IOException;

import doc.org.arpoffeine.helper.SystemHelper;
import doc.org.arpoffeine.util.Constants;
import doc.org.arpoffeine.util.ExecuteCommand;

/**
 * Service class with purpose to run listening library
 */

@EIntentService
@SuppressWarnings("FieldCanBeLocal")
public class ListenService extends IntentService implements Constants {

    private static final String TAG = "ListenService";

    @SystemService protected PowerManager   mPowerManager;
    @SystemService protected WifiManager    mWifiManager;
    @Bean          protected SystemHelper   mSystemHelper;
    @Bean          protected ExecuteCommand mExecuteCommand;

    private static volatile WifiManager.WifiLock  sWifiLock;
    private static volatile PowerManager.WakeLock sWakeLock;
    private volatile Thread mThread;

    @ServiceAction
    void startListenService() {
        if (DEBUG) Log.d(TAG, "startListenService");
        mSystemHelper.executeCommand("chmod 777 " + mSystemHelper.getBinaryPath(true, false));
        sWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "sWifiLock");
        sWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sWakeLock");
        sWifiLock.acquire();
        sWakeLock.acquire();

        try {
            mThread = mExecuteCommand;
            mExecuteCommand.executeCommand(mSystemHelper.getBinaryPath(true, false), true);
            mThread.setDaemon(true);
            mThread.start();
            mThread.join();
        } catch (IOException e) {
            Log.e(TAG, "error initializing command", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "was interrupted", e);
        } finally {
            Log.d(TAG, "finally");
            if (mThread != null)  mThread = null;
            if (sWifiLock.isHeld()) sWifiLock.release();
            if (sWakeLock.isHeld()) sWakeLock.release();
            stopForeground(true);
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
        mSystemHelper.executeCommand(CLEANUP_COMMAND_ARPOFFEINE);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        // AA requires empty method
    }

    public ListenService() {
        super(TAG);
    }
}
