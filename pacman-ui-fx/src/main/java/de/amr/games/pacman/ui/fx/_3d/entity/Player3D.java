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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import javafx.animation.RotateTransition;
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
			Color from = Color.YELLOW, to = Color.LIGHTGRAY;
			Color color = Color.color(lerp(from.getRed(), to.getRed(), t), lerp(from.getGreen(), to.getGreen(), t),
					lerp(from.getBlue(), to.getBlue(), t));
			material.setDiffuseColor(color);
		}
	}

	private class TurningAnimation {

		private RotateTransition rotate = new RotateTransition(Duration.seconds(0.25), Player3D.this);
		private Direction targetDir;

		public TurningAnimation() {
			reset();
		}

		public void reset() {
			rotate.setAxis(Rotate.Z_AXIS);
			rotate.setFromAngle(rotationAngle(player.dir()));
			targetDir = player.dir();
		}

		public void update() {
			if (targetDir != player.dir()) {
				rotate.stop();
				int[] angles = rotationAngles(player.dir(), targetDir);
				rotate.setFromAngle(angles[1]);
				rotate.setToAngle(angles[0]);
				rotate.play();
				targetDir = player.dir();
			}
		}
	}

	public final Pac player;
	private final Group model3D;
	private final TurningAnimation turningAnimation;

	public Player3D(Pac player, Group model3D) {
		this.player = player;
		this.model3D = model3D;
		turningAnimation = new TurningAnimation();
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateZ(-4);
		getChildren().addAll(model3D, light);
		reset();
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
		turningAnimation.reset();
	}

	public void update() {
		setVisible(player.visible && !outsideMaze(player));
		setTranslateX(player.position.x + HTS);
		setTranslateY(player.position.y + HTS);
		if (!player.dead) {
			turningAnimation.update();
		}
	}

	public Transition createImpaleAnimation(Duration duration) {
		ImpaleAnimation impale = new ImpaleAnimation(duration);
		head().setMaterial(impale.material);
		return impale;
	}

	private Shape3D head() {
		return (Shape3D) model3D.getChildrenUnmodifiable().get(0);
	}
}