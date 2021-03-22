package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.common.scene2d.Assets2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	private PacMan_IntermissionScene2_Controller sceneController;

	public PacMan_IntermissionScene2() {
		super(Assets2D.RENDERING_2D.get(GameVariant.PACMAN), Assets2D.SOUND.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene2_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		sceneController.update();
		rendering.drawLevelCounter(gc, gameController.game(), t(25), t(34));
		rendering.drawNail(gc, sceneController.nail);
		rendering.drawPlayer(gc, sceneController.pac);
		if (sceneController.nailDistance() < 0) {
			rendering.drawGhost(gc, sceneController.blinky, false);
		} else {
			rendering.drawBlinkyStretched(gc, sceneController.blinky, sceneController.nail.position,
					sceneController.nailDistance() / 4);
		}
	}
}