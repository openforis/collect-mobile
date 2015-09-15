package org.openforis.collect.android.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageNames {
    private static Map<String, Locale> localeByIso3 = mapLocaleByIso3();

    public static String nameOfIso3(String iso3) {
        if (iso3 == null)
            throw new IllegalArgumentException("iso3 language must not be null");
        Locale locale = localeByIso3.get(iso3.toLowerCase());
        if (locale == null)
            return iso3;
        return locale.getDisplayLanguage();
    }

    private static Map<String, Locale> mapLocaleByIso3() {
        String[] languages = Locale.getISOLanguages();
        Map<String, Locale> localeByIso3 = new HashMap<String, Locale>(languages.length);
        for (String language : languages) {
            Locale locale = new Locale(language);
            localeByIso3.put(locale.getISO3Language().toLowerCase(), locale);
        }
        return localeByIso3;
    }
}
