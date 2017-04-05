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

import android.support.v4.app.Fragment;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.res.StringRes;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.activity.MainActivity;
import doc.org.arpoffeine.util.Constants;

/**
 * Fragment class that shows Help section
 */

@EFragment(R.layout.fragment_help)
@SuppressWarnings("FieldCanBeLocal")
public class HelpFragment extends Fragment implements Constants {

    private final String TAG = "HelpFragment";

    @StringRes(R.string.nav_help)
    String title;

    private MainActivity mActivity;

    @AfterInject
    protected void afterInject() {
        if (DEBUG) Log.d(TAG, "afterInject");
        mActivity = (MainActivity) getActivity();
        mActivity.getMFab().hide();
        mActivity.getMToolbar().setTitle(title);
        mActivity.getMToolbar().setSubtitle(null);
        MainActivity.sNoTitle = true;
        MainActivity.sHelp = true;
        mActivity.getMToolbar().getMenu().clear();
    }

    @Override
    public void onDestroy() {
        MainActivity.sNoTitle = false;
        MainActivity.sHelp = false;
        mActivity.getMFab().show();
        mActivity.getMToolbar().inflateMenu(R.menu.main);
        super.onDestroy();
    }
}
