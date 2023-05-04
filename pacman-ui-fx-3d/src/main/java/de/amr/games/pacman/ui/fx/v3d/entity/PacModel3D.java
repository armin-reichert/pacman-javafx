/*
MIT License

Copyright (c) 2012-2023 Armin Reichert

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

import java.net.URL;
import java.util.stream.Stream;

import de.amr.games.pacman.ui.fx.rendering2d.MsPacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.PacManColoring;
import de.amr.games.pacman.ui.fx.v3d.app.Game3d;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * Pac-Man 3D model.
 * 
 * @author Armin Reichert
 */
public class PacModel3D extends Model3D {

	private static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	private static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	private static final String MESH_ID_PALATE = "Sphere_grey_wall";

	public static MeshView headMeshView(Node pacShape) {
		return meshView(pacShape, MESH_ID_HEAD);
	}

	public static MeshView eyesMeshView(Node pacShape) {
		return meshView(pacShape, MESH_ID_EYES);
	}

	public static MeshView palateMeshView(Node pacShape) {
		return meshView(pacShape, MESH_ID_PALATE);
	}

	public PacModel3D(URL url) {
		super(url);
	}

	/**
	 * @param size     Pac-Man size in pixels
	 * @param coloring colors
	 * @return root node of Pac-Man parts
	 */
	public Node createPacManNode(double size, PacManColoring coloring) {
		requirePositive(size, "Pac-Man 3D shape size must be positive but is %f");
		requireNonNull(coloring);

		return new Group(createShape(size, coloring.headColor(), coloring.eyesColor(), coloring.palateColor()));
	}

	/**
	 * @param size     Ms. Pac-Man size in pixels
	 * @param coloring colors
	 * @return root node of Ms. Pac-Man parts
	 */
	public Node createMsPacManNode(double size, MsPacManColoring coloring) {
		requirePositive(size, "Ms. Pac-Man 3D shape size must be positive but is %f");
		requireNonNull(coloring);

		return new Group(createShape(size, coloring.headColor(), coloring.eyesColor(), coloring.palateColor()),
				createBeautyAccessories(size, coloring.headColor(), coloring.hairBowColor(), coloring.hairBowPearlsColor()));
	}

	private Group createShape(double size, Color headColor, Color eyesColor, Color palateColor) {
		var head = new MeshView(mesh(MESH_ID_HEAD));
		head.setId(cssID(MESH_ID_HEAD));
		head.setMaterial(Game3d.ResMgr.coloredMaterial(headColor));

		var eyes = new MeshView(mesh(MESH_ID_EYES));
		eyes.setId(cssID(MESH_ID_EYES));
		eyes.setMaterial(Game3d.ResMgr.coloredMaterial(eyesColor));

		var palate = new MeshView(mesh(MESH_ID_PALATE));
		palate.setId(cssID(MESH_ID_PALATE));
		palate.setMaterial(Game3d.ResMgr.coloredMaterial(palateColor));

		var centerTransform = Model3D.centerOverOrigin(head);
		Stream.of(head, eyes, palate).map(Node::getTransforms).forEach(tf -> tf.add(centerTransform));

		var root = new Group(head, eyes, palate);
		root.getTransforms().add(Model3D.scale(root, size));
		// TODO new obj importer has all meshes upside-down and backwards. Why?
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

		return root;
	}

	private Group createBeautyAccessories(double pacSize, Color headColor, Color bowColor, Color pearlColor) {
		var bowMaterial = Game3d.ResMgr.coloredMaterial(bowColor);

		var bowLeft = new Sphere(1.2);
		bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
		bowLeft.setMaterial(bowMaterial);

		var bowRight = new Sphere(1.2);
		bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
		bowRight.setMaterial(bowMaterial);

		var pearlMaterial = Game3d.ResMgr.coloredMaterial(pearlColor);

		var pearlLeft = new Sphere(0.4);
		pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
		pearlLeft.setMaterial(pearlMaterial);

		var pearlRight = new Sphere(0.4);
		pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
		pearlRight.setMaterial(pearlMaterial);

		var beautySpot = new Sphere(0.25);
		beautySpot.setMaterial(Game3d.ResMgr.coloredMaterial(Color.rgb(100, 100, 100)));
		beautySpot.getTransforms().addAll(new Translate(-1.8, -3.7, -1));

		var silicone = Game3d.ResMgr.coloredMaterial(headColor.deriveColor(0, 1.0, 0.96, 1.0));

		var boobLeft = new Sphere(1.5);
		boobLeft.setMaterial(silicone);
		boobLeft.getTransforms().addAll(new Translate(-1.5, -1.2, pacSize * 0.35));

		var boobRight = new Sphere(1.5);
		boobRight.setMaterial(silicone);
		boobRight.getTransforms().addAll(new Translate(-1.5, 1.2, pacSize * 0.35));

		return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
	}
}