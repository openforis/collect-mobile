package org.openforis.collect.android.coordinate

import org.openforis.collect.model.CollectRecord
import org.openforis.collect.model.CollectSurveyContext
import org.openforis.collect.persistence.DatabaseLookupProvider
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder
import org.openforis.idm.metamodel.Survey
import org.openforis.idm.metamodel.validation.DistanceCheck
import org.openforis.idm.metamodel.validation.ValidationResult
import org.openforis.idm.metamodel.validation.ValidationResults
import org.openforis.idm.metamodel.validation.Validator
import org.openforis.idm.model.*
import spock.lang.Specification

/**
 * @author Daniel Wiell
 */
class DistanceCheckTest extends Specification {
    static final Coordinate TEST_COORDINATE = Coordinate.parseCoordinate("SRID=EPSG:21035;POINT(805750 9333820)")
    Entity cluster = createCluster()

    def 'Valid distance'() {
        when:
        ValidationResults results = validateVehicleLocation("SRID=EPSG:21035;POINT(805750 9333820)")

        then:
        !containsDistanceCheck(results.errors)
        !containsDistanceCheck(results.warnings)
    }

    def 'Error distance'() {
        when:
        def results = validateVehicleLocation("SRID=EPSG:21035;POINT(915750 9333820)")

        then:
        containsDistanceCheck(results.errors)
    }

    def 'Warning distance'() {
        when:
        def results = validateVehicleLocation("SRID=EPSG:21035;POINT(885750 9333820)")

        then:
        containsDistanceCheck(results.warnings)
    }

    private ValidationResults validateVehicleLocation(String coordStr) {
        def coord = Coordinate.parseCoordinate(coordStr)
        def vehicleLocation = EntityBuilder.addValue(cluster, "vehicle_location", coord)
        validate(vehicleLocation)
    }

    private ValidationResults validate(Attribute attribute) {
        Validator validator = attribute.record.surveyContext.validator
        validator.validate(attribute)
    }

    private boolean containsDistanceCheck(List<ValidationResult> errors) {
        errors.find {
            it.validator instanceof DistanceCheck
        }
    }

    private Entity createCluster() {
        def is = getClass().getResourceAsStream("/test.idm.xml")
        def surveyContext = new TestSurveyContext()
        def parser = new CollectSurveyIdmlBinder(surveyContext)
        def survey = parser.unmarshal(is)
        def record = new CollectRecord(survey, "2.0", "cluster")
        def cluster = record.getRootEntity()
        EntityBuilder.addValue(cluster, "id", new Code("001"))
        return cluster
    }

    private static class TestSurveyContext extends CollectSurveyContext {
        TestSurveyContext() {
            expressionFactory.lookupProvider = new TestLookupProvider()
        }
    }

    private static class TestLookupProvider extends DatabaseLookupProvider {

        @Override
        Object lookup(Survey survey, String name, String attribute, Object... keyValuePairs) {
            return TEST_COORDINATE
        }
    }
}
