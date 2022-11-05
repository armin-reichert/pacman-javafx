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
import de.amr.games.pacman.ui.fx._2d.rendering.GhostColors;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
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
public class Ghost3D extends MovingCreature3D {

	private enum Look {
		NORMAL_COLOR, FRIGHTENED_COLOR, FLASHING, EYES_ONLY, NUMBER;
	}

	private final ColoredGhost3D coloredGhost3D;
	private final Box numberCube = new Box(TS, TS, TS);
	private final Image[] numberImages;
	private Look look;

	public Ghost3D(Ghost ghost, Model3D model3D, Rendering2D r2D, GhostColors colors) {
		super(ghost);
		numberImages = r2D.createGhostValueList().frames().map(r2D.spritesheet()::subImage).toArray(Image[]::new);
		coloredGhost3D = new ColoredGhost3D(model3D, colors);
	}

	public void reset(GameModel game) {
		update(game);
		resetMovement();
	}

	public void update(GameModel game) {
		var newLook = lookForCurrentState(game);
		if (look != newLook) {
			changeLook(game, newLook);
		}
		if (look != Look.NUMBER) {
			updateMovement();
		}
		setVisible(guy.isVisible() && !outsideWorld(game)); // ???
	}

	private void changeLook(GameModel game, Look newLook) {
		look = newLook;
		switch (newLook) {
		case NORMAL_COLOR -> {
			coloredGhost3D.lookNormal();
			resetMovement();
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case FRIGHTENED_COLOR -> {
			coloredGhost3D.lookFrightened();
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case FLASHING -> {
			int numFlashes = game.level.numFlashes();
			if (numFlashes > 0) {
				coloredGhost3D.lookFlashing(numFlashes);
			} else {
				coloredGhost3D.lookFrightened();
			}
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case EYES_ONLY -> {
			coloredGhost3D.lookEyesOnly();
			resetMovement();
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case NUMBER -> {
			var ghost = (Ghost) guy;
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
		double centerX = guy.getPosition().x() + World.HTS;
		return centerX < 0 || centerX > game.world().numCols() * World.TS;
	}

	private Look lookForCurrentState(GameModel game) {
		var ghost = (Ghost) guy;
		return switch (ghost.getState()) {
		case LOCKED, LEAVING_HOUSE -> game.powerTimer.isRunning() && game.killedIndex[ghost.id] == -1
				? frightenedOrFlashingLook(game)
				: Look.NORMAL_COLOR;
		case FRIGHTENED -> frightenedOrFlashingLook(game);
		case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES_ONLY;
		case EATEN -> Look.NUMBER;
		default -> Look.NORMAL_COLOR;
		};
	}

	private Look frightenedOrFlashingLook(GameModel game) {
		return game.isPacPowerFading() ? Look.FLASHING : Look.FRIGHTENED_COLOR;
	}

	private void configureNumberCube(int valueIndex) {
		var texture = numberImages[valueIndex];
		var material = new PhongMaterial();
		material.setBumpMap(texture);
		material.setDiffuseMap(texture);
		numberCube.setMaterial(material);
	}
}