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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Creature;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.GhostColored3D;
import de.amr.games.pacman.ui.fx._3d.animation.NumberBox3D;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;

/**
 * 3D representation of a ghost.
 * <p>
 * A ghost is displayed in one of the following modes:
 * <ul>
 * <li>complete, colorful ghost with blue eyes,
 * <li>complete, blue ghost with red eyes,
 * <li>complete, blue-white flashing ghost,
 * <li>eyes only (dead ghost),
 * <li>number cube/quad (dead ghost value).
 * </ul>
 * 
 * @author Armin Reichert
 */
public class Ghost3D extends MovingCreature3D {

	private enum Look {
		COLORED_DRESS, BLUE_DRESS, FLASHING_DRESS, EYES, NUMBER;
	}

	private final Ghost ghost;
	private final NumberBox3D numberAnimation3D;
	private final GhostColored3D coloredGhost3D;
	private Look look;

	public Ghost3D(Ghost ghost, Model3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		numberAnimation3D = new NumberBox3D(
				r2D.createGhostValueList().frames().map(r2D::getSpriteImage).toArray(Image[]::new));
		coloredGhost3D = new GhostColored3D(this, ghost, model3D);
	}

	@Override
	public Creature guy() {
		return ghost;
	}

	public void reset(GameModel game) {
		update(game);
		coloredGhost3D.reset();
	}

	public void update(GameModel game) {
		var newLook = lookForCurrentState(game);
		if (look != newLook) {
			changeLook(game, newLook);
		}
		if (look != Look.NUMBER) {
			coloredGhost3D.update();
		}
		setVisible(ghost.isVisible() && !outsideWorld(game)); // ???
	}

	private boolean outsideWorld(GameModel game) {
		double centerX = ghost.getPosition().x() + World.HTS;
		return centerX < 0 || centerX > game.world().numCols() * World.TS;
	}

	private Look lookForCurrentState(GameModel game) {
		return switch (ghost.getState()) {
		case LOCKED, LEAVING_HOUSE -> game.powerTimer.isRunning() && game.killedIndex[ghost.id] == -1 ? frightenedLook(game)
				: Look.COLORED_DRESS;
		case FRIGHTENED -> frightenedLook(game);
		case ENTERING_HOUSE, RETURNING_TO_HOUSE -> Look.EYES;
		case EATEN -> Look.NUMBER;
		default -> Look.COLORED_DRESS;
		};

	}

	private Look frightenedLook(GameModel game) {
		return game.isPacPowerFading() ? Look.FLASHING_DRESS : Look.BLUE_DRESS;
	}

	private void changeLook(GameModel game, Look newLook) {
		look = newLook;
		switch (newLook) {
		case COLORED_DRESS -> {
			coloredGhost3D.wearColoredDress();
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case BLUE_DRESS -> {
			coloredGhost3D.wearBlueDress(0);
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case FLASHING_DRESS -> {
			coloredGhost3D.wearBlueDress(game.level.numFlashes());
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case EYES -> {
			coloredGhost3D.dress().setVisible(false);
			getChildren().setAll(coloredGhost3D.getRoot());
		}
		case NUMBER -> {
			numberAnimation3D.selectNumberAtIndex(game.killedIndex[ghost.id]);
			getChildren().setAll(numberAnimation3D);
			// rotate node such that number can be read from left to right
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + newLook);
		}
	}
}