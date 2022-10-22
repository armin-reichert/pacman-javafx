/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Base class for 3D creatures (Pac-Man, ghosts) that move through the maze and turn around corners.
 * 
 * @param <C> type of creature (Ghost, Pac)
 * 
 * @author Armin Reichert
 */
public abstract class MovingCreature3D<C extends Creature> extends Group {

	public record Turn(int from, int to) {
	}

	private static double getRotation(Direction dir) {
		return switch (dir) {
		case LEFT -> 0;
		case RIGHT -> 180;
		case UP -> 90;
		case DOWN -> 270;
		default -> 0;
		};
	}

	//@formatter:off
	private static final Turn[][] TURN_ANGLES = {
		{ new Turn(0, 0),  new Turn(180, 0),   new Turn(90, 0),   new Turn(-90, 0) }, // LEFT
		{ new Turn(0,180), new Turn(180, 180), new Turn(90, 180), new Turn(270,180) }, // RIGHT
		{ new Turn(0, 90), new Turn(180, 90),  new Turn(90, 90),  new Turn(270, 90) }, // UP
		{ new Turn(0,-90), new Turn(180, 270), new Turn(90, -90), new Turn(-90, -90) }, // DOWN
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == null ? 0 : dir.ordinal();
	}

	protected final C guy;
	protected Direction targetDir;
	private RotateTransition turnAnimation;

	protected MovingCreature3D(C guy) {
		this.guy = guy;
		turnAnimation = new RotateTransition(Duration.seconds(0.25), this);
		turnAnimation.setAxis(Rotate.Z_AXIS);
		turnAnimation.setInterpolator(Interpolator.EASE_BOTH);
	}

	protected void resetMovement() {
		targetDir = guy.moveDir();
		turnAnimation.stop();
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(getRotation(targetDir));
	}

	protected void updateMovement() {
		setVisible(guy.isVisible());
		setTranslateX(guy.getPosition().x() + HTS);
		setTranslateY(guy.getPosition().y() + HTS);
		setTranslateZ(-HTS);
		if (guy.moveDir() != targetDir) {
			startTurningAnimation();
			targetDir = guy.moveDir();
		}
	}

	private void startTurningAnimation() {
		int from = index(guy.moveDir());
		int to = index(targetDir);
		if (from != to) {
			var turn = TURN_ANGLES[from][to];
			turnAnimation.setFromAngle(turn.from);
			turnAnimation.setToAngle(turn.to);
			turnAnimation.playFromStart();
		}
	}
}