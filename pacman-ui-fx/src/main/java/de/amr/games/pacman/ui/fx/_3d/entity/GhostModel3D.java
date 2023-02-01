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
package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.ui.fx._3d.ObjModel;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Ghost mesh taken from a Blender model created by Gianmarco Cavallaccio (https://www.artstation.com/gianmart).
 * <p>
 * 
 * @author Armin Reichert
 */
public class GhostModel3D {

	private GhostModel3D() {
	}

	private static final ObjModel OBJ_MODEL = new ObjModel(ResourceMgr.urlFromRelPath("model3D/ghost.obj"));
	private static final String ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	private static final String ID_GHOST_EYE_BALLS = "Sphere.009_Sphere.036_white";
	private static final String ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";
	private static final double GHOST_SIZE = 8.5;

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	public static Node createGhost3D(Color dressColor, Color eyeBallColor, Color pupilColor) {
		var dress = OBJ_MODEL.createMeshView(ID_GHOST_DRESS);
		dress.setMaterial(new PhongMaterial(dressColor));

		var eyeBalls = OBJ_MODEL.createMeshView(ID_GHOST_EYE_BALLS);
		eyeBalls.setMaterial(new PhongMaterial(eyeBallColor));

		var pupils = OBJ_MODEL.createMeshView(ID_GHOST_PUPILS);
		pupils.setMaterial(new PhongMaterial(pupilColor));

		var center = centerOverOrigin(dress);
		dress.getTransforms().add(center);

		var eyes = new Group(pupils, eyeBalls);
		eyes.getTransforms().add(center);

		var ghost3D = new Group(dress, eyes);
		ghost3D.getTransforms().add(new Translate(0, 0, -1.5));
		ghost3D.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		ghost3D.getTransforms().add(scale(ghost3D, GHOST_SIZE));

		return ghost3D;
	}

	private static Group eyes(Node ghost3D) {
		var root = (Group) ghost3D;
		return (Group) root.getChildren().get(1);
	}

	public static Shape3D dress(Node ghost3D) {
		var root = (Group) ghost3D;
		return (Shape3D) root.getChildren().get(0);
	}

	public static Shape3D pupils(Node ghost3D) {
		return (Shape3D) eyes(ghost3D).getChildren().get(0);
	}

	public static Shape3D eyeBalls(Node ghost3D) {
		return (Shape3D) eyes(ghost3D).getChildren().get(1);
	}
}