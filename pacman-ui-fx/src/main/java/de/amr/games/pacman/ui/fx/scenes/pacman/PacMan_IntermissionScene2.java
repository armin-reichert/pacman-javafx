package de.amr.games.pacman.ui.fx.scenes.pacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_PacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import javafx.geometry.Rectangle2D;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

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
	private TimedSequence<Rectangle2D> blinkyStretchedAnimation;
	private TimedSequence<Rectangle2D> blinkyDamagedAnimation;

	public PacMan_IntermissionScene2() {
		super(GameRendering2D.RENDERING_PACMAN, SoundAssets.get(GameVariant.PACMAN));
	}

	@Override
	public void start() {
		super.start();
		sceneController = new SceneController(gameController);
		sceneController.init();
		pacMan2D = new Player2D(sceneController.pac, rendering);
		blinky2D = new Ghost2D(sceneController.blinky, rendering);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		blinkyStretchedAnimation = rendering.createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = rendering.createBlinkyDamagedAnimation();
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		// TODO cleanup
		GameRendering2D_PacMan r = (GameRendering2D_PacMan) rendering;
		r.drawNail(gc, sceneController.nail);
		pacMan2D.render(gc);
		if (sceneController.nailDistance() < 0) {
			blinky2D.render(gc);
		} else {
			drawBlinkyStretched(sceneController.blinky, sceneController.nail.position, sceneController.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Ghost blinky, V2d nailPosition, int stretching) {
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		rendering.drawSprite(gc, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			blinky2D.render(gc);
		} else {
			Rectangle2D damagedDress = blinkyDamagedAnimation.frame(blinky.dir == Direction.UP ? 0 : 1);
			rendering.drawSprite(gc, damagedDress, (int) (blinky.position.x - 4), (int) (blinky.position.y - 4));
		}
	}
}