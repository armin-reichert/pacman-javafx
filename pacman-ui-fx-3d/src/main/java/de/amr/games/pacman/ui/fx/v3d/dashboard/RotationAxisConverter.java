/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.StringConverter;

import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class RotationAxisConverter extends StringConverter<Point3D> {

    private final String txtX;
    private final String txtY;
    private final String txtZ;

    public RotationAxisConverter(String xAxisText, String yAxisText, String zAxisText) {
        txtX = Objects.requireNonNull(xAxisText);
        txtY = Objects.requireNonNull(yAxisText);
        txtZ = Objects.requireNonNull(zAxisText);
    }

    @Override
    public String toString(Point3D p) {
        if (p == null) {
            return "";
        }
        if (p.equals(Rotate.X_AXIS)) {
            return txtX;
        }
        if (p.equals(Rotate.Y_AXIS)) {
            return txtY;
        }
        if (p.equals(Rotate.Z_AXIS)) {
            return txtZ;
        }
        return p.toString();
    }

    @Override
    public Point3D fromString(String string) {
        if (txtX.equals(string))
            return Rotate.X_AXIS;
        if (txtY.equals(string))
            return Rotate.Y_AXIS;
        if (txtZ.equals(string))
            return Rotate.Z_AXIS;
        return Point3D.ZERO;
    }
}