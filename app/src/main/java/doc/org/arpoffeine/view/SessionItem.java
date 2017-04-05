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
package doc.org.arpoffeine.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DrawableRes;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.adapter.BindableLinearLayout;
import doc.org.arpoffeine.adapter.SessionAdapter;
import doc.org.arpoffeine.domain.Session;
import doc.org.arpoffeine.fragment.WebViewFragment;
import doc.org.arpoffeine.helper.ConfigHelper;
import doc.org.arpoffeine.helper.ExportHelper;
import doc.org.arpoffeine.helper.NotificationHelper;
import doc.org.arpoffeine.helper.SystemHelper;
import doc.org.arpoffeine.util.Constants;
import doc.org.arpoffeine.util.ISharedPreferences_;

/**
 * A view that displays a session item.
 */

@EViewGroup(R.layout.session_item)
public class SessionItem extends BindableLinearLayout<Session> implements Constants, PopupMenu.OnMenuItemClickListener {

    @StringRes(R.string.session_item_type_filtered)                protected String      mItemFiltered;
    @StringRes(R.string.session_item_type_generic)               protected String      mItemGeneric;

    @ViewById(R.id.session_item_image)  protected ImageView   mItemImage;
    @ViewById(R.id.session_item_saved)  protected ImageView   mItemSaved;
    @ViewById(R.id.session_item_domain) protected TextView    mItemDomain;
    @ViewById(R.id.session_item_type)   protected TextView    mItemType;
    @ViewById(R.id.session_item_ip)     protected TextView    mItemIp;
    @ViewById(R.id.session_item_id)     protected TextView    mItemId;
    @ViewById(R.id.session_item_menu)   protected ImageButton mItemMenu;

    @DrawableRes(R.drawable.stackoverflow) protected Drawable mStackoverflowIcon;
    @DrawableRes(R.drawable.facebook)      protected Drawable mFacebookIcon;
    @DrawableRes(R.drawable.linkedin)      protected Drawable mLinkedInIcon;
    @DrawableRes(R.drawable.twitter)       protected Drawable mTwitterIcon;
    @DrawableRes(R.drawable.youtube)       protected Drawable mYoutubeIcon;
    @DrawableRes(R.drawable.amazon)        protected Drawable mAmazonIcon;
    @DrawableRes(R.drawable.flickr)        protected Drawable mFlickrIcon;
    @DrawableRes(R.drawable.google)        protected Drawable mGoogleIcon;
    @DrawableRes(R.drawable.reddit)        protected Drawable mRedditIcon;
    @DrawableRes(R.drawable.cookie)        protected Drawable mCookieIcon;
    @DrawableRes(R.drawable.pikabu)        protected Drawable mPikabuIcon;
    @DrawableRes(R.drawable.steam)         protected Drawable mSteamIcon;
    @DrawableRes(R.drawable.ebay)          protected Drawable mEbayIcon;
    @DrawableRes(R.drawable.vk)            protected Drawable mVkIcon;
    @DrawableRes(R.drawable.ok)            protected Drawable mOkIcon;

    @Bean protected ExportHelper       mExportHelper;
    @Bean protected ConfigHelper       mConfigHelper;
    @Bean protected SystemHelper       mSystemHelper;
    @Bean protected SessionAdapter     mSessionAdapter;
    @Bean protected NotificationHelper mNotificationHelper;

    @SystemService
    protected ConnectivityManager mConnManager;

    @Pref
    protected ISharedPreferences_ mPreferences;

    protected MainActivity mActivity;
    protected int mPosition;

    public SessionItem(Context context) {
        super(context);
        mActivity = (MainActivity) context;
    }

    @Override
    public void bind(Session session, int position) {
        this.mPosition = position;
        mItemDomain.setText(session.getMUrl());
        mItemIp.setText(session.getMIp());
        mItemId.setText(String.valueOf(session.hashCode()));

        if (session.isMSaved())
             mItemSaved.setVisibility(VISIBLE);
        else mItemSaved.setVisibility(INVISIBLE);

        if (session.isMType())
             mItemType.setText(mItemGeneric);
        else mItemType.setText(mItemFiltered);

        // TODO: Make them depend on filter.json
        if      (session.getMUrl().contains("amazon"))        mItemImage.setImageDrawable(mAmazonIcon);
        else if (session.getMUrl().contains("ebay"))          mItemImage.setImageDrawable(mEbayIcon);
        else if (session.getMUrl().contains("facebook"))      mItemImage.setImageDrawable(mFacebookIcon);
        else if (session.getMUrl().contains("flickr"))        mItemImage.setImageDrawable(mFlickrIcon);
        else if (session.getMUrl().contains("google"))        mItemImage.setImageDrawable(mGoogleIcon);
        else if (session.getMUrl().contains("linkedin"))      mItemImage.setImageDrawable(mLinkedInIcon);
        else if (session.getMUrl().contains("twitter"))       mItemImage.setImageDrawable(mTwitterIcon);
        else if (session.getMUrl().contains("youtube"))       mItemImage.setImageDrawable(mYoutubeIcon);
        else if (session.getMUrl().contains("reddit"))        mItemImage.setImageDrawable(mRedditIcon);
        else if (session.getMUrl().contains("vk.com"))        mItemImage.setImageDrawable(mVkIcon);
        else if (session.getMUrl().contains("ok.ru"))         mItemImage.setImageDrawable(mOkIcon);
        else if (session.getMUrl().contains("odnoklassniki")) mItemImage.setImageDrawable(mOkIcon);
        else if (session.getMUrl().contains("pikabu"))        mItemImage.setImageDrawable(mPikabuIcon);
        else if (session.getMUrl().contains("steam"))         mItemImage.setImageDrawable(mSteamIcon);
        else if (session.getMUrl().contains("stackoverflow")) mItemImage.setImageDrawable(mStackoverflowIcon);
        else mItemImage.setImageDrawable(mCookieIcon);
    }

    @Override
    // TODO: Make it use AA or move to another class
    public boolean onMenuItemClick(MenuItem item) {
        Session session = mSessionAdapter.getItem(mPosition);

        switch (item.getItemId()){
            case ID_NORMAL:
                if (mConnManager.getActiveNetworkInfo() == null || !mConnManager.getActiveNetworkInfo().isConnected()) {
                    mNotificationHelper.notifyNoConnection(false);
                    break;
                }

                FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
                transaction.addToBackStack("webViewFragment");

                WebViewFragment webViewFragment = mActivity.getMWebViewFragment();

                Bundle bundle = new Bundle();
                bundle.putSerializable(BUNDLE_KEY_SESSION, session);
                webViewFragment.setArguments(bundle);

                transaction.replace(R.id.main_frame_container, webViewFragment).commit();
                break;

            case ID_SAVE:
                mSystemHelper.saveSessionToFile(session);
                mSessionAdapter.notifyItemRangeChanged(0, mSessionAdapter.getItemCount());
                mNotificationHelper.notifyCookieSaved();
                break;

            case ID_DELETE:
                mSystemHelper.deleteSessionFile(session);
                mSessionAdapter.notifyItemRangeChanged(0, mSessionAdapter.getItemCount());
                mNotificationHelper.notifyCookieRemoved();
                break;

            case ID_BLACKLIST:
                mConfigHelper.addToBL(session);
                mNotificationHelper.notifyAddedToBL(session.getMName());
                break;

            case ID_EXPORT:
                mExportHelper.exportSession(session);
                break;
        }
        return false;
    }

    // TODO: Move to another class
    @Click(R.id.session_item_menu)
    void sessionItemMenuClick(){
        Session session = mSessionAdapter.getItem(mPosition);

        // It's necessary to override Popup menu manually because of the "Saved" condition
        PopupMenu popupMenu = new PopupMenu(mActivity, mItemMenu);
        popupMenu.getMenu()     .add(0, ID_NORMAL,    1, R.string.menu_open_normal);
        if (session.isMSaved())
            popupMenu.getMenu() .add(0, ID_DELETE,    2, R.string.menu_remove);
        else
            popupMenu.getMenu() .add(0, ID_SAVE,      3, R.string.menu_save);
        popupMenu.getMenu()     .add(0, ID_BLACKLIST, 4, R.string.menu_black_list);
        popupMenu.getMenu()     .add(0, ID_EXPORT,    5, R.string.menu_export);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }
}
