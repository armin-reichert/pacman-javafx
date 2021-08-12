package de.amr.games.pacman.ui.fx.scenes.pacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.entities._2d.pacman.Nail2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import javafx.geometry.Rectangle2D;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	private static final V2i LEVEL_COUNTER_POS = new V2i(25, 34);

	class SceneController extends PacMan_IntermissionScene2_Controller {

		public SceneController(PacManGameController gameController) {
			super(gameController);
		}

		@Override
		public void playIntermissionSound() {
			sounds.play(PacManGameSound.INTERMISSION_2);
		}
	}

	private SceneController sceneController;
	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private Nail2D nail2D;
	private TimedSequence<Rectangle2D> blinkyStretchedAnimation;
	private TimedSequence<Rectangle2D> blinkyDamagedAnimation;

	public PacMan_IntermissionScene2() {
		super(PacManScenes.RENDERING, PacManScenes.SOUNDS, 28, 36);
	}

	@Override
	public void init() {
		super.init();
		sceneController = new SceneController(gameController);
		sceneController.init();
		pacMan2D = new Player2D(sceneController.pac, rendering);
		blinky2D = new Ghost2D(sceneController.blinky, rendering);
		nail2D = new Nail2D(sceneController.nail, (Rendering2D_PacMan) rendering);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		blinkyStretchedAnimation = PacManScenes.RENDERING.createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = PacManScenes.RENDERING.createBlinkyDamagedAnimation();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		renderLevelCounter(LEVEL_COUNTER_POS);
		pacMan2D.render(gc);
		nail2D.render(gc);
		if (sceneController.nailDistance() < 0) {
			blinky2D.render(gc);
		} else {
			drawBlinkyStretched(sceneController.blinky, sceneController.nail.position(), sceneController.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Ghost blinky, V2d nailPosition, int stretching) {
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		rendering.renderSprite(gc, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			blinky2D.render(gc);
		} else {
			Rectangle2D damagedDress = blinkyDamagedAnimation.frame(blinky.dir() == Direction.UP ? 0 : 1);
			rendering.renderSprite(gc, damagedDress, (int) (blinky.position().x - 4), (int) (blinky.position().y - 4));
		}
	}
}