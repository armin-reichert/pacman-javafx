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

import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx._3d.ObjModel;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
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

	private static final ObjModel OBJ_MODEL = new ObjModel(ResourceMgr.urlFromRelPath("model3D/12206_Fruit_v1_L3.obj"));
	private static final String MESH_NAME = "Fruit";

	private Group root;
	private Shape3D shape;
	private Animation animation;

	public Pellet3D(Vector2i tile, PhongMaterial material, double radius) {

		root = new Group();
		shape = OBJ_MODEL.createMeshView(MESH_NAME);
		shape.setMaterial(material);
		root.getChildren().add(shape);

		root.setTranslateX(tile.x() * TS + HTS);
		root.setTranslateY(tile.y() * TS + HTS);
		root.setTranslateZ(-HTS + 1);

		root.setRotationAxis(Rotate.Z_AXIS);
		root.setRotate(90);
		root.setUserData(tile);

		var bounds = shape.getBoundsInLocal();
		var max = Math.max(bounds.getWidth(), bounds.getHeight());
		max = Math.max(max, bounds.getDepth());
		var scaling = new Scale(2 * radius / max, 2 * radius / max, 2 * radius / max);
		root.getTransforms().setAll(scaling);
	}

	@Override
	public Node getRoot() {
		return root;
	}

	@Override
	public void eat() {
		var hideAfterDelay = Ufx.afterSeconds(0.05, () -> root.setVisible(false));
		if (animation != null) {
			new SequentialTransition(hideAfterDelay, animation).play();
		} else {
			hideAfterDelay.play();
		}
	}

	@Override
	public Optional<Animation> getEatenAnimation() {
		return Optional.ofNullable(animation);
	}

	public void setEatenAnimation(Animation animation) {
		this.animation = animation;
	}

	@Override
	public String toString() {
		return String.format("[Pellet, tile: %s]", tile());
	}
}