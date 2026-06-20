/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Shows coordinates axes (x-axis=red, y-axis=green, z-axis=blue).
 */
public class CoordinateSystem extends Group {

    public CoordinateSystem() {
        this(1000);
    }

    public CoordinateSystem(double axisLength) {
        Sphere origin = new Sphere(1);
        origin.setMaterial(new PhongMaterial(Color.CHOCOLATE));

        Cylinder xAxis = createAxis(Color.RED.brighter(), axisLength);
        xAxis.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        Sphere xAxisMarker = new Sphere(1);
        xAxisMarker.setMaterial(new PhongMaterial(Color.RED));
        xAxisMarker.setTranslateX(10);

        Cylinder yAxis = createAxis(Color.GREEN.brighter(), axisLength);
        Sphere yAxisMarker = new Sphere(1);
        yAxisMarker.setMaterial(new PhongMaterial(Color.GREEN));
        yAxisMarker.setTranslateY(10);

        Cylinder zAxis = createAxis(Color.BLUE.brighter(), axisLength / 2);
        zAxis.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        Sphere zAxisMarker = new Sphere(1);
        zAxisMarker.setMaterial(new PhongMaterial(Color.BLUE));
        zAxisMarker.setTranslateZ(10);

        getChildren().addAll(origin, xAxis, xAxisMarker, yAxis, yAxisMarker, zAxis, zAxisMarker);
    }

    // Cylinder height points to y-direction
    private Cylinder createAxis(Color color, double height) {
        Cylinder axis = new Cylinder(0.25, height);
        axis.setMaterial(new PhongMaterial(color));
        return axis;
    }
}