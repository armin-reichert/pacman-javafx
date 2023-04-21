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

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.model.common.Validator.requirePositive;
import static de.amr.games.pacman.model.common.world.World.tileAt;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.util.Vector3f;
import javafx.animation.Animation;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

/**
 * 3D pellet.
 * 
 * @author Armin Reichert
 */
public class Pellet3D implements Eatable3D {

	private final Shape3D shape;

	public Pellet3D(Model3D model3D, double radius) {
		requireNonNull(model3D);
		requirePositive(radius, "Pellet3D radius must be positive but is %f");

		shape = model3D.meshView(AppRes.Models3D.MESH_ID_PELLET);
		shape.setRotationAxis(Rotate.Z_AXIS);
		shape.setRotate(90);
		shape.setUserData(this);
		var bounds = shape.getBoundsInLocal();
		var max = Math.max(Math.max(bounds.getWidth(), bounds.getHeight()), bounds.getDepth());
		var scaling = new Scale(2 * radius / max, 2 * radius / max, 2 * radius / max);
		shape.getTransforms().add(scaling);
	}

	public void placeAtTile(Vector2i tile) {
		requireNonNull(tile);

		shape.setTranslateX(tile.x() * TS + HTS);
		shape.setTranslateY(tile.y() * TS + HTS);
		shape.setTranslateZ(-HTS);
	}

	@Override
	public Vector3f position() {
		return new Vector3f((float) shape.getTranslateX(), (float) shape.getTranslateY(), (float) shape.getTranslateZ());
	}

	@Override
	public Vector2i tile() {
		return tileAt((float) shape.getTranslateX(), (float) shape.getTranslateY());
	}

	@Override
	public Shape3D getRoot() {
		return shape;
	}

	@Override
	public void eat() {
		var hideAfterDelay = Ufx.afterSeconds(0.05, () -> shape.setVisible(false));
		hideAfterDelay.play();
	}

	@Override
	public Optional<Animation> getEatenAnimation() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return String.format("[Pellet, tile: %s, %s]", tile(), shape);
	}
}