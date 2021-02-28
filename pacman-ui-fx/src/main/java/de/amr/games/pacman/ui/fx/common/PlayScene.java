package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2d;
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
		double speed = 1.0 / clock.sec(1);
		V2d camPosition = new V2d(cam.getTranslateX(), cam.getTranslateY());
		V2d playerPosition = game.pac.position.scaled(scaling);
		// TODO how to position the camera such that the player gets centered on the rotated scene?
		V2d target = playerPosition.minus(width / 2, height / 2);
		V2d velocity = target.minus(camPosition).scaled(speed);
		cam.setTranslateX(cam.getTranslateX() + velocity.x);
		cam.setTranslateY(cam.getTranslateY() + velocity.y);
	}
}