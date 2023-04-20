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

import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.Turn;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
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
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
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

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final double HEAD_BANGING_ANGLE_FROM = -25;
	private static final double HEAD_BANGING_ANGLE_TO = 15;

	private static final double HIP_SWAY_ANGLE_FROM = -20;
	private static final double HIP_SWAY_ANGLE_TO = 20;

	private static final double EXCITEMENT = 1.5;

	private static final Duration HEAD_BANGING__DURATION = Duration.seconds(0.2);
	private static final Duration COLLAPSING_DURATION = Duration.seconds(2);

	private static final Color LIGHT_COLOR = Color.rgb(255, 255, 0, 0.25);

	public final BooleanProperty walkingAnimatedPy = new SimpleBooleanProperty(this, "walkingAnimated", false) {
		@Override
		protected void invalidated() {
			if (get()) {
				createWalkingAnimation();
			} else {
				endWalkingAnimation();
				walkingAnimation = null;
			}
		}
	};

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final Pac pac;
	private final Group root = new Group();
	private final Color headColor;
	private final PointLight light;
	private final Translate position = new Translate();
	private final Rotate orientation = new Rotate();
	private RotateTransition walkingAnimation;
	private boolean swayingHips;
	private boolean excited;
	private Animation dyingAnimation;

	public Pac3D(Pac pac, Node pacNode, Color headColor, boolean swayingHips) {
		requireNonNull(pac);
		requireNonNull(pacNode);
		requireNonNull(headColor);
		this.pac = pac;
		this.headColor = headColor;
		this.swayingHips = swayingHips;
		this.light = createLight();
		pacNode.getTransforms().setAll(position, orientation);
		PacModel3D.eyesMeshView(pacNode).drawModeProperty().bind(Env.d3_drawModePy);
		PacModel3D.headMeshView(pacNode).drawModeProperty().bind(Env.d3_drawModePy);
		PacModel3D.palateMeshView(pacNode).drawModeProperty().bind(Env.d3_drawModePy);
		root.getChildren().add(pacNode);
		walkingAnimatedPy.bind(Env.d3_pacWalkingAnimatedPy);
	}

	private void createWalkingAnimation() {
		walkingAnimation = new RotateTransition(HEAD_BANGING__DURATION, root);
		walkingAnimation.setAxis(noddingAxis());
		double excitement = excited ? EXCITEMENT : 1;
		if (swayingHips) {
			walkingAnimation.setFromAngle(HIP_SWAY_ANGLE_FROM * excitement);
			walkingAnimation.setToAngle(HIP_SWAY_ANGLE_TO * excitement);
		} else {
			walkingAnimation.setFromAngle(HEAD_BANGING_ANGLE_FROM * excitement);
			walkingAnimation.setToAngle(HEAD_BANGING_ANGLE_TO * excitement);
		}
		walkingAnimation.setCycleCount(Animation.INDEFINITE);
		walkingAnimation.setAutoReverse(true);
		walkingAnimation.setRate(excitement);
		walkingAnimation.setInterpolator(Interpolator.EASE_BOTH);
	}

	public void onGetsPower() {
		excited = true;
		if (walkingAnimation == null) {
			return;
		}
		walkingAnimation.stop();
		createWalkingAnimation();
		walkingAnimation.play();
	}

	public void onLosesPower() {
		excited = false;
		if (walkingAnimation == null) {
			return;
		}
		walkingAnimation.stop();
		createWalkingAnimation();
		walkingAnimation.play();
	}

	public Node getRoot() {
		return root;
	}

	public Translate position() {
		return position;
	}

	public PointLight light() {
		return light;
	}

	public void init(GameLevel level) {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		endWalkingAnimation();
		updatePosition();
		turnToMoveDirection();
		updateVisibility(level);
		updateLight(level);
	}

	public void update(GameLevel level) {
		updatePosition();
		turnToMoveDirection();
		updateVisibility(level);
		updateAnimations();
		updateLight(level);
	}

	private void updatePosition() {
		position.setX(pac.center().x());
		position.setY(pac.center().y());
		position.setZ(-5.0);
	}

	private void turnToMoveDirection() {
		turnTo(pac.moveDir());
	}

	public void turnTo(Direction dir) {
		var angle = Turn.angle(dir);
		if (angle != orientation.getAngle()) {
			orientation.setAngle(angle);
		}
	}

	private void updateVisibility(GameLevel level) {
		root.setVisible(pac.isVisible() && !outsideWorld(level.world()));
	}

	private void updateAnimations() {
		if (walkingAnimation == null) {
			return;
		}
		var axis = noddingAxis();
		if (pac.isStandingStill()) {
			endWalkingAnimation();
			root.setRotate(0);
			return;
		}
		if (walkingAnimation.getStatus() != Status.RUNNING || !axis.equals(walkingAnimation.getAxis())) {
			walkingAnimation.stop();
			walkingAnimation.setAxis(axis);
			walkingAnimation.playFromStart();
			LOG.trace("%s: Nodding started", pac.name());
		}
	}

	private void endWalkingAnimation() {
		if (walkingAnimation != null && walkingAnimation.getStatus() == Status.RUNNING) {
			walkingAnimation.stop();
			root.setRotationAxis(noddingAxis());
			root.setRotate(0);
			LOG.trace("%s: Nodding stopped", pac.name());
		}
	}

	private Point3D noddingAxis() {
		if (swayingHips) {
			return Rotate.Z_AXIS;
		}
		return pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
	}

	public void createPacManDyingAnimation() {
		var numSpins = 15;

		var spinning = new RotateTransition(COLLAPSING_DURATION.divide(numSpins), root);
		spinning.setAxis(Rotate.Z_AXIS);
		spinning.setByAngle(360);
		spinning.setCycleCount(numSpins);
		spinning.setInterpolator(Interpolator.EASE_OUT);

		var shrinking = new ScaleTransition(COLLAPSING_DURATION, root);
		shrinking.setToX(0.5);
		shrinking.setToY(0.5);
		shrinking.setToZ(0.0);

		var falling = new TranslateTransition(COLLAPSING_DURATION, root);
		falling.setToZ(4);

		dyingAnimation = new SequentialTransition(Ufx.pause(0.4), new ParallelTransition(spinning, shrinking, falling),
				Ufx.pause(0.25));

		dyingAnimation.setOnFinished(e -> root.setTranslateZ(0));
	}

	public void createMsPacManDyingAnimation() {
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;

		var spin = new RotateTransition(Duration.seconds(0.25), root);
		spin.setAxis(axis);
		spin.setByAngle(pac.moveDir() == Direction.LEFT || pac.moveDir() == Direction.DOWN ? -90 : 90);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setDelay(Duration.seconds(0.5));
		spin.setOnFinished(e -> root.setRotate(90));

		dyingAnimation = new SequentialTransition(spin, Ufx.pause(2));
	}

	public Animation dyingAnimation() {
		return dyingAnimation;
	}

	private PointLight createLight() {
		var pointLight = new PointLight();
		pointLight.setColor(LIGHT_COLOR);
		pointLight.setMaxRange(2 * TS);
		pointLight.translateXProperty().bind(position.xProperty());
		pointLight.translateYProperty().bind(position.yProperty());
		pointLight.setTranslateZ(-10);
		return pointLight;
	}

	private void updateLight(GameLevel level) {
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		var maxRange = pac.isPowerFading(level) ? 4 : 8;
		light.setLightOn(lightedPy.get() && isVisible && isAlive && hasPower);
		light.setMaxRange(hasPower ? maxRange * TS : 0);
	}

	private boolean outsideWorld(World world) {
		double worldWidth = world.numCols() * TS;
		return position.getX() < 4 || position.getX() > worldWidth - 4;
	}
}