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

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
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
 * TODO: 3D-model for Ms. Pac-Man TODO: animated 3D models
 * 
 * @author Armin Reichert
 */
public class Player3D extends Group {

	//@formatter:off
	//TODO there sure is a more elegant way to do this
	private static final int[][][] ROTATION_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private static int[] rotationAngles(Direction from, Direction to) {
		return ROTATION_ANGLES[index(from)][index(to)];
	}

	private static int rotationAngle(Direction dir) {
		return ROTATION_ANGLES[index(dir)][0][0];
	}

	private static boolean outsideMaze(Pac player) {
		return player.position().x < 0 || player.position().x > (player.world.numCols() - 1) * TS;
	}

	public final Pac player;

	private final RotateTransition turningAnimation;
	private final PointLight light;
	private Direction targetDir;

	public Player3D(Pac player, PacManModel3D model3D) {
		this.player = player;
		targetDir = player.dir();
		turningAnimation = new RotateTransition(Duration.seconds(0.25), this);
		turningAnimation.setAxis(Rotate.Z_AXIS);
		light = new PointLight(Color.WHITE);
		light.setTranslateZ(-4);
		getChildren().addAll(model3D.createPacMan(), light);
		reset();
	}

	public void reset() {
		setVisible(player.isVisible() && !outsideMaze(player));
		setScaleX(1);
		setScaleY(1);
		setScaleZ(1);
		setTranslateX(player.position().x);
		setTranslateY(player.position().y);
		setTranslateZ(-3);
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(rotationAngle(player.dir()));
	}

	public void playTurningAnimation(Direction from, Direction to) {
		int[] angles = rotationAngles(from, to);
		turningAnimation.setFromAngle(angles[1]);
		turningAnimation.setToAngle(angles[0]);
		turningAnimation.play();
	}

	public void update() {
		setVisible(player.isVisible() && !outsideMaze(player));
		setTranslateX(player.position().x);
		setTranslateY(player.position().y);
		if (player.dead) {
			return;
		}
		if (targetDir != player.dir()) {
			turningAnimation.stop();
			playTurningAnimation(player.dir(), targetDir);
			targetDir = player.dir();
		}
	}
}