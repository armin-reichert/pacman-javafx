/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import java.util.stream.Stream;

import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Tree representing the complete Pac-Man 3D shape.
 * 
 * @author Armin Reichert
 */
public class PacShape3D {

	public static final String ID_HEAD = "head";
	public static final String ID_EYES = "eyes";
	public static final String ID_PALATE = "palate";

	/**
	 * @param model3D     the 3D-model
	 * @param size        Pac-Man size in pixels
	 * @param headColor   color of head shape
	 * @param eyesColor   color of eyes
	 * @param palateColor color of palate
	 * @return tree of Pac-Man parts
	 */
	public static Group createPacManShape(Model3D model3D, double size, Color headColor, Color eyesColor,
			Color palateColor) {
		return new Group(createShape(model3D, size, headColor, eyesColor, palateColor));
	}

	/**
	 * @param model3D     the 3D-model
	 * @param size        Ms. Pac-Man size in pixels
	 * @param headColor   color of head shape
	 * @param eyesColor   color of eyes
	 * @param palateColor color of palate
	 * @param bowColor    color of hair bows
	 * @param pearlsColor color of "pearls" connecting hair bows
	 * @return tree of Ms. Pac-Man parts
	 */
	public static Group createMsPacManShape(Model3D model3D, double size, Color headColor, Color eyesColor,
			Color palateColor, Color bowColor, Color pearlsColor) {
		return new Group(createShape(model3D, size, headColor, eyesColor, palateColor),
				createBeautyAccessories(size, bowColor, pearlsColor));
	}

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	private static Group createShape(Model3D model3D, double size, Color headColor, Color eyesColor, Color palateColor) {
		var head = new MeshView(model3D.mesh(AppResources.MESH_ID_HEAD));
		head.setId(ID_HEAD);
		head.setMaterial(ResourceMgr.coloredMaterial(headColor));

		var eyes = new MeshView(model3D.mesh(AppResources.MESH_ID_EYES));
		eyes.setId(ID_EYES);
		eyes.setMaterial(ResourceMgr.coloredMaterial(eyesColor));

		var palate = new MeshView(model3D.mesh(AppResources.MESH_ID_PALATE));
		palate.setId(ID_PALATE);
		palate.setMaterial(ResourceMgr.coloredMaterial(palateColor));

		var centerTransform = centerOverOrigin(head);
		Stream.of(head, eyes, palate).map(Node::getTransforms).forEach(tf -> tf.add(centerTransform));

		var root = new Group(head, eyes, palate);
		root.getTransforms().add(scale(root, size));
		// TODO new obj importer has all meshes upside-down and backwards. Why?
		root.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		root.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		root.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));

		return root;
	}

	private static Group createBeautyAccessories(double pacSize, Color bowColor, Color pearlColor) {
		var root = new Group();

		var bowMaterial = ResourceMgr.coloredMaterial(bowColor);

		var bowLeft = new Sphere(1.2);
		bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
		bowLeft.setMaterial(bowMaterial);

		var bowRight = new Sphere(1.2);
		bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
		bowRight.setMaterial(bowMaterial);

		var pearlMaterial = ResourceMgr.coloredMaterial(pearlColor);

		var pearlLeft = new Sphere(0.6);
		pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
		pearlLeft.setMaterial(pearlMaterial);

		var pearlRight = new Sphere(0.6);
		pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
		pearlRight.setMaterial(pearlMaterial);

		var beautySpotMaterial = ResourceMgr.coloredMaterial(Color.rgb(100, 100, 100));
		var beautySpot = new Sphere(0.25);
		beautySpot.setMaterial(beautySpotMaterial);
		beautySpot.getTransforms().addAll(new Translate(-2.0, -3.7, -pacSize * 0.3));

		root.getChildren().addAll(bowLeft, bowRight, pearlLeft, pearlRight, beautySpot);

		return root;
	}
}