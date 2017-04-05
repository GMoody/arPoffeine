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
import android.content.Intent;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.domain.CookieWrapper;
import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.util.Constants;
import lombok.NoArgsConstructor;

/**
 * Class that deals with the export
 */

@NoArgsConstructor
@EBean(scope = EBean.Scope.Singleton)
@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class ExportHelper implements Constants {

    private final String TAG = "ExportHelper";

    @RootContext
    protected Context mContext;

    @StringRes(R.string.title_export)
    protected String mTitleExport;
    @StringRes(R.string.title_action)
    protected String mTitleAction;

    @UiThread
    public void exportSession(Session session) {
        if (DEBUG) Log.d(TAG, "exportSession");

        try {
            // Creates main JSON session object and puts inside parameters
            JSONObject sessionJSON = new JSONObject();
            sessionJSON.put("ID",      session.getMId());
            sessionJSON.put("HC",      session.hashCode());
            sessionJSON.put("IP",      session.getMIp());
            sessionJSON.put("Domain",  session.getMName());

            // Creates mCookies JSON array and puts mCookies inside in the loop
            JSONArray cookies = new JSONArray();
            for (CookieWrapper cw : session.getMCookies()) {
                JSONObject cookie = new JSONObject();
                cookie.put(cw.getMCookie().getName(), cw.getMCookie().getValue());
                cookies.put(cookie);
            }
            // Puts mCookies JSON array inside JSON session
            sessionJSON.put("cookies", cookies);

            // Creates "Send" intent, puts header and session inside
            Intent exportIntent = new Intent(android.content.Intent.ACTION_SEND);
            exportIntent.setType("plain/text");
            String title = APPLICATION_TAG + " " + mTitleExport;
            exportIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
            exportIntent.putExtra(Intent.EXTRA_TEXT, sessionJSON.toString(4));

            // Creates and launches "Choose application" intent
            Intent intent = Intent.createChooser(exportIntent, mTitleAction);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } catch (JSONException e) {
            Log.e(TAG, "Error with creating JSON");
        }
    }
}
