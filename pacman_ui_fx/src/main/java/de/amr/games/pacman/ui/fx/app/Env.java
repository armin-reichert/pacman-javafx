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
package de.amr.games.pacman.ui.fx.app;

import java.util.List;

import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

/**
 * Global stuff.
 * 
 * @author Armin Reichert
 */
public class Env {

	private Env() {
	}

	public static final List<String> FLOOR_TEXTURES = List.of("none", "penrose-tiling.jpg", "escher-texture.jpg");

	public static final BooleanProperty axesVisiblePy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color> bgColorPy = new SimpleObjectProperty<>(Color.CORNFLOWERBLUE);
	public static final ObjectProperty<DrawMode> drawMode3DPy = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(Color.rgb(10, 10, 70));
	public static final StringProperty floorTexturePy = new SimpleStringProperty(FLOOR_TEXTURES.get(0));
	public static final IntegerProperty mazeResolutionPy = new SimpleIntegerProperty(4);
	public static final DoubleProperty mazeWallHeightPy = new SimpleDoubleProperty(2.0);
	public static final BooleanProperty pausedPy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final DoubleProperty pipOpacityPy = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty pipSceneHeightPy = new SimpleDoubleProperty(ArcadeWorld.TILES_Y * World.TS);
	public static final BooleanProperty pipVisiblePy = new SimpleBooleanProperty(false);
	public static final BooleanProperty showDebugInfoPy = new SimpleBooleanProperty(false);
	public static final BooleanProperty squirtingPy = new SimpleBooleanProperty(false);
	public static final IntegerProperty targetFrameratePy = new SimpleIntegerProperty(60);
	public static final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(false);
	public static final BooleanProperty use3DScenePy = new SimpleBooleanProperty(true);

	public static void toggle(BooleanProperty booleanProperty) {
		booleanProperty.set(!booleanProperty.get());
	}
}