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

import static de.amr.games.pacman.model.world.World.HTS;
import static de.amr.games.pacman.ui.fx.util.Animations.now;

import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._3d.animation.ImpaleAnimation;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of the player.
 * 
 * <p>
 * TODO: 3D-model for Ms. Pac-Man, mouth animation
 * 
 * @author Armin Reichert
 */
public class Player3D extends Creature3D {

	private final Pac player;
	private final Shape3D head;

	public Player3D(Pac player, Group completePlayer) {
		this.player = player;
		head = (Shape3D) completePlayer.getChildrenUnmodifiable().get(0);
		turningAnimation = new RotateTransition(Duration.seconds(0.3), this);
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateZ(-4);
		getChildren().addAll(completePlayer, light);
		reset();
	}

	private void resetTurning() {
		double angle = rotationAngle(player.dir());
		turningAnimation.setAxis(Rotate.Z_AXIS);
		turningAnimation.setFromAngle(angle);
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(angle);
		targetDir = player.dir();
	}

	public Animation dyingAnimation(SoundManager sounds) {
		var spin = new RotateTransition(Duration.seconds(0.2), this);
		spin.setAxis(Rotate.Z_AXIS);
		spin.setByAngle(360);
		spin.setCycleCount(10);

		var shrink = new ScaleTransition(Duration.seconds(2), this);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		var playSound = now(() -> sounds.play(GameSounds.PACMAN_DEATH));

		return new SequentialTransition(//
				new ImpaleAnimation(Duration.seconds(1), head, Color.YELLOW, Color.LIGHTGRAY), //
				new ParallelTransition(spin, shrink, playSound));
	}

	public void reset() {
		head.setMaterial(new PhongMaterial(Color.YELLOW));
		setScaleX(1.05);
		setScaleY(1.05);
		setScaleZ(1.05);
		setVisible(player.visible && !outsideMaze(player));
		setTranslateX(player.position.x + HTS);
		setTranslateY(player.position.y + HTS);
		setTranslateZ(-HTS);
		resetTurning();
	}

	public void update() {
		setVisible(player.visible && !outsideMaze(player));
		setTranslateX(player.position.x + HTS);
		setTranslateY(player.position.y + HTS);
		if (!player.dead) {
			updateDirection(player);
		}
	}
}