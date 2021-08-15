package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Stork2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a
 * little blue bundle. The stork drops the bundle, which falls to the ground in
 * front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny
 * Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends AbstractGameScene2D {

	private static final V2i LEVEL_COUNTER_POS = new V2i(25, 34);

	private class SceneController extends MsPacMan_IntermissionScene3_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_3);
		}

		@Override
		public void playFlapAnimation() {
			flap2D.animation.restart();
		}
	}

	private SceneController sceneController;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3() {
		super(MsPacManScenes.RENDERING, MsPacManScenes.SOUNDS, 28, 36);
	}

	@Override
	public void init() {
		super.init();
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap, MsPacManScenes.RENDERING);
		msPacMan2D = new Player2D(sceneController.msPacMan, rendering);
		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		stork2D = new Stork2D(sceneController.stork, MsPacManScenes.RENDERING);
		bag2D = new JuniorBag2D(sceneController.bag, (Rendering2D_MsPacMan) rendering);
		pacMan2D.munchingAnimations = MsPacManScenes.RENDERING.createSpouseMunchingAnimations();
		stork2D.animation.restart();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		renderLevelCounter(LEVEL_COUNTER_POS);
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		stork2D.render(gc);
		bag2D.render(gc);
	}
}