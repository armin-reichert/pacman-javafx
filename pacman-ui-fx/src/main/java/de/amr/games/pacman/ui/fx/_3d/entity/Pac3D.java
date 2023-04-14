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

import static de.amr.games.pacman.model.common.world.World.TS;
import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Pac;
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
import javafx.scene.shape.MeshView;
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

	private static final double NODDING_ANGLE_FROM = -20;
	private static final double NODDING_ANGLE_TO = 10;

	private static final double GAYNODDING_ANGLE_FROM = -20;
	private static final double GAYNODDING_ANGLE_TO = 20;

	private static final double EXCITEMENT = 1.5;

	private static final Duration NODDING_DURATION = Duration.seconds(0.25);
	private static final Duration COLLAPSING_DURATION = Duration.seconds(2);

	public final BooleanProperty noddingPy = new SimpleBooleanProperty(this, "nodding", false) {
		@Override
		protected void invalidated() {
			if (get()) {
				createNoddingAnimation();
			} else {
				endNodding();
				noddingAnimation = null;
			}
		}
	};

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final GameLevel level;
	private final Pac pac;
	private final Node pacNode;
	private final Group root = new Group();
	private final Color headColor;
	private final PointLight light;
	private final Translate position = new Translate();
	private final Rotate orientation = new Rotate();
	private RotateTransition noddingAnimation;
	private boolean gayMovement;
	private boolean excited;
	private Animation dyingAnimation;

	public Pac3D(GameLevel level, Pac pac, Node pacNode, Color headColor, boolean gayMovement) {
		requireNonNull(level);
		requireNonNull(pac);
		requireNonNull(pacNode);
		requireNonNull(headColor);
		this.level = level;
		this.pac = pac;
		this.pacNode = pacNode;
		this.headColor = headColor;
		this.gayMovement = gayMovement;
		this.light = createLight();
		pacNode.getTransforms().setAll(position, orientation);
		head().drawModeProperty().bind(Env.d3_drawModePy);
		eyes().drawModeProperty().bind(Env.d3_drawModePy);
		palate().drawModeProperty().bind(Env.d3_drawModePy);
		root.getChildren().add(pacNode);
		noddingPy.bind(Env.d3_pacNoddingPy);
	}

	private void createNoddingAnimation() {
		noddingAnimation = new RotateTransition(NODDING_DURATION, root);
		noddingAnimation.setAxis(noddingAxis());
		double excitement = excited ? EXCITEMENT : 1;
		if (gayMovement) {
			noddingAnimation.setFromAngle(GAYNODDING_ANGLE_FROM * excitement);
			noddingAnimation.setToAngle(GAYNODDING_ANGLE_TO * excitement);
		} else {
			noddingAnimation.setFromAngle(NODDING_ANGLE_FROM * excitement);
			noddingAnimation.setToAngle(NODDING_ANGLE_TO * excitement);
		}
		noddingAnimation.setCycleCount(Animation.INDEFINITE);
		noddingAnimation.setAutoReverse(true);
		noddingAnimation.setRate(excitement);
		noddingAnimation.setInterpolator(Interpolator.EASE_BOTH);
	}

	public void onGetsPower() {
		excited = true;
		if (noddingAnimation == null) {
			return;
		}
		noddingAnimation.stop();
		createNoddingAnimation();
		noddingAnimation.play();
		LOG.info("I'm so excited, and I just can't hide it!");
	}

	public void onLosesPower() {
		excited = false;
		if (noddingAnimation == null) {
			return;
		}
		noddingAnimation.stop();
		createNoddingAnimation();
		noddingAnimation.play();
		LOG.info("I lost my power");
	}

	public Node getRoot() {
		return root;
	}

	public MeshView head() {
		return PacModel3D.meshView(pacNode, PacModel3D.MESH_ID_HEAD);
	}

	public MeshView palate() {
		return PacModel3D.meshView(pacNode, PacModel3D.MESH_ID_PALATE);
	}

	public MeshView eyes() {
		return PacModel3D.meshView(pacNode, PacModel3D.MESH_ID_EYES);
	}

	public Translate getPosition() {
		return position;
	}

	public PointLight getLight() {
		return light;
	}

	public void init() {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		endNodding();
		updatePosition();
		turnToMoveDirection();
		updateVisibility();
		updateLight();
	}

	public void update() {
		updatePosition();
		turnToMoveDirection();
		updateVisibility();
		updateAnimations();
		updateLight();
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

	private void updateVisibility() {
		root.setVisible(pac.isVisible() && !outsideWorld());
	}

	private void updateAnimations() {
		if (noddingAnimation == null) {
			return;
		}
		var axis = noddingAxis();
		if (pac.isStandingStill()) {
			endNodding();
			root.setRotate(0);
			return;
		}
		if (noddingAnimation.getStatus() != Status.RUNNING || !axis.equals(noddingAnimation.getAxis())) {
			noddingAnimation.stop();
			noddingAnimation.setAxis(axis);
			noddingAnimation.playFromStart();
			LOG.trace("%s: Nodding started", pac.name());
		}
	}

	private void endNodding() {
		if (noddingAnimation != null && noddingAnimation.getStatus() == Status.RUNNING) {
			noddingAnimation.stop();
			root.setRotationAxis(noddingAxis());
			root.setRotate(0);
			LOG.trace("%s: Nodding stopped", pac.name());
		}
	}

	private Point3D noddingAxis() {
		if (gayMovement) {
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
		spin.setByAngle(pac.moveDir() == Direction.LEFT ? -90 : 90);
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
		pointLight.setColor(Color.rgb(255, 255, 0, 0.25));
		pointLight.setMaxRange(2 * TS);
		pointLight.translateXProperty().bind(position.xProperty());
		pointLight.translateYProperty().bind(position.yProperty());
		pointLight.setTranslateZ(-10);
		return pointLight;
	}

	private void updateLight() {
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		var maxRange = pac.isPowerFading(level) ? 4 : 8;
		light.setLightOn(lightedPy.get() && isVisible && isAlive && hasPower);
		light.setMaxRange(hasPower ? maxRange * TS : 0);
	}

	private boolean outsideWorld() {
		double worldWidth = level.world().numCols() * TS;
		return position.getX() < 4 || position.getX() > worldWidth - 4;
	}
}