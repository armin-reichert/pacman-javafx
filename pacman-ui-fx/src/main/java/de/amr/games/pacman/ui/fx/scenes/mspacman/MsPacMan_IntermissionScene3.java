package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.rendering.Assets2D;
import de.amr.games.pacman.ui.fx.rendering.MsPacManGameRendering;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends AbstractGameScene2D {

	private class SceneController extends MsPacMan_IntermissionScene3_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_3);
		}
	}

	private SceneController sceneController;

	public MsPacMan_IntermissionScene3() {
		super(Assets2D.RENDERING_2D.get(GameVariant.MS_PACMAN), SoundAssets.get(GameVariant.MS_PACMAN));
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
		MsPacManGameRendering r = (MsPacManGameRendering) rendering;
		r.drawFlap(gc, sceneController.flap);
		r.drawStork(gc, sceneController.stork);
		r.drawPlayer(gc, sceneController.msPacMan);
		r.drawSpouse(gc, sceneController.pacMan);
		r.drawJuniorBag(gc, sceneController.bag);
	}
}