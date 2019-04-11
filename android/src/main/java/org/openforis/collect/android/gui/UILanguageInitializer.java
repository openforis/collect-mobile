package org.openforis.collect.android.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.openforis.collect.android.Settings;

import java.util.Locale;

/**
 * @author Stefano Ricci
 */
public class UILanguageInitializer {

    static final String PREFERENCE_KEY = "ui_language";

    public static void init(Context context) {
        Settings.UILanguage lang = determineUiLanguageFromPreferences(context);

        String langCode = determineLanguageCode(lang);

        Locale locale = new Locale(langCode);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (!locale.equals(config.locale)) {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
    }

    protected static Settings.UILanguage determineUiLanguageFromPreferences(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uiLangCode = preferences.getString(PREFERENCE_KEY, null);
        return Settings.UILanguage.fromCode(uiLangCode);
    }

    private static String determineLanguageCode(Settings.UILanguage lang) {
        if (lang == Settings.UILanguage.SYSTEM_DEFAULT) {
            String defaultLangCode = Locale.getDefault().getLanguage();
            if (Settings.UILanguage.isSupported(defaultLangCode))
                return defaultLangCode;
            else
                return Settings.UILanguage.getDefault().getCode();
        } else {
            return lang.getCode();
        }
    }
}
