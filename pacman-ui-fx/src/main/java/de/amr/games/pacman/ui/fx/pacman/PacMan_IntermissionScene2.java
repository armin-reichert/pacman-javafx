package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	private PacMan_IntermissionScene2_Controller animation;

	public PacMan_IntermissionScene2(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new PacMan_IntermissionScene2_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		animation.update();
		clearCanvas();
		GraphicsContext g = canvas.getGraphicsContext2D();
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
		rendering.drawNail(g, animation.nail);
		rendering.drawPlayer(g, animation.pac);
		if (animation.nailDistance() < 0) {
			rendering.drawGhost(g, animation.blinky, false);
		} else {
			rendering.drawBlinkyStretched(g, animation.blinky, animation.nail.position, animation.nailDistance() / 4);
		}
	}
}