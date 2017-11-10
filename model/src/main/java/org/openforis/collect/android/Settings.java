package org.openforis.collect.android;

import org.openforis.collect.model.User;

public class Settings {
    private static String crew;
    private static boolean compassEnabled;
    private static boolean remoteSyncEnabled = false;
    private static String remoteCollectAddress;
    private static String remoteCollectUsername;
    private static String remoteCollectPassword;

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
        return remoteCollectAddress;
    }

    public synchronized static void setRemoteCollectPassword(String remoteCollectPassword) {
        Settings.remoteCollectPassword = remoteCollectPassword;
    }

}
