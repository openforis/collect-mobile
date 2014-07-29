package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiCoordinateAttribute extends UiAttribute {
    private Double x;
    private Double y;
    private UiSpatialReferenceSystem spatialReferenceSystem;

    public UiCoordinateAttribute(int id, UiCoordinateDefinition definition) {
        super(id, definition);
    }

    public synchronized Double getX() {
        return x;
    }

    public synchronized void setX(Double x) {
        this.x = x;
    }

    public synchronized Double getY() {
        return y;
    }

    public synchronized void setY(Double y) {
        this.y = y;
    }

    public synchronized UiSpatialReferenceSystem getSpatialReferenceSystem() {
        return spatialReferenceSystem;
    }

    public synchronized void setSpatialReferenceSystem(UiSpatialReferenceSystem spatialReferenceSystem) {
        this.spatialReferenceSystem = spatialReferenceSystem;
    }

    public UiCoordinateDefinition getDefinition() {
        return (UiCoordinateDefinition) super.getDefinition();
    }

    @Override
    public String valueAsString() {
        return isEmpty() ? null : (x + ", " + y);
    }

    public boolean isEmpty() {
        return x == null || y == null;
    }

    public String format() {
        return (x == null ? "Unspecified" : x) + ", " + (y == null ? "Unspecified" : y);
    }

    public String toString() {
        return getLabel() + ": " + format();
    }
}
