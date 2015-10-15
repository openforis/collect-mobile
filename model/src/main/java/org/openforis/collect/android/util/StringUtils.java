package org.openforis.collect.android.util;

import static java.lang.Math.ceil;

/**
 * @author Daniel Wiell
 */
public class StringUtils {
    private static final String ELLIPSIS = "...";

    public static String normalizeWhiteSpace(String s) {
        if (s == null)
            return null;
        return s.replaceAll("[\\s]+", " ");
    }

    public static String ellipsisMiddle(String s, int maxLength) {
        if (s == null) return null;
        maxLength = Math.max(maxLength, ELLIPSIS.length());
        int length = s.length();
        if (length <= maxLength)
            return s;
        int charsToRemove = 3 + length - maxLength;
        int endOfStart = (int) (ceil(length / 2d - charsToRemove / 2d));
        int endOfEnd = (int) (ceil(length / 2d + charsToRemove / 2d));
        return s.substring(0, endOfStart) + ELLIPSIS + s.substring(endOfEnd);
    }

}

