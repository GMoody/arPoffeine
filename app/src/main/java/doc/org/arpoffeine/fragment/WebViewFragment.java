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
package doc.org.arpoffeine.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import cz.msebera.android.httpclient.cookie.Cookie;
import doc.org.arpoffeine.R;
import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.domain.CookieWrapper;
import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.util.Constants;
import lombok.Getter;

/**
 * Fragment class that sets up cookies into sessions and displays in the WebView
 */

@EFragment(R.layout.fragment_webview)
@SuppressWarnings({"deprecation", "SetJavaScriptEnabled"})
public class WebViewFragment extends Fragment implements Constants {

    private static final String TAG = "WebViewFragment";

    @Getter @ViewById(R.id.webVew)
    protected WebView mWebView;

    @Getter private Session      mSession;
    @Getter private String       mUrl;
            private MainActivity mActivity;

    @AfterViews
    protected void afterViews() {
        setupWebView();
        setupCookies();
        mUrl = mSession.getMUrl();
        mWebView.loadUrl(mSession.getMUrl());
    }

    @AfterInject
    protected void afterInject() {
        if (DEBUG) Log.d(TAG, "afterInject");

        mActivity = (MainActivity) getActivity();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            CookieSyncManager.createInstance(getActivity());

        mSession = (Session) getArguments().getSerializable(BUNDLE_KEY_SESSION);
        if (mSession == null) {
            getActivity().getSupportFragmentManager().popBackStackImmediate();
            return;
        }

        mActivity.getMToolbar().getMenu().clear();
        mActivity.getMToolbar().inflateMenu(R.menu.webview_menu);
        mActivity.getMToolbar().setTitle(mSession.getMName());
        mActivity.getMToolbar().setSubtitle(mSession.getMUrl());
        mActivity.getMFab().hide();

        MainActivity.sNoTitle = true;
    }

    @Background
    protected void setupCookies() {
        Log.i(TAG, "setupCookies");
        CookieManager manager = CookieManager.getInstance();

        if (manager.hasCookies()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                 manager.removeAllCookies(null);
            else manager.removeAllCookie();
        }

        for (CookieWrapper cookieWrapper : mSession.getMCookies()) {
            Cookie cookie = cookieWrapper.getMCookie();
            String cookieString = cookie.getName() + "=" + cookie.getValue()  +
                                             "; Domain=" + cookie.getDomain() +
                                               "; Path=" + cookie.getPath();
            manager.setCookie(cookie.getDomain(), cookieString);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
             manager.flush();
        else CookieSyncManager.getInstance().sync();
    }

    @UiThread
    protected void setupWebView() {
        mWebView.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setUserAgentString("foo");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mActivity.getMToolbar().setSubtitle(url);
            mUrl = url;
            view.loadUrl(url);
            return true;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            mActivity.getMToolbar().setSubtitle(request.getUrl().toString());
            mUrl = request.getUrl().toString();
            return super.shouldOverrideUrlLoading(view, request);
        }
    }
}
