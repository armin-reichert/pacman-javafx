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
package de.amr.games.pacman.ui.fx.v3d.entity;

import static de.amr.games.pacman.lib.Globals.requirePositive;
import static java.util.Objects.requireNonNull;

import de.amr.games.pacman.ui.fx.rendering2d.GhostColoring;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx.v3d.app.AppRes3d;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

	private final Group root;
	private final Group eyesGroup;
	private final Group dressGroup;
	private final Shape3D dressShape;
	private final Shape3D eyeballsShape;
	private final Shape3D pupilsShape;

	private final GhostColoring coloring;
	private final ObjectProperty<Color> dressColorPy = new SimpleObjectProperty<>(this, "dressColor", Color.ORANGE);
	private final ObjectProperty<Color> eyeballsColorPy = new SimpleObjectProperty<>(this, "eyeballsColor", Color.WHITE);
	private final ObjectProperty<Color> pupilsColorPy = new SimpleObjectProperty<>(this, "pupilsColor", Color.BLUE);

	private ParallelTransition flashingAnimation;
	private ColorFlashing dressFlashingAnimation;
	private ColorFlashing pupilsFlashingAnimation;

	public ColoredGhost3D(Model3D model3D, GhostColoring coloring, double size) {
		requireNonNull(model3D);
		requireNonNull(coloring);
		requirePositive(size, "ColoredGhost3D size must be positive but is %f");

		this.coloring = coloring;

		dressShape = new MeshView(model3D.mesh(AppRes3d.Models3D.MESH_ID_GHOST_DRESS));
		dressShape.setMaterial(Ufx.createColorBoundMaterial(dressColorPy));
		dressColorPy.set(coloring.dress());

		eyeballsShape = new MeshView(model3D.mesh(AppRes3d.Models3D.MESH_ID_GHOST_EYEBALLS));
		eyeballsShape.setMaterial(Ufx.createColorBoundMaterial(eyeballsColorPy));
		eyeballsColorPy.set(coloring.eyeballs());

		pupilsShape = new MeshView(model3D.mesh(AppRes3d.Models3D.MESH_ID_GHOST_PUPILS));
		pupilsShape.setMaterial(Ufx.createColorBoundMaterial(pupilsColorPy));
		pupilsColorPy.set(coloring.pupils());

		var centerTransform = Model3D.centerOverOrigin(dressShape);
		dressShape.getTransforms().add(centerTransform);

		dressGroup = new Group(dressShape);

		eyesGroup = new Group(pupilsShape, eyeballsShape);
		eyesGroup.getTransforms().add(centerTransform);

		root = new Group(dressGroup, eyesGroup);

		// TODO check this: new obj importer has all meshes upside-down and backwards. Why?
		root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		root.getTransforms().add(Model3D.scale(root, size));
	}

	public Node getRoot() {
		return root;
	}

	public Group getEyesGroup() {
		return eyesGroup;
	}

	public Group getDressGroup() {
		return dressGroup;
	}

	public Shape3D dressShape() {
		return dressShape;
	}

	public Shape3D eyeballsShape() {
		return eyeballsShape;
	}

	public Shape3D pupilsShape() {
		return pupilsShape;
	}

	public void appearFlashing(int numFlashes, double durationSeconds) {
		ensureFlashingAnimationIsPlaying(numFlashes, durationSeconds);
		dressColorPy.bind(dressFlashingAnimation.colorPy);
		eyeballsColorPy.set(coloring.eyeballsFrightened());
		pupilsColorPy.bind(pupilsFlashingAnimation.colorPy);
		dressShape.setVisible(true);
	}

	public void appearFrightened() {
		dressColorPy.unbind();
		dressColorPy.set(coloring.dressFrightened());
		eyeballsColorPy.set(coloring.eyeballsFrightened());
		pupilsColorPy.unbind();
		pupilsColorPy.set(coloring.pupilsFrightened());
		dressShape.setVisible(true);
		ensureFlashingAnimationIsStopped();
	}

	public void appearNormal() {
		dressColorPy.unbind();
		dressColorPy.set(coloring.dress());
		eyeballsColorPy.set(coloring.eyeballs());
		pupilsColorPy.unbind();
		pupilsColorPy.set(coloring.pupils());
		dressShape.setVisible(true);
		ensureFlashingAnimationIsStopped();
	}

	public void appearEyesOnly() {
		appearNormal();
		dressShape.setVisible(false);
	}

	private void createFlashingAnimation(int numFlashes, double durationSeconds) {
		dressFlashingAnimation = new ColorFlashing(coloring.dressFrightened(), coloring.dressFlashing(), durationSeconds,
				numFlashes);
		pupilsFlashingAnimation = new ColorFlashing(coloring.pupilsFrightened(), coloring.pupilsFlashing(), durationSeconds,
				numFlashes);
		flashingAnimation = new ParallelTransition(dressFlashingAnimation, pupilsFlashingAnimation);
	}

	private void ensureFlashingAnimationIsPlaying(int numFlashes, double durationSeconds) {
		if (flashingAnimation == null) {
			createFlashingAnimation(numFlashes, durationSeconds);
		}
		if (flashingAnimation.getStatus() != Status.RUNNING) {
			flashingAnimation.playFromStart();
		}
	}

	private void ensureFlashingAnimationIsStopped() {
		if (flashingAnimation != null && flashingAnimation.getStatus() == Status.RUNNING) {
			flashingAnimation.stop();
			flashingAnimation = null;
		}
	}
}