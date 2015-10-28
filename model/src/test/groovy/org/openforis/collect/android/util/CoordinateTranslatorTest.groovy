package org.openforis.collect.android.util

import spock.lang.Specification
import spock.lang.Unroll

import static org.openforis.collect.android.util.CoordinateTranslator.toMargins

class CoordinateTranslatorTest extends Specification {
    @Unroll
    def 'toMargins(#compassBearing, #destinationBearing, #radius) == #expected'() {
        def expectedMargins = expected as int[]

        expect:
            toMargins(compassBearing, destinationBearing, radius) == expectedMargins

        where:
            compassBearing | destinationBearing | radius || expected
            0              | 0                  | 10     || [10, 0]
            0              | 90                 | 10     || [20, 10]
            0              | 180                | 10     || [10, 20]
            0              | 270                | 10     || [0, 10]

            90             | 0                  | 10     || [0, 10]
            90             | 90                 | 10     || [10, 0]
            90             | 180                | 10     || [20, 10]
            90             | 270                | 10     || [10, 20]

            180            | 0                  | 10     || [10, 20]
            180            | 90                 | 10     || [0, 10]
            180            | 180                | 10     || [10, 0]
            180            | 270                | 10     || [20, 10]

            270            | 0                  | 10     || [20, 10]
            270            | 90                 | 10     || [10, 20]
            270            | 180                | 10     || [0, 10]
            270            | 270                | 10     || [10, 0]

            80             | 80                 | 10     || [10, 0]
            120            | 120                | 10     || [10, 0]
    }
}
