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
import static de.amr.games.pacman.ui.fx._3d.entity.PacShape3D.eyes;
import static de.amr.games.pacman.ui.fx._3d.entity.PacShape3D.head;
import static de.amr.games.pacman.ui.fx._3d.entity.PacShape3D.palate;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._3d.animation.CollapseAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Duration NODDING_DURATION = Duration.seconds(0.2);

	public final BooleanProperty noddingPy = new SimpleBooleanProperty(this, "nodding", false) {
		@Override
		protected void invalidated() {
			if (get()) {
				createNoddingAnimation();
			} else {
				endNoddingAnimation();
				nodding = null;
			}
		}
	};

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final GameLevel level;
	private final Pac pac;
	private final Rotate moveDirRotate;
	private RotateTransition nodding;
	private final Group root;
	private final Color headColor;
	private final PointLight light;

	public Pac3D(GameLevel level, Pac pac, Group pacNode, Color headColor) {
		this.level = Objects.requireNonNull(level);
		this.pac = Objects.requireNonNull(pac);
		this.headColor = Objects.requireNonNull(headColor);
		root = new Group(Objects.requireNonNull(pacNode));
		Stream.of(head(pacNode), eyes(pacNode), palate(pacNode)).map(Shape3D::drawModeProperty)
				.forEach(py -> py.bind(drawModePy));
		moveDirRotate = new Rotate(Turn.angle(pac.moveDir()), Rotate.Z_AXIS);
		pacNode.getTransforms().setAll(moveDirRotate);
		noddingPy.bind(Env.d3_pacNoddingPy);
		light = createLight();
		init();
	}

	public Node getRoot() {
		return root;
	}

	public PointLight getLight() {
		return light;
	}

	public void init() {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		endNoddingAnimation();
		update();
	}

	public void update() {
		if (outsideWorld()) {
			root.setVisible(false);
		} else {
			root.setVisible(pac.isVisible());
		}
		root.setTranslateX(pac.center().x());
		root.setTranslateY(pac.center().y());
		root.setTranslateZ(-HTS);
		moveDirRotate.setAngle(Turn.angle(pac.moveDir()));
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
		root.setRotationAxis(axis);
		if (nodding != null) {
			updateNodding();
		}
		updateLight();
	}

	private void createNoddingAnimation() {
		nodding = new RotateTransition(NODDING_DURATION, root);
		nodding.setFromAngle(-30);
		nodding.setToAngle(30);
		nodding.setCycleCount(Animation.INDEFINITE);
		nodding.setAutoReverse(true);
		nodding.setInterpolator(Interpolator.EASE_BOTH);
	}

	private void updateNodding() {
		if (pac.velocity().length() == 0 || !pac.moveResult.moved || pac.restingTicks() == Pac.REST_FOREVER) {
			endNoddingAnimation();
			root.setRotate(0);
		} else if (nodding.getStatus() != Status.RUNNING) {
			var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			nodding.setAxis(axis);
			nodding.playFromStart();
			LOG.trace("%s: Nodding created and started", pac.name());
		}
	}

	private void endNoddingAnimation() {
		if (nodding != null && nodding.getStatus() == Status.RUNNING) {
			nodding.stop();
			LOG.trace("%s: Nodding stopped", pac.name());
		}
	}

	public Animation createDyingAnimation() {
		var variant = level.game().variant();
		return switch (variant) {
		case MS_PACMAN -> createMsPacManDyingAnimation();
		case PACMAN -> createPacManDyingAnimation();
		default -> throw new IllegalArgumentException("Unknown game variant: %s".formatted(variant));
		};
	}

	private Transition createPacManDyingAnimation() {
		return new SequentialTransition(Ufx.pause(0.25), new CollapseAnimation(root).getAnimation());
	}

	private Transition createMsPacManDyingAnimation() {
		var layOnBack = new RotateTransition(Duration.seconds(0.2), root);
		layOnBack.setAxis(Rotate.Y_AXIS);
		layOnBack.setFromAngle(0);
		layOnBack.setToAngle(90);

		var spin = new RotateTransition(Duration.seconds(0.2), root);
		spin.setAxis(Rotate.Y_AXIS);
		spin.setByAngle(-180);
		spin.setCycleCount(4);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setDelay(Duration.seconds(0.3));

		return new SequentialTransition(layOnBack, spin, Ufx.pause(2.0));
	}

	private PointLight createLight() {
		var pointLight = new PointLight();
		pointLight.setColor(Color.rgb(255, 255, 0, 0.25));
		pointLight.setMaxRange(2 * TS);
		pointLight.translateXProperty().bind(root.translateXProperty());
		pointLight.translateYProperty().bind(root.translateYProperty());
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