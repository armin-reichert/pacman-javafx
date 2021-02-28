package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.scene.Camera;
import javafx.scene.canvas.GraphicsContext;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends GameScene {

	public PlayScene(double scaling, FXRendering rendering, SoundManager sounds) {
		super(scaling, rendering, sounds);
	}

	@Override
	public void update() {
	}

	@Override
	public void drawCanvas() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		boolean flashing = rendering.mazeAnimations().mazeFlashing(game.level.mazeNumber).hasStarted();
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
		if (!game.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), t(34));
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}

	@Override
	public void updateCamera(Camera cam) {
		double lerp = 1.0 / 60;
		double sx = scaling * game.pac.position.x;
		double sy = scaling * game.pac.position.y;
		double targetX = sx - width / 2;
		double targetY = sy - height / 2;
		double dx = targetX - cam.getTranslateX();
		double dy = targetY - cam.getTranslateY();
		cam.setTranslateX(cam.getTranslateX() + dx * lerp);
		cam.setTranslateY(cam.getTranslateY() + dy * lerp);
	}
}