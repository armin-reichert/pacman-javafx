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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.FillTransition3D;
import de.amr.games.pacman.ui.fx._3d.model.PacModel3D;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
 * Missing: Specific 3D-model for Ms. Pac-Man, mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D extends Group {

	private final Logger logger = LogManager.getFormatterLogger();

	private final Pac pac;
	private final PacModel3D model3D;
	private final Group modelRoot;
	private final Motion motion = new Motion();
	private final Color normalSkullColor;
	private final ObjectProperty<Color> skullColorProperty = new SimpleObjectProperty<>();

	public Pac3D(Pac pac, PacModel3D model3D, Color skullColor, Color eyesColor, Color palateColor) {
		this.pac = pac;
		this.model3D = model3D;
		normalSkullColor = skullColor;
		skullColorProperty.set(skullColor);
		var skullMaterial = new PhongMaterial();
		skullMaterial.diffuseColorProperty().bind(skullColorProperty);
		skullMaterial.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> skullColorProperty.get().brighter(), skullColorProperty));
		modelRoot = model3D.createPacMan(skullColor, eyesColor, palateColor);
		model3D.skull(modelRoot).setMaterial(skullMaterial);
		var light = new PointLight(Color.WHITE);
		light.setTranslateZ(-8);
		getChildren().addAll(modelRoot, light);
	}

	public void reset() {
		modelRoot.setScaleX(1.0);
		modelRoot.setScaleY(1.0);
		modelRoot.setScaleZ(1.0);
		skullColorProperty.set(normalSkullColor);
		update();
	}

	public void update() {
		motion.update(pac, this);
		updateAppearance();
	}

	private void updateAppearance() {
		skullColorProperty.set(normalSkullColor);
		setVisible(pac.visible);
		setOpacity(1);
		if (outsideWorld()) {
			setVisible(true);
			skullColorProperty.set((Color.color(0.1, 0.1, 0.1, 0.2)));
		} else {
			double fadeStart = 32.0;
			double dist = distFromPortal();
			if (dist <= fadeStart) { // fade into shadow
				setVisible(true);
				double opacity = U.lerp(0.2, 1, dist / fadeStart);
				logger.info("Distance from portal: %.2f Opacity: %.2f", dist, opacity);
				skullColorProperty.set(Color.color(normalSkullColor.getRed() * opacity, normalSkullColor.getGreen() * opacity,
						normalSkullColor.getBlue() * opacity, opacity));
			}
		}
	}

	private double distFromPortal() {
		double centerX = pac.position.x + World.HTS;
		double rightEdge = ArcadeWorld.TILES_X * World.TS;
		if (centerX < 0 || centerX > rightEdge) {
			return 0;
		}
		return Math.abs(Math.min(centerX, rightEdge - centerX));
	}

	private boolean outsideWorld() {
		double centerX = pac.position.x + World.HTS;
		return centerX < 0 || centerX > ArcadeWorld.TILES_X * World.TS;
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
				new FillTransition3D(Duration.seconds(1), model3D.skull(modelRoot), normalSkullColor, ghostColor), //
				new FillTransition3D(Duration.seconds(1), model3D.skull(modelRoot), ghostColor, Color.GHOSTWHITE), //
				new ParallelTransition(spin, shrink));
	}
}