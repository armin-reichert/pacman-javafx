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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx._3d.ObjModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

/**
 * 3D pellet.
 * 
 * @author Armin Reichert
 */
public class Pellet3D implements Eatable3D {

	private static final ObjModel OBJ_MODEL = new ObjModel("model3D/12206_Fruit_v1_L3.obj");
	private static final String MESH_NAME = "Fruit";

	private final Shape3D shape = OBJ_MODEL.createMeshView(MESH_NAME);

	public Pellet3D(PhongMaterial material, double radius) {
		shape.setMaterial(material);
		shape.setRotationAxis(Rotate.Z_AXIS);
		shape.setRotate(90);
		var bounds = shape.getBoundsInLocal();
		var max = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
		var scaling = new Scale(2 * radius / max, 2 * radius / max, 2 * radius / max);
		shape.getTransforms().setAll(scaling);
	}

	public void setTile(Vector2i tile) {
		shape.setUserData(tile);
		shape.setTranslateX(tile.x() * TS + HTS);
		shape.setTranslateY(tile.y() * TS + HTS);
		shape.setTranslateZ(-HTS + 1);
	}

	@Override
	public Node getRoot() {
		return shape;
	}

	@Override
	public void eat() {
		var hideAfterDelay = Ufx.afterSeconds(0.05, () -> shape.setVisible(false));
		hideAfterDelay.play();
	}

	@Override
	public String toString() {
		return String.format("[Pellet, tile: %s]", tile());
	}
}