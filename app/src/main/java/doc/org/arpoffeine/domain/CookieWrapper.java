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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;
import doc.org.arpoffeine.util.Constants;
import lombok.Getter;
import lombok.Setter;

/**
 * Class that holds information about cookies
 */

public class CookieWrapper implements Serializable, Constants {

    private static final long serialVersionUID = 1L;

    @Getter @Setter
    private Cookie mCookie;
    @Getter @Setter
    private String mUrl;

    public CookieWrapper(Cookie cookie, String url){
        this.mCookie = cookie;
        this.mUrl = url;
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(mCookie.getDomain());
        outputStream.writeObject(mCookie.getName());
        outputStream.writeObject(mCookie.getPath());
        outputStream.writeObject(mCookie.getValue());
    }

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        String domain = (String) inputStream.readObject();
        String name   = (String) inputStream.readObject();
        String path   = (String) inputStream.readObject();
        String value  = (String) inputStream.readObject();

        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setVersion(0);
        this.mCookie = cookie;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CookieWrapper)) return false;
        CookieWrapper cookie = (CookieWrapper) o;
        return  mUrl.equals(cookie.mUrl) &&
                getMCookie().getName()  .equals(cookie.getMCookie().getName())  &&
                getMCookie().getValue() .equals(cookie.getMCookie().getValue()) &&
                getMCookie().getPath()  .equals(cookie.getMCookie().getPath())  &&
                getMCookie().getDomain().equals(cookie.getMCookie().getDomain());
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (mUrl != null) result += mUrl.hashCode();
        result += getMCookie().getName()  .hashCode();
        result += getMCookie().getPath()  .hashCode();
        result += getMCookie().getValue() .hashCode();
        result += getMCookie().getDomain().hashCode();
        return result;
    }
}
