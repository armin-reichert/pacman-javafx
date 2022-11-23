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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.GhostColorScheme;
import de.amr.games.pacman.ui.fx._3d.animation.Creature3DMovement;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
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

	private final Ghost ghost;
	private final Creature3DMovement movement;
	private final ColoredGhost3D coloredGhost3D;
	private final Box numberCube = new Box(TS, TS, TS);
	private final Image[] numberImages;
	private Look look;

	public Ghost3D(Ghost ghost, Model3D model3D, Image[] numberImages, GhostColorScheme colors) {
		this.ghost = ghost;
		this.numberImages = numberImages;
		coloredGhost3D = new ColoredGhost3D(model3D, colors);
		movement = new Creature3DMovement(this, ghost);
	}

	public void reset(GameModel game) {
		update(game);
		movement.reset();
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

	// 2022-11-14: I wanted to add a point light to each ghost but learned today, that JavaFX only allows up to 3 point
	// lights per subscene. Very sad.
	private void useColoredGhost() {
		getChildren().setAll(coloredGhost3D.getRoot());
	}

	private void changeLook(GameModel game, Look newLook) {
		look = newLook;
		switch (newLook) {
		case NORMAL -> {
			coloredGhost3D.lookNormal();
			movement.reset();
			useColoredGhost();
		}
		case FRIGHTENED -> {
			coloredGhost3D.lookFrightened();
			useColoredGhost();
		}
		case FLASHING -> {
			int numFlashes = game.level.numFlashes();
			if (numFlashes > 0) {
				coloredGhost3D.lookFlashing(numFlashes);
			} else {
				coloredGhost3D.lookFrightened();
			}
			useColoredGhost();
		}
		case EYES -> {
			coloredGhost3D.lookEyesOnly();
			movement.reset();
			useColoredGhost();
		}
		case NUMBER -> {
			configureNumberCube(game.killedIndex[ghost.id]);
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
		return centerX < 0 || centerX > game.level.world().numCols() * World.TS;
	}

	private Look computeLookForCurrentState(GameModel game) {
		return switch (ghost.getState()) {
		case LOCKED, LEAVING_HOUSE -> normalOrFrightenedOrFlashingLook(game);
		case FRIGHTENED -> frightenedOrFlashingLook(game);
		case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
		case EATEN -> Look.NUMBER;
		default -> Look.NORMAL;
		};
	}

	private Look normalOrFrightenedOrFlashingLook(GameModel game) {
		if (game.powerTimer.isRunning() && game.killedIndex[ghost.id] == -1) {
			return frightenedOrFlashingLook(game);
		}
		return Look.NORMAL;
	}

	private Look frightenedOrFlashingLook(GameModel game) {
		return game.isPacPowerFading() ? Look.FLASHING : Look.FRIGHTENED;
	}

	private void configureNumberCube(int valueIndex) {
		var texture = numberImages[valueIndex];
		var material = new PhongMaterial();
		material.setBumpMap(texture);
		material.setDiffuseMap(texture);
		numberCube.setMaterial(material);
	}
}