package de.amr.games.pacman.ui.fx.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D {

	private PacMan_IntermissionScene1_Controller sceneController;

	public PacMan_IntermissionScene1() {
		super(Assets2D.RENDERING_2D.get(GameVariant.PACMAN), SoundAssets.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene1_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		rendering.drawGhost(gc, sceneController.blinky, false);
		if (sceneController.phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPlayer(gc, sceneController.pac);
		} else {
			gc.save();
			gc.translate(0, -10);
			rendering.drawBigPacMan(gc, sceneController.pac);
			gc.restore();
		}
		rendering.drawLevelCounter(gc, gameController.game(), t(25), t(34));
	}
}