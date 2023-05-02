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
package de.amr.games.pacman.ui.fx3d._3d.entity;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.world.World;
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

	private final Bonus bonus;
	private final Image symbolImage;
	private final Image pointsImage;
	private final Box shape;

	private RotateTransition eatenAnimation;
	private RotateTransition edibleAnimation;
	private boolean moving;

	public Bonus3D(Bonus bonus, Image symbolImage, Image pointsImage, boolean moving) {
		checkNotNull(bonus);
		checkNotNull(symbolImage);
		checkNotNull(pointsImage);

		this.bonus = bonus;
		this.symbolImage = symbolImage;
		this.pointsImage = pointsImage;
		this.shape = new Box(TS, TS, TS);
		this.moving = moving;

		edibleAnimation = new RotateTransition(Duration.seconds(1), shape);
		edibleAnimation.setAxis(Rotate.Z_AXIS); // to trigger initial change
		edibleAnimation.setFromAngle(0);
		edibleAnimation.setToAngle(360);
		edibleAnimation.setInterpolator(Interpolator.LINEAR);
		edibleAnimation.setCycleCount(Animation.INDEFINITE);

		eatenAnimation = new RotateTransition(Duration.seconds(1), shape);
		eatenAnimation.setAxis(Rotate.X_AXIS);
		eatenAnimation.setFromAngle(0);
		eatenAnimation.setToAngle(360);
		eatenAnimation.setInterpolator(Interpolator.LINEAR);
		eatenAnimation.setRate(2);
	}

	public void update(GameLevel level) {
		setPosition(bonus.entity().center());
		boolean visible = bonus.state() != Bonus.STATE_INACTIVE && !outsideWorld(level.world());
		shape.setVisible(visible);
		updateEdibleAnimation();
	}

	private void updateEdibleAnimation() {
		if (moving) {
			var movingBonus = (MovingBonus) bonus;
			var axis = movingBonus.entity().moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
			if (!axis.equals(edibleAnimation.getAxis())) {
				edibleAnimation.stop();
				edibleAnimation.setAxis(axis);
				if (movingBonus.entity().moveDir() == Direction.UP || movingBonus.entity().moveDir() == Direction.RIGHT) {
					edibleAnimation.setRate(-1);
				} else {
					edibleAnimation.setRate(1);
				}
				edibleAnimation.play();
			}
		} else if (edibleAnimation.getAxis() != Rotate.X_AXIS) {
			edibleAnimation.stop();
			edibleAnimation.setAxis(Rotate.X_AXIS);
			edibleAnimation.play();
		}
	}

	public void showEdible() {
		var imageView = new ImageView(symbolImage);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(TS);
		showImage(imageView.getImage());
		shape.setWidth(TS);
		updateEdibleAnimation();
		edibleAnimation.playFromStart();
	}

	public void showEaten() {
		var imageView = new ImageView(pointsImage);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(1.8 * TS);
		showImage(imageView.getImage());
		edibleAnimation.stop();
		eatenAnimation.playFromStart();
		shape.setRotationAxis(Rotate.X_AXIS);
		shape.setRotate(0);
		shape.setWidth(1.8 * TS);
	}

	private void showImage(Image texture) {
		var material = new PhongMaterial(Color.WHITE);
		material.setDiffuseMap(texture);
		shape.setMaterial(material);
	}

	public Node getRoot() {
		return shape;
	}

	public void hide() {
		shape.setVisible(false);
	}

	public void setPosition(Vector2f position) {
		shape.setTranslateX(position.x());
		shape.setTranslateY(position.y());
		shape.setTranslateZ(-HTS);
	}

	private boolean outsideWorld(World world) {
		double x = bonus.entity().center().x();
		return x < HTS || x > world.numCols() * TS - HTS;
	}
}