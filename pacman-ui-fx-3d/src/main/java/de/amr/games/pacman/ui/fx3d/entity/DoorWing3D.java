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
package de.amr.games.pacman.ui.fx3d.entity;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.checkTileNotNull;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx3d.app.AppRes3d;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

/**
 * Part a ghosthouse door.
 * 
 * @author Armin Reichert
 */
public class DoorWing3D {

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	private final ObjectProperty<PhongMaterial> barMaterialPy = new SimpleObjectProperty<>(this, "barMaterial",
			new PhongMaterial(Color.PINK));

	private final Group root = new Group();

	private PhongMaterial doorOpenMaterial;
	private PhongMaterial doorClosedMaterial;

	public DoorWing3D(Vector2i tile, Color color) {
		checkTileNotNull(tile);
		checkNotNull(color);

		doorClosedMaterial = AppRes3d.Manager.coloredMaterial(color); // TODO
		doorOpenMaterial = AppRes3d.Manager.coloredMaterial(AppRes3d.Manager.color(Color.gray(0.8), 0.1)); // TODO

		for (int i = 0; i < 2; ++i) {
			var vbar = new Cylinder(1, 8);
			vbar.materialProperty().bind(barMaterialPy);
			double x = tile.x() * 8 + i * 4 + 2;
			double y = tile.y() * 8 + 4;
			vbar.setTranslateX(x);
			vbar.setTranslateY(y);
			vbar.setTranslateZ(-4);
			vbar.setRotationAxis(Rotate.X_AXIS);
			vbar.setRotate(90);
			vbar.drawModeProperty().bind(drawModePy);
			root.getChildren().add(vbar);
		}
		var hbar = new Cylinder(0.5, 9);
		hbar.materialProperty().bind(barMaterialPy);
		hbar.setTranslateX(tile.x() * 8 + 4);
		hbar.setTranslateY(tile.y() * 8 + 4);
		hbar.setTranslateZ(-4);
		hbar.setRotationAxis(Rotate.Z_AXIS);
		hbar.setRotate(90);
		root.getChildren().add(hbar);
	}

	public Node getRoot() {
		return root;
	}

	public void setOpen(boolean open) {
		barMaterialPy.set(open ? doorOpenMaterial : doorClosedMaterial);
	}
}