package de.amr.games.pacman.ui.fx.scenes.pacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.pacman.BlinkyNaked2D;
import de.amr.games.pacman.ui.fx.entities._2d.pacman.BlinkyPatched2D;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller.Phase;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends AbstractGameScene2D {

	private class SceneController extends PacMan_IntermissionScene3_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_3, 2);
		}
	}

	private SceneController sceneController;
	private Player2D pacMan2D;
	private BlinkyPatched2D blinkyPatched2D;
	private BlinkyNaked2D blinkyNaked2D;

	public PacMan_IntermissionScene3() {
		super(PacManScenes.RENDERING, PacManScenes.SOUNDS);
	}

	@Override
	public void init() {
		super.init();
		sceneController = new SceneController(gameController);
		sceneController.init();
		pacMan2D = new Player2D(sceneController.pac, rendering);
		blinkyPatched2D = new BlinkyPatched2D(sceneController.blinky, PacManScenes.RENDERING);
		blinkyNaked2D = new BlinkyNaked2D(sceneController.blinky, PacManScenes.RENDERING);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinkyPatched2D.getAnimation().restart();
		blinkyNaked2D.getAnimation().restart();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		renderLevelCounter(new V2i(25, 34));
		pacMan2D.render(gc);
		if (sceneController.phase == Phase.CHASING_PACMAN) {
			blinkyPatched2D.render(gc);
		} else {
			blinkyNaked2D.render(gc);
		}
	}
}