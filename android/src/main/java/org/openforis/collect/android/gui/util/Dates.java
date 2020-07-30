package org.openforis.collect.android.gui.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class Dates {

    public static final SimpleDateFormat FORMAT_FULL = new SimpleDateFormat("dd MMMMM yyyy (HH:mm:ss)", Locale.ENGLISH);

    public static String formatFull(Date date) {
        return FORMAT_FULL.format(date);
    }
}
