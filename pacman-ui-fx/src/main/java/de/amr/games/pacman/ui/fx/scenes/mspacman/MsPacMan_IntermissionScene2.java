package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene2_Controller;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D<GameRendering2D_MsPacMan> {

	private class SceneController extends MsPacMan_IntermissionScene2_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_2);
		}

		@Override
		public void playFlapAnimation() {
			flap2D.getAnimation().restart();
		}

	}

	private SceneController sceneController;
	private Player2D<GameRendering2D_MsPacMan> msPacMan2D;
	private Player2D<GameRendering2D_MsPacMan> pacMan2D;
	private Flap2D flap2D;

	public MsPacMan_IntermissionScene2() {
		super(GameRendering2D.RENDERING_MS_PACMAN, SoundAssets.get(GameVariant.MS_PACMAN));
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap, GameRendering2D.RENDERING_MS_PACMAN);
		msPacMan2D = new Player2D<>(sceneController.msPacMan, rendering);
		pacMan2D = new Player2D<>(sceneController.pacMan, rendering);
		pacMan2D.setMunchingAnimations(GameRendering2D.RENDERING_MS_PACMAN.createSpouseMunchingAnimations());
		msPacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
	}
}