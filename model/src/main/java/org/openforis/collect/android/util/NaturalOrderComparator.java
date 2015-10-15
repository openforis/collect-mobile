package org.openforis.collect.android.util;

import java.util.Comparator;

public class NaturalOrderComparator<T> implements Comparator<T> {
    public int compare(T o1, T o2) {
        char[] chars1 = o1.toString().toCharArray(), chars2 = o2.toString().toCharArray();
        int length1 = chars1.length, length2 = chars2.length;
        int pos1 = 0, pos2 = 0;
        int bias = 0;
        while (pos1 < length1 && pos2 < length2) {
            char c1 = chars1[pos1], c2 = chars2[pos2];
            boolean isDigit1 = Character.isDigit(c1), isDigit2 = Character.isDigit(c2);
            if (isDigit1 != isDigit2) {
                if (c1 < c2)
                    return -1;
                if (c1 > c2)
                    return 1;
            }
            if (isDigit1) {
                int numberPos1 = nonZeroPos(chars1, pos1), numberPos2 = nonZeroPos(chars2, pos2);
                int numberBias = 0;
                while (numberPos1 < length1 && numberPos2 < length2
                        && isDigit1 && isDigit2) {
                    c1 = chars1[numberPos1];
                    c2 = chars2[numberPos2];
                    isDigit1 = Character.isDigit(c1);
                    isDigit2 = Character.isDigit(c2);
                    if (c1 != c2) {
                        if (isDigit1 != isDigit2)
                            return isDigit1 ? 1 : -1; // The string with longer number is bigger
                        if (isDigit1 && numberBias == 0) {
                            if (c1 < c2)
                                numberBias = -1;
                            if (c1 > c2)
                                numberBias = 1;
                        } else if (!isDigit1) {
                            if (c1 < c2)
                                return -1;
                            else if (c1 > c2)
                                return 1;
                        }
                    }
                    numberPos1++;
                    numberPos2++;
                    if (bias == 0)
                        bias = numberBias;
                }
                int numberLength1 = numberPos1 - pos1;
                int numberLength2 = numberPos2 - pos2;
                if (numberLength1 != numberLength2 && bias == 0)
                    bias = numberLength1 < numberLength2 ? -1 : 1;
                pos1 = numberPos1;
                pos2 = numberPos2;
            } else {
                if (c1 < c2)
                    return -1;
                if (c1 > c2)
                    return 1;
                pos1++;
                pos2++;
            }
        }
        if (pos1 < length1)
            return 1;
        if (pos2 < length2)
            return -1;

        return bias;
    }

    private int nonZeroPos(char[] chars, int pos) {
        int nonZeroPos = pos;
        char c = chars[nonZeroPos];
        int length = chars.length;
        while (nonZeroPos < length - 1 && c == '0') {
            nonZeroPos++;
            c = chars[nonZeroPos];
        }
        return nonZeroPos;
    }

}
