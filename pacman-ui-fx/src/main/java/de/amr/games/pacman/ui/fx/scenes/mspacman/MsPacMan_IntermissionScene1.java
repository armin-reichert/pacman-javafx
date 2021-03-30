package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.Flap2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.Ghost2D;
import de.amr.games.pacman.ui.fx.rendering.Heart2D;
import de.amr.games.pacman.ui.fx.rendering.Player2D;
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

		@Override
		public void playFlapAnimation() {
			flap2D.getAnimation().restart();
		}
	}

	private SceneController sceneController;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Ghost2D inky2D;
	private Ghost2D pinky2D;
	private Flap2D flap2D;
	private Heart2D heart2D;

	public MsPacMan_IntermissionScene1() {
		super(GameRendering2D.RENDERING_MS_PACMAN, SoundAssets.get(GameVariant.MS_PACMAN));
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController, rendering);
		sceneController.start();
		flap2D = new Flap2D(sceneController.flap);
		flap2D.setRendering(rendering);
		msPacMan2D = new Player2D(sceneController.msPac);
		msPacMan2D.setRendering(rendering);
		msPacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		pacMan2D = new Player2D(sceneController.pacMan);
		pacMan2D.setRendering(rendering);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		inky2D = new Ghost2D(sceneController.inky);
		inky2D.setRendering(rendering);
		inky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		pinky2D = new Ghost2D(sceneController.pinky);
		pinky2D.setRendering(rendering);
		pinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		heart2D = new Heart2D(sceneController.heart);
		heart2D.setImage(rendering.getHeart());
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		inky2D.render(gc);
		pinky2D.render(gc);
		heart2D.render(gc);
	}
}