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

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.ui.fx._3d.entity.Model3DHelper.lerp;
import static de.amr.games.pacman.ui.fx.util.Animations.now;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
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

	private class ImpaleAnimation extends Transition {

		private PhongMaterial material = new PhongMaterial(Color.YELLOW);

		public ImpaleAnimation(Duration duration) {
			setCycleCount(1);
			setCycleDuration(duration);
		}

		@Override
		protected void interpolate(double t) {
			if (t == 0) {
				head().setMaterial(material);
			}
			Color from = Color.YELLOW, to = Color.LIGHTGRAY;
			Color color = Color.color(lerp(from.getRed(), to.getRed(), t), lerp(from.getGreen(), to.getGreen(), t),
					lerp(from.getBlue(), to.getBlue(), t));
			material.setDiffuseColor(color);
		}
	}

	public final Pac player;
	private Group model3D;
	private RotateTransition turningAnimation;
	private Direction targetDir;

	public Player3D(Pac player, Group model3D) {
		this.player = player;
		this.model3D = model3D;
		this.turningAnimation = new RotateTransition(Duration.seconds(0.25), this);
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateZ(-4);
		getChildren().addAll(model3D, light);
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

	private void updateTurning() {
		if (targetDir != player.dir()) {
			turningAnimation.stop();
			int[] angles = rotationAngles(player.dir(), targetDir);
			turningAnimation.setFromAngle(angles[1]);
			turningAnimation.setToAngle(angles[0]);
			turningAnimation.play();
			targetDir = player.dir();
		}
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

		var playSound = now(() -> sounds.play(PacManGameSound.PACMAN_DEATH));

		return new SequentialTransition(//
				new ImpaleAnimation(Duration.seconds(2)), //
				new ParallelTransition(spin, shrink, playSound));
	}

	public void reset() {
		head().setMaterial(new PhongMaterial(Color.YELLOW));
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
			updateTurning();
		}
	}

	private Shape3D head() {
		return (Shape3D) model3D.getChildrenUnmodifiable().get(0);
	}
}