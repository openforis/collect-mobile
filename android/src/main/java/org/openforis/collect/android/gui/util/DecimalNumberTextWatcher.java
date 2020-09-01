package org.openforis.collect.android.gui.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class DecimalNumberTextWatcher implements TextWatcher {

    private static final DecimalFormat NUMBER_FORMAT;
    private static final char DECIMAL_SEPARATOR;
    private static final char GROUPING_SEPARATOR;
    private static final char MINUS_SIGN = '-';
    private static final char ZERO = '0';
    private static final String DECIMAL_SEPARATOR_STR;
    private static final String GROUPING_SEPARATOR_STR;

    static {
        NUMBER_FORMAT = (DecimalFormat) DecimalFormat.getInstance();
        NUMBER_FORMAT.setGroupingUsed(true);
        NUMBER_FORMAT.setMaximumFractionDigits(Integer.MAX_VALUE);
        NUMBER_FORMAT.setMaximumIntegerDigits(Integer.MAX_VALUE);

        DECIMAL_SEPARATOR = NUMBER_FORMAT.getDecimalFormatSymbols().getDecimalSeparator();
        DECIMAL_SEPARATOR_STR = String.valueOf(DECIMAL_SEPARATOR);
        GROUPING_SEPARATOR = NUMBER_FORMAT.getDecimalFormatSymbols().getGroupingSeparator();
        GROUPING_SEPARATOR_STR = String.valueOf(GROUPING_SEPARATOR);
    }

    private EditText editText;

    public DecimalNumberTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            editText.removeTextChangedListener(this);
            String oldText = editText.getText().toString();

            if (!oldText.isEmpty()) {
                int oldSelection = editText.getSelectionStart();

                String preparedOldText = prepareToFormat(oldText);

                int preparedOldSelection = oldSelection + (preparedOldText.length() - oldText.length());

                String newText = preparedOldText.isEmpty() ? "" : format(preparedOldText);
                editText.setText(newText);
                int newSelectionIndex = calculateNewSelectionIndex(preparedOldText, preparedOldSelection, newText);
                editText.setSelection(newSelectionIndex);
            }
        } catch (NumberFormatException ex) {
            //ignore
        } finally {
            editText.addTextChangedListener(this);
        }
    }

    @NonNull
    private String prepareToFormat(String val) {
        boolean negative = val.charAt(0) == MINUS_SIGN;
        String absVal = negative ? val.substring(1) : val;

        if (! absVal.isEmpty()) {
            //remove invalid starting characters
            if (absVal.charAt(0) != DECIMAL_SEPARATOR && !Character.isDigit(absVal.charAt(0))) {
                absVal = absVal.substring(1);
            }
            if (! absVal.isEmpty()) {
                if (absVal.charAt(0) == DECIMAL_SEPARATOR) {
                    //starts with .XXX => add leading 0 => 0.XXX
                    absVal = ZERO + absVal;
                } else if (absVal.length() > 1
                        && absVal.charAt(0) == ZERO && absVal.charAt(1) != DECIMAL_SEPARATOR) {
                    //starts with 0XXX => remove leading 0 => XXX
                    absVal = absVal.substring(1);
                }
            }
        }
        return (negative ? MINUS_SIGN : "") + absVal;
    }

    private static String format(String value) {
        String absVal;
        boolean negative;
        if (value.charAt(0) == MINUS_SIGN) {
            negative = true;
            absVal = value.substring(1);
        } else {
            negative = false;
            absVal = value;
        }
        //remove all grouping separators
        absVal = absVal.replaceAll(Pattern.quote(GROUPING_SEPARATOR_STR), "");

        StringBuilder sb = new StringBuilder();

        if (! absVal.isEmpty()) {
            String[] parts = absVal.split(Pattern.quote(DECIMAL_SEPARATOR_STR));
            String integerPart;
            String decimalPart;
            switch(parts.length) {
                case 1:
                    integerPart = absVal;
                    decimalPart = "";
                    break;
                case 2:
                    integerPart = parts[0];
                    decimalPart = parts[1];
                    break;
                default:
                    //double decimal separator found
                    integerPart = parts[0];
                    decimalPart = parts[2];
            }
            int j = integerPart.length() - 1;
            if (integerPart.charAt(integerPart.length() -1) == DECIMAL_SEPARATOR) {
                j--;
                sb.append(DECIMAL_SEPARATOR_STR);
            }
            //add grouping separator
            int groupSize = 0;
            for (int k = j; k >= 0; k--) {
                if (groupSize == 3) {
                    sb.insert(0, GROUPING_SEPARATOR);
                    groupSize = 0;
                }
                sb.insert(0, integerPart.charAt(k));
                groupSize++;
            }
            //add decimal part (if any)
            if (decimalPart.length() > 0) {
                sb.append(DECIMAL_SEPARATOR);
                sb.append(decimalPart);
            }
        }
        if (negative) {
            sb.insert(0, MINUS_SIGN);
        }
        return sb.toString();
    }

    private static int calculateNewSelectionIndex(String oldText, int oldSelection, String newText) {
        int newSelectionIndex;
        if (oldSelection > 0) {
            int groupingSeparators = countOccurrences(oldText.substring(0, oldSelection), GROUPING_SEPARATOR);
            int absoluteSelection = oldSelection - groupingSeparators;
            int newGroupingSeparators = 0;
            //count grouping separators in new text
            int i = 0;
            while(i < newText.length() && i <= (absoluteSelection + newGroupingSeparators)) {
                if (newText.charAt(i) == GROUPING_SEPARATOR) {
                    newGroupingSeparators++;
                    i++;
                }
                i++;
            }
            newSelectionIndex = absoluteSelection + newGroupingSeparators;
            if (newSelectionIndex > newText.length()) {
                newSelectionIndex = newText.length();
            }
            if (newSelectionIndex > 0 && newText.charAt(newSelectionIndex-1) == GROUPING_SEPARATOR) {
                newSelectionIndex--; //helps deleting value
            }
        } else {
            newSelectionIndex = 0;
        }
        return newSelectionIndex;
    }

    private static int countOccurrences(String text, char character) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }
}