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

import java.util.HashMap;
import java.util.Map;

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

	private final Map<Integer, Image> numberImages;
	private final Rendering2D rendering2D;
	private final RotateTransition rotation;
	private final PhongMaterial skin;

	public Bonus3D(Rendering2D rendering2D) {
		super(TS, TS, TS);
		this.rendering2D = rendering2D;
		numberImages = new HashMap<>();
		rendering2D.getBonusValueSprites().forEach((n, r) -> numberImages.put(n, rendering2D.extractRegion(r)));
		skin = new PhongMaterial(Color.WHITE);
		rotation = new RotateTransition(Duration.seconds(2), this);
		rotation.setAxis(Rotate.X_AXIS);
		rotation.setByAngle(360);
		rotation.setOnFinished(e -> hide());
		setTranslateZ(-4);
		hide();
	}

	private Image symbolImage(int symbol) {
		return rendering2D.extractRegion(rendering2D.getSymbolSprites().get(symbol));
	}

	public void update(Bonus bonus) {
		if (bonus != null) {
			setTranslateX(bonus.position.x + HTS);
			setTranslateY(bonus.position.y + HTS);
		}
	}

	public void hide() {
		rotation.stop();
		setVisible(false);
	}

	public void showSymbol(Bonus bonus) {
		Image symbolImage = symbolImage(bonus.symbol);
		skin.setBumpMap(symbolImage);
		skin.setDiffuseMap(symbolImage);
		setMaterial(skin);
		setWidth(TS);
		setVisible(true);
		rotation.setRate(1);
		rotation.setCycleCount(Transition.INDEFINITE);
		rotation.play();
	}

	public void showPoints(Bonus bonus) {
		Image pointsImage = numberImages.get(bonus.points);
		skin.setBumpMap(pointsImage);
		skin.setDiffuseMap(pointsImage);
		setMaterial(skin);
		if (bonus.points >= 1000) {
			setWidth(10);
		}
		setVisible(true);
		rotation.stop();
		rotation.setRate(3);
		rotation.setCycleCount(3);
		rotation.play();
	}
}