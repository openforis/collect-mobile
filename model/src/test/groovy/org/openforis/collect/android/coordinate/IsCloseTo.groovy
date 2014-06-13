package org.openforis.collect.android.coordinate

import org.hamcrest.BaseMatcher
import org.hamcrest.Description

/**
 * @author Daniel Wiell
 */
class IsCloseTo extends BaseMatcher {
    private final List<Number> values
    private final Number epsilon

    IsCloseTo(List<Number> values, Number epsilon) {
        this.values = values
        this.epsilon = epsilon
    }

    boolean matches(items) {
        maxDelta(items) <= epsilon
    }

    void describeMismatch(items, Description description) {
        description.appendText("Number in ")
                .appendValue(items)
                .appendText(" differed by ")
                .appendValue(maxDelta(items))
    }

    void describeTo(Description description) {
        description.appendText("all numbers value within ")
                .appendValue(epsilon)
                .appendText(" of ")
                .appendValue(values)
    }

    private double maxDelta(items) {
        def max = [values, items].transpose().collect {
            delta(it[0], it[1])
        }.max()
        return max
    }

    private Number delta(Number value1, Number value2) {
        // handle special values (infinity, nan)
        if (value1 == value2) return 0

        (value1 - value2).abs()
    }



    public static IsCloseTo isCloseTo(double[] values, Number epsilon) {
        new IsCloseTo(values as List<Number>, epsilon)
    }
}
