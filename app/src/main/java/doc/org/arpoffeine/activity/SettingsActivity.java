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
package doc.org.arpoffeine.activity;

import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceScreen;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Set;

import doc.org.arpoffeine.R;
import doc.org.arpoffeine.helper.ConfigHelper;
import doc.org.arpoffeine.util.Constants;
import doc.org.arpoffeine.util.ISharedPreferences_;

/**
 * Settings activity that deals with application preferences
 */

@EActivity
@SuppressWarnings("FieldCanBeLocal")
@PreferenceScreen(R.xml.preferences)
public class SettingsActivity extends PreferenceActivity implements Constants {

    private final String TAG = "SettingsActivity";

    @Pref
    ISharedPreferences_ mPreferences;

    @ViewById(R.id.settings_toolbar)
    Toolbar mToolbar;

    @Bean
    ConfigHelper configHelper;

    @PreferenceByKey(R.string.preference_key_filter)
    MultiSelectListPreference filter;

    @PreferenceChange(R.string.preference_key_generic)
    void prefGenericSelected(boolean newValue, Preference preference) {
        mPreferences.isGeneric().put(newValue);
    }

    @PreferenceChange(R.string.preference_key_vibration)
    void prefVibrationSelected(boolean newValue, Preference preference) {
        mPreferences.isVibration().put(newValue);
    }

    @PreferenceChange(R.string.preference_key_sounds)
    void prefSoundsSelected(boolean newValue, Preference preference) {
        mPreferences.isSounds().put(newValue);
    }

    @PreferenceChange(R.string.preference_key_lights)
    void prefLightsSelected(boolean newValue, Preference preference) {
        mPreferences.isLights().put(newValue);
    }

    @PreferenceChange(R.string.preference_key_filter)
    void prefFilterSelected(Set<String> newValue, Preference preference) {
        mPreferences.filter().put(newValue);
    }

    @AfterViews
    void afterViews() {
        if (DEBUG) Log.d(TAG, "afterViews");
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @AfterPreferences
    void initPrefs() {
        int size = configHelper.getMFilterList().size();
        String[] filteredValues = configHelper.getMFilterList().toArray(new String[size]);
        filter.setEntries(filteredValues);
        filter.setEntryValues(filteredValues);
        filter.setSummary(R.string.preference_description_filter);
    }
}
