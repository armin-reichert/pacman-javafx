package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene2_Controller;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over.
 * After three turns, they both rapidly run from left to right and right to
 * left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D<Rendering2D_MsPacMan> {

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
	private Player2D<Rendering2D_MsPacMan> msPacMan2D;
	private Player2D<Rendering2D_MsPacMan> pacMan2D;
	private Flap2D flap2D;

	public MsPacMan_IntermissionScene2() {
		super(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, Rendering2D_Impl.RENDERING_MS_PACMAN, MsPacManScenes.SOUNDS);
	}

	@Override
	public void init() {
		super.init();
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap, Rendering2D_Impl.RENDERING_MS_PACMAN);
		msPacMan2D = new Player2D<>(sceneController.msPacMan, rendering);
		pacMan2D = new Player2D<>(sceneController.pacMan, rendering);
		pacMan2D.setMunchingAnimations(Rendering2D_Impl.RENDERING_MS_PACMAN.createSpouseMunchingAnimations());
		msPacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		renderLevelCounter(new V2i(25, 34));
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
	}
}