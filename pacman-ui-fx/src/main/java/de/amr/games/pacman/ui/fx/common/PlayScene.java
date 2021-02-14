package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.PacManGameModel;
import javafx.scene.paint.Color;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene<R extends SceneRendering> extends AbstractPacManGameScene<R> {

	public PlayScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling);
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
		rendering.showGameState(game);
		rendering.drawPac(game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGhost(ghost, game));
		rendering.drawBonus(game.bonus, game);
		rendering.drawScore(game, game.state == PacManGameState.INTRO || game.attractMode);
		if (!game.attractMode) {
			rendering.drawLivesCounter(game, t(2), t(34));
		}
		rendering.drawLevelCounter(game, t(25), t(34));
	}
}