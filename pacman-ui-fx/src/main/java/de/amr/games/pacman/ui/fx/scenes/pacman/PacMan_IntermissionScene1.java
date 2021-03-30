package de.amr.games.pacman.ui.fx.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.PacManGameAnimations2D;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_PacMan;
import de.amr.games.pacman.ui.fx.rendering.Ghost2D;
import de.amr.games.pacman.ui.fx.rendering.Player2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;
import de.amr.games.pacman.ui.sound.PacManGameSound;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D {

	private class SceneController extends PacMan_IntermissionScene1_Controller {

		public SceneController(PacManGameController gameController, PacManGameAnimations2D animations) {
			super(gameController, animations);
		}

		@Override
		public void playIntermissionSound() {
			sounds.loop(PacManGameSound.INTERMISSION_1, 2);
		}
	}

	private SceneController sceneController;
	private Player2D pacMan2D;
	private Ghost2D blinky2D;

	public PacMan_IntermissionScene1() {
		super(GameRendering2D.RENDERING_PACMAN, SoundAssets.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController, rendering);
		sceneController.start();
		pacMan2D = new Player2D(sceneController.pac);
		pacMan2D.setRendering(rendering);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinky2D = new Ghost2D(sceneController.blinky);
		blinky2D.setRendering(rendering);
		blinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		blinky2D.getFrightenedAnimation().restart();
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		render();
	}

	public void render() {
		GameRendering2D_PacMan r = (GameRendering2D_PacMan) rendering;
		blinky2D.render(gc);
		if (sceneController.phase == Phase.BLINKY_CHASING_PACMAN) {
			pacMan2D.render(gc);
		} else {
			gc.save();
			gc.translate(0, -10);
			r.drawBigPacMan(gc, sceneController.pac);
			gc.restore();
		}
		r.drawLevelCounter(gc, gameController.game(), t(25), t(34));
	}
}