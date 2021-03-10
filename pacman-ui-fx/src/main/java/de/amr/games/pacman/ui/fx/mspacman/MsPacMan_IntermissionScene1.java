package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.fx.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene1_Controller;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

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

	private MsPacMan_IntermissionScene1_Controller animation;

	public MsPacMan_IntermissionScene1(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		super(controller, rendering, sounds);
	}

	@Override
	public void start() {
		animation = new MsPacMan_IntermissionScene1_Controller(controller, rendering, sounds);
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
		rendering.drawPlayer(g, animation.msPac);
		rendering.drawSpouse(g, animation.pacMan);
		rendering.drawGhost(g, animation.inky, false);
		rendering.drawGhost(g, animation.pinky, false);
		rendering.drawHeart(g, animation.heart);
	}
}