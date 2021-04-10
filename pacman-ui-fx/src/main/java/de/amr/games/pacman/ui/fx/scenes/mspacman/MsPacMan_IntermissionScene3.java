package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.entities._2d.LevelCounter2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Stork2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
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
public class MsPacMan_IntermissionScene3 extends AbstractGameScene2D<GameRendering2D_MsPacMan> {

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
	private LevelCounter2D<GameRendering2D_MsPacMan> levelCounter2D;
	private Player2D<GameRendering2D_MsPacMan> msPacMan2D;
	private Player2D<GameRendering2D_MsPacMan> pacMan2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3() {
		super(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, GameRendering2D.RENDERING_MS_PACMAN,
				PacManGameUI_JavaFX.SOUNDS_MS_PACMAN);
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController);
		sceneController.init();
		levelCounter2D = new LevelCounter2D<>(rendering);
		levelCounter2D.setRightUpperCorner(new V2i(25, 34));
		levelCounter2D.setLevelSymbols(game().levelSymbols);
		levelCounter2D.setLevelNumberSupplier(() -> game().currentLevelNumber);
		flap2D = new Flap2D(sceneController.flap, GameRendering2D.RENDERING_MS_PACMAN);
		msPacMan2D = new Player2D<>(sceneController.msPacMan, rendering);
		pacMan2D = new Player2D<>(sceneController.pacMan, rendering);
		stork2D = new Stork2D(sceneController.stork, GameRendering2D.RENDERING_MS_PACMAN);
		bag2D = new JuniorBag2D(sceneController.bag, rendering);
		pacMan2D.setMunchingAnimations(GameRendering2D.RENDERING_MS_PACMAN.createSpouseMunchingAnimations());
		stork2D.getAnimation().restart();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		levelCounter2D.render(gc);
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		stork2D.render(gc);
		bag2D.render(gc);
	}
}