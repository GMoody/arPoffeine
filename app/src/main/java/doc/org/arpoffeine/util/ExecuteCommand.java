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
package doc.org.arpoffeine.util;

import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import doc.org.arpoffeine.helper.ConfigHelper;
import lombok.NoArgsConstructor;

/**
 * Class with purpose to run and interrupt input and output streams
 */

@EBean
@NoArgsConstructor
public class ExecuteCommand extends Thread implements Constants {

    private static final String TAG = "ExecuteCommand";

    @RootContext
    protected Context mContext;

    @Bean
    protected ConfigHelper mConfigHelper;

    private boolean          mListen;
    private String           mCommand;
    private Process          mProcess;
    private BufferedReader   mReader;
    private BufferedReader   mErrorReader;
    private DataOutputStream mOutputStream;

    public void executeCommand(String command, boolean listen) throws IOException {
        this.mCommand = command;
        this.mListen = listen;
        this.mProcess = Runtime.getRuntime().exec("su");
        this.mOutputStream = new DataOutputStream(mProcess.getOutputStream());
        this.mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
        this.mErrorReader = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
    }

    public void run() {
        try {
            if (DEBUG) Log.d(TAG, " Executing command: " + mCommand + "\n");

            mOutputStream.writeBytes(mCommand + '\n');
            mOutputStream.flush();
            StreamGobbler errorGobbler  = new StreamGobbler(mErrorReader);
            StreamGobbler stdOutGobbler = new StreamGobbler(mReader);
            errorGobbler. setDaemon(true);
            stdOutGobbler.setDaemon(true);
            errorGobbler. start();
            stdOutGobbler.start();
            mOutputStream.writeBytes("exit\n");
            mOutputStream.flush();

            // Ensure the interruption of the thread
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    mProcess.exitValue();
                    Thread.currentThread().interrupt();
                } catch (IllegalThreadStateException e) {
                    Thread.sleep(250);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error running commands", e);
        } catch (InterruptedException e) {
            try {
                mReader.      close();
                mErrorReader. close();
                mOutputStream.close();
            } catch (IOException ex) {
                Log.e(TAG, "IOException", ex);
            } finally {
                mProcess.destroy();
            }
        } finally {
            mProcess.destroy();
        }
    }

    private class StreamGobbler extends Thread {

        private BufferedReader mBufferedReader;

        private StreamGobbler(BufferedReader br) {
            mBufferedReader = br;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            try {
                while (true) {
                    String line;
                    if (mBufferedReader.ready())
                        line = mBufferedReader.readLine();
                    else {
                        Thread.sleep(200);
                        continue;
                    }

                    if (DEBUG) Log.d(TAG, "Command: " + mCommand + "\tline:\t" + line + "\n");
                    if (mListen) mConfigHelper.process(line);
                }
            } catch (IOException ex) {
                if (DEBUG) Log.w(TAG, "StreamGobbler stream closed");
            } catch (InterruptedException e) {
                Log.e(TAG, "StreamGobbler: " + e);
            } finally {
                try {
                    mBufferedReader.close();
                } catch (IOException e) {
                    if (DEBUG) Log.w(TAG, "StreamGobbler: ", e);
                }
            }
        }
    }
}
