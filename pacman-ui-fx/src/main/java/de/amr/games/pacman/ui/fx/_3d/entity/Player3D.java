/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.PacManGameWorld;
import javafx.animation.Animation.Status;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of the player.
 * 
 * <p>
 * TODO: need 3D-model for Ms. Pac-Man
 * 
 * @author Armin Reichert
 */
public class Player3D extends Group {

	//@formatter:off
	private static final int[][][] ROTATION_INTERVALS = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} },
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, 
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} },
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} },
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private static int[] rotationInterval(Direction from, Direction to) {
		return ROTATION_INTERVALS[index(from)][index(to)];
	}

	public final Pac player;
	private final RotateTransition rotateTransition;
	private final PointLight light;
	private Direction targetDir;

	public Player3D(Pac player, PacManModel3D model3D) {
		this.player = player;
		targetDir = player.dir();
		rotateTransition = new RotateTransition(Duration.seconds(0.25), this);
		rotateTransition.setAxis(Rotate.Z_AXIS);
		light = new PointLight(Color.WHITE);
		light.setTranslateZ(-4);
		getChildren().addAll(model3D.createPacMan(), light);
		reset();
		setTranslateZ(-3);
	}

	public void reset() {
		setVisible(player.isVisible() && !outsideMaze(player));
		setTranslateX(player.position().x);
		setTranslateY(player.position().y);
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(rotationAngle(player.dir()));
	}

	public int rotationAngle(Direction dir) {
		return ROTATION_INTERVALS[index(dir)][index(dir)][0];
	}

	public void playRotateAnimation(Direction from, Direction to) {
		if (rotateTransition.getStatus() == Status.RUNNING) {
			rotateTransition.stop();
		}
		int[] rotationInterval = rotationInterval(from, to);
		rotateTransition.setFromAngle(rotationInterval[1]);
		rotateTransition.setToAngle(rotationInterval[0]);
		rotateTransition.play();
	}

	public void update() {
		setVisible(player.isVisible() && !outsideMaze(player));
		setTranslateX(player.position().x);
		setTranslateY(player.position().y);
		if (player.dead) {
			return;
		}
		if (targetDir != player.dir()) {
			playRotateAnimation(player.dir(), targetDir);
			targetDir = player.dir();
		}
	}

	private boolean outsideMaze(Pac player) {
		return player.position().x < 0 || player.position().x > (player.world.numCols() - 1) * PacManGameWorld.TS;
	}
}