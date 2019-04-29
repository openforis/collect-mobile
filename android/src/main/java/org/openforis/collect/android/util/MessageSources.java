package org.openforis.collect.android.util;

import org.openforis.collect.manager.MessageSource;

import java.util.Locale;

public abstract  class MessageSources {

    public static String getMessage(MessageSource messageSource, String key) {
        return getMessage(messageSource, key, Locale.getDefault(), Locale.ENGLISH);
    }

    public static String getMessage(MessageSource messageSource, String key, Locale locale, Locale defaultLocale) {
        String message = messageSource.getMessage(locale, key);
        if (message == null && defaultLocale != null && !defaultLocale.equals(locale)) {
            message = messageSource.getMessage(defaultLocale, key);
        }
        return message;
    }
}
