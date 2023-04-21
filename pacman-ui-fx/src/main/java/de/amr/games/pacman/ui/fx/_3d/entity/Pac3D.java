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

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.model.common.Validator.checkNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.IllegalGameVariantException;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx._3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx._3d.animation.Turn;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D-representation of Pac-Man and Ms. Pac-Man.
 * 
 * <p>
 * Missing: Real 3D model for Ms. Pac-Man, Mouth animation...
 * 
 * @author Armin Reichert
 */
public class Pac3D {

	public interface WalkingAnimation {
		void update(Pac pac);

		void end(Pac pac);

		void setPowerMode(boolean power);

		Animation animation();
	}

	public interface DyingAnimation {

		Animation animation();
	}

	private class MsPacManDyingAnimation implements Pac3D.DyingAnimation {

		private static final Logger LOG = LogManager.getFormatterLogger();

		private final Animation animation;

		public MsPacManDyingAnimation() {
			var spin = new RotateTransition(Duration.seconds(0.5), root);
			spin.setAxis(Rotate.X_AXIS);
			spin.setFromAngle(0);
			spin.setToAngle(360);
			spin.setInterpolator(Interpolator.LINEAR);
			spin.setCycleCount(2);
			animation = new SequentialTransition(Ufx.afterSeconds(0, () -> {
				LOG.info("Before dying animation: %s", Pac3D.this);
			}), Ufx.pause(1), spin, Ufx.pause(2));
		}

		@Override
		public Animation animation() {
			return animation;
		}
	}

	private class PacManDyingAnimation implements Pac3D.DyingAnimation {

		private final Animation animation;

		public PacManDyingAnimation() {
			var totalDuration = Duration.seconds(2);
			var numSpins = 15;

			var spinning = new RotateTransition(totalDuration.divide(numSpins), root);
			spinning.setAxis(Rotate.Z_AXIS);
			spinning.setByAngle(360);
			spinning.setCycleCount(numSpins);
			spinning.setInterpolator(Interpolator.LINEAR);

			var shrinking = new ScaleTransition(totalDuration, root);
			shrinking.setToX(0.5);
			shrinking.setToY(0.5);
			shrinking.setToZ(0.0);

			var falling = new TranslateTransition(totalDuration, root);
			falling.setToZ(4);

			animation = new SequentialTransition(//
					Ufx.pause(0.4), //
					new ParallelTransition(spinning, shrinking, falling), //
					Ufx.pause(0.25));

			animation.setOnFinished(e -> {
				root.setVisible(false);
				root.setTranslateZ(0);
			});
		}

		@Override
		public Animation animation() {
			return animation;
		}
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> headColorPy = new SimpleObjectProperty<>(this, "headColor", Color.YELLOW);
	public final BooleanProperty lightedPy = new SimpleBooleanProperty(this, "lighted", true);

	private final Pac pac;
	private final Group root = new Group();
	private final Color headColor;
	private final Translate position = new Translate();
	private final Rotate orientation = new Rotate();
	private final WalkingAnimation walkingAnimation;
	private final DyingAnimation dyingAnimation;

	public Pac3D(GameVariant gameVariant, Pac pac, Node pacNode, Color headColor) {
		checkNotNull(gameVariant);
		checkNotNull(pac);
		checkNotNull(pacNode);
		checkNotNull(headColor);

		this.pac = pac;
		this.headColor = headColor;

		pacNode.getTransforms().setAll(position, orientation);

		PacModel3D.eyesMeshView(pacNode).drawModeProperty().bind(Env.d3_drawModePy);
		PacModel3D.headMeshView(pacNode).drawModeProperty().bind(Env.d3_drawModePy);
		PacModel3D.palateMeshView(pacNode).drawModeProperty().bind(Env.d3_drawModePy);
		root.getChildren().add(pacNode);

		walkingAnimation = switch (gameVariant) {
		case MS_PACMAN -> new HipSwaying(root);
		case PACMAN -> new HeadBanging(root);
		default -> throw new IllegalGameVariantException(gameVariant);
		};

		dyingAnimation = switch (gameVariant) {
		case MS_PACMAN -> new MsPacManDyingAnimation();
		case PACMAN -> new PacManDyingAnimation();
		default -> throw new IllegalGameVariantException(gameVariant);
		};
	}

	@Override
	public String toString() {
		return "Pac3D[position=%s, orientation=%s, walking: %s]".formatted(position, orientation,
				walkingAnimation.animation().getStatus());
	}

	public Node getRoot() {
		return root;
	}

	public Rotate orientation() {
		return orientation;
	}

	public Translate position() {
		return position;
	}

	public DyingAnimation dyingAnimation() {
		return dyingAnimation;
	}

	public WalkingAnimation walkingAnimation() {
		return walkingAnimation;
	}

	public void init(GameLevel level) {
		headColorPy.set(headColor);
		root.setScaleX(1.0);
		root.setScaleY(1.0);
		root.setScaleZ(1.0);
		updatePosition();
		turnToMoveDirection();
		updateVisibility(level);
		walkingAnimation.end(pac);
	}

	public void update(GameLevel level) {
		if (pac.isDead()) {
			walkingAnimation.end(pac);
		} else {
			updatePosition();
			updateVisibility(level);
			turnToMoveDirection();
			walkingAnimation.update(pac);
		}
	}

	private void updatePosition() {
		position.setX(pac.center().x());
		position.setY(pac.center().y());
		position.setZ(-5.0);
	}

	private void turnToMoveDirection() {
		turnTo(pac.moveDir());
	}

	public void turnTo(Direction dir) {
		var angle = Turn.angle(dir);
		if (angle != orientation.getAngle()) {
			orientation.setAxis(Rotate.Z_AXIS);
			orientation.setAngle(angle);
		}
	}

	private void updateVisibility(GameLevel level) {
		root.setVisible(pac.isVisible() && !outsideWorld(level.world()));
	}

	private boolean outsideWorld(World world) {
		return position.getX() < HTS || position.getX() > TS * world.numCols() - HTS;
	}
}