/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx.v3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx.v3d.animation.Turn;
import de.amr.games.pacman.ui.fx.v3d.animation.WalkingAnimation;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.*;
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

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.coloredMaterial;
import static de.amr.games.pacman.ui.fx.util.Ufx.actionAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;
import static de.amr.games.pacman.ui.fx.v3d.model.Model3D.meshView;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man. Uses the OBJ model "pacman.obj".
 * 
 * <p>
 * Missing: Specific 3D model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	public static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	public static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	public static final String MESH_ID_PALATE = "Sphere_grey_wall";

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final Pac pac;
	private final Group root;
	private final Color headColor;
	private final Translate position = new Translate();
	private final Rotate orientation = new Rotate();
	private WalkingAnimation walkingAnimation;

	static Group createPacManGroup(Model3D model3D, Theme theme) {
		var body = createBody(model3D, 9,
				theme.color("pacman.color.head"),
				theme.color("pacman.color.eyes"),
				theme.color("pacman.color.palate"));
		return new Group(body);
	}

	static Group createMsPacManGroup(Model3D model3D, Theme theme) {
		var body = createBody(model3D, 9,
				theme.color("mspacman.color.head"),
				theme.color("mspacman.color.eyes"),
				theme.color("mspacman.color.palate"));
		return new Group(body, createFeminineParts(theme, 9));
	}

	public static Pac3D createPacMan3D(Model3D model3D, Theme theme, Pac pacMan) {
		checkNotNull(model3D);
		checkNotNull(theme);
		checkNotNull(pacMan);

		var pac3D = new Pac3D(createPacManGroup(model3D, theme), pacMan, theme.color("pacman.color.head"));
		pac3D.walkingAnimation = new HeadBanging(pacMan, pac3D.root);
		pac3D.drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);

		return pac3D;
	}

	public static Pac3D createMsPacMan3D(Model3D model3D, Theme theme, Pac msPacMan) {
		checkNotNull(model3D);
		checkNotNull(theme);
		checkNotNull(msPacMan);

		var pac3D = new Pac3D(createMsPacManGroup(model3D, theme), msPacMan, theme.color("mspacman.color.head"));
		pac3D.walkingAnimation = new HipSwaying(msPacMan, pac3D.root);
		pac3D.drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);

		return pac3D;
	}

	private static Group createBody(Model3D model3D, double size, Color headColor, Color eyesColor, Color palateColor) {
		var head = new MeshView(model3D.mesh(MESH_ID_HEAD));
		head.setId(Model3D.cssID(MESH_ID_HEAD));
		head.setMaterial(coloredMaterial(headColor));

		var eyes = new MeshView(model3D.mesh(MESH_ID_EYES));
		eyes.setId(Model3D.cssID(MESH_ID_EYES));
		eyes.setMaterial(coloredMaterial(eyesColor));

		var palate = new MeshView(model3D.mesh(MESH_ID_PALATE));
		palate.setId(Model3D.cssID(MESH_ID_PALATE));
		palate.setMaterial(coloredMaterial(palateColor));

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

	private static Group createFeminineParts(Theme theme, double pacSize) {
		var bowMaterial = coloredMaterial(theme.color("mspacman.color.hairbow"));

		var bowLeft = new Sphere(1.2);
		bowLeft.getTransforms().addAll(new Translate(3.0, 1.5, -pacSize * 0.55));
		bowLeft.setMaterial(bowMaterial);

		var bowRight = new Sphere(1.2);
		bowRight.getTransforms().addAll(new Translate(3.0, -1.5, -pacSize * 0.55));
		bowRight.setMaterial(bowMaterial);

		var pearlMaterial = coloredMaterial(theme.color("mspacman.color.hairbow.pearls"));

		var pearlLeft = new Sphere(0.4);
		pearlLeft.getTransforms().addAll(new Translate(2, 0.5, -pacSize * 0.58));
		pearlLeft.setMaterial(pearlMaterial);

		var pearlRight = new Sphere(0.4);
		pearlRight.getTransforms().addAll(new Translate(2, -0.5, -pacSize * 0.58));
		pearlRight.setMaterial(pearlMaterial);

		var beautySpot = new Sphere(0.25);
		beautySpot.setMaterial(coloredMaterial(Color.rgb(100, 100, 100)));
		beautySpot.getTransforms().addAll(new Translate(-1.8, -3.7, -1));

		var silicone = coloredMaterial(theme.color("mspacman.color.boobs"));

		var boobLeft = new Sphere(1.5);
		boobLeft.setMaterial(silicone);
		boobLeft.getTransforms().addAll(new Translate(-1.5, -1.2, pacSize * 0.35));

		var boobRight = new Sphere(1.5);
		boobRight.setMaterial(silicone);
		boobRight.getTransforms().addAll(new Translate(-1.5, 1.2, pacSize * 0.35));

		return new Group(bowLeft, bowRight, pearlLeft, pearlRight, boobLeft, boobRight, beautySpot);
	}

	private Pac3D(Node pacNode, Pac pac, Color headColor) {
		this.root = new Group(pacNode);
		this.pac = pac;
		this.headColor = headColor;
		pacNode.getTransforms().setAll(position, orientation);
		meshView(pacNode, MESH_ID_EYES).drawModeProperty().bind(drawModePy);
		meshView(pacNode, MESH_ID_HEAD).drawModeProperty().bind(drawModePy);
		meshView(pacNode, MESH_ID_PALATE).drawModeProperty().bind(drawModePy);
	}

	public Group getRoot() {
		return root;
	}

	public Pac pac() {
		return pac;
	}

	public Rotate orientation() {
		return orientation;
	}

	public Translate position() {
		return position;
	}

	public Animation dyingAnimation(GameVariant variant)
	{
		return switch (variant) {
			case MS_PACMAN -> createMsPacManDyingAnimation();
			case PACMAN -> createPacManDyingAnimation();
		};
	}

	public WalkingAnimation walkingAnimation() {
		return walkingAnimation;
	}

	public void init() {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		updatePosition();
		turnTo(pac.moveDir());
		updateVisibility();
		walkingAnimation.hold();
	}

	public void update() {
		if (pac.isDead()) {
			walkingAnimation.hold();
		} else {
			updatePosition();
			updateVisibility();
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

	private void updateVisibility() {
		root.setVisible(pac.isVisible() && !outsideWorld());
	}

	private boolean outsideWorld() {
		return position.getX() < HTS || position.getX() > TS * pac.level().world().numCols() - HTS;
	}

	private Animation createMsPacManDyingAnimation() {
		var spin = new RotateTransition(Duration.seconds(0.5), root);
		spin.setAxis(Rotate.X_AXIS); //TODO check this
		spin.setFromAngle(0);
		spin.setToAngle(360);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setRate(2);
		return new SequentialTransition(
				pauseSeconds(0.5),
				spin,
				pauseSeconds(2)
		);
	}

	private Animation createPacManDyingAnimation() {
		Duration spinningDuration = Duration.seconds(1.5);
		short numSpins = 10;

		var spinning = new RotateTransition(spinningDuration.divide(numSpins), root);
		spinning.setAxis(Rotate.Z_AXIS);
		spinning.setByAngle(360);
		spinning.setCycleCount(numSpins);
		spinning.setInterpolator(Interpolator.LINEAR);

		var shrinking = new ScaleTransition(spinningDuration, root);
		shrinking.setToX(0.75);
		shrinking.setToY(0.75);
		shrinking.setToZ(0.0);
		shrinking.setInterpolator(Interpolator.LINEAR);

		var falling = new TranslateTransition(spinningDuration, root);
		falling.setToZ(4);
		falling.setInterpolator(Interpolator.EASE_IN);

		var animation = new SequentialTransition(
				actionAfterSeconds(0, () -> {
					//TODO does not yet work as I want to
					init();
					turnTo(Direction.RIGHT);
				}),
				pauseSeconds(0.5),
				new ParallelTransition(spinning, shrinking, falling),
				pauseSeconds(1.0)
		);

		animation.setOnFinished(e -> {
			root.setVisible(false);
			root.setTranslateZ(0);
		});

		return animation;
	}
}