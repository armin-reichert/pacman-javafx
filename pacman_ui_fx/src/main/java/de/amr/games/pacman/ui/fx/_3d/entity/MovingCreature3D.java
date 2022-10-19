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
 * @author Armin Reichert
 */
public abstract class MovingCreature3D extends Group {

	//@formatter:off
	private static final int[][][] TURN_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	private static int index(Direction dir) {
		if (dir == null) {
			return 0;
		}
		return switch (dir) {
		case LEFT -> 0;
		case RIGHT -> 1;
		case UP -> 2;
		case DOWN -> 3;
		};
	}

	protected Direction targetDir;

	public abstract Creature guy();

	public void resetMovement() {
		targetDir = guy().moveDir();
		updateMovement();
	}

	public void updateMovement() {
		setVisible(guy().isVisible());
		setTranslateX(guy().getPosition().x() + HTS);
		setTranslateY(guy().getPosition().y() + HTS);
		setTranslateZ(-HTS);
		if (targetDir != guy().moveDir()) {
			int[] angles = TURN_ANGLES[index(targetDir)][index(guy().moveDir())];
			var turning = new RotateTransition(Duration.seconds(0.3), this);
			turning.setAxis(Rotate.Z_AXIS);
			turning.setInterpolator(Interpolator.EASE_BOTH);
			turning.setFromAngle(angles[0]);
			turning.setToAngle(angles[1]);
			turning.playFromStart();
			targetDir = guy().moveDir();
		}
	}
}