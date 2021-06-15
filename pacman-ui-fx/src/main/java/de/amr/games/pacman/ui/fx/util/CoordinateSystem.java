package de.amr.games.pacman.ui.fx.util;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

/**
 * Shows coordinates axes (x-axis=red, y-axis=green, z-axis=blue).
 * 
 * @author Armin Reichert
 */
public class CoordinateSystem extends Group {

	public CoordinateSystem(double axisLength) {
		Sphere origin = new Sphere(1);
		origin.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		Cylinder xAxis = createAxis(Color.RED.brighter(), axisLength);
		Cylinder yAxis = createAxis(Color.GREEN.brighter(), axisLength);
		Cylinder zAxis = createAxis(Color.BLUE.brighter(), axisLength / 2);

		xAxis.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
		zAxis.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

		getChildren().addAll(origin, xAxis, yAxis, zAxis);
	}

	// Cylinder height points to y-direction
	private Cylinder createAxis(Color color, double height) {
		Cylinder axis = new Cylinder(0.25, height);
		axis.setMaterial(new PhongMaterial(color));
		return axis;
	}
}