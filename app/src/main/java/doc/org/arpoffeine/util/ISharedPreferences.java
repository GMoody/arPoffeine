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

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultStringSet;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

import java.util.Set;

import doc.org.arpoffeine.R;

/**
 * Interface for managing application preferences
 */

@SharedPref(SharedPref.Scope.UNIQUE)
public interface ISharedPreferences {

    @DefaultBoolean(value = true,  keyRes = R.string.preference_key_generic)
    boolean isGeneric();

    @DefaultBoolean(value = false, keyRes = R.string.preference_key_vibration)
    boolean isVibration();

    @DefaultBoolean(value = false, keyRes = R.string.preference_key_sounds)
    boolean isSounds();

    @DefaultBoolean(value = true, keyRes = R.string.preference_key_lights)
    boolean isLights();

    @DefaultStringSet(value = {}, keyRes = R.string.preference_key_filter)
    Set<String> filter();
}
