package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public abstract class PreferencesUtils {

    public static <T> T getPreference(Context context, String key, Class<T> type) {
        return getPreference(context, key, type, null);
    }
    public static <T> T getPreference(Context context, String key, Class<T> type, T defaultValue) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (type == String.class) {
            return (T) preferences.getString(key, (String) defaultValue);
        } else if (type == Boolean.class) {
            return (T) new Boolean(preferences.getBoolean(key, (Boolean) defaultValue));
        } else if (type == Float.class) {
            return (T) new Float(preferences.getFloat(key, (Float) defaultValue));
        } else if (type == Integer.class) {
            return (T) new Integer(preferences.getInt(key, (Integer) defaultValue));
        } else if (type == Long.class) {
            return (T) new Long(preferences.getLong(key, (Long) defaultValue));
        }
        return null;
    }

    public static <T> void setPreference(Context context, String key, T value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        }
        editor.commit();
    }
}
