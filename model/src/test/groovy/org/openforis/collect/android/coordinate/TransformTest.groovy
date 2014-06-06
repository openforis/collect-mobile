package org.openforis.collect.android.coordinate

import org.openforis.collect.android.util.CoordinateUtils
import spock.lang.Ignore
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
@Ignore
class TransformTest extends Specification {
    def 'Test'() {
        when:
        def result = CoordinateUtils.transform("EPSG:21035", [0, 0] as double[], "EPSG:32717")

        then:
        result == [1, 2]
    }

}
