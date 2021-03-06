package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
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
public class PlayScene2D extends AbstractGameScene2D {

	public PlayScene2D(Camera camera, PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		super(camera, controller, rendering, sounds);
	}

	@Override
	public void draw(GraphicsContext g) {
		GameModel game = controller.getGame();
		boolean flashing = rendering.mazeAnimations().mazeFlashing(game.level.mazeNumber).hasStarted();
		rendering.drawMaze(g, game.level.mazeNumber, 0, t(3), flashing);
		if (!flashing) {
			rendering.drawFoodTiles(g, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(g, game.level.world.energizerTiles());
		}
		rendering.drawGameState(g, game);
		rendering.drawBonus(g, game.bonus);
		rendering.drawPlayer(g, game.pac);
		game.ghosts().forEach(ghost -> rendering.drawGhost(g, ghost, game.pac.powerTicksLeft > 0));
		rendering.drawScore(g, game, game.state == PacManGameState.INTRO || game.attractMode);
		if (!game.attractMode) {
			rendering.drawLivesCounter(g, game, t(2), t(34));
		}
		rendering.drawLevelCounter(g, game, t(25), t(34));
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	public void updateCamera() {
		// TODO how to position the camera such that the player gets centered on the *rotated* scene?
//		double speed = 1.0 / clock.sec(1);
//		V2d camPosition = new V2d(cam.getTranslateX(), cam.getTranslateY());
//		V2d playerPosition = controller.getGame().pac.position.scaled(scale.getX());
//		V2d target = playerPosition.plus(sceneSize.scaled(-0.5)).plus(sceneSize.x / 2, 0);
//		V2d velocity = target.minus(camPosition).scaled(speed);
//		V2d newCamPosition = camPosition.plus(velocity);
//		cam.setTranslateX(newCamPosition.x);
//		cam.setTranslateY(newCamPosition.y);
	}

	@Override
	public void start() {
	}

	@Override
	public void update() {
	}

	@Override
	public void end() {
	}
}