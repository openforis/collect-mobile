package org.openforis.collect.android.util;

public class CoordinateTranslator {

    public static int[] toMargins(double compassBearing, double destinationBearing, double radius) {
        double t = 90 - (destinationBearing - compassBearing); // Unit circle angle
        double x = Math.cos(Math.toRadians(t)); // Unit coordinates
        double y = Math.sin(Math.toRadians(t));

        // Translate into margins
        double leftMargin = radius + radius * x;
        double topMargin = radius - radius * y;
        return new int[]{(int) Math.round(leftMargin), (int) Math.round(topMargin)};
    }
}
