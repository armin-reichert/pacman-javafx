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
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
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
	private final Shape3D skull;
	private final PointLight light;

	private Color skullColor = Color.YELLOW;
	private Color eyesColor = Color.rgb(20, 20, 20);
	private Color palateColor = Color.CHOCOLATE;

	public Player3D(Pac player, PacManModel3D model3D) {
		this.player = player;
		Group pac3D = model3D.createPacMan(skullColor, eyesColor, palateColor);
		this.skull = (Shape3D) pac3D.getChildren().get(0);
		this.light = new PointLight(Color.WHITE);
		getChildren().addAll(pac3D, light);
		light.setTranslateZ(-HTS);
		turningAnimation.setNode(this);
		reset();
	}

	public void reset() {
		setScaleX(1.05);
		setScaleY(1.05);
		setScaleZ(1.05);
		setRotate(turnAngle(player.dir()));
		skull.setMaterial(new PhongMaterial(skullColor));
		update();
	}

	@Override
	public void update() {
		update(player);
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
				new ImpaleAnimation(Duration.seconds(1), skull, Color.YELLOW, Color.LIGHTGRAY), //
				new ParallelTransition(spin, shrink, playSound));
	}
}