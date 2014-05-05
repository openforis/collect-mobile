package org.openforis.collect.android.util;

/**
 * @author Daniel Wiell
 */
public class StringUtils {
    public static String normalizeWhiteSpace(String s) {
        if (s == null)
            return null;
        return s.replaceAll("[\\s]+", " ");
    }
}
