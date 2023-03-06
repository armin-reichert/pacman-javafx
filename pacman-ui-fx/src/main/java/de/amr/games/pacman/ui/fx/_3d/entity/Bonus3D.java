/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

	private final Box shape;
	private final Bonus bonus;
	private final Image symbolImage;
	private final Image pointsImage;

	public Bonus3D(Bonus bonus, Image symbolImage, Image pointsImage) {
		shape = new Box(TS, TS, TS);
		this.bonus = bonus;
		this.symbolImage = symbolImage;
		this.pointsImage = pointsImage;
	}

	public Node getRoot() {
		return shape;
	}

	public void hide() {
		shape.setVisible(false);
	}

	public void setPosition(Vector2f position) {
		shape.setTranslateX(position.x() + HTS);
		shape.setTranslateY(position.y() + HTS);
		shape.setTranslateZ(-HTS);
	}

	public void update() {
		setPosition(bonus.entity().position());
		shape.setVisible(bonus.state() != BonusState.INACTIVE);
	}

	public void showSymbol() {
		var imageView = new ImageView(symbolImage);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(TS);
		setTexture(imageView.getImage());
		shape.setWidth(TS);
		rotate(1, Animation.INDEFINITE, 1);
	}

	public void showPoints() {
		var imageView = new ImageView(pointsImage);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(1.8 * TS);
		setTexture(imageView.getImage());
		shape.setWidth(1.8 * TS);
		rotate(1, 3, 2);
	}

	private void setTexture(Image texture) {
		var skin = new PhongMaterial(Color.WHITE);
		skin.setBumpMap(texture);
		skin.setDiffuseMap(texture);
		shape.setMaterial(skin);
	}

	private void rotate(double seconds, int cycleCount, int rate) {
		var rot = new RotateTransition(Duration.seconds(seconds), shape);
		rot.setAxis(Rotate.X_AXIS);
		rot.setFromAngle(0);
		rot.setToAngle(360);
		rot.setInterpolator(Interpolator.LINEAR);
		rot.setCycleCount(cycleCount);
		rot.setRate(rate);
		rot.play();
	}
}