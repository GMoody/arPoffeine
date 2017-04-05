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

import android.os.Handler;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import doc.org.arpoffeine.activity.MainActivity;
import lombok.NoArgsConstructor;

/**
 * Class that handles the update thread of the UI
 */

@EBean
@NoArgsConstructor
@SuppressWarnings("WeakerAccess")
public class RefreshHandler extends Handler {

    @RootContext
    protected MainActivity mActivity;

    private boolean mWorking = false;

    public void startAutoRefresh() {
        if (!mWorking) autoRefresh.run();
        mWorking = true;
    }

    public void stopAutoRefresh() {
        if (mWorking) removeCallbacks(autoRefresh);
        mWorking = false;
    }

    private Runnable autoRefresh = new Runnable() {
        @Override
        public void run() {
            mActivity.refresh();
            postDelayed(autoRefresh, 1000);
        }
    };
}
