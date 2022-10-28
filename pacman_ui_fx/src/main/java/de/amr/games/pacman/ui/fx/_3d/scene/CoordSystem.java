/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.scene;

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
public class CoordSystem extends Group {

	public CoordSystem() {
		this(1000);
	}

	public CoordSystem(double axisLength) {
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