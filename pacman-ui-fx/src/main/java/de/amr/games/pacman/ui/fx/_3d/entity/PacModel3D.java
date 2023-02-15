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

import java.util.stream.Stream;

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
 * Pac-Man mesh taken from a Blender model created by Gianmarco Cavallaccio (https://www.artstation.com/gianmart).
 * <p>
 * 
 * @author Armin Reichert
 */
public class PacModel3D {

	private PacModel3D() {
	}

	private static final ObjModel OBJ_MODEL = new ObjModel(ResourceMgr.urlFromRelPath("model3D/pacman.obj"));
	private static final String ID_PAC_EYES = "Sphere.008_Sphere.010_grey_wall";
	private static final String ID_PAC_HEAD = "Sphere_yellow_packman";
	private static final String ID_PAC_PALATE = "Sphere_grey_wall";
	private static final double PAC_SIZE = 9.0;

	private static Translate centerOverOrigin(Node node) {
		var bounds = node.getBoundsInLocal();
		return new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
	}

	private static Scale scale(Node node, double size) {
		var bounds = node.getBoundsInLocal();
		return new Scale(size / bounds.getWidth(), size / bounds.getHeight(), size / bounds.getDepth());
	}

	/**
	 * @param eyesColor   Pac-Man eyes color
	 * @param palateColor Pac-Man palate color
	 * @return transformation group representing a 3D Pac-Man.
	 */
	public static Node createPac3D(Color eyesColor, Color palateColor) {
		var headMaterial = new PhongMaterial(Color.YELLOW);

		var head = OBJ_MODEL.createMeshView(ID_PAC_HEAD);
		head.setMaterial(headMaterial);

		var eyes = OBJ_MODEL.createMeshView(ID_PAC_EYES);
		eyes.setMaterial(new PhongMaterial(eyesColor));

		var palate = OBJ_MODEL.createMeshView(ID_PAC_PALATE);
		palate.setMaterial(new PhongMaterial(palateColor));

		var center = centerOverOrigin(head);
		Stream.of(head, eyes, palate).forEach(meshView -> {
			meshView.getTransforms().add(center);
		});

		var root = new Group(head, eyes, palate);
		root.getTransforms().addAll(new Translate(0, 0, -1), scale(root, PAC_SIZE), new Rotate(90, Rotate.X_AXIS));

		return root;
	}

	public static Shape3D head(Node root) {
		return (Shape3D) ((Group) root).getChildren().get(0);
	}

	public static Shape3D eyes(Node root) {
		return (Shape3D) ((Group) root).getChildren().get(1);
	}

	public static Shape3D palate(Node root) {
		return (Shape3D) ((Group) root).getChildren().get(2);
	}
}