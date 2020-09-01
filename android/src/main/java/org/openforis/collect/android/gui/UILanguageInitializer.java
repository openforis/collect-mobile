package org.openforis.collect.android.gui;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.openforis.collect.android.Settings;

import java.util.Locale;

/**
 * @author Stefano Ricci
 */
public class UILanguageInitializer {

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

    static Settings.UILanguage determineUiLanguageFromPreferences(Context context) {
        Settings.PreferredLanguageMode preferredLanguageMode = Settings.getPreferredLanguageMode();
        String langCode = preferredLanguageMode == Settings.PreferredLanguageMode.SPECIFIED
                ? Settings.getPreferredLanguage()
                : null;
        return Settings.UILanguage.fromCode(langCode);
    }

    private static String determineLanguageCode(Settings.UILanguage lang) {
        if (lang == null) {
            String systemDefaultLangCode = Resources.getSystem().getConfiguration().locale.getLanguage();
            if (Settings.UILanguage.isSupported(systemDefaultLangCode))
                return systemDefaultLangCode;
            else
                return Settings.UILanguage.getDefault().getCode();
        } else {
            return lang.getCode();
        }
    }
}
