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

import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

/**
 * Global stuff.
 * 
 * @author Armin Reichert
 */
public class Env {

	private Env() {
	}

	// Public properties

	public static final BooleanProperty $axesVisible = new SimpleBooleanProperty(false);
	public static final BooleanProperty $debugUI = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode> $drawMode3D = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final IntegerProperty $mazeResolution = new SimpleIntegerProperty(8);
	public static final DoubleProperty $mazeWallHeight = new SimpleDoubleProperty(2.0);
	public static final BooleanProperty $paused = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Perspective> $perspective = new SimpleObjectProperty<>(
			Perspective.CAM_NEAR_PLAYER);
	public static final BooleanProperty $mazeFloorHasTexture = new SimpleBooleanProperty(false);
	public static final BooleanProperty $3D = new SimpleBooleanProperty(true);

	public static void toggle(BooleanProperty bp) {
		bp.set(!bp.get());
	}
}