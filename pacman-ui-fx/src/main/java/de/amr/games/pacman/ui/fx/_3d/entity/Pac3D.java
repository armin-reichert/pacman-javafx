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

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.IllegalGameVariantException;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.HeadBanging;
import de.amr.games.pacman.ui.fx._3d.animation.HipSwaying;
import de.amr.games.pacman.ui.fx._3d.animation.MsPacManDyingAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.PacManDyingAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.Turn;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.animation.Animation;
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
	}

	public interface DyingAnimation {

		Animation animation();
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
		case MS_PACMAN -> new MsPacManDyingAnimation(pac, root);
		case PACMAN -> new PacManDyingAnimation(pac, root);
		default -> throw new IllegalGameVariantException(gameVariant);
		};
	}

	public Node getRoot() {
		return root;
	}

	public Translate position() {
		return position;
	}

	public Animation dyingAnimation() {
		return dyingAnimation.animation();
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
		updatePosition();
		turnToMoveDirection();
		updateVisibility(level);
		walkingAnimation.update(pac);
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