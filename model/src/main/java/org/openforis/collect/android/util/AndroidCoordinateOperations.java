package org.openforis.collect.android.util;

import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem;
import org.openforis.idm.geospatial.CoordinateOperations;
import org.openforis.idm.metamodel.SpatialReferenceSystem;
import org.openforis.idm.model.Coordinate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel Wiell
 */
public class AndroidCoordinateOperations implements CoordinateOperations {
    private Map<String, SpatialReferenceSystem> spatialReferenceSystems = new ConcurrentHashMap<String, SpatialReferenceSystem>();
    public void parseSRS(List<SpatialReferenceSystem> list) {
        for (SpatialReferenceSystem srs : list)
            parseSRS(srs);
    }

    public void parseSRS(SpatialReferenceSystem srs) {
        if (!spatialReferenceSystems.containsKey(srs.getId()))
            spatialReferenceSystems.put(srs.getId(), srs);
    }

    public double orthodromicDistance(double startX, double startY, String startSRSId, double destX, double destY, String destSRSId) {
        return CoordinateUtils.distance(toUiSrs(startSRSId), new double[] {startX, startY}, toUiSrs(destSRSId), new double[] {destX, destY});
    }

    public double orthodromicDistance(Coordinate from, Coordinate to) {
        return CoordinateUtils.distance(toUiSrs(from), toUiCoordinate(from), toUiSrs(to), toUiCoordinate(to));
    }

    private double[] toUiCoordinate(Coordinate coordinate) {
        return new double[] {coordinate.getX(), coordinate.getY()};
    }

    private UiSpatialReferenceSystem toUiSrs(Coordinate coordinate) {
        SpatialReferenceSystem srs = spatialReferenceSystems.get(coordinate.getSrsId());
        return new UiSpatialReferenceSystem(srs.getId(), srs.getWellKnownText(), srs.getLabels().get(0).getText());
    }

    private UiSpatialReferenceSystem toUiSrs(String srsId) {
        SpatialReferenceSystem srs = spatialReferenceSystems.get(srsId);
        return new UiSpatialReferenceSystem(srs.getId(), srs.getWellKnownText(), srs.getLabels().get(0).getText());
    }

    public SpatialReferenceSystem fetchSRS(String code) {
        return spatialReferenceSystems.get(code);
    }

    public SpatialReferenceSystem fetchSRS(String code, Set<String> labelLanguages) {
        return fetchSRS(code);
    }

    public Set<String> getAvailableSRSs() {
        return spatialReferenceSystems.keySet();
    }
}
