package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene2_Controller;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D {

	private MsPacMan_IntermissionScene2_Controller animation;

	public MsPacMan_IntermissionScene2(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new MsPacMan_IntermissionScene2_Controller(controller, rendering, sounds);
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
		rendering.drawFlap(g, animation.flap);
		rendering.drawPlayer(g, animation.msPacMan);
		rendering.drawSpouse(g, animation.pacMan);
	}
}