/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.scene.cams;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class CamConfiguration {

	public final DoubleProperty nearClipPy = new SimpleDoubleProperty(0.1);
	public final DoubleProperty farClipPy = new SimpleDoubleProperty(100.0);
	public final DoubleProperty fieldOfViewPy = new SimpleDoubleProperty(30.0);
	public final DoubleProperty translateXPy = new SimpleDoubleProperty();
	public final DoubleProperty translateYPy = new SimpleDoubleProperty();
	public final DoubleProperty translateZPy = new SimpleDoubleProperty();
	public final ObjectProperty<Point3D> rotationAxisPy = new SimpleObjectProperty<>(Rotate.X_AXIS);
	public final DoubleProperty rotatePy = new SimpleDoubleProperty();
}