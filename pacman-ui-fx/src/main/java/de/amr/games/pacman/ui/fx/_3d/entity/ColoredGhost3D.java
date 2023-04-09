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

import java.util.Objects;

import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.util.Ufx;
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
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	private final Group root;
	private final Group eyesGroup;
	private final Shape3D dress;
	private final Shape3D eyeBalls;
	private final Shape3D pupils;

	private final GhostColoring coloring;
	private final ObjectProperty<Color> dressColorPy = new SimpleObjectProperty<>(this, "dressColor", Color.ORANGE);
	private final ObjectProperty<Color> eyeBallsColorPy = new SimpleObjectProperty<>(this, "eyeBallsColor", Color.WHITE);
	private final ObjectProperty<Color> pupilsColorPy = new SimpleObjectProperty<>(this, "pupilsColor", Color.BLUE);

	private ParallelTransition flashing;
	private ColorFlashing dressFlashing;
	private ColorFlashing pupilsFlashing;

	public ColoredGhost3D(Model3D model3D, GhostColoring coloring, double size) {
		this.coloring = Objects.requireNonNull(coloring, "Ghost colors must not be null");

		dress = new MeshView(model3D.mesh(AppResources.MESH_ID_GHOST_DRESS));
		dress.setMaterial(Ufx.createColorBoundMaterial(dressColorPy));
		dressColorPy.set(coloring.normalDress());

		eyeBalls = new MeshView(model3D.mesh(AppResources.MESH_ID_GHOST_EYE_BALLS));
		eyeBalls.setMaterial(Ufx.createColorBoundMaterial(eyeBallsColorPy));
		eyeBallsColorPy.set(coloring.normalEyeBalls());

		pupils = new MeshView(model3D.mesh(AppResources.MESH_ID_GHOST_PUPILS));
		pupils.setMaterial(Ufx.createColorBoundMaterial(pupilsColorPy));
		pupilsColorPy.set(coloring.normalPupils());

		var centerTransform = centerOverOrigin(dress);
		dress.getTransforms().add(centerTransform);

		eyesGroup = new Group(pupils, eyeBalls);
		eyesGroup.getTransforms().add(centerTransform);

		root = new Group(dress, eyesGroup);
		// TODO new obj importer has all meshes upside-down and backwards. Why?
		root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		root.getTransforms().add(scale(root, size));
	}

	public Node getRoot() {
		return root;
	}

	public Group getEyesGroup() {
		return eyesGroup;
	}

	public Shape3D dress() {
		return dress;
	}

	public Shape3D eyeBalls() {
		return eyeBalls;
	}

	public Shape3D pupils() {
		return pupils;
	}

	public void appearFlashing(int numFlashes, double durationSeconds) {
		ensureFlashingPlaying(numFlashes, durationSeconds);
		dressColorPy.bind(dressFlashing.colorPy);
		eyeBallsColorPy.set(coloring.frightenedEyeBalls());
		pupilsColorPy.bind(pupilsFlashing.colorPy);
		dress.setVisible(true);
	}

	public void appearFrightened() {
		dressColorPy.unbind();
		dressColorPy.set(coloring.frightenedDress());
		eyeBallsColorPy.set(coloring.frightenedEyeBalls());
		pupilsColorPy.unbind();
		pupilsColorPy.set(coloring.frightendPupils());
		dress.setVisible(true);
		ensureFlashingStopped();
	}

	public void appearNormal() {
		dressColorPy.unbind();
		dressColorPy.set(coloring.normalDress());
		eyeBallsColorPy.set(coloring.normalEyeBalls());
		pupilsColorPy.unbind();
		pupilsColorPy.set(coloring.normalPupils());
		dress.setVisible(true);
		ensureFlashingStopped();
	}

	public void appearEyesOnly() {
		appearNormal();
		dress.setVisible(false);
	}

	private void createFlashing(int numFlashes, double durationSeconds) {
		dressFlashing = new ColorFlashing(coloring.frightenedDress(), coloring.flashingDress(), durationSeconds,
				numFlashes);
		pupilsFlashing = new ColorFlashing(coloring.frightendPupils(), coloring.flashingPupils(), durationSeconds,
				numFlashes);
		flashing = new ParallelTransition(dressFlashing, pupilsFlashing);
	}

	private void ensureFlashingPlaying(int numFlashes, double durationSeconds) {
		if (flashing == null) {
			createFlashing(numFlashes, durationSeconds);
		}
		if (flashing.getStatus() != Status.RUNNING) {
			flashing.playFromStart();
		}
	}

	private void ensureFlashingStopped() {
		if (flashing != null && flashing.getStatus() == Status.RUNNING) {
			flashing.stop();
			flashing = null;
		}
	}
}