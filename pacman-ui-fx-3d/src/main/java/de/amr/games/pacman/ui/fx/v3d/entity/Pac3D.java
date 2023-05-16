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
package de.amr.games.pacman.ui.fx.v3d.entity;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.PacManColoring;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx.v3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx.v3d.animation.Turn;
import de.amr.games.pacman.ui.fx.v3d.app.Game3d;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man.
 * 
 * <p>
 * Missing: Real 3D model for Ms. Pac-Man, Mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	public static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	public static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	public static final String MESH_ID_PALATE = "Sphere_grey_wall";

	private static class MsPacManDyingAnimation implements DyingAnimation {

		private final Animation animation;

		public MsPacManDyingAnimation(Node node) {
			var spin = new RotateTransition(Duration.seconds(0.5), node);
			spin.setAxis(Rotate.X_AXIS);
			spin.setFromAngle(0);
			spin.setToAngle(360);
			spin.setInterpolator(Interpolator.LINEAR);
			spin.setCycleCount(4);
			spin.setRate(2);
			//@formatter:off
			animation = new SequentialTransition(
				Ufx.pauseSeconds(0.5),
				spin,
				Ufx.pauseSeconds(2)
			);
			//@formatter:on
		}

		@Override
		public Animation animation() {
			return animation;
		}
	}

	private static class PacManDyingAnimation implements DyingAnimation {

		private final Animation animation;
		private final RotateTransition spinning;

		public PacManDyingAnimation(Pac3D pac3D) {
			var totalDuration = Duration.seconds(1.5);
			var numSpins = 12;

			spinning = new RotateTransition(totalDuration.divide(numSpins), pac3D.root);
			spinning.setAxis(Rotate.Z_AXIS);
			spinning.setByAngle(360);
			spinning.setCycleCount(numSpins);
			spinning.setInterpolator(Interpolator.LINEAR);
			spinning.setRate(1.0); // TODO

			var shrinking = new ScaleTransition(totalDuration, pac3D.root);
			shrinking.setToX(0.75);
			shrinking.setToY(0.75);
			shrinking.setToZ(0.0);

			var falling = new TranslateTransition(totalDuration, pac3D.root);
			falling.setToZ(4);

			animation = new SequentialTransition( //
					Ufx.actionAfterSeconds(0, () -> clearOrientation(pac3D)), //
					Ufx.pauseSeconds(0.5), //
					new ParallelTransition(spinning, shrinking, falling), //
					Ufx.actionAfterSeconds(0.5, () -> restoreOrientation(pac3D)) //
			);

			animation.setOnFinished(e -> {
				pac3D.root.setVisible(false);
				pac3D.root.setTranslateZ(0);
			});
		}

		private void clearOrientation(Pac3D pac3D) {
			var pacNode = pac3D.root.getChildren().get(0);
			pacNode.getTransforms().remove(pac3D.orientation);
		}

		private void restoreOrientation(Pac3D pac3D) {
			var pacNode = pac3D.root.getChildren().get(0);
			pacNode.getTransforms().add(pac3D.orientation);
		}

		@Override
		public Animation animation() {
			return animation;
		}
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private Pac pac;
	private Group root;
	private Color headColor;
	private Translate position = new Translate();
	private Rotate orientation = new Rotate();
	private WalkingAnimation walkingAnimation;
	private DyingAnimation dyingAnimation;

	public static Group createPacManGroup(Model3D model3D, PacManColoring colors) {
		return new Group(createShape(model3D, 9, colors.headColor(), colors.eyesColor(), colors.palateColor()));
	}

	public static Pac3D createPacMan3D(Model3D model3D, Pac pacMan, PacManColoring colors) {
		checkNotNull(model3D);
		checkNotNull(pacMan);
		checkNotNull(colors);

		var pacGroup = createPacManGroup(model3D, colors);
		var pac3D = new Pac3D(pacGroup, pacMan, colors.headColor());
		pac3D.walkingAnimation = new HeadBanging(pacMan, pac3D.root);
		pac3D.dyingAnimation = new PacManDyingAnimation(pac3D);
		pac3D.drawModePy.bind(Game3d.d3_drawModePy);

		return pac3D;
	}

	public static Pac3D createMsPacMan3D(Model3D model3D, Pac msPacMan, MsPacManColoring colors) {
		checkNotNull(model3D);
		checkNotNull(msPacMan);
		checkNotNull(colors);

		var pacGroup = createMsPacManGroup(model3D, colors);
		var pac3D = new Pac3D(pacGroup, msPacMan, colors.headColor());
		pac3D.walkingAnimation = new HipSwaying(msPacMan, pac3D.root);
		pac3D.dyingAnimation = new MsPacManDyingAnimation(pac3D.root);
		pac3D.drawModePy.bind(Game3d.d3_drawModePy);

		return pac3D;
	}

	public static Group createMsPacManGroup(Model3D model3D, MsPacManColoring colors) {
		return new Group(createShape(model3D, 9, colors.headColor(), colors.eyesColor(), colors.palateColor()),
				createBeautyAccessories(9, colors.headColor(), colors.hairBowColor(), colors.hairBowPearlsColor()));
	}

	private static Group createShape(Model3D model3D, double size, Color headColor, Color eyesColor, Color palateColor) {
		var head = new MeshView(model3D.mesh(MESH_ID_HEAD));
		head.setId(Model3D.cssID(MESH_ID_HEAD));
		head.setMaterial(ResourceManager.coloredMaterial(headColor));

		var eyes = new MeshView(model3D.mesh(MESH_ID_EYES));
		eyes.setId(Model3D.cssID(MESH_ID_EYES));
		eyes.setMaterial(ResourceManager.coloredMaterial(eyesColor));

		var palate = new MeshView(model3D.mesh(MESH_ID_PALATE));
		palate.setId(Model3D.cssID(MESH_ID_PALATE));
		palate.setMaterial(ResourceManager.coloredMaterial(palateColor));

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

	private static Group createBeautyAccessories(double pacSize, Color headColor, Color bowColor, Color pearlColor) {
		var bowMaterial = ResourceManager.coloredMaterial(bowColor);

		var bowLeft = new Sphere(1.2);
		bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
		bowLeft.setMaterial(bowMaterial);

		var bowRight = new Sphere(1.2);
		bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
		bowRight.setMaterial(bowMaterial);

		var pearlMaterial = ResourceManager.coloredMaterial(pearlColor);

		var pearlLeft = new Sphere(0.4);
		pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
		pearlLeft.setMaterial(pearlMaterial);

		var pearlRight = new Sphere(0.4);
		pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
		pearlRight.setMaterial(pearlMaterial);

		var beautySpot = new Sphere(0.25);
		beautySpot.setMaterial(ResourceManager.coloredMaterial(Color.rgb(100, 100, 100)));
		beautySpot.getTransforms().addAll(new Translate(-1.8, -3.7, -1));

		var silicone = ResourceManager.coloredMaterial(headColor.deriveColor(0, 1.0, 0.96, 1.0));

		var boobLeft = new Sphere(1.5);
		boobLeft.setMaterial(silicone);
		boobLeft.getTransforms().addAll(new Translate(-1.5, -1.2, pacSize * 0.35));

		var boobRight = new Sphere(1.5);
		boobRight.setMaterial(silicone);
		boobRight.getTransforms().addAll(new Translate(-1.5, 1.2, pacSize * 0.35));

		return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
	}

	private Pac3D(Node pacNode, Pac pac, Color headColor) {
		this.pac = pac;
		this.headColor = headColor;
		root = new Group(pacNode);
		pacNode.getTransforms().setAll(position, orientation);
		Model3D.meshView(pacNode, MESH_ID_EYES).drawModeProperty().bind(Game3d.d3_drawModePy);
		Model3D.meshView(pacNode, MESH_ID_HEAD).drawModeProperty().bind(Game3d.d3_drawModePy);
		Model3D.meshView(pacNode, MESH_ID_PALATE).drawModeProperty().bind(Game3d.d3_drawModePy);
	}

	@Override
	public String toString() {
		return "Pac3D[position=%s, orientation=%s, walking: %s]".formatted(position, orientation,
				walkingAnimation.animation().getStatus());
	}

	public Node getRoot() {
		return root;
	}

	public Rotate orientation() {
		return orientation;
	}

	public Translate position() {
		return position;
	}

	public DyingAnimation dyingAnimation() {
		return dyingAnimation;
	}

	public WalkingAnimation walkingAnimation() {
		return walkingAnimation;
	}

	public void init(GameLevel level) {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		updatePosition();
		turnTo(pac.moveDir());
		updateVisibility(level);
		walkingAnimation.hold();
	}

	public void update(GameLevel level) {
		if (pac.isDead()) {
			walkingAnimation.hold();
		} else {
			updatePosition();
			updateVisibility(level);
			turnTo(pac.moveDir());
			walkingAnimation.walk();
		}
	}

	private void updatePosition() {
		position.setX(pac.center().x());
		position.setY(pac.center().y());
		position.setZ(-5.0);
	}

	public void turnTo(Direction dir) {
		var angle = Turn.angle(dir);
		if (angle != orientation.getAngle()) {
			orientation.setAxis(Rotate.Z_AXIS);
			orientation.setAngle(angle);
		}
	}

	private void updateVisibility(GameLevel level) {
		root.setVisible(pac.isVisible() && !outsideWorld(level.world()));
	}

	private boolean outsideWorld(World world) {
		return position.getX() < HTS || position.getX() > TS * world.numCols() - HTS;
	}
}