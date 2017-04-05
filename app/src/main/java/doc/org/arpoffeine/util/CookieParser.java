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

import android.util.Log;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.ormlite.annotations.OrmLiteDao;

import java.sql.SQLException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import doc.org.arpoffeine.domain.CookieWrapper;
import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.domain.db.CookieBlackList;
import doc.org.arpoffeine.domain.db.DomainBlackList;
import doc.org.arpoffeine.helper.ConfigHelper;
import doc.org.arpoffeine.helper.DatabaseHelper;
import lombok.NoArgsConstructor;

/**
 * Class with purpose to parse session source string
 */

@EBean
@NoArgsConstructor
@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class CookieParser implements ICookiesParser {

    private final String TAG = "CookieParser";

    @OrmLiteDao(helper = DatabaseHelper.class)
    protected Dao<DomainBlackList, Long> mDomainBlackListDao;

    @OrmLiteDao(helper = DatabaseHelper.class)
    protected Dao<CookieBlackList, Long> mCookieBlackListDao;

    @Bean
    protected ConfigHelper mConfigHelper;

    @Pref
    protected ISharedPreferences_ mPreferences;

    @Override
    public Session parseCookies(String sourceString) {
        try {
            // Splits source string into string array
            String[] splittedSS = sourceString.split("\\|\\|\\|");
            if (splittedSS.length < 3) return null;

            // Extracts host from splitted source string
            String host = splittedSS[1].replaceAll("Host=", "").replaceAll("www.", "").toLowerCase().trim();
            if (host.isEmpty()) return null;

            // Checks if domain is in black list
            for (DomainBlackList dbl : mDomainBlackListDao.queryForAll())
                if (host.contains(dbl.getDomain())) return null;

            // Checks if Generic mode is checked or it should filter by domain
            if (!mPreferences.isGeneric().get()) {
                boolean filtered = false;
                for (String domain : mPreferences.filter().get())
                    if (host.contains(domain)) {
                        filtered = true;
                        break;
                    }
                if (!filtered) return null;
            }

            // Extracts url from host
            String url;
            if (!host.startsWith("http://")) url = "http://" + host;
            else url = host;

            ArrayList<CookieWrapper> cookieList = new ArrayList<>();

            // Splits cookies into string array
            String[] cookies = splittedSS[0].split(";");
            for (String cookieString : cookies) {

                // Handles cookies format
                String[] values = cookieString.split("=");
                if (cookieString.endsWith("="))
                    values[values.length - 1] = values[values.length - 1] + "=";
                values[0] = values[0].replaceAll("Cookie:", "").trim();

                // Checks if cookie is in the black list
                if (mCookieBlackListDao.queryForEq("cookie", values[0]).size() != 0) continue;

                // Handles cookies format
                String val = "";
                for (int i = 1; i < values.length; i++) {
                    if (i > 1) val += "=";
                    val += values[i];
                }

                // Creates cookie and sets up all attributes
                BasicClientCookie cookie = new BasicClientCookie(values[0], val);
                cookie.setDomain(host);
                cookie.setPath("/");
                cookie.setVersion(0);
                cookieList.add(new CookieWrapper(cookie, url));
            }

            // Extracts IP from source string
            String IP = splittedSS[2].replace("IP", "").replace("=", "");

            // Sets up the type of the session
            boolean type = mPreferences.isGeneric().get();
            return new Session(cookieList, url, host, IP, type);
        } catch (SQLException e) {
            Log.e(TAG, "parseCookies SQLException", e);
            return null;
        }
    }
}
