package org.openforis.collect.android.gui.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class NumberTextWatcher implements TextWatcher {

    private static final char MINUS_SIGN = '-';
    private static final char ZERO = '0';

    private final EditText editText;
    private final boolean groupingUsed;
    private final Character groupingSeparator;
    private final String groupingSeparatorStr;
    private final DecimalFormat numberFormat;
    private final char decimalSeparator;
    private final String decimalSeparatorStr;

    public NumberTextWatcher(EditText editText, boolean groupingUsed) {
        this.editText = editText;
        this.groupingUsed = groupingUsed;

        numberFormat = (DecimalFormat) DecimalFormat.getInstance();
        numberFormat.setGroupingUsed(groupingUsed);
        numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        numberFormat.setMaximumIntegerDigits(Integer.MAX_VALUE);

        decimalSeparator = numberFormat.getDecimalFormatSymbols().getDecimalSeparator();
        decimalSeparatorStr = String.valueOf(decimalSeparator);

        if (groupingUsed) {
            groupingSeparator = numberFormat.getDecimalFormatSymbols().getGroupingSeparator();
            groupingSeparatorStr = String.valueOf(groupingSeparator);
        } else {
            groupingSeparator = null;
            groupingSeparatorStr = null;
        }
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
            if (absVal.charAt(0) != decimalSeparator && !Character.isDigit(absVal.charAt(0))) {
                absVal = absVal.substring(1);
            }
            if (! absVal.isEmpty()) {
                if (absVal.charAt(0) == decimalSeparator) {
                    //starts with .XXX => add leading 0 => 0.XXX
                    absVal = ZERO + absVal;
                } else if (absVal.length() > 1
                        && absVal.charAt(0) == ZERO && absVal.charAt(1) != decimalSeparator) {
                    //starts with 0XXX => remove leading 0 => XXX
                    absVal = absVal.substring(1);
                }
            }
        }
        return (negative ? MINUS_SIGN : "") + absVal;
    }

    private String format(String value) {
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
        if (groupingUsed) {
            absVal = absVal.replaceAll(Pattern.quote(groupingSeparatorStr), "");
        }

        StringBuilder sb = new StringBuilder();

        if (! absVal.isEmpty()) {
            String[] parts = absVal.split(Pattern.quote(decimalSeparatorStr));
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
            if (integerPart.charAt(integerPart.length() -1) == decimalSeparator) {
                j--;
                sb.append(decimalSeparatorStr);
            }
            //add grouping separator
            int groupSize = 0;
            for (int k = j; k >= 0; k--) {
                if (groupingUsed && groupSize == 3) {
                    sb.insert(0, groupingSeparator);
                    groupSize = 0;
                }
                sb.insert(0, integerPart.charAt(k));
                groupSize++;
            }
            //add decimal part (if any)
            if (decimalPart.length() > 0) {
                sb.append(decimalSeparator);
                sb.append(decimalPart);
            }
        }
        if (negative) {
            sb.insert(0, MINUS_SIGN);
        }
        return sb.toString();
    }

    private int calculateNewSelectionIndex(String oldText, int oldSelection, String newText) {
        int newSelectionIndex;
        if (oldSelection > 0) {
            int groupingSeparators = countOccurrences(oldText.substring(0, oldSelection), groupingSeparator);
            int absoluteSelection = oldSelection - groupingSeparators;
            int newGroupingSeparators = 0;
            //count grouping separators in new text
            int i = 0;
            if (groupingUsed) {
                while (i < newText.length() && i <= (absoluteSelection + newGroupingSeparators)) {
                    if (newText.charAt(i) == groupingSeparator) {
                        newGroupingSeparators++;
                        i++;
                    }
                    i++;
                }
            }
            newSelectionIndex = absoluteSelection + newGroupingSeparators;
            if (newSelectionIndex > newText.length()) {
                newSelectionIndex = newText.length();
            }
            if (groupingUsed) {
                if (newSelectionIndex > 0 && newText.charAt(newSelectionIndex - 1) == groupingSeparator) {
                    newSelectionIndex--; //helps deleting value
                }
            }
        } else {
            newSelectionIndex = 0;
        }
        return newSelectionIndex;
    }

    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

    private static int countOccurrences(String text, Character character) {
        if (character == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }
}