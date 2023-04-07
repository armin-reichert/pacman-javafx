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

import static de.amr.games.pacman.model.common.world.World.HTS;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Ghost;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class GhostMovementAnimator {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Duration TURN_DURATION = Duration.seconds(0.15);

	private static final byte L = 0;
	private static final byte U = 1;
	private static final byte R = 2;
	private static final byte D = 3;

	//@formatter:off
	private static byte dirIndex(Direction dir) {
		return switch (dir) {	case LEFT -> L;	case RIGHT -> R; case UP -> U; case DOWN -> D; default -> L; };
	}
	//@formatter:on

	//@formatter:off
	private static final byte[][][] TURNS = {
		{ null,    {L, R}, {L, U},  {L, -U} }, // LEFT  -> *
		{ {R, L},  null,   {R, U},  {R, D}  }, // RIGHT -> *
		{ {U, L},  {U, R}, null,    {U, D}  }, // UP    -> *
		{ {-U, L}, {D, R}, {-U, U}, null    }, // DOWN  -> *
	};
	//@formatter:on

	private static double angle(byte dirIndex) {
		return dirIndex * 90.0;
	}

	private final Ghost ghost;
	private final Node ghostNode;

	private Direction turnTargetDir;
	private final RotateTransition turnRotation;

	public GhostMovementAnimator(Ghost ghost, Node ghostNode) {
		this.ghost = Objects.requireNonNull(ghost);
		this.ghostNode = Objects.requireNonNull(ghostNode);

		turnRotation = new RotateTransition(TURN_DURATION, ghostNode);
		turnRotation.setAxis(Rotate.Z_AXIS);
		turnRotation.setInterpolator(Interpolator.EASE_BOTH);
		turnRotation.setOnFinished(e -> {
			LOG.trace("%s: Turn rotation finished", ghost.name());
		});
	}

	public void init() {
		turnTargetDir = ghost.moveDir();
		turnRotation.stop();
		ghostNode.setRotationAxis(Rotate.Z_AXIS);
		ghostNode.setRotate(angle(dirIndex(ghost.moveDir())));
	}

	public void update() {
		ghostNode.setTranslateX(ghost.center().x());
		ghostNode.setTranslateY(ghost.center().y());
		ghostNode.setTranslateZ(-HTS);
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
	}

	private void playTurnAnimation(Direction fromDir, Direction toDir) {
		var turn = TURNS[fromDir.ordinal()][toDir.ordinal()];
		double fromAngle = angle(turn[0]);
		double toAngle = angle(turn[1]);
		turnRotation.stop();
		turnRotation.setAxis(Rotate.Z_AXIS);
		turnRotation.setFromAngle(fromAngle);
		turnRotation.setToAngle(toAngle);
		turnRotation.playFromStart();
	}
}