package org.openforis.collect.android.viewmodel;

/**
 * @author Daniel Wiell
 */
public class UiCoordinateAttribute extends UiAttribute {
    private Double x;
    private Double y;

    public UiCoordinateAttribute(int id, Definition definition) {
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

    public String toString() {
        return getLabel() + ": " + (x == null ? "Unspecified" : x) + ", " + (y == null ? "Unspecified" : y);
    }
}
