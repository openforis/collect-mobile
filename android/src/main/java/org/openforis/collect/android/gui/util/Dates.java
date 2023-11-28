package org.openforis.collect.android.gui.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class Dates {

    public static final SimpleDateFormat FORMAT_FULL = new SimpleDateFormat("dd MMMMM yyyy (HH:mm:ss)", Locale.ENGLISH);
    public static final SimpleDateFormat FORMAT_ISO = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    public static String formatFull(Date date) {
        return FORMAT_FULL.format(date);
    }

    public static String formatISO(Date date) {
        return FORMAT_ISO.format(date);
    }

    public static String formatNowISO() {
        return formatISO(new Date());
    }
}
