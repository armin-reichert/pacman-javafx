package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D {

	private PacMan_IntermissionScene1_Controller sceneController;

	public PacMan_IntermissionScene1(PacManGameController controller, PacManGameRendering2D rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		sceneController = new PacMan_IntermissionScene1_Controller(controller, rendering, sounds);
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
		rendering.drawGhost(g, sceneController.blinky, false);
		if (sceneController.phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPlayer(g, sceneController.pac);
		} else {
			g.translate(0, -10);
			rendering.drawBigPacMan(g, sceneController.pac);
			g.translate(0, 10);
		}
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
	}
}