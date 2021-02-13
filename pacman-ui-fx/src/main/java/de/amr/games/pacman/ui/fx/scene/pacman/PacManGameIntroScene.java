package de.amr.games.pacman.ui.fx.scene.pacman;

import de.amr.games.pacman.heaven.God;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.scene.common.AbstractPacManGameScene;
import javafx.scene.paint.Color;

public class PacManGameIntroScene extends AbstractPacManGameScene {

	public PacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling, false);
	}

	@Override
	public void render() {
		if (game.state.ticksRun() == God.clock.sec(5)) {
			game.attractMode = true;
		}
		fill(Color.BLUE);
	}
}