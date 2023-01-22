/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx;

import java.util.List;

import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

/**
 * @author Armin Reichert
 */
public final class Env3D {

	private Env3D() {
	}

	public static final List<String> FLOOR_TEXTURES = List.of("none", "penrose-tiling.jpg", "escher-texture.jpg");

	public static final BooleanProperty axesVisiblePy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(Color.rgb(0, 0, 33));
	public static final StringProperty floorTexturePy = new SimpleStringProperty("none");
	public static final ObjectProperty<Color> lightColorPy = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final DoubleProperty mazeWallHeightPy = new SimpleDoubleProperty(1.5);
	public static final DoubleProperty mazeWallThicknessPy = new SimpleDoubleProperty(1.5);
	public static final BooleanProperty pacLightedPy = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty squirtingEffectPy = new SimpleBooleanProperty(true);
}