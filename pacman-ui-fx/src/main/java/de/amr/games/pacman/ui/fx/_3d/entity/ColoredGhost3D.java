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

import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import de.amr.games.pacman.ui.fx._3d.ObjModel;
import de.amr.games.pacman.ui.fx._3d.animation.ColorFlashing;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation.Status;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public class ColoredGhost3D {

	private static final ObjModel OBJ_MODEL = new ObjModel("model3D/ghost.obj");
	private static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String MESH_ID_GHOST_EYE_BALLS = "Sphere.009_Sphere.036_white";
	private static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	private static final double SIZE = 8.5;

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	public static Group createGhost3D(Color dressColor, Color eyeBallColor, Color pupilColor) {
		var dress = OBJ_MODEL.createMeshView(MESH_ID_GHOST_DRESS);
		dress.setMaterial(new PhongMaterial(dressColor));

		var eyeBalls = OBJ_MODEL.createMeshView(MESH_ID_GHOST_EYE_BALLS);
		eyeBalls.setMaterial(new PhongMaterial(eyeBallColor));

		var pupils = OBJ_MODEL.createMeshView(MESH_ID_GHOST_PUPILS);
		pupils.setMaterial(new PhongMaterial(pupilColor));

		var center = centerOverOrigin(dress);
		dress.getTransforms().add(center);

		var eyes = new Group(pupils, eyeBalls);
		eyes.getTransforms().add(center);

		var ghost3D = new Group(dress, eyes);
		ghost3D.getTransforms().add(new Translate(0, 0, -1.5));
		ghost3D.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		ghost3D.getTransforms().add(scale(ghost3D, SIZE));

		return ghost3D;
	}

	private final Group root;
	private final Shape3D dress;
	private final Shape3D eyeBalls;
	private final Shape3D pupils;

	private final GhostColoring coloring;
	private final ObjectProperty<Color> dressColorPy;
	private final ObjectProperty<Color> eyeBallsColorPy;
	private final ObjectProperty<Color> pupilsColorPy;

	private ParallelTransition flashing;
	private ColorFlashing dressFlashing;
	private ColorFlashing pupilsFlashing;

	public ColoredGhost3D(GhostColoring coloring) {
		this.coloring = coloring;
		root = createGhost3D(coloring.normalDress(), coloring.normalEyeBalls(), coloring.normalPupils());
		dressColorPy = new SimpleObjectProperty<>(this, "dressColor", coloring.normalDress());
		eyeBallsColorPy = new SimpleObjectProperty<>(this, "eyeBallsColor", coloring.normalEyeBalls());
		pupilsColorPy = new SimpleObjectProperty<>(this, "pupilsColor", coloring.normalPupils());
		dress = (Shape3D) root.getChildren().get(0);
		dress.setMaterial(Ufx.createColorBoundMaterial(dressColorPy));
		var eyes = (Group) root.getChildren().get(1);
		eyeBalls = (Shape3D) eyes.getChildren().get(1);
		eyeBalls.setMaterial(Ufx.createColorBoundMaterial(eyeBallsColorPy));
		pupils = (Shape3D) eyes.getChildren().get(0);
		pupils.setMaterial(Ufx.createColorBoundMaterial(pupilsColorPy));
	}

	public Node getRoot() {
		return root;
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