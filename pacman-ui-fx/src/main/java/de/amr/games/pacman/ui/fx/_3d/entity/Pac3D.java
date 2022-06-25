/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.FillTransition3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man or Ms. Pac-Man.
 * 
 * <p>
 * TODO: Specific 3D-model for Ms. Pac-Man, mouth animation
 * 
 * @author Armin Reichert
 */
public class Pac3D extends Group {

	private final Pac pac;
	private final PacManModel3D model3D;
	private final Group modelRoot;
	private final Motion motion = new Motion();
	private final Color skullColor;

	public Pac3D(Pac pac, PacManModel3D model3D, Color skullColor, Color eyesColor, Color palateColor) {
		this.pac = pac;
		this.model3D = model3D;
		this.skullColor = skullColor;
		modelRoot = model3D.createPacMan(skullColor, eyesColor, palateColor);
		var light = new PointLight(Color.WHITE);
		light.setTranslateZ(-8);
		getChildren().addAll(modelRoot, light);
	}

	public void reset() {
		modelRoot.setScaleX(1.0);
		modelRoot.setScaleY(1.0);
		modelRoot.setScaleZ(1.0);
		update();
	}

	public void update() {
		motion.update(pac, this);
		double centerX = pac.position.x + World.HTS;
		double leftEdge = 0;
		double rightEdge = ArcadeWorld.TILES_X * World.TS;
		double distFromEdge = Math.min(centerX - leftEdge, rightEdge - centerX);
		boolean outsideWorld = centerX < leftEdge || centerX > rightEdge;
		updateAppearance(outsideWorld, distFromEdge);
	}

	private void updateAppearance(boolean outsideWorld, double distFromEdge) {
		model3D.skull(modelRoot).setMaterial(createMaterial(skullColor));
		if (outsideWorld) {
			// show as shadow
			setVisible(true);
			setOpacity(0.5);
		} else {
			setVisible(pac.visible);
			setOpacity(1);
			if (distFromEdge < 8) {
				// fade
				Color transparent = Color.color(skullColor.getRed(), skullColor.getGreen(), skullColor.getBlue(), 0.1);
				model3D.skull(modelRoot).setMaterial(createMaterial(transparent));
			}
		}
	}

	private PhongMaterial createMaterial(Color diffuseColor) {
		var material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		return material;
	}

	public Animation dyingAnimation(Color ghostColor) {
		var spin = new RotateTransition(Duration.seconds(0.2), modelRoot);
		spin.setAxis(Rotate.Z_AXIS);
		spin.setByAngle(360);
		spin.setCycleCount(10);

		var shrink = new ScaleTransition(Duration.seconds(2), modelRoot);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		return new SequentialTransition( //
				new FillTransition3D(Duration.seconds(1), model3D.skull(modelRoot), skullColor, ghostColor), //
				new FillTransition3D(Duration.seconds(1), model3D.skull(modelRoot), ghostColor, Color.GHOSTWHITE), //
				new ParallelTransition(spin, shrink));
	}
}