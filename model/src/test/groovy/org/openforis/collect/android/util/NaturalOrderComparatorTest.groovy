package org.openforis.collect.android.util

import spock.lang.Specification
import spock.lang.Unroll

class NaturalOrderComparatorTest extends Specification {
    def comparator = new NaturalOrderComparator()

    @Unroll
    def 'naturalOrderComparator().compare("#s1", "#s2") == #expectation'(String s1, String s2, int expectation) {
        expect:
            comparator.compare(s1, s2) == expectation

        where:
            s1      | s2      | expectation
            '0a'    | '01'    | -1
            '1 0a'  | '1 01'  | -1

            '1 01'  | '1 a'   | -1
            '1a'    | '01 a'  | 1

            '01'    | '1a'    | -1
            '01'    | '1 a'   | -1
            '001'   | '1 a'   | -1

            ' '     | 'a'     | -1
            'a'     | ' '     | 1

            ' '     | '1'     | -1
            '1'     | ' '     | 1

            'a'     | 'b'     | -1
            'a'     | 'a'     | 0
            'b'     | 'a'     | 1

            'a'     | 'aa'    | -1
            'aa'    | 'aa'    | 0
            'aa'    | 'a'     | 1

            '1'     | '2'     | -1
            '1'     | '1'     | 0
            '2'     | '1'     | 1

            '1'     | '10'    | -1
            '10'    | '10'    | 0
            '10'    | '1'     | 1

            '1'     | 'a'     | -1
            'a'     | '1'     | 1

            '1a'    | '10a'   | -1
            '10a'   | '1a'    | 1

            '01'    | '1a'    | -1
            '1a'    | '01'    | 1

            '2'     | '10'    | -1
            '10'    | '10'    | 0
            '10'    | '2'     | 1

            'a2'    | 'a10'   | -1
            'a10'   | 'a10'   | 0
            'a10'   | 'a2'    | 1

            'a2 b'  | 'a10 b' | -1
            'a10 b' | 'a10 b' | 0
            'a10 b' | 'a2 b'  | 1

            '1'     | '02'    | -1
            '02'    | '02'    | 0
            '02'    | '1'     | 1

            '1'     | '002'   | -1
            '002'   | '002'   | 0
            '002'   | '1'     | 1

            '01'    | '2'     | -1
            '01'    | '01'    | 0
            '2'     | '01'    | 1

            '0'     | '00'    | -1
            '0'     | '0'     | 0
            '00'    | '0'     | 1

            '0a0'   | '0b'    | -1
            '0a0'   | '0a0'   | 0
            '0b0'   | '0a'    | 1

            '0202'  | '211'   | -1
            '202'  | '0211'   | -1

            '0122'  | '211'   | -1
            '0122'  | '0122'  | 0
            '0211'  | '122'   | 1

            '0122a' | '211a'  | -1
            '0122a' | '0122a' | 0
            '0211a' | '122a'  | 1

            '1 01'  | '01 1'  | -1
            '01 1'  | '1 01'  | 1
    }

    def 'Natural order comparator sorts list as expected'() {
        def expectedOrder = [
                ' ', '0', '00', '1', '01', '001', '1 01', '01 1',
                '1 a', '01 a', '1a', '01a', '1a1', '1aa', '01aa', '01aaa', '1aaaa',
                '11', '011', '11a', '011a',
                'a', 'a1', 'a1a', 'a11', 'a11a', 'aa'
        ]
        def shuffled = expectedOrder.sort(false) { Math.random() }

        when:
            def sorted = shuffled.sort(false, comparator)
        then:
            sorted == expectedOrder
    }
}
