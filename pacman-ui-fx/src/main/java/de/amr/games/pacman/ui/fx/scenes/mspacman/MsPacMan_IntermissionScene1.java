package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx.entities._2d.mspacman.Heart2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;

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

		public SceneController(PacManGameController gameController) {
			super(gameController);
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
		super(Scenes.RENDERING, Scenes.SOUNDS);
	}

	@Override
	public void init() {
		super.init();
		sceneController = new SceneController(gameController);
		sceneController.init();
		flap2D = new Flap2D(sceneController.flap, Scenes.RENDERING);
		msPacMan2D = new Player2D(sceneController.msPac, rendering);
		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		inky2D = new Ghost2D(sceneController.inky, rendering);
		pinky2D = new Ghost2D(sceneController.pinky, rendering);
		heart2D = new Heart2D(sceneController.heart, (Rendering2D_MsPacMan) rendering);
		// overwrite by Pac-Man instead of Ms. Pac-Man sprites:
		pacMan2D.setMunchingAnimations(Scenes.RENDERING.createSpouseMunchingAnimations());
		msPacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		inky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		pinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		renderLevelCounter(new V2i(25, 34));
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		inky2D.render(gc);
		pinky2D.render(gc);
		heart2D.render(gc);
	}
}