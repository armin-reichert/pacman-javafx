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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
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

	private final Bonus bonus;

	public Bonus3D(Bonus bonus) {
		super(TS, TS, TS);
		this.bonus = bonus;
	}

	public void update() {
		setTranslateX(bonus.entity().position().x() + HTS);
		setTranslateY(bonus.entity().position().y() + HTS);
		setTranslateZ(-HTS);
		setVisible(bonus.state() != BonusState.INACTIVE);
	}

	public void showSymbol(Rendering2D r2D) {
		setTexture(r2D.spritesheet().region(r2D.bonusSymbolSprite(bonus.index())));
		setWidth(TS);
		rotate(1, Animation.INDEFINITE, 1);
	}

	public void showPoints(Rendering2D r2D) {
		setTexture(r2D.spritesheet().region(r2D.bonusValueSprite(bonus.index())));
		setWidth(bonus.value() >= 1000 ? TS * 2 : TS);
		rotate(1, 3, 2);
	}

	private void setTexture(Image texture) {
		var skin = new PhongMaterial(Color.WHITE);
		skin.setBumpMap(texture);
		skin.setDiffuseMap(texture);
		setMaterial(skin);
	}

	private void rotate(double seconds, int cycleCount, int rate) {
		var rot = new RotateTransition(Duration.seconds(seconds), this);
		rot.setAxis(Rotate.X_AXIS);
		rot.setFromAngle(0);
		rot.setToAngle(360);
		rot.setCycleCount(cycleCount);
		rot.setRate(rate);
		rot.play();
	}
}