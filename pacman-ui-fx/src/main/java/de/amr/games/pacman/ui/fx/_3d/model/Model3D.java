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

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * This is a part of the model created by Gianmarco Cavallaccio (https://www.artstation.com/gianmart). I extracted the
 * meshes for Pac-Man and a ghost into separate obj files.
 * 
 * @author Armin Reichert
 */
public class Model3D {

	private static Model3D instance;

	public static Model3D get() {
		if (instance == null) {
			instance = new Model3D();
		}
		return instance;
	}

	private static Translate centeredOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scaled(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	private ObjModel pacManModel;
	private ObjModel ghostModel;

	private Model3D() {
		pacManModel = new ObjModel(getClass().getResource("/common/gianmarco/pacman.obj"));
		ghostModel = new ObjModel(getClass().getResource("/common/gianmarco/ghost.obj"));
	}

	public Group createPac(Color skullColor, Color eyesColor, Color palateColor) {
		MeshView skull = pacManModel.createMeshView("Sphere_yellow_packman");
		skull.setMaterial(new PhongMaterial(skullColor));

		MeshView eyes = pacManModel.createMeshView("Sphere.008_Sphere.010_grey_wall");
		eyes.setMaterial(new PhongMaterial(eyesColor));

		MeshView palate = pacManModel.createMeshView("Sphere_grey_wall");
		palate.setMaterial(new PhongMaterial(palateColor));

		Translate centered = centeredOverOrigin(skull);
		skull.getTransforms().add(centered);
		eyes.getTransforms().add(centered);
		palate.getTransforms().add(centered);

		Stream.of(skull, eyes, palate).forEach(meshView -> meshView.drawModeProperty().bind(Env.drawMode3D));

		Group pacman = new Group(skull, eyes, palate);
		pacman.getTransforms().add(scaled(pacman, 8.5));
		pacman.getTransforms().add(new Rotate(90, Rotate.X_AXIS));

		return pacman;
	}

	public Shape3D pacSkull(Group pac) {
		return (Shape3D) pac.getChildren().get(0);
	}

	public Shape3D pacEyes(Group pac) {
		return (Shape3D) pac.getChildren().get(1);
	}

	public Shape3D pacPalate(Group pac) {
		return (Shape3D) pac.getChildren().get(2);
	}

	public Group createGhost(Color skinColor, Color eyeBallColor, Color pupilColor) {
		MeshView skin = ghostModel.createMeshView("Sphere.004_Sphere.034_light_blue_ghost");
		skin.setMaterial(new PhongMaterial(skinColor));

		MeshView eyeBalls = ghostModel.createMeshView("Sphere.009_Sphere.036_white");
		eyeBalls.setMaterial(new PhongMaterial(eyeBallColor));

		MeshView pupils = ghostModel.createMeshView("Sphere.010_Sphere.039_grey_wall");
		pupils.setMaterial(new PhongMaterial(pupilColor));

		Stream.of(skin, eyeBalls, pupils).forEach(meshView -> meshView.drawModeProperty().bind(Env.drawMode3D));

		Group eyes = new Group(pupils, eyeBalls);

		Group ghost = new Group(skin, eyes);
		ghost.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		ghost.getTransforms().add(scaled(ghost, World.TS));

		Translate centered = centeredOverOrigin(skin);
		skin.getTransforms().add(centered);
		eyes.getTransforms().add(centered);

		return ghost;
	}

	public Shape3D ghostSkin(Group ghost) {
		return (Shape3D) ghost.getChildren().get(0);
	}

	public Group ghostEyes(Group ghost) {
		return (Group) ghost.getChildren().get(1);
	}

	public Shape3D ghostEyesPupils(Group ghost) {
		return (Shape3D) ghostEyes(ghost).getChildren().get(2);
	}

	public Shape3D ghostEyesBalls(Group ghost) {
		return (Shape3D) ghostEyes(ghost).getChildren().get(1);
	}

}