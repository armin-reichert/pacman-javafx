package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they
 * quickly move upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms.
 * Pac-Man face each other at the top of the screen and a big pink heart appears above them. (Played
 * after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends AbstractGameScene2D {

	private class SceneController extends MsPacMan_IntermissionScene1_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_1, 1);
		}
	}

	private SceneController sceneController;

	public MsPacMan_IntermissionScene1() {
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
		GameRendering2D_MsPacMan r = (GameRendering2D_MsPacMan) rendering;
		sceneController.update();
		r.drawFlap(gc, sceneController.flap);
		r.drawPlayer(gc, sceneController.msPac);
		r.drawSpouse(gc, sceneController.pacMan);
		r.drawGhost(gc, sceneController.inky, false);
		r.drawGhost(gc, sceneController.pinky, false);
		r.drawHeart(gc, sceneController.heart);
	}
}