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
package de.amr.games.pacman.ui.fx._3d.model;

import java.net.URL;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.util.ObjModel;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * This uses part of a Blender model created by Gianmarco Cavallaccio (https://www.artstation.com/gianmart).
 * <p>
 * 
 * One mesh for Pac-Man and one for a ghost are sufficient because their appearance is managed programmatically.
 * 
 * @author Armin Reichert
 */
public class Model3D {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static URL PAC_MODEL3D_URL = Env.url("model3D/pacman.obj");
	private static URL GHOST_MODEL3D_URL = Env.url("model3D/ghost.obj");

	private static final String MESH_PAC_EYES = "Sphere.008_Sphere.010_grey_wall";
	private static final String MESH_PAC_FACE = "Sphere_yellow_packman";
	private static final String MESH_PAC_PALATE = "Sphere_grey_wall";
	private static final String MESH_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String MESH_GHOST_EYE_BALLS = "Sphere.009_Sphere.036_white";
	private static final String MESH_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	private static final double PAC_SIZE = 9.0;
	private static final double GHOST_SIZE = 8.5;

	private static final Image FACE_TEXTURE = Ufx.image("graphics/gold_sandblasted_specular.jpeg");

	private static Translate centerOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	private ObjModel pacModel;
	private ObjModel ghostModel;

	public Model3D() {
		LOGGER.info("Pac-Man 3D model URL: %s", PAC_MODEL3D_URL);
		pacModel = new ObjModel(PAC_MODEL3D_URL);
		LOGGER.info("Ghost 3D model URL: %s", GHOST_MODEL3D_URL);
		ghostModel = new ObjModel(GHOST_MODEL3D_URL);
	}

	public Group createPac(Color eyesColor, Color palateColor) {
		var faceMaterial = new PhongMaterial();
		faceMaterial.setDiffuseMap(FACE_TEXTURE);

		var face = pacModel.createMeshView(MESH_PAC_FACE);
		face.setMaterial(faceMaterial);

		var eyes = pacModel.createMeshView(MESH_PAC_EYES);
		eyes.setMaterial(new PhongMaterial(eyesColor));

		var palate = pacModel.createMeshView(MESH_PAC_PALATE);
		palate.setMaterial(new PhongMaterial(palateColor));

		var center = centerOverOrigin(face);
		Stream.of(face, eyes, palate).forEach(meshView -> {
			meshView.getTransforms().add(center);
			meshView.drawModeProperty().bind(Env.drawModePy);
		});

		var root3D = new Group(face, eyes, palate);
		root3D.getTransforms().add(new Translate(0, 0, -1));
		root3D.getTransforms().add(scale(root3D, PAC_SIZE));
		root3D.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

		return root3D;
	}

	public Shape3D pacFace(Group pac) {
		return (Shape3D) pac.getChildren().get(0);
	}

	public Shape3D pacEyes(Group pac) {
		return (Shape3D) pac.getChildren().get(1);
	}

	public Shape3D pacPalate(Group pac) {
		return (Shape3D) pac.getChildren().get(2);
	}

	public Group createGhost(Color dressColor, Color eyeBallColor, Color pupilColor) {
		var dress = ghostModel.createMeshView(MESH_GHOST_DRESS);
		dress.setMaterial(new PhongMaterial(dressColor));

		var eyeBalls = ghostModel.createMeshView(MESH_GHOST_EYE_BALLS);
		eyeBalls.setMaterial(new PhongMaterial(eyeBallColor));

		var pupils = ghostModel.createMeshView(MESH_GHOST_PUPILS);
		pupils.setMaterial(new PhongMaterial(pupilColor));

		Stream.of(dress, eyeBalls, pupils).forEach(meshView -> meshView.drawModeProperty().bind(Env.drawModePy));

		var center = centerOverOrigin(dress);
		dress.getTransforms().add(center);

		var eyes = new Group(pupils, eyeBalls);
		eyes.getTransforms().add(center);

		var ghost3D = new Group(dress, eyes);
		ghost3D.getTransforms().add(new Translate(0, 0, -1));
		ghost3D.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		ghost3D.getTransforms().add(scale(ghost3D, GHOST_SIZE));

		return ghost3D;
	}

	public Shape3D ghostDress(Group ghost3D) {
		return (Shape3D) ghost3D.getChildren().get(0);
	}

	public Group ghostEyes(Group ghost3D) {
		return (Group) ghost3D.getChildren().get(1);
	}

	public Shape3D ghostEyePupils(Group ghost3D) {
		return (Shape3D) ghostEyes(ghost3D).getChildren().get(0);
	}

	public Shape3D ghostEyeBalls(Group ghost3D) {
		return (Shape3D) ghostEyes(ghost3D).getChildren().get(1);
	}
}