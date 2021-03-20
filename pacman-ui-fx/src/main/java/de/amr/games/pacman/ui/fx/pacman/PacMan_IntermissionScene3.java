package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.common.scene2d.Assets2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller.Phase;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends AbstractGameScene2D {

	private PacMan_IntermissionScene3_Controller sceneController;

	public PacMan_IntermissionScene3() {
		super(Assets2D.RENDERING_2D.get(GameType.PACMAN), Assets2D.SOUND.get(GameType.PACMAN));
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene3_Controller(controller, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		sceneController.update();
		rendering.drawLevelCounter(gc, controller.selectedGame(), t(25), t(34));
		rendering.drawPlayer(gc, sceneController.pac);
		if (sceneController.phase == Phase.CHASING_PACMAN) {
			rendering.drawBlinkyPatched(gc, sceneController.blinky);
		} else {
			rendering.drawBlinkyNaked(gc, sceneController.blinky);
		}
	}
}