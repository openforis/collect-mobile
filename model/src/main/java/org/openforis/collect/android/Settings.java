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
    private static PreferredLanguageMode preferredLanguageMode;
    private static String preferredLanguage;

    private static FontScale fontScale;

    public enum UILanguage {
        ALBANIAN("sq", "Albanian"),
        ENGLISH("en", "English"),
        FRENCH("fr", "French"),
        RUSSIAN("ru", "Russian"),
        SPANISH("es", "Spanish"),
        SWEDISH("sv", "Swedish");

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

        public static UILanguage fromCode(String code) {
            if (code == null)
                return null;
            for (UILanguage lang : values())
                if (code.equalsIgnoreCase(lang.code))
                    return lang;
            return null;
        }

        public static boolean isSupported(String code) {
            return fromCode(code) != null;
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

    public enum FontScale {
        SMALL(0.8f),
        NORMAL(1.0f),
        BIG(1.2f),
        VERY_BIG(1.5f);

        private float value;

        FontScale(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
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

    public synchronized static FontScale getFontScale() {
        return fontScale != null ? fontScale : FontScale.NORMAL;
    }

    public static void setFontScale(FontScale fontScale) {
        Settings.fontScale = fontScale;
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
