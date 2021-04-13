package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Stork2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;

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
public class MsPacMan_IntermissionScene3 extends AbstractGameScene2D<Rendering2D_MsPacMan> {

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
			flap2D.getAnimation().restart();
		}
	}

	private SceneController sceneController;
	private Player2D<Rendering2D_MsPacMan> msPacMan2D;
	private Player2D<Rendering2D_MsPacMan> pacMan2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3() {
		super(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, Rendering2D_Impl.RENDERING_MS_PACMAN, MsPacManScenes.SOUNDS);
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap, Rendering2D_Impl.RENDERING_MS_PACMAN);
		msPacMan2D = new Player2D<>(sceneController.msPacMan, rendering);
		pacMan2D = new Player2D<>(sceneController.pacMan, rendering);
		stork2D = new Stork2D(sceneController.stork, Rendering2D_Impl.RENDERING_MS_PACMAN);
		bag2D = new JuniorBag2D(sceneController.bag, rendering);
		pacMan2D.setMunchingAnimations(Rendering2D_Impl.RENDERING_MS_PACMAN.createSpouseMunchingAnimations());
		stork2D.getAnimation().restart();
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
		stork2D.render(gc);
		bag2D.render(gc);
	}
}