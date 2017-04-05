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
package doc.org.arpoffeine.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import doc.org.arpoffeine.util.Constants;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds information about session with cookies
 */

public class Session implements Serializable, Constants {

    private static final long serialVersionUID = 1L;

    @Getter @Setter
    private UUID mId;
    @Getter @Setter
    private String mName;
    @Getter @Setter
    private String mUrl;
    @Getter @Setter
    private String mIp;
    @Getter @Setter
    private boolean mSaved;
    @Getter @Setter
    private boolean mType;
    @Getter @Setter
    private ArrayList<CookieWrapper> mCookies;

    public Session(ArrayList<CookieWrapper> cookies, String url, String name, String ip, boolean type) {
        this.mId = UUID.randomUUID();
        this.mCookies = cookies;
        this.mUrl = url;
        this.mIp = ip;
        this.mName = name;
        this.mType = type;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (mType) result += 1;
        if (mSaved) result += 2;
        result += mId.hashCode();
        result += mName.hashCode();
        result += mUrl.hashCode();
        result += mIp.hashCode();
        result += getCookiesHashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Session)) return false;
        Session session = (Session) o;
        return  mType == session.mType &&
                mIp.equals(session.mIp) &&
                mSaved == session.mSaved &&
                mUrl.equals(session.mUrl) &&
                mName.equals(session.mName) &&
                getCookiesHashCode() == session.getCookiesHashCode();
    }

    private int getCookiesHashCode() {
        int cookiesHashCode = 0;
        for (CookieWrapper cookie : this.getMCookies())
            cookiesHashCode += cookie.hashCode();
        return cookiesHashCode;
    }
}
