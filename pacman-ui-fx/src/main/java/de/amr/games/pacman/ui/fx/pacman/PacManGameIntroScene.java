package de.amr.games.pacman.ui.fx.pacman;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;

/**
 * Intro scene of the PacMan game.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntroScene extends AbstractPacManGameScene {

	enum Phase {
		BEGIN, BLINKY, PINKY, INKY, CLYDE, POINTS, CHASING_PAC, CHASING_GHOSTS, PRESS_KEY
	}

	public PacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling, false);
	}

	@Override
	public void render() {
		if (game.state.ticksRun() == God.clock.sec(30)) {
			game.attractMode = true;
		}
		fill(Color.BLACK);

	}
}