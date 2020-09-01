package org.openforis.collect.android.gui.input;

import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.NumberKeyListener;
import android.view.KeyEvent;

import androidx.annotation.NonNull;

import java.text.DecimalFormatSymbols;

class DecimalSeparatorAwareKeyListener extends NumberKeyListener {

    private static final char DECIMAL_SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();
    private static final char MINUS_SIGN = '-';
    /**
     * The characters that are used.
     *
     * @see KeyEvent#getMatch
     * @see #getAcceptedChars
     */
    private static final char[] ACCEPTED_CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', MINUS_SIGN, DECIMAL_SEPARATOR
    };

    @Override
    @NonNull
    protected char[] getAcceptedChars() {
        return ACCEPTED_CHARS;
    }

    public int getInputType() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);

        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }

        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();

        /*
         * Find out if the existing text has '-' or '.' characters.
         */

        for (int i = 0; i < dstart; i++) {
            char c = dest.charAt(i);

            if (c == MINUS_SIGN) {
                sign = i;
            } else if (c == DECIMAL_SEPARATOR) {
                decimal = i;
            }
        }
        for (int i = dend; i < dlen; i++) {
            char c = dest.charAt(i);

            if (c == MINUS_SIGN) {
                return "";    // Nothing can be inserted in front of a '-'.
            } else if (c == DECIMAL_SEPARATOR) {
                decimal = i;
            }
        }

        /*
         * If it does, we must strip them out from the source.
         * In addition, '-' must be the very first character,
         * and nothing can be inserted before an existing '-'.
         * Go in reverse order so the offsets are stable.
         */

        SpannableStringBuilder stripped = null;

        for (int i = end - 1; i >= start; i--) {
            char c = source.charAt(i);
            boolean strip = false;

            if (c == MINUS_SIGN) {
                if (i != start || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i;
                }
            } else if (c == DECIMAL_SEPARATOR) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i;
                }
            }

            if (strip) {
                if (end == start + 1) {
                    return "";  // Only one character, and it was stripped.
                }

                if (stripped == null) {
                    stripped = new SpannableStringBuilder(source, start, end);
                }

                stripped.delete(i - start, i + 1 - start);
            }
        }

        if (stripped != null) {
            return stripped;
        } else if (out != null) {
            return out;
        } else {
            return null;
        }
    }
}
