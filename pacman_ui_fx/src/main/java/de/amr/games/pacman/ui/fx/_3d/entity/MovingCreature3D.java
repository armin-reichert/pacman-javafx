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

	//@formatter:off
	private static final Turn[][] TURN_ANGLES = {
		{ new Turn(  0, 0), new Turn(  0, 180), new Turn(  0, 90), new Turn(  0, -90) }, // LEFT
		{ new Turn(180, 0), new Turn(180, 180), new Turn(180, 90), new Turn(180, 270) }, // RIGHT
		{ new Turn( 90, 0), new Turn( 90, 180), new Turn( 90, 90), new Turn( 90, 270) }, // UP
		{ new Turn(-90, 0), new Turn(270, 180), new Turn(-90, 90), new Turn(-90, -90) }, // DOWN
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == null ? 0 : dir.ordinal();
	}

	protected final C guy;
	protected Direction targetDir;

	protected MovingCreature3D(C guy) {
		this.guy = guy;
	}

	protected void resetMovement() {
		targetDir = guy.moveDir();
		updateMovement();
	}

	protected void updateMovement() {
		setVisible(guy.isVisible());
		setTranslateX(guy.getPosition().x() + HTS);
		setTranslateY(guy.getPosition().y() + HTS);
		setTranslateZ(-HTS);
		if (targetDir != guy.moveDir()) {
			int fromIndex = index(targetDir);
			int toIndex = index(guy.moveDir());
			var turn = TURN_ANGLES[fromIndex][toIndex];
			var animation = new RotateTransition(Duration.seconds(0.3), this);
			animation.setAxis(Rotate.Z_AXIS);
			animation.setInterpolator(Interpolator.EASE_BOTH);
			animation.setFromAngle(turn.from);
			animation.setToAngle(turn.to);
			animation.playFromStart();
			targetDir = guy.moveDir();
		}
	}
}