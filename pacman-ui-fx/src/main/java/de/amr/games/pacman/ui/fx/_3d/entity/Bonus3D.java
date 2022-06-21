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
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
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
public class Bonus3D {

	private final Box box;

	public Bonus3D() {
		box = new Box(TS, TS, TS);
		box.setTranslateZ(-HTS);
	}

	public Node getRoot() {
		return box;
	}

	public void update(Bonus bonus) {
		box.setTranslateX(bonus.entity().position.x + box.getWidth() / 2);
		box.setTranslateY(bonus.entity().position.y + box.getHeight() / 2);
		box.setVisible(bonus.state() != BonusState.INACTIVE);
	}

	public void showSymbol(Bonus bonus, Rendering2D r2D) {
		setTexture(r2D.getSpriteImage(r2D.getBonusSymbolSprite(bonus.symbol())));
		rotate(1, Animation.INDEFINITE, 1);
		box.setWidth(TS);
	}

	public void showPoints(Bonus bonus, Rendering2D r2D) {
		setTexture(r2D.getSpriteImage(r2D.getBonusValueSprite(bonus.symbol())));
		rotate(1, 5, 2);
		box.setWidth(bonus.value() >= 1000 ? TS * 1.25 : TS);
	}

	private void setTexture(Image texture) {
		var skin = new PhongMaterial(Color.WHITE);
		skin.setBumpMap(texture);
		skin.setDiffuseMap(texture);
		box.setMaterial(skin);
	}

	private void rotate(double seconds, int cycleCount, int rate) {
		var rot = new RotateTransition(Duration.seconds(seconds), box);
		rot.setAxis(Rotate.X_AXIS);
		rot.setFromAngle(0);
		rot.setToAngle(360);
		rot.setCycleCount(cycleCount);
		rot.setRate(rate);
		rot.play();
	}
}