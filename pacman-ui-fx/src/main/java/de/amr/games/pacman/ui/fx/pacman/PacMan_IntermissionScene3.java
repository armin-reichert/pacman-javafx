package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene3_Controller.Phase;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing
 * dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends AbstractGameScene2D {

	private PacMan_IntermissionScene3_Controller animation;

	public PacMan_IntermissionScene3(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new PacMan_IntermissionScene3_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		GraphicsContext g = canvas.getGraphicsContext2D();
		clearCanvas();
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
		rendering.drawPlayer(g, animation.pac);
		if (animation.phase == Phase.CHASING_PACMAN) {
			rendering.drawBlinkyPatched(g, animation.blinky);
		} else {
			rendering.drawBlinkyNaked(g, animation.blinky);
		}
	}
}