package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D {

	private class SceneController extends MsPacMan_IntermissionScene2_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_2);
		}

	}

	private SceneController sceneController;

	public MsPacMan_IntermissionScene2() {
		super(GameRendering2D.RENDERING_MS_PACMAN, SoundAssets.get(GameVariant.MS_PACMAN));
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
		GameRendering2D_MsPacMan r = (GameRendering2D_MsPacMan) rendering;
		r.drawFlap(gc, sceneController.flap);
		r.drawPlayer(gc, sceneController.msPacMan);
		r.drawSpouse(gc, sceneController.pacMan);
	}
}