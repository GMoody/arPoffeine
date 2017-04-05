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

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.adapter.SessionAdapter;
import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.util.Constants;
import lombok.NoArgsConstructor;

/**
 * Class that deals with system files
 */

@NoArgsConstructor
@EBean(scope = EBean.Scope.Singleton)
@SuppressWarnings({"ResultOfMethodCallIgnored", "deprecation", "FieldCanBeLocal"})
public class SystemHelper implements Constants {

    private final String TAG = "SystemHelper";

    private final String ARPOFFEINE_ARM64_V8A   = "arpoffeine_arm64_v8a";
    private final String ARPOFFEINE_ARMEABI_V7A = "arpoffeine_armeabi_v7a";
    private final String ARPOFFEINE_ARMEABI     = "arpoffeine_armeabi";
    private final String ARPOFFEINE_MIPS64      = "arpoffeine_mips64";
    private final String ARPOFFEINE_MIPS        = "arpoffeine_mips";
    private final String ARPOFFEINE_x86         = "arpoffeine_x86";
    private final String ARPOFFEINE_x86_64      = "arpoffeine_x86_64";

    private final String ARPSPOOF_ARM64_V8A     = "arpspoof_arm64_v8a";
    private final String ARPSPOOF_ARMEABI_V7A   = "arpspoof_armeabi_v7a";
    private final String ARPSPOOF_ARMEABI       = "arpspoof_armeabi";
    private final String ARPSPOOF_MIPS64        = "arpspoof_mips64";
    private final String ARPSPOOF_MIPS          = "arpspoof_mips";
    private final String ARPSPOOF_x86           = "arpspoof_x86";
    private final String ARPSPOOF_x86_64        = "arpspoof_x86_64";

    @Bean
    protected SessionAdapter mSessionAdapter;

    private File         mDirectory, mFile;
    private String       mSavedPath;
    private String       mSavedFile;
    private Process      mProcess;
    private MainActivity mContext;

    @Background
    public void init(MainActivity activity) {
        this.mContext = activity;
        mSavedPath = activity.getFilesDir() + File.separator + "mSaved";
        mSavedFile = mSavedPath + File.separator + "[";
    }

    @Background(serial = "executeCommand")
    public void executeCommand(String command) {
        try {
            // Starts new SU session
            if (mProcess == null || mProcess.getOutputStream() == null)
                mProcess = new ProcessBuilder().command("su").start();
            if (DEBUG) Log.d(TAG, "Command: " + command + "\n");

            // Writes command and executes
            mProcess.getOutputStream().write((command + "\n").getBytes("ASCII"));
            mProcess.getOutputStream().flush();

            // Writes output from command if Debug
            if (DEBUG) {

                // Receives error stream from mProcess
                StringBuffer sb = new StringBuffer();
                BufferedReader errorStream = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
                Thread.sleep(10);

                // Writes error from command if it exists
                while (errorStream.ready())
                    sb.append(errorStream.readLine());
                String s = sb.toString().trim();
                if (!s.equalsIgnoreCase(""))
                    if (DEBUG) Log.d(TAG, "Error with command: " + command + ": " + s + "\n");

                // Receives input stream from mProcess
                sb = new StringBuffer();
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
                Thread.sleep(10);

                // Writes output from command
                while (inputStream.ready())
                    sb.append(inputStream.readLine());
                s = sb.toString().trim();
                if (!s.equalsIgnoreCase(""))
                    if (DEBUG) Log.d(TAG, "Output from command: " + command + ": " + s + "\n");
            }
            Thread.sleep(100);
        } catch (Exception e) {
            Log.e(TAG, "Error executing: " + command, e);
        }
    }

