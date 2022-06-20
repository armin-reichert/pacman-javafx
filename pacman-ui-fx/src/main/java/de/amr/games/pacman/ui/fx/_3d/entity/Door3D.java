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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Ghosthouse door.
 * 
 * @author Armin Reichert
 */
public class Door3D {

	public final DoubleProperty doorHeight = new SimpleDoubleProperty(HTS);

	private final Box box;
	private final V2d centerPosition;

	public Door3D(V2i tile, boolean leftWing, Color color) {
		box = new Box(TS - 1.0, 1.0, doorHeight.get());
		box.setUserData(this);
		box.depthProperty().bind(doorHeight.add(2));
		box.setMaterial(new PhongMaterial(color));
		box.setTranslateX((double) tile.x * TS + HTS);
		box.setTranslateY((double) tile.y * TS + HTS);
		box.translateZProperty().bind(doorHeight.divide(-2.0).subtract(0.5));
		box.drawModeProperty().bind(Env.drawMode3D);
		var centerX = leftWing ? (tile.x + 1) * TS : (tile.x - 1) * TS;
		centerPosition = new V2d(centerX, tile.y * TS);
	}

	public V2d getCenterPosition() {
		return centerPosition;
	}

	public Node getNode() {
		return box;
	}

	public void setOpen(boolean open) {
		box.setVisible(!open);
	}
}