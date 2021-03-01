package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.GameScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntermissionScene1_Controller.Phase;
import javafx.scene.canvas.GraphicsContext;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends GameScene {

	private PacMan_IntermissionScene1_Controller animation;

	public PacMan_IntermissionScene1(PacManGameController controller, double scaling, FXRendering rendering,
			SoundManager sounds) {
		super(controller, scaling, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new PacMan_IntermissionScene1_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void draw(GraphicsContext g) {
		rendering.drawGhost(g, animation.blinky, false);
		if (animation.phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPlayer(g, animation.pac);
		} else {
			g.translate(0, -10);
			rendering.drawBigPacMan(g, animation.pac);
			g.translate(0, 10);
		}
		rendering.drawLevelCounter(g, controller.getGame(), t(25), t(34));
	}
}