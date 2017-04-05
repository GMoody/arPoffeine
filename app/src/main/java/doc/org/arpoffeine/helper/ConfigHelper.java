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

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.ormlite.annotations.OrmLiteDao;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.TreeSet;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.adapter.SessionAdapter;
import doc.org.arpoffeine.util.CookieParser;
import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.domain.db.CookieBlackList;
import doc.org.arpoffeine.domain.db.DomainBlackList;
import doc.org.arpoffeine.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Class that deals with blacklist files and filter file
 */

@NoArgsConstructor
@EBean(scope = EBean.Scope.Singleton)
@SuppressWarnings({"WeakerAccess", "MismatchedQueryAndUpdateOfCollection", "FieldCanBeLocal"})
public class ConfigHelper implements Constants {

    private final String TAG           = "ConfigHelper";
    private final String DOMAIN_BL_KEY = "domain";
    private final String COOKIE_BL_KEY = "cookie";
    private final String FILTER_KEY    = "domain";

    @Bean
    protected SessionAdapter mSessionAdapter;

    @Getter
    private TreeSet<String>       mFilterList = new TreeSet<>();
    private InputStream           mInputStream;
    private JSONArray             mJsonArray;
    private MainActivity          mContext;
    private ByteArrayOutputStream mOutputStream;

    @OrmLiteDao(helper = DatabaseHelper.class)
    protected Dao<DomainBlackList, Long> mDomainBlackListDao;

    @OrmLiteDao(helper = DatabaseHelper.class)
    protected Dao<CookieBlackList, Long> mCookieBlackListDao;

    @Bean
    protected CookieParser mCookieParser;

    @Background(serial = "init")
    public void init(MainActivity activity) {
        this.mContext = activity;
        loadFilters();
        loadCookieBL();
        loadDomainBL();
    }

    @Background(serial = "init")
    public void loadDomainBL() {
        if (DEBUG) Log.d(TAG, "loadDomainBL");
        try {
            readFiles(0);
            DomainBlackList domainBlackList;
            mJsonArray = new JSONArray(mOutputStream.toString());
            for (int i = 0; i < mJsonArray.length(); i++) {
                String domain = mJsonArray.getJSONObject(i).getString(DOMAIN_BL_KEY);
                if (mDomainBlackListDao.queryForEq(DOMAIN_BL_KEY, domain).size() == 0) {
                    domainBlackList = new DomainBlackList();
                    domainBlackList.setDomain(domain);
                    mDomainBlackListDao.create(domainBlackList);
                }
            }
        } catch (JSONException | SQLException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Background(serial = "init")
    protected void loadCookieBL() {
        if (DEBUG) Log.d(TAG, "loadCookieBL");
        try {
            readFiles(1);
            CookieBlackList cookieBlackList;
            mJsonArray = new JSONArray(mOutputStream.toString());
            for (int i = 0; i < mJsonArray.length(); i++) {
                String cookie = mJsonArray.getJSONObject(i).getString(COOKIE_BL_KEY);
                if (mCookieBlackListDao.queryForEq(COOKIE_BL_KEY, cookie).size() == 0) {
                    cookieBlackList = new CookieBlackList();
                    cookieBlackList.setCookie(cookie);
                    mCookieBlackListDao.create(cookieBlackList);
                }
            }
        } catch (JSONException | SQLException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Background(serial = "init")
    protected void loadFilters() {
        if (DEBUG) Log.d(TAG, "loadFilters");
        try {
            readFiles(2);
            mJsonArray = new JSONArray(mOutputStream.toString());
            for (int i = 0; i < mJsonArray.length(); i++)
                mFilterList.add(mJsonArray.getJSONObject(i).getString(FILTER_KEY));
        } catch (JSONException | IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    protected void readFiles(int type) throws IOException {
        mOutputStream = new ByteArrayOutputStream();
        switch (type) {
            case 0:
                mInputStream = mContext.getResources().openRawResource(R.raw.domain_blacklist);
                break;
            case 1:
                mInputStream = mContext.getResources().openRawResource(R.raw.cookie_blacklist);
                break;
            case 2:
                mInputStream = mContext.getResources().openRawResource(R.raw.filter);
                break;
        }

        int ctr = mInputStream.read();
        while (ctr != -1) {
            mOutputStream.write(ctr);
            ctr = mInputStream.read();
        }
        mInputStream.close();
    }

    @UiThread
    public void process(String line) {
        if (DEBUG) Log.d(TAG, "process");
        Session session = mCookieParser.parseCookies(line);
        if (session != null) {
            if (!mSessionAdapter.getMItemsToShow().contains(session))
                 mSessionAdapter.addItem(session);
            else {
                int pos = mSessionAdapter.getMItemsToStore().indexOf(session);
                if (!session.isMSaved())
                    mSessionAdapter.removeItem(session);
                mSessionAdapter.addItem(session, pos);
            }
            mContext.notifyUser();
        }
    }

    @Background
    public void addToBL(Session session) {
        if (DEBUG) Log.d(TAG, "addToBL");
        try {
            DomainBlackList domainBlackList = new DomainBlackList();
            domainBlackList.setDomain(session.getMName());
            mDomainBlackListDao.create(domainBlackList);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Background
    public void clearBL() {
        if (DEBUG) Log.d(TAG, "clearBL");
        try {
            mDomainBlackListDao.delete(mDomainBlackListDao.queryForAll());
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
