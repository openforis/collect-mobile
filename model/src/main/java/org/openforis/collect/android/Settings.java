package org.openforis.collect.android;

import org.openforis.collect.model.User;

public class Settings {
    private static String crew;
    private static boolean compassEnabled;

    public synchronized static String getCrew() {
        return crew;
    }

    public synchronized static void setCrew(String crew) {
        Settings.crew = crew;
    }

    public static boolean isCompassEnabled() {
        return compassEnabled;
    }

    public static void setCompassEnabled(boolean compassEnabled) {
        Settings.compassEnabled = compassEnabled;
    }

    public static User user() {
        return new User(crewToUsername());
    }

    private static String crewToUsername() {
        return crew == null ? "" : crew.replaceAll("\\W", "_").toLowerCase();
    }
}
