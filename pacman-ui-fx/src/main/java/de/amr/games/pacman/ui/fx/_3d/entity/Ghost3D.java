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

import static de.amr.games.pacman.ui.fx.util.Ufx.requirePositive;
import static java.util.Objects.requireNonNull;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.GhostColoring;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.animation.Turn;
import javafx.animation.Animation;
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
import javafx.scene.transform.Translate;
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

	private static final Duration BRAKE_DURATION = Duration.seconds(0.4);

	private enum Look {
		NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER;
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final GameLevel level;
	private final Ghost ghost;
	private final Group root = new Group();
	private final Group numberGroup = new Group();
	private final Group coloredGhostGroup = new Group();
	private final ColoredGhost3D coloredGhost3D;
	private final Box numberCube = new Box(14, 8, 8);
	private final Translate position = new Translate();
	private final Rotate orientation = new Rotate();
	private final RotateTransition brakeAnimation;
	private final RotateTransition dressAnimation;
	private final RotateTransition numberAnimation;
	private Image numberImage;
	private Look currentLook;

	public Ghost3D(GameLevel level, Ghost ghost, GhostColoring colors, Model3D model3D, double size) {
		requireNonNull(level);
		requireNonNull(ghost);
		requireNonNull(colors);
		requireNonNull(model3D);
		requirePositive(size, "Ghost3D size must be positive but is %f");

		this.level = level;
		this.ghost = ghost;

		root.getChildren().addAll(coloredGhostGroup, numberGroup);

		coloredGhost3D = new ColoredGhost3D(model3D, colors, size);
		coloredGhost3D.dressShape().drawModeProperty().bind(drawModePy);
		coloredGhost3D.eyeballsShape().drawModeProperty().bind(drawModePy);
		coloredGhost3D.pupilsShape().drawModeProperty().bind(drawModePy);
		coloredGhostGroup.getChildren().add(coloredGhost3D.getRoot());
		coloredGhostGroup.getTransforms().addAll(position, orientation);

		numberGroup.getChildren().add(numberCube);

		numberAnimation = new RotateTransition(Duration.seconds(3.0), numberCube);
		numberAnimation.setAxis(Rotate.X_AXIS);
		numberAnimation.setByAngle(360);
		numberAnimation.setInterpolator(Interpolator.LINEAR);
		numberAnimation.setCycleCount(Animation.INDEFINITE);

		brakeAnimation = new RotateTransition(BRAKE_DURATION, coloredGhost3D.getRoot());
		brakeAnimation.setAxis(Rotate.Y_AXIS);
		brakeAnimation.setFromAngle(0);
		brakeAnimation.setToAngle(-35);
		brakeAnimation.setAutoReverse(true);
		brakeAnimation.setCycleCount(2);

		dressAnimation = new RotateTransition(Duration.seconds(0.3), coloredGhost3D.getDressGroup());
		// TODO I expected this should be the z-axis but... (maybe my expectations are wrong)
		dressAnimation.setAxis(Rotate.Y_AXIS);
		dressAnimation.setFromAngle(-15);
		dressAnimation.setToAngle(15);
		dressAnimation.setCycleCount(Animation.INDEFINITE);
		dressAnimation.setAutoReverse(true);

		init();
	}

	public Node getRoot() {
		return root;
	}

	public void init() {
		brakeAnimation.stop();
		dressAnimation.stop();
		updatePosition();
		turnToMoveDirection();
		updateVisbility();
		updateLook();
	}

	public void update() {
		updatePosition();
		turnToMoveDirection();
		updateVisbility();
		updateLook();
		updateAnimations();
	}

	private void updatePosition() {
		position.setX(ghost.center().x());
		position.setY(ghost.center().y());
		position.setZ(-5);
	}

	private void turnToMoveDirection() {
		var angle = Turn.angle(ghost.moveDir());
		if (angle != orientation.getAngle()) {
			orientation.setAngle(angle);
		}
	}

	private void updateVisbility() {
		root.setVisible(ghost.isVisible() && !outsideWorld());
	}

	private void updateAnimations() {
		if (currentLook != Look.NUMBER) {
			if (ghost.moveResult.tunnelEntered) {
				brakeAnimation.playFromStart();
			}
			if (dressAnimation.getStatus() != Status.RUNNING) {
				dressAnimation.play();
			}
		} else {
			dressAnimation.stop();
		}
	}

	private void showAsGhost(boolean showAsGhost) {
		coloredGhostGroup.setVisible(showAsGhost);
		numberCube.setVisible(!showAsGhost);
		if (showAsGhost) {
			numberAnimation.stop();
		} else {
			numberAnimation.playFromStart();
		}
	}

	private void updateLook() {
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
			numberGroup.setTranslateX(ghost.center().x());
			numberGroup.setTranslateY(ghost.center().y());
		}
		default -> throw new IllegalArgumentException("Unknown Ghost3D look: %s ".formatted(look));
		}
		showAsGhost(look != Look.NUMBER);
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

	private boolean outsideWorld() {
		double centerX = ghost.position().x() + World.HTS;
		return centerX < 0 || centerX > level.world().numCols() * World.TS;
	}

	public void setNumberImage(Image numberImage) {
		this.numberImage = numberImage;
	}
}