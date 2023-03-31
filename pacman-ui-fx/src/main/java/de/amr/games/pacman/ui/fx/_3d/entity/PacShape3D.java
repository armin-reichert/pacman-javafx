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
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public class PacShape3D {
	private static final Model3D HEAD_3D = new Model3D("model3D/pacman.obj");
	private static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	private static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	private static final String MESH_ID_PALATE = "Sphere_grey_wall";

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	private static Group createShape(double size, Color headColor, Color eyesColor, Color palateColor) {
		var head = new MeshView(HEAD_3D.mesh(MESH_ID_HEAD));
		head.setMaterial(ResourceMgr.coloredMaterial(headColor));

		var eyes = new MeshView(HEAD_3D.mesh(MESH_ID_EYES));
		eyes.setMaterial(ResourceMgr.coloredMaterial(eyesColor));

		var palate = new MeshView(HEAD_3D.mesh(MESH_ID_PALATE));
		palate.setMaterial(ResourceMgr.coloredMaterial(palateColor));

		var centerTransform = centerOverOrigin(head);
		Stream.of(head, eyes, palate).forEach(meshView -> meshView.getTransforms().add(centerTransform));

		var headGroup = new Group(head, eyes, palate);
		headGroup.getTransforms().addAll(new Translate(0, 0, -1), scale(headGroup, size), new Rotate(90, Rotate.X_AXIS));

		// TODO new obj importer has all meshes upside-down and backwards. Why?
		headGroup.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
		headGroup.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
		return headGroup;
	}

	public static Group createPacManShape(double size, Color headColor, Color eyesColor, Color palateColor) {
		return new Group(createShape(size, headColor, eyesColor, palateColor));
	}

	public static Group createMsPacManShape(double size, Color headColor, Color eyesColor, Color palateColor) {
		return new Group(createShape(size, headColor, eyesColor, palateColor),
				createBeautyAccessories(size, Color.RED, Color.BLUE));
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

	public static Shape3D head(Group root) {
		var headGroup = (Group) root.getChildren().get(0);
		return (Shape3D) headGroup.getChildren().get(0);
	}

	public static Shape3D eyes(Group root) {
		var headGroup = (Group) root.getChildren().get(0);
		return (Shape3D) headGroup.getChildren().get(1);
	}

	public static Shape3D palate(Group root) {
		var headGroup = (Group) root.getChildren().get(0);
		return (Shape3D) headGroup.getChildren().get(2);
	}
}