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
import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.IllegalGameVariantException;
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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
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

	private static final Duration NODDING_DURATION = Duration.seconds(0.2);
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

	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final GameLevel level;
	private final Pac pac;
	private final Group root = new Group();
	private final Color headColor;
	private final PointLight light;
	private final Translate position = new Translate();
	private final Rotate orientation = new Rotate();
	private RotateTransition noddingAnimation;

	public Pac3D(GameLevel level, Pac pac, Node pacNode, Color headColor) {
		requireNonNull(level);
		requireNonNull(pac);
		requireNonNull(pacNode);
		requireNonNull(headColor);
		this.level = level;
		this.pac = pac;
		this.headColor = headColor;
		this.light = createLight();
		pacNode.getTransforms().setAll(position, orientation);
		root.getChildren().add(pacNode);
		noddingPy.bind(Env.d3_pacNoddingPy);
	}

	private void createNoddingAnimation() {
		noddingAnimation = new RotateTransition(NODDING_DURATION, root);
		noddingAnimation.setAxis(Rotate.X_AXIS);
		noddingAnimation.setFromAngle(-40);
		noddingAnimation.setToAngle(20);
		noddingAnimation.setCycleCount(Animation.INDEFINITE);
		noddingAnimation.setAutoReverse(true);
		noddingAnimation.setInterpolator(Interpolator.EASE_BOTH);
	}

	public Node getRoot() {
		return root;
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
		updateVisbility();
		updateLight();
	}

	public void update() {
		updatePosition();
		turnToMoveDirection();
		updateVisbility();
		updateAnimations();
		updateLight();
	}

	private void updatePosition() {
		position.setX(pac.center().x());
		position.setY(pac.center().y());
		position.setZ(-5.0);
	}

	private void turnToMoveDirection() {
		var angle = Turn.angle(pac.moveDir());
		if (angle != orientation.getAngle()) {
			orientation.setAngle(angle);
		}
	}

	public void turnTo(Direction dir) {
		var angle = Turn.angle(dir);
		if (angle != orientation.getAngle()) {
			orientation.setAngle(angle);
		}
	}

	private void updateVisbility() {
		root.setVisible(pac.isVisible() && !outsideWorld());
	}

	private void updateAnimations() {
		if (noddingAnimation == null) {
			return;
		}
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
		if (pac.velocity().length() == 0 || !pac.moveResult.moved || pac.restingTicks() == Pac.REST_FOREVER) {
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
			var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			root.setRotationAxis(axis);
			root.setRotate(0);
			LOG.trace("%s: Nodding stopped", pac.name());
		}
	}

	public Animation createDyingAnimation() {
		var variant = level.game().variant();
		return switch (variant) {
		case MS_PACMAN -> createMsPacManDyingAnimation();
		case PACMAN -> createPacManDyingAnimation();
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	private Animation createPacManDyingAnimation() {
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

		var animation = new SequentialTransition(Ufx.pause(1), new ParallelTransition(spinning, shrinking, falling),
				Ufx.pause(0.25));

		animation.setOnFinished(e -> root.setTranslateZ(0));
		return animation;
	}

	private Animation createMsPacManDyingAnimation() {
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;

		var spin = new RotateTransition(Duration.seconds(0.25), root);
		spin.setAxis(axis);
		spin.setByAngle(pac.moveDir() == Direction.LEFT ? -90 : 90);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setDelay(Duration.seconds(0.25));

		return new SequentialTransition(spin, Ufx.pause(2));
	}

	private PointLight createLight() {
		var pointLight = new PointLight();
		pointLight.setColor(Color.rgb(255, 255, 0, 0.25));
		pointLight.setMaxRange(2 * TS);
		pointLight.translateXProperty().bind(position.xProperty());
		pointLight.translateYProperty().bind(position.yProperty());
		pointLight.setTranslateZ(-TS);
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
		double centerX = pac.position().x() + HTS;
		return centerX < HTS || centerX > level.world().numCols() * TS - HTS;
	}
}