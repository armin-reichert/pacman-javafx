package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	private PacMan_IntermissionScene2_Controller sceneController;

	public PacMan_IntermissionScene2(PacManGameController controller, PacManGameRendering2D rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene2_Controller(controller, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		sceneController.update();
		clearCanvas();
		GraphicsContext g = canvas.getGraphicsContext2D();
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
		rendering.drawNail(g, sceneController.nail);
		rendering.drawPlayer(g, sceneController.pac);
		if (sceneController.nailDistance() < 0) {
			rendering.drawGhost(g, sceneController.blinky, false);
		} else {
			rendering.drawBlinkyStretched(g, sceneController.blinky, sceneController.nail.position, sceneController.nailDistance() / 4);
		}
	}
}