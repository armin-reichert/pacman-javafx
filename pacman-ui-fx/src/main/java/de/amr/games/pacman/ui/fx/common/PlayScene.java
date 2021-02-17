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

	public PlayScene(PacManGameModel game, double width, double height, double scaling, R rendering) {
		super(game, null, width, height, scaling, rendering);
	}

	@Override
	public void update() {
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		boolean flashing = rendering.mazeFlashing(game.level.mazeNumber).hasStarted();
		rendering.drawMaze(g, game.level.mazeNumber, 0, t(3), flashing);
		if (!flashing) {
			rendering.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		rendering.signalGameState(g, game);
		rendering.drawPac(g, game.pac, game);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game));
		rendering.drawBonus(g, game.bonus, game);
		rendering.drawScore(g, game, game.state == PacManGameState.INTRO || game.attractMode);
		if (!game.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), t(34));
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}
}