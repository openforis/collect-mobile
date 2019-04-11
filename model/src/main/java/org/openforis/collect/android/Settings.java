package org.openforis.collect.android;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.User;

public class Settings {
    private static String crew;
    private static boolean compassEnabled;
    private static boolean remoteSyncEnabled = false;
    private static String remoteCollectAddress;
    private static String remoteCollectUsername;
    private static String remoteCollectPassword;
    private static UILanguage uiLanguage;
    private static PreferredLanguageMode preferredLanguageMode;
    private static String preferredLanguage;

    public enum UILanguage {
        SYSTEM_DEFAULT(null, "System default"),
        ENGLISH("en", "English"),
        SPANISH("es", "Spanish"),
        RUSSIAN("ru", "Russian");

        private String code;
        private String label;

        UILanguage(String code, String label) {
            this.code = code;
            this.label = label;
        }


        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }

        public static String[] codes() {
            UILanguage[] langs = values();
            String[] codes = new String[langs.length];
            for (int i = 0; i < langs.length; i++) {
                codes[i] = langs[i].code;
            }
            return codes;
        }


        public static String[] labels() {
            UILanguage[] langs = values();
            String[] labels = new String[langs.length];
            for (int i = 0; i < langs.length; i++) {
                labels[i] = langs[i].label;
            }
            return labels;
        }

        public static UILanguage fromCode(String code) {
            if (code == null)
                return SYSTEM_DEFAULT;
            for (UILanguage lang : values())
                if (code.equalsIgnoreCase(lang.code))
                    return lang;
            return SYSTEM_DEFAULT;
        }

        public static boolean isSupported(String code) {
            return fromCode(code) != SYSTEM_DEFAULT;
        }

        public static UILanguage getDefault() {
            return ENGLISH;
        }
    }

    public enum PreferredLanguageMode {
        SYSTEM_DEFAULT,
        SURVEY_DEFAULT,
        SPECIFIED
    }

    public synchronized static String getCrew() {
        return crew;
    }

    public synchronized static void setCrew(String crew) {
        Settings.crew = crew;
    }

    public synchronized static boolean isCompassEnabled() {
        return compassEnabled;
    }

    public synchronized static void setCompassEnabled(boolean compassEnabled) {
        Settings.compassEnabled = compassEnabled;
    }

    public synchronized static User user() {
        if (isRemoteSyncEnabled() && StringUtils.isNotBlank(remoteCollectUsername)) {
            return new User(remoteCollectUsername);
        } else {
            return new User(crewToUsername());
        }
    }

    private static String crewToUsername() {
        return crew == null ? "" : crew.replaceAll("\\W", "_").toLowerCase();
    }

    public synchronized static UILanguage getUiLanguage() {
        return uiLanguage;
    }

    public static void setUiLanguage(UILanguage uiLanguage) {
        Settings.uiLanguage = uiLanguage;
    }

    public synchronized static PreferredLanguageMode getPreferredLanguageMode() {
        return preferredLanguageMode;
    }

    public synchronized static void setPreferredLanguageMode(PreferredLanguageMode preferredLanguageMode) {
        Settings.preferredLanguageMode = preferredLanguageMode;
    }

    public synchronized static String getPreferredLanguage() {
        return preferredLanguage;
    }

    public synchronized static void setPreferredLanguage(String preferredLanguage) {
        Settings.preferredLanguage = preferredLanguage;
    }

    public synchronized static boolean isRemoteSyncEnabled() {
        return remoteSyncEnabled;
    }

    public synchronized static void setRemoteSyncEnabled(boolean remoteSyncEnabled) {
        Settings.remoteSyncEnabled = remoteSyncEnabled;
    }

    public synchronized static String getRemoteCollectAddress() {
        return remoteCollectAddress;
    }

    public synchronized static void setRemoteCollectAddress(String remoteCollectAddress) {
        Settings.remoteCollectAddress = remoteCollectAddress;
    }

    public synchronized static String getRemoteCollectUsername() {
        return remoteCollectUsername;
    }

    public synchronized static void setRemoteCollectUsername(String remoteCollectUsername) {
        Settings.remoteCollectUsername = remoteCollectUsername;
    }

    public synchronized static String getRemoteCollectPassword() {
        return remoteCollectPassword;
    }

    public synchronized static void setRemoteCollectPassword(String remoteCollectPassword) {
        Settings.remoteCollectPassword = remoteCollectPassword;
    }

}