    @UiThread
    public void readSessionFiles() {
        if (DEBUG) Log.d(TAG, "readSessionFiles");

        mDirectory = new File(mSavedPath);
        if (!mDirectory.exists() || !mDirectory.isDirectory()) {
            mDirectory.mkdirs();
            return;
        } else if (mDirectory.listFiles().length == 0) return;

        for (File mFile : mDirectory.listFiles()) {
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(new FileInputStream(mFile));
                Session session = (Session) in.readObject();
                in.close();
                session.setMSaved(true);

                if (!mSessionAdapter.getMItemsToShow().contains(session))
                     mSessionAdapter.addItem(session, 0);
            } catch (Exception e) {
                Log.e(TAG, "Error with deserialization!");
            }
        }
    }

    @Background
    public void saveSessionToFile(Session session) {
        if (DEBUG) Log.d(TAG, "saveSessionToFile");

        // Creates directory if it does not exist
        mDirectory = new File(mSavedPath);
        if (!mDirectory.exists()) mDirectory.mkdirs();

        // Marks session as mSaved before creating the file in order to generate new hashcode
        session.setMSaved(true);

        // Removes old session file if exists and creates new one
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mFile = new File(mSavedFile + session.getMId() + "]");
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(session);
            out.close();

            if (mFile.exists()) mFile.delete();
            mFile.createNewFile();
            bos.writeTo(new FileOutputStream(mFile.getAbsolutePath()));
        } catch (IOException ioe) {
            Log.e(TAG, "Error with saving session to file");
        }
    }

    @Background
    public void deleteSessionFile(Session session) {
        if (DEBUG) Log.d(TAG, "deleteSessionFile");

        // Removes mSaved Auth from directory
        if (session == null) return;
        mFile = new File(mSavedFile + session.getMId() + "]");
        if (mFile.exists())
            // Just in case trying to remove file 5 times in the row
            for (int i = 0; i < 5; i++)
                if (mFile.delete()) break;

        // Marks Auth as "not mSaved"
        session.setMSaved(false);
    }

    @Background
    public void deleteSessionFiles() {
        if (DEBUG) Log.d(TAG, "deleteSessionFiles");

        // Removes all mSaved files from the directory
        File[] sessions = new File(mSavedPath).listFiles();
        for (File session : sessions)
            if (!session.isDirectory())
                session.delete();

        // Makes saved sessions unsaved
        for (Session session : mSessionAdapter.getMItemsToShow())
            if (session.isMSaved()) session.setMSaved(false);
    }

    @Background
    public void checkLibraries() {
        if (DEBUG) Log.d(TAG, "checkLibraries");

        String abi = getDeviceAbi();
        InputStream inputStream = null;
        InputStream inARPSpoof  = null;

        switch (abi) {
            case "arm64-v8a":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_arm64_v8a);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_arm64_v8a);
                break;
            case "armeabi":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_armeabi);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_armeabi);
                break;
            case "armeabi-v7a":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_armeabi_v7a);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_armeabi_v7a);
                break;
            case "mips":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_mips);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_mips);
                break;
            case "mips64":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_mips64);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_mips64);
                break;
            case "x86":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_x86);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_x86);
                break;
            case "x86_64":
                inputStream = mContext.getResources().openRawResource(R.raw.arpoffeine_x86_64);
                inARPSpoof  = mContext.getResources().openRawResource(R.raw.arpspoof_x86_64);
                break;
        }

        FileOutputStream out;

        try {
            mFile = new File(getBinaryPath(true, false));
            if (mFile.exists()) mFile.delete();
            out = mContext.openFileOutput(getBinaryPath(true, true), Context.MODE_PRIVATE);
            byte[] bytes = new byte[64];
            while (inputStream.read(bytes) > -1) out.write(bytes);
            out.flush();
            out.close();
            executeCommand("chmod 777 " + getBinaryPath(true, false));

            mFile = new File(getBinaryPath(false, false));
            if (mFile.exists()) mFile.delete();
            out = mContext.openFileOutput(getBinaryPath(false, true), Context.MODE_PRIVATE);
            bytes = new byte[64];
            while (inARPSpoof.read(bytes) > -1) out.write(bytes);
            out.flush();
            out.close();
            executeCommand("chmod 777 " + getBinaryPath(false, false));
        } catch (Exception e) {
            Log.e(TAG, "checkLibraries: " + e);
        }
    }

    private String getDeviceAbi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
             return Build.CPU_ABI;
        else return Build.SUPPORTED_ABIS[0];
    }

    public String getBinaryPath(boolean flag, boolean fileName) {
        String abi = getDeviceAbi();
        String path = mContext.getFilesDir().getAbsolutePath() + File.separator;
        String file = null;

        switch (abi) {
            case "arm64-v8a":
                if (flag) {
                    path += ARPOFFEINE_ARM64_V8A;
                    file  = ARPOFFEINE_ARM64_V8A;
                } else {
                    path += ARPSPOOF_ARM64_V8A;
                    file  = ARPSPOOF_ARM64_V8A;
                }
                break;
            case "armeabi":
                if (flag) {
                    path += ARPOFFEINE_ARMEABI;
                    file  = ARPOFFEINE_ARMEABI;
                } else {
                    path += ARPSPOOF_ARMEABI;
                    file  = ARPSPOOF_ARMEABI;
                }
                break;
            case "armeabi-v7a":
                if (flag) {
                    path += ARPOFFEINE_ARMEABI_V7A;
                    file  = ARPOFFEINE_ARMEABI_V7A;
                } else {
                    path += ARPSPOOF_ARMEABI_V7A;
                    file  = ARPSPOOF_ARMEABI_V7A;
                }
                break;
            case "mips":
                if (flag) {
                    path += ARPOFFEINE_MIPS;
                    file  = ARPOFFEINE_MIPS;
                } else {
                    path += ARPSPOOF_MIPS;
                    file  = ARPSPOOF_MIPS;
                }
                break;
            case "mips64":
                if (flag) {
                    path += ARPOFFEINE_MIPS64;
                    file  = ARPOFFEINE_MIPS64;
                } else {
                    path += ARPSPOOF_MIPS64;
                    file  = ARPSPOOF_MIPS64;
                }
                break;
            case "x86":
                if (flag) {
                    path += ARPOFFEINE_x86;
                    file  = ARPOFFEINE_x86;
                } else {
                    path += ARPSPOOF_x86;
                    file  = ARPSPOOF_x86;
                }
                break;
            case "x86_64":
                if (flag) {
                    path += ARPOFFEINE_x86_64;
                    file  = ARPOFFEINE_x86_64;
                } else {
                    path += ARPSPOOF_x86_64;
                    file  = ARPSPOOF_x86_64;
                }
                break;
        }

        if (fileName) return file;
        else return path;
    }

    public boolean checkSu() {
        if (DEBUG) Log.d(TAG, "checkSu");

        boolean hasRoot       = false;
        Process process       = null;
        DataOutputStream dos = null;
        InputStreamReader isr = null;

        try {
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            isr = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(isr);

            dos.writeBytes("id" + "\n");
            dos.flush();

            dos.writeBytes("exit \n");
            dos.flush();

            String line = reader.readLine();
            while (line != null) {
                Set<String> ID = new HashSet<>(Arrays.asList(line.split(" ")));
                for (String id : ID) {
                    if (id.toLowerCase().contains("uid=0")) {
                        hasRoot = true;
                        break;
                    }
                }
                line = reader.readLine();
            }
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            Log.e(TAG, ERROR_CHECKING_SU, e);
        } finally {
            try {
                if (dos != null) dos.close();
                if (isr != null) isr.close();
            } catch (IOException e) {
                Log.e(TAG, ERROR_CHECKING_SU, e);
            } finally {
                if (process != null) process.destroy();
            }
        }
        return hasRoot;
    }
}
