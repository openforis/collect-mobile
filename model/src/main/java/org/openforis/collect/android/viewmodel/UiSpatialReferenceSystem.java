package org.openforis.collect.android.viewmodel;

import com.google.common.base.Objects;

/**
 * @author Daniel Wiell
 */
public class UiSpatialReferenceSystem {
    public static final UiSpatialReferenceSystem LAT_LNG_SRS = new UiSpatialReferenceSystem("" +
            "EPSG:4326", "GEOGCS[\"WGS 84\",\n" +
            "    DATUM[\"WGS_1984\",\n" +
            "        SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
            "            AUTHORITY[\"EPSG\",\"7030\"]],\n" +
            "        AUTHORITY[\"EPSG\",\"6326\"]],\n" +
            "    PRIMEM[\"Greenwich\",0,\n" +
            "        AUTHORITY[\"EPSG\",\"8901\"]],\n" +
            "    UNIT[\"degree\",0.01745329251994328,\n" +
            "        AUTHORITY[\"EPSG\",\"9122\"]],\n" +
            "    AUTHORITY[\"EPSG\",\"4326\"]]", "Lat Lng");


    public final String id;
    public final String wellKnownText;
    public final String label;

    public UiSpatialReferenceSystem(String id, String wellKnownText, String label) {
        this.id = id;
        this.wellKnownText = wellKnownText;
        this.label = label;
    }

    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UiSpatialReferenceSystem other = (UiSpatialReferenceSystem) obj;
        return Objects.equal(this.id, other.id);
    }

    public String toString() {
        return label;
    }
}
