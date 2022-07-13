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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.CreatureMotionAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.GhostBodyAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.GhostValueAnimation;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import javafx.scene.Group;
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
public class Ghost3D extends Group {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private enum Mode {
		COLORED_DRESS, BLUE_DRESS, FLASHING_DRESS, EYES, NUMBER;
	}

	private final Ghost ghost;
	private final GhostValueAnimation value;
	private final GhostBodyAnimation body;
	private Mode mode;

	public Ghost3D(Ghost ghost, Model3D model3D, Rendering2D r2D) {
		this.ghost = ghost;
		value = new GhostValueAnimation(r2D);
		body = new GhostBodyAnimation(ghost, model3D, new CreatureMotionAnimation(ghost, this));
	}

	public void reset(GameModel game) {
		update(game);
		body.reset();
	}

	public void update(GameModel game) {
		var newMode = switch (ghost.getState()) {
		case LOCKED, LEAVING_HOUSE -> game.powerTimer.isRunning() ? frightenedMode(game) : Mode.COLORED_DRESS;
		case FRIGHTENED -> frightenedMode(game);
		case ENTERING_HOUSE -> Mode.EYES;
		case EATEN -> Mode.NUMBER;
		case RETURNING_TO_HOUSE -> Mode.EYES;
		default -> Mode.COLORED_DRESS;
		};
		if (mode != newMode) {
			setMode(game, newMode);
		}
		if (mode != Mode.NUMBER) {
			body.update();
		}
		setVisible(ghost.isVisible());
		setOpacity(1.0);
		game.world().portals().stream().filter(portal -> portal.distance(ghost) <= 30.0).findFirst().ifPresent(portal -> {
			var distance = portal.distance(ghost);
			var opacity = U.lerp(0.5, 1.0, distance / 30.0);
//			var dressColor = body.dressColorPy.get();
//			body.dressColorPy.set(Color.color(dressColor.getRed(), dressColor.getGreen(), dressColor.getBlue(), opacity));
			LOGGER.info("Ghost %s, opacity %.2f, is at %.2f px from portal %s", ghost.name, opacity, distance, portal);
			setVisible(distance > 16.0);
		});
	}

	private Mode frightenedMode(GameModel game) {
		return game.isPacPowerFading() ? Mode.FLASHING_DRESS : Mode.BLUE_DRESS;
	}

	private void setMode(GameModel game, Mode newMode) {
		mode = newMode;
		getChildren().setAll(newMode == Mode.NUMBER ? value.getRoot() : body.getRoot());
		switch (newMode) {
		case COLORED_DRESS -> body.wearColoredDress();
		case BLUE_DRESS -> body.wearBlueDress(0);
		case FLASHING_DRESS -> body.wearBlueDress(game.level.numFlashes);
		case EYES -> body.dress().setVisible(false);
		case NUMBER -> {
			value.selectNumberAtIndex(ghost.killedIndex);
			// rotate node such that number can be read from left to right
			setRotationAxis(Rotate.X_AXIS);
			setRotate(0);
		}
		}
	}
}