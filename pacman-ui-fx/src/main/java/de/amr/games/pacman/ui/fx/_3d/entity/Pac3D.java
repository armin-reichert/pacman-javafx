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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.animation.CollapseAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.MoveAnimation;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

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

	private static Group createHeadGroup(double size, Color headColor, Color eyesColor, Color palateColor) {
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

	public static Group createPacMan(double size, Color headColor, Color eyesColor, Color palateColor) {
		return new Group(createHeadGroup(size, headColor, eyesColor, palateColor));
	}

	public static Group createMsPacMan(double size, Color headColor, Color eyesColor, Color palateColor) {
		return new Group(createHeadGroup(size, headColor, eyesColor, palateColor),
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

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);

	private final Pac pac;
	private final MoveAnimation moveAnimation;
	private final Group root;
	private Color headColor;

	public Pac3D(Pac pac, Group root, Color headColor) {
		this.pac = pac;
		this.root = root;
		this.moveAnimation = new MoveAnimation(root, pac);
		this.headColor = headColor;
		Stream.of(head(root), eyes(root), palate(root)).forEach(part -> part.drawModeProperty().bind(drawModePy));
	}

	public Node getRoot() {
		return root;
	}

	public void init() {
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		moveAnimation.init();
		moveAnimation.update();
		headColorPy.set(headColor);
	}

	public void update(GameLevel level) {
		moveAnimation.update();
		if (outsideWorld(level.world())) {
			root.setVisible(false);
		} else {
			root.setVisible(pac.isVisible());
		}
		root.setTranslateX(pac.center().x());
		root.setTranslateY(pac.center().y());
		root.setTranslateZ(-HTS);
	}

	private boolean outsideWorld(World world) {
		double centerX = pac.position().x() + HTS;
		return centerX < HTS || centerX > world.numCols() * TS - HTS;
	}

	/**
	 * TODO Provide different animation for Ms. Pac-Man (rotation like in 2D scene)
	 * 
	 * @return dying animation (must not be longer than time reserved by game controller which is 5 seconds!)
	 */
	public Animation createDyingAnimation() {
		return new CollapseAnimation(root).getAnimation();
	}
}