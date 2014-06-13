package org.openforis.collect.android.coordinate

import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem
import spock.lang.Specification

import static IsCloseTo.isCloseTo
import static org.openforis.collect.android.util.CoordinateUtils.transform
import static org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem.LAT_LNG_SRS
import static spock.util.matcher.HamcrestSupport.that

/**
 * @author Daniel Wiell
 */
class TransformTest extends Specification {

    def utm17s = new UiSpatialReferenceSystem('EPSG:32717',
            '''PROJCS["WGS 84 / UTM zone 17S",
    GEOGCS["WGS 84",
        DATUM["WGS_1984",
            SPHEROID["WGS 84",6378137,298.257223563,
                AUTHORITY["EPSG","7030"]],
            AUTHORITY["EPSG","6326"]],
        PRIMEM["Greenwich",0,
            AUTHORITY["EPSG","8901"]],
        UNIT["degree",0.01745329251994328,
            AUTHORITY["EPSG","9122"]],
        AUTHORITY["EPSG","4326"]],
    UNIT["metre",1,
        AUTHORITY["EPSG","9001"]],
    PROJECTION["Transverse_Mercator"],
    PARAMETER["latitude_of_origin",0],
    PARAMETER["central_meridian",-81],
    PARAMETER["scale_factor",0.9996],
    PARAMETER["false_easting",500000],
    PARAMETER["false_northing",10000000],
    AUTHORITY["EPSG","32717"],
    AXIS["Easting",EAST],
    AXIS["Northing",NORTH]]''', '17S')

    def utm18s = new UiSpatialReferenceSystem('EPSG:32718',
            '''PROJCS["WGS 84 / UTM zone 18S",
    GEOGCS["WGS 84",
        DATUM["WGS_1984",
            SPHEROID["WGS 84",6378137,298.257223563,
                AUTHORITY["EPSG","7030"]],
            AUTHORITY["EPSG","6326"]],
        PRIMEM["Greenwich",0,
            AUTHORITY["EPSG","8901"]],
        UNIT["degree",0.01745329251994328,
            AUTHORITY["EPSG","9122"]],
        AUTHORITY["EPSG","4326"]],
    UNIT["metre",1,
        AUTHORITY["EPSG","9001"]],
    PROJECTION["Transverse_Mercator"],
    PARAMETER["latitude_of_origin",0],
    PARAMETER["central_meridian",-75],
    PARAMETER["scale_factor",0.9996],
    PARAMETER["false_easting",500000],
    PARAMETER["false_northing",10000000],
    AUTHORITY["EPSG","32718"],
    AXIS["Easting",EAST],
    AXIS["Northing",NORTH]]''', '18S')

    def utm19s = new UiSpatialReferenceSystem('EPSG:32719',
            '''PROJCS["WGS 84 / UTM zone 19S",
    GEOGCS["WGS 84",
        DATUM["WGS_1984",
            SPHEROID["WGS 84",6378137,298.257223563,
                AUTHORITY["EPSG","7030"]],
            AUTHORITY["EPSG","6326"]],
        PRIMEM["Greenwich",0,
            AUTHORITY["EPSG","8901"]],
        UNIT["degree",0.01745329251994328,
            AUTHORITY["EPSG","9122"]],
        AUTHORITY["EPSG","4326"]],
    UNIT["metre",1,
        AUTHORITY["EPSG","9001"]],
    PROJECTION["Transverse_Mercator"],
    PARAMETER["latitude_of_origin",0],
    PARAMETER["central_meridian",-69],
    PARAMETER["scale_factor",0.9996],
    PARAMETER["false_easting",500000],
    PARAMETER["false_northing",10000000],
    AUTHORITY["EPSG","32719"],x
    AXIS["Easting",EAST],
    AXIS["Northing",NORTH]]''', '19S')


    def nonTrimmedWellKnownText = new UiSpatialReferenceSystem('EPSG:32719',
            '''
                PROJCS["Arc 1960 / UTM zone 35S",
    GEOGCS["Arc 1960",
        DATUM["Arc_1960",
            SPHEROID["Clarke 1880 (RGS)",6378249.145,293.465,
                AUTHORITY["EPSG","7012"]],
            TOWGS84[-160.0, -6.0, -302.0, 0.0, 0.0, 0.0, 0.0],
            AUTHORITY["EPSG","6210"]],
        PRIMEM["Greenwich",0,
            AUTHORITY["EPSG","8901"]],
        UNIT["degree",0.01745329251994328,
            AUTHORITY["EPSG","9122"]],
        AUTHORITY["EPSG","4210"]],
    UNIT["metre",1,
        AUTHORITY["EPSG","9001"]],
    PROJECTION["Transverse_Mercator"],
    PARAMETER["latitude_of_origin",0],
    PARAMETER["central_meridian",27],
    PARAMETER["scale_factor",0.9996],
    PARAMETER["false_easting",500000],
    PARAMETER["false_northing",10000000],
    AUTHORITY["EPSG","21035"],
    AXIS["Easting",EAST],
    AXIS["Northing",NORTH]]

    ''', 'UTM zone 35S')

    def 'Transitive transformation gives same result as direct transformation'() {
        def latLng = [-74.5817184, -8.3751095] as double[]

        when:
        def direct17 = transform(LAT_LNG_SRS, latLng, utm17s)
        def transitive18 = transform(utm17s, direct17, utm18s)
        def direct18 = transform(LAT_LNG_SRS, latLng, utm18s)

        then:
        assert that(transitive18, isCloseTo(direct18, 0.000001))
    }

    def 'Can create a chain of transformations'() {
        def latLng = [-74.5817184, -8.3751095] as double[]

        when:
        def result17 = transform(LAT_LNG_SRS, latLng, utm17s)
        def result18 = transform(utm17s, result17, utm18s)
        def result19 = transform(utm18s, result18, utm19s)
        def resultLatLng = transform(utm19s, result19, LAT_LNG_SRS)

        then:
        assert that(latLng, isCloseTo(resultLatLng, 0.000001))
    }


    def 'Rounding errors'() {
        def results = [[-74.5817184, -8.3751095] as double[]]
        def distances = [0d]

        when:
        10000.times {
            def intermediate = transform(LAT_LNG_SRS, results.last(), utm17s)
            results << transform(utm17s, intermediate, LAT_LNG_SRS)
            distances << distance(results.first(), results.last())
        }

        then:
        distances.max() < 0.1d
    }


    def 'Works with non-trimmed well known text'() {
        expect:
        transform(LAT_LNG_SRS, [-74.5817184, -8.3751095] as double[], nonTrimmedWellKnownText)
    }


    private double distance(double[] from, double[] to) {
        final int R = 6371; // Radius of the earth

        double lat1 = from[1]
        double lon1 = from[0]
        double lat2 = to[1]
        double lon2 = to[0]

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
}
