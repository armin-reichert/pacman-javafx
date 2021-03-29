package de.amr.games.pacman.ui.fx.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.rendering.Assets2D;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	class SceneController extends PacMan_IntermissionScene2_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_2);
		}
	}

	private SceneController sceneController;

	public PacMan_IntermissionScene2() {
		super(Assets2D.RENDERING_2D.get(GameVariant.PACMAN), SoundAssets.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController, rendering);
		sceneController.start();
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		PacManGameRendering r = (PacManGameRendering) rendering;
		r.drawLevelCounter(gc, gameController.game(), t(25), t(34));
		r.drawNail(gc, sceneController.nail);
		r.drawPlayer(gc, sceneController.pac);
		if (sceneController.nailDistance() < 0) {
			r.drawGhost(gc, sceneController.blinky, false);
		} else {
			r.drawBlinkyStretched(gc, sceneController.blinky, sceneController.nail.position,
					sceneController.nailDistance() / 4);
		}
	}
}