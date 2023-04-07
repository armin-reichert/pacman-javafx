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

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D representation of a ghost.
 * <p>
 * A ghost is displayed in one of the following modes:
 * <ul>
 * <li>normal: colored ghost with blue eyes,
 * <li>frightened: blue ghost with empty pinkish eyes (ghost looking blind),
 * <li>frightened/flashing: blue-white flashing skin, pink-red flashing eyes,
 * <li>dead: blue eyes only,
 * <li>eaten: number cube showing eaten ghost's value.
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Duration TURN_DURATION = Duration.seconds(0.15);

	private enum Look {
		NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER;
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final GameLevel level;
	private final Ghost ghost;
	private final Group root = new Group();
	private final ColoredGhost3D coloredGhost3D;
	private final Box numberCube = new Box(World.TS, World.TS, World.TS);
	private Direction turnTargetDir;
	private final RotateTransition turnRotation;
	private final RotateTransition brakeAnimation;
	private Image numberImage;
	private Look currentLook;

	public Ghost3D(GameLevel level, Ghost ghost, GhostColoring colors) {
		this.level = Objects.requireNonNull(level, "Game level must not be null");
		this.ghost = Objects.requireNonNull(ghost, "Ghost must not be null");

		coloredGhost3D = new ColoredGhost3D(colors);
		coloredGhost3D.dress().drawModeProperty().bind(drawModePy);
		coloredGhost3D.eyeBalls().drawModeProperty().bind(drawModePy);
		coloredGhost3D.pupils().drawModeProperty().bind(drawModePy);

		root.getChildren().add(coloredGhost3D.getRoot());
		root.getChildren().add(numberCube);

		turnRotation = new RotateTransition(TURN_DURATION, root);
		turnRotation.setAxis(Rotate.Z_AXIS);
		turnRotation.setInterpolator(Interpolator.EASE_BOTH);
		turnRotation.setOnFinished(e -> {
			LOG.trace("%s: Turn rotation finished", ghost.name());
		});
		brakeAnimation = createBrakeAnimation(coloredGhost3D.getRoot());

		init();
	}

	private void showAsColoredGhost(boolean visible) {
		coloredGhost3D.getRoot().setVisible(visible);
		numberCube.setVisible(!visible);
	}

	public Node getRoot() {
		return root;
	}

	public void init() {
		turnTargetDir = ghost.moveDir();
		turnRotation.stop();
		root.setRotationAxis(Rotate.Z_AXIS);
		root.setRotate(Turn.angle(ghost.moveDir()));
		update();
	}

	public void update() {
		var newLook = switch (ghost.state()) {
		case LOCKED, LEAVING_HOUSE -> normalOrFrightenedOrFlashingLook();
		case FRIGHTENED -> frightenedOrFlashingLook();
		case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
		case EATEN -> Look.NUMBER;
		default -> Look.NORMAL;
		};
		if (currentLook != newLook) {
			setLook(newLook);
		}
		if (currentLook != Look.NUMBER) {
			root.setTranslateX(ghost.center().x());
			root.setTranslateY(ghost.center().y());
			root.setTranslateZ(-HTS);
			if (turnRotation.getStatus() == Status.RUNNING) {
				LOG.trace("%s: Turn animation is running", ghost.name());
				return;
			}
			// guy move direction changed?
			// TODO: store info in guy.moveResult?
			if (turnTargetDir != ghost.moveDir()) {
				playTurnAnimation(turnTargetDir, ghost.moveDir());
				turnTargetDir = ghost.moveDir();
			}
			if (ghost.moveResult.tunnelEntered) {
				brakeAnimation.playFromStart();
			}
		}
		root.setVisible(ghost.isVisible() && !outsideWorld());
	}

	private RotateTransition createBrakeAnimation(Node node) {
		var animation = new RotateTransition(Duration.seconds(0.4), node);
		animation.setAxis(Rotate.Y_AXIS);
		animation.setFromAngle(0);
		animation.setToAngle(-35);
		animation.setAutoReverse(true);
		animation.setCycleCount(2);
		return animation;
	}

	private void playTurnAnimation(Direction fromDir, Direction toDir) {
		var turnAngles = Turn.angles(fromDir, toDir);
		turnRotation.stop();
		turnRotation.setAxis(Rotate.Z_AXIS);
		turnRotation.setFromAngle(turnAngles.from());
		turnRotation.setToAngle(turnAngles.to());
		turnRotation.playFromStart();
	}

	public void setNumberImage(Image numberImage) {
		this.numberImage = numberImage;
	}

	private void setLook(Look look) {
		this.currentLook = look;
		switch (look) {
		case NORMAL -> {
			coloredGhost3D.appearNormal();
		}
		case FRIGHTENED -> {
			coloredGhost3D.appearFrightened();
		}
		case FLASHING -> {
			if (level.numFlashes > 0) {
				coloredGhost3D.appearFlashing(level.numFlashes, 1.0);
			} else {
				coloredGhost3D.appearFrightened();
			}
		}
		case EYES -> {
			coloredGhost3D.appearEyesOnly();
		}
		case NUMBER -> {
			var material = new PhongMaterial();
			material.setBumpMap(numberImage);
			material.setDiffuseMap(numberImage);
			numberCube.setMaterial(material);
			// rotate node such that number can be read from left to right
			root.setRotationAxis(Rotate.X_AXIS);
			root.setRotate(0);
		}
		default -> throw new IllegalArgumentException("Unknown Ghost3D look: %s ".formatted(look));
		}
		showAsColoredGhost(look != Look.NUMBER);
	}

	private boolean outsideWorld() {
		double centerX = ghost.position().x() + World.HTS;
		return centerX < 0 || centerX > level.world().numCols() * World.TS;
	}

	private Look normalOrFrightenedOrFlashingLook() {
		if (level.pac().powerTimer().isRunning() && ghost.killedIndex() == -1) {
			return frightenedOrFlashingLook();
		}
		return Look.NORMAL;
	}

	private Look frightenedOrFlashingLook() {
		return level.pac().isPowerFading(level) ? Look.FLASHING : Look.FRIGHTENED;
	}
}