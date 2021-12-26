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
import javafx.animation.RotateTransition;
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
public class Player3D extends Creature3D {

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
		setVisible(player.visible && !outsideMaze(player));
		setScaleX(1);
		setScaleY(1);
		setScaleZ(1);
		setTranslateX(player.position.x + PacManGameWorld.HTS);
		setTranslateY(player.position.y + PacManGameWorld.HTS);
		setTranslateZ(-4);
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(rotationAngle(player.dir()));
	}

	public void update() {
		setVisible(player.visible && !outsideMaze(player));
		setTranslateX(player.position.x + PacManGameWorld.HTS);
		setTranslateY(player.position.y + PacManGameWorld.HTS);
		if (player.dead) {
			return;
		}
		if (targetDir != player.dir()) {
			turningAnimation.stop();
			int[] angles = rotationAngles(player.dir(), targetDir);
			turningAnimation.setFromAngle(angles[1]);
			turningAnimation.setToAngle(angles[0]);
			turningAnimation.play();
			targetDir = player.dir();
		}
	}
}