package de.amr.games.pacman.ui.fx.scene.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.PacManGameModel;
import javafx.scene.paint.Color;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends AbstractPacManGameScene {

	public PlayScene(PacManGameModel game, double width, double height, double scaling, boolean msPacMan) {
		super(game, null, width, height, scaling, msPacMan);
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		boolean flashing = rendering.mazeFlashing(game.level.mazeNumber).hasStarted();
		rendering.drawMaze(game.level.mazeNumber, 0, t(3), flashing);
		if (!flashing) {
			rendering.drawFoodTiles(game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(game.level.world.energizerTiles());
		}
		if (game.attractMode || game.state == PacManGameState.GAME_OVER) {
			rendering.signalGameOverState(game);
		} else if (game.state == PacManGameState.READY) {
			rendering.signalReadyState(game);
		}
		rendering.drawPac(game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGhost(ghost, game));
		rendering.drawBonus(game.bonus, game);
		rendering.drawScore(game);
		if (!game.attractMode) {
			rendering.drawLivesCounter(game, t(2), t(34));
			rendering.drawLevelCounter(game, t(25), t(34));
		}
	}
}