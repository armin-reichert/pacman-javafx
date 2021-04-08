package de.amr.games.pacman.ui.fx.scenes.pacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.LevelCounter2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.pacman.BigPacMan2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_PacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D<GameRendering2D_PacMan> {

	private class SceneController extends PacMan_IntermissionScene1_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_1, 2);
		}
	}

	private SceneController sceneController;
	private LevelCounter2D<GameRendering2D_PacMan> levelCounter2D;
	private Player2D<GameRendering2D_PacMan> pacMan2D;
	private Ghost2D<GameRendering2D_PacMan> blinky2D;
	private BigPacMan2D bigPacMan2D;

	public PacMan_IntermissionScene1() {
		super(GameRendering2D.RENDERING_PACMAN, SoundAssets.get(GameVariant.PACMAN));
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
		pacMan2D = new Player2D<>(sceneController.pac, rendering);
		blinky2D = new Ghost2D<>(sceneController.blinky, rendering);
		bigPacMan2D = new BigPacMan2D(sceneController.pac, GameRendering2D.RENDERING_PACMAN);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		blinky2D.getFrightenedAnimation().restart();
		bigPacMan2D.getMunchingAnimation().restart();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		levelCounter2D.render(gc);
		blinky2D.render(gc);
		if (sceneController.phase == Phase.BLINKY_CHASING_PACMAN) {
			pacMan2D.render(gc);
		} else {
			bigPacMan2D.render(gc);
		}
	}
}