package de.amr.games.pacman.ui.fx.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_PacMan;
import de.amr.games.pacman.ui.fx.rendering.Ghost2D;
import de.amr.games.pacman.ui.fx.rendering.Player2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.PacManGameSound;
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
		pacMan2D = new Player2D(sceneController.pac);
		pacMan2D.setRendering(rendering);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
		blinky2D = new Ghost2D(sceneController.blinky);
		blinky2D.setRendering(rendering);
		blinky2D.getKickingAnimations().values().forEach(TimedSequence::restart);
		blinkyStretchedAnimation = rendering.createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = rendering.createBlinkyDamagedAnimation();
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		render();
	}

	public void render() {
		GameRendering2D_PacMan r = (GameRendering2D_PacMan) rendering;
		r.drawLevelCounter(gc, gameController.game(), t(25), t(34));
		r.drawNail(gc, sceneController.nail);
		pacMan2D.render(gc);
		if (sceneController.nailDistance() < 0) {
			blinky2D.render(gc);
		} else {
			drawBlinkyStretched(sceneController.nail.position, sceneController.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(V2d nailPosition, int stretching) {
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		rendering.drawSprite(gc, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			blinky2D.render(gc);
		} else {
			Rectangle2D blinkyDamaged = blinkyDamagedAnimation.frame(blinky2D.ghost.dir == Direction.UP ? 0 : 1);
			rendering.drawSprite(gc, blinkyDamaged, (int) (blinky2D.ghost.position.x - 4),
					(int) (blinky2D.ghost.position.y - 4));
		}
	}

}