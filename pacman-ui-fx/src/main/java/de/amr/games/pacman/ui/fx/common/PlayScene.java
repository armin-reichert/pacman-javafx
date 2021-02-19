package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import javafx.scene.paint.Color;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene<RENDERING extends SceneRendering> extends GameScene<RENDERING> {

	public PlayScene(double width, double height, double scaling, int gameType) {
		super(width, height, scaling, gameType);
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		SceneRendering r = rendering();
		boolean flashing = r.mazeFlashing(game.level.mazeNumber).hasStarted();
		r.drawMaze(g, game.level.mazeNumber, 0, t(3), flashing);
		if (!flashing) {
			r.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile), game.level::containsEatenFood);
			r.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		r.signalGameState(g, game);
		r.drawPac(g, game.pac, game);
		game.ghosts().forEach(ghost -> r.drawGhost(g, ghost, game));
		r.drawBonus(g, game.bonus, game);
		r.drawScore(g, game, game.state == PacManGameState.INTRO || game.attractMode);
		if (PacManGameFXUI.flashMessage().isPresent()) {
			drawFlashMessage();
		} else {
			if (!game.attractMode) {
				r.drawLivesCounter(g, game, t(2), t(34));
			}
			r.drawLevelCounter(g, game, t(25), t(34));
		}
	}
}