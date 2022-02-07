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
import static de.amr.games.pacman.model.world.World.TS;

import de.amr.games.pacman.model.pacman.entities.Bonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D bonus symbol.
 * 
 * @author Armin Reichert
 */
public class Bonus3D extends Box {

	private final Rendering2D r2D;
	private final RotateTransition rotation;
	private final PhongMaterial skin;

	public Bonus3D(Rendering2D r2D) {
		super(TS, TS, TS);
		this.r2D = r2D;
		skin = new PhongMaterial(Color.WHITE);
		rotation = new RotateTransition(Duration.seconds(1), this);
		rotation.setAxis(Rotate.X_AXIS);
		rotation.setByAngle(360);
		rotation.setOnFinished(e -> setVisible(false));
		visibleProperty().addListener($1 -> {
			if (!isVisible()) {
				rotation.stop();
			}
		});
		setTranslateZ(-4);
		setVisible(false);
	}

	public void update(Bonus bonus) {
		if (bonus != null) {
			setTranslateX(bonus.position.x + HTS);
			setTranslateY(bonus.position.y + HTS);
		}
	}

	public void showSymbol(int symbol) {
		Image image = r2D.extractRegion(r2D.getSymbolSprite(symbol));
		skin.setBumpMap(image);
		skin.setDiffuseMap(image);
		setMaterial(skin);
		setWidth(TS);
		setVisible(true);
		rotation.stop();
		rotation.setRate(1);
		rotation.setCycleCount(Transition.INDEFINITE);
		rotation.play();
	}

	public void showPoints(int points) {
		Image image = r2D.extractRegion(r2D.getBonusValueSprite(points));
		skin.setBumpMap(image);
		skin.setDiffuseMap(image);
		setMaterial(skin);
		if (points >= 1000) {
			setWidth(10);
		}
		setVisible(true);
		rotation.stop();
		rotation.setRate(2);
		rotation.setCycleCount(5);
		rotation.play();
	}
}