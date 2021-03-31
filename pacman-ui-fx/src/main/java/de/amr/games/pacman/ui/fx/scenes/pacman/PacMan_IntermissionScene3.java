package de.amr.games.pacman.ui.fx.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.MazeAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_PacMan;
import de.amr.games.pacman.ui.fx.rendering.Player2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller.Phase;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends AbstractGameScene2D {

	private class SceneController extends PacMan_IntermissionScene3_Controller {

		public SceneController(PacManGameController gameController, MazeAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_3, 2);
		}
	}

	private SceneController sceneController;
	private Player2D pacMan2D;

	public PacMan_IntermissionScene3() {
		super(GameRendering2D.RENDERING_PACMAN, SoundAssets.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController, rendering);
		sceneController.start();
		pacMan2D = new Player2D(sceneController.pac);
		pacMan2D.setRendering(rendering);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		render();
	}

	public void render() {
		GameRendering2D_PacMan r = (GameRendering2D_PacMan) rendering;
		r.drawLevelCounter(gc, gameController.game(), t(25), t(34));
		pacMan2D.render(gc);
		if (sceneController.phase == Phase.CHASING_PACMAN) {
			r.drawBlinkyPatched(gc, sceneController.blinky);
		} else {
			r.drawBlinkyNaked(gc, sceneController.blinky);
		}
	}
}