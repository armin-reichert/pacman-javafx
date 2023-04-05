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
package de.amr.games.pacman.ui.fx._3d.animation;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class MovementAnimator {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public record Turn(int fromAngle, int toAngle) {
	}

	private static final short L = 0;
	private static final short U = 90;
	private static final short R = 180;
	private static final short D = 270;

	private static double angle(Direction dir) {
		return switch (dir) {
		case LEFT -> L;
		case RIGHT -> R;
		case UP -> U;
		case DOWN -> D;
		default -> L;
		};
	}

	//@formatter:off
	private static final Turn[][] TURN_ANGLES = {
		{ null,            new Turn(L, R), new Turn(L, U),  new Turn(L, -U) }, // LEFT  -> *
		{ new Turn(R, L),  null,           new Turn(R, U),  new Turn(R, D)  }, // RIGHT -> *
		{ new Turn(U, L),  new Turn(U, R), null,            new Turn(U, D)  }, // UP    -> *
		{ new Turn(-U, L), new Turn(D, R), new Turn(-U, U), null            }, // DOWN  -> *
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == null ? 0 : dir.ordinal();
	}

	private final Creature guy;
	private final Node guyNode;
	private RotateTransition turnRotation;
	private RotateTransition nodRotation;
	private Direction turnTargetDir;
	private boolean noddingEnabled = false;

	public MovementAnimator(Creature guy, Node guyNode) {
		this.guy = Objects.requireNonNull(guy);
		this.guyNode = Objects.requireNonNull(guyNode);
	}

	public void setNoddingEnabled(boolean noddingEnabled) {
		this.noddingEnabled = noddingEnabled;
	}

	public void init() {
		turnRotation = null;
		nodRotation = null;
		turnTargetDir = guy.moveDir();
		rotateGuyToMoveDirection();
	}

	public void update() {
		// turn to new move dir?
		if (turnTargetDir != guy.moveDir()) {
			nodRotation = null;
			startTurning(turnTargetDir, guy.moveDir());
			turnTargetDir = guy.moveDir();
			return;
		}
		// turn animation running?
		if (turnRotation != null && turnRotation.getStatus() == Status.RUNNING) {
			return;
		}
		// start/play nodding?
		if (!noddingEnabled) {
			return;
		}
		if (nodRotation == null) {
			startNodding();
		}
		rotateGuyToMoveDirection();
	}

	private void rotateGuyToMoveDirection() {
		guyNode.getTransforms().setAll(new Rotate(angle(guy.moveDir()), Rotate.Z_AXIS));
	}

	private void startTurning(Direction fromDir, Direction toDir) {
		var turn = TURN_ANGLES[index(fromDir)][index(toDir)];
		turnRotation = new RotateTransition(Duration.seconds(0.3), guyNode);
		turnRotation.setAxis(Rotate.Z_AXIS);
		turnRotation.setInterpolator(Interpolator.EASE_BOTH);
		turnRotation.setOnFinished(e -> LOG.trace("%s: Turn rotation finished", guy.name()));
		turnRotation.setAxis(Rotate.Z_AXIS);
		turnRotation.setFromAngle(turn.fromAngle);
		turnRotation.setToAngle(turn.toAngle);
		turnRotation.playFromStart();
		LOG.info("%s: Turn rotation started", guy.name());
	}

	private void startNodding() {
		nodRotation = new RotateTransition(Duration.seconds(0.5), guyNode);
		var axis = switch (guy.moveDir()) {
		case UP, DOWN -> Rotate.X_AXIS;
		case LEFT, RIGHT -> Rotate.Y_AXIS;
		default -> throw new IllegalArgumentException();
		};
		nodRotation.setAxis(axis);
		nodRotation.setFromAngle(-20);
		nodRotation.setToAngle(20);
		nodRotation.setCycleCount(Animation.INDEFINITE);
		nodRotation.setAutoReverse(true);
		nodRotation.setInterpolator(Interpolator.EASE_BOTH);
		nodRotation.playFromStart();
	}
}