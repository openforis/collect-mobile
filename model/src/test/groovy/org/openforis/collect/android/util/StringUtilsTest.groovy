package org.openforis.collect.android.util

import spock.lang.Specification
import spock.lang.Unroll

class StringUtilsTest extends Specification {
    @Unroll
    def 'ellipsisMiddle("#input", #length) == "#expectation"'() {
        expect:
            StringUtils.ellipsisMiddle(input, length) == expectation
        where:
            input                    | length | expectation
            '12345'                  | 5      | '12345'
            '12345'                  | 4      | '1...'
            '123456'                 | 5      | '1...6'
            '123456'                 | 4      | '1...'
            '123456'                 | 3      | '...'
            '123456'                 | 2      | '...'
            '1234567'                | 6      | '12...7'
            '1234567'                | 5      | '1...7'
            '1234567'                | 4      | '1...'
            '1234567'                | 3      | '...'
            '12345678'               | 7      | '12...78'
            '12345678'               | 6      | '12...8'
            '12345678'               | 5      | '1...8'
            '12345678'               | 4      | '1...'
            '12345678'               | 3      | '...'
            '123456789'              | 8      | '123...89'
            'abcdefghijklmnopqrstuv' | 13     | 'abcde...rstuv'
            'abcdefghijklmnopqrstuv' | 12     | 'abcde...stuv'
            null                     | 12     | null
    }

}
