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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import doc.org.arpoffeine.R;

import doc.org.arpoffeine.adapter.SessionAdapter;
import doc.org.arpoffeine.util.Constants;
import doc.org.arpoffeine.util.SimpleItemTouchHelperCallback;
import lombok.Getter;

/**
 * Fragment class that holds RecyclerView list with sessions
 */

@EFragment(R.layout.fragment_sessions_list)
public class SessionsFragment extends Fragment implements Constants {

    private static final String TAG = "SessionsFragment";

    @Getter @ViewById(R.id.sessionsRV)
    protected RecyclerView mSessionsRV;

    @Bean protected SessionAdapter mSessionAdapter;
    @Bean protected SimpleItemTouchHelperCallback mCallback;

    @AfterViews
    protected void afterViews(){
        if (DEBUG) Log.d(TAG, "afterViews");

        mSessionsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSessionsRV.setItemAnimator (new DefaultItemAnimator());
        mSessionsRV.setAdapter(mSessionAdapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(mCallback);
        touchHelper.attachToRecyclerView(mSessionsRV);
    }
}
