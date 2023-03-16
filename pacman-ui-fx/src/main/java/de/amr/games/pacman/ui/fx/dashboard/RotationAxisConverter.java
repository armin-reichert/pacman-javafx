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

package de.amr.games.pacman.ui.fx.dashboard;

import java.util.Objects;

import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.util.StringConverter;

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