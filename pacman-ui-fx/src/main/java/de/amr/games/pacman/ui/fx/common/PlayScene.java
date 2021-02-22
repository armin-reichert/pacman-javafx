package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.rendering.DefaultRendering;
import javafx.scene.Group;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends GameScene {

	public PlayScene(Group root, double width, double height, double scaling, DefaultRendering rendering) {
		super(root, width, height, scaling, rendering);
	}

	@Override
	public void update() {
	}

	@Override
	public void render() {
		boolean flashing = rendering.mazeFlashing(game.level.mazeNumber).hasStarted();
		rendering.drawMaze(g, game.level.mazeNumber, 0, t(3), flashing);
		if (!flashing) {
			rendering.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		rendering.drawGameState(g, game);
		rendering.drawPlayer(g, game.pac);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game.pac.powerTicksLeft > 0));
		rendering.drawBonus(g, game.bonus);
		rendering.drawScore(g, game, game.state == PacManGameState.INTRO || game.attractMode);
		if (PacManGameUI_JavaFX.flashMessage().isPresent()) {
			drawFlashMessage();
		} else {
			if (!game.attractMode) {
				rendering.drawLivesCounter(g, game, t(2), t(34));
			}
			rendering.drawLevelCounter(g, game, t(25), t(34));
		}
	}
}