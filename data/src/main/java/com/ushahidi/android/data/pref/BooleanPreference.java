/*
 * Copyright (c) 2014 Ushahidi.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program in the file LICENSE-AGPL. If not, see
 * https://www.gnu.org/licenses/agpl-3.0.html
 */

package com.ushahidi.android.data.pref;

import com.google.common.base.Preconditions;

import android.content.SharedPreferences;

/**
 * Preference for saving Boolean values
 *
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class BooleanPreference extends BasePreference<Boolean> {

    /**
     * Constructs a new {@link com.ushahidi.android.data.pref.StringPreference}
     *
     * @param sharedPreferences SharedPreferences to be used for storing the value.
     * @param key               The key for the preference
     */
    public BooleanPreference(SharedPreferences sharedPreferences, String key) {
        this(sharedPreferences, key, false);
    }

    /**
     * Constructs a new {@link com.ushahidi.android.data.pref.StringPreference}
     *
     * @param sharedPreferences SharedPreferences to be used for storing the value.
     * @param key               The key for the preference
     * @param defaultValue      The default value
     */
    public BooleanPreference(SharedPreferences sharedPreferences, String key,
            Boolean defaultValue) {
        super(sharedPreferences, key, defaultValue);
    }

    /**
     * Gets the saved Boolean
     *
     * @return The saved Boolean
     */
    @Override
    public Boolean get() {
        return getSharedPreferences().getBoolean(getKey(), getDefaultValue());
    }

    /**
     * Sets the Boolean to be saved
     *
     * @param value The Boolean value to be saved
     */
    @Override
    public void set(Boolean value) {
        Preconditions.checkNotNull(value, "Boolean cannot be null");
        this.set((boolean) value);
    }

    public void set(boolean value) {
        getSharedPreferences().edit().putBoolean(getKey(), value).apply();
    }
}
