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

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Objects;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.GhostColorScheme;
import de.amr.games.pacman.ui.fx._3d.animation.Creature3DMovement;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;

/**
 * 3D representation of a ghost.
 * <p>
 * A ghost is displayed in one of the following modes:
 * <ul>
 * <li>normal: colored ghost with blue eyes,
 * <li>frightened: blue ghost with empty pinkish eyes (ghost looking blind),
 * <li>frightened/flashing: blue-white flashing skin, pink-red flashing eyes,
 * <li>dead: blue eyes only,
 * <li>eaten: number cube showing eaten ghost's value.
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends Group {

	private enum Look {
		NORMAL, FRIGHTENED, FLASHING, EYES, NUMBER;
	}

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final Ghost ghost;
	private final Creature3DMovement movement;
	private final ColoredGhost3D coloredGhost3D;
	private final Box numberCube = new Box(TS, TS, TS);
	private Image numberImage;
	private Look look;

	public Ghost3D(Ghost ghost, GhostColorScheme colors) {
		this.ghost = Objects.requireNonNull(ghost, "Ghost must not be null");
		Objects.requireNonNull(colors, "Ghost colors must not be null");
		coloredGhost3D = new ColoredGhost3D(colors);
		coloredGhost3D.dress().drawModeProperty().bind(drawModePy);
		coloredGhost3D.eyeBalls().drawModeProperty().bind(drawModePy);
		coloredGhost3D.pupils().drawModeProperty().bind(drawModePy);
		movement = new Creature3DMovement(this, ghost);
	}

	public void init(GameModel game) {
		movement.init();
		update(game);
	}

	public void update(GameModel game) {
		var newLook = computeLookForCurrentState(game);
		if (look != newLook) {
			changeLook(game, newLook);
		}
		if (look != Look.NUMBER) {
			movement.update();
		}
		setVisible(ghost.isVisible() && !outsideWorld(game)); // ???
	}

	public void setNumberImage(Image numberImage) {
		this.numberImage = numberImage;
	}

	private void changeLook(GameModel game, Look newLook) {
		look = newLook;
		switch (newLook) {
		case NORMAL -> {
			coloredGhost3D.appearNormal();
			movement.init();
			getChildren().setAll(coloredGhost3D.root());
		}
		case FRIGHTENED -> {
			coloredGhost3D.appearFrightened();
			getChildren().setAll(coloredGhost3D.root());
		}
		case FLASHING -> {
			int numFlashes = game.level().numFlashes();
			if (numFlashes > 0) {
				coloredGhost3D.appearFlashing(numFlashes);
			} else {
				coloredGhost3D.appearFrightened();
			}
			getChildren().setAll(coloredGhost3D.root());
		}
		case EYES -> {
			coloredGhost3D.appearEyesOnly();
			movement.init();
			getChildren().setAll(coloredGhost3D.root());
		}
		case NUMBER -> {
			var material = new PhongMaterial();
			material.setBumpMap(numberImage);
			material.setDiffuseMap(numberImage);
			numberCube.setMaterial(material);
			getChildren().setAll(numberCube);
			// rotate node such that number can be read from left to right
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + newLook);
		}
	}

	private boolean outsideWorld(GameModel game) {
		double centerX = ghost.position().x() + World.HTS;
		return centerX < 0 || centerX > game.level().world().numCols() * World.TS;
	}

	private Look computeLookForCurrentState(GameModel game) {
		return switch (ghost.state()) {
		case LOCKED, LEAVING_HOUSE -> normalOrFrightenedOrFlashingLook(game);
		case FRIGHTENED -> frightenedOrFlashingLook(game);
		case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
		case EATEN -> Look.NUMBER;
		default -> Look.NORMAL;
		};
	}

	private Look normalOrFrightenedOrFlashingLook(GameModel game) {
		if (game.pac().powerTimer().isRunning() && ghost.killedIndex() == -1) {
			return frightenedOrFlashingLook(game);
		}
		return Look.NORMAL;
	}

	private Look frightenedOrFlashingLook(GameModel game) {
		return game.pac().isPowerFading(game) ? Look.FLASHING : Look.FRIGHTENED;
	}
}