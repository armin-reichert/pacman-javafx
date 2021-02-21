package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.scene.Group;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends GameScene {

	private final Rendering rendering;

	public PlayScene(Group root, double width, double height, double scaling, Rendering rendering) {
		super(root, width, height, scaling);
		this.rendering = rendering;
	}

	@Override
	public void update() {
	}

	@Override
	public void render() {
		clear();
		boolean flashing = rendering.mazeFlashing(game.level.mazeNumber).hasStarted();
		rendering.drawMaze(g, game.level.mazeNumber, 0, t(3), flashing);
		if (!flashing) {
			rendering.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		rendering.signalGameState(g, game);
		rendering.drawPac(g, game.pac);
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