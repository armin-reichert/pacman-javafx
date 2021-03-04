package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.GameScene2D;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle.
 * The stork drops the bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and
 * finally opens up to reveal a tiny Pac-Man. (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 implements GameScene2D {

	protected final PacManGameController controller;
	protected final FXRendering rendering;
	protected final SoundManager sounds;
	private MsPacMan_IntermissionScene3_Controller animation;

	public MsPacMan_IntermissionScene3(PacManGameController controller, FXRendering rendering, SoundManager sounds) {
		this.controller = controller;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	@Override
	public void start() {
		animation = new MsPacMan_IntermissionScene3_Controller(controller, rendering, sounds);
		animation.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		animation.update();
	}

	@Override
	public void draw(GraphicsContext g) {
		rendering.drawFlap(g, animation.flap);
		rendering.drawStork(g, animation.stork);
		rendering.drawPlayer(g, animation.msPacMan);
		rendering.drawSpouse(g, animation.pacMan);
		rendering.drawJuniorBag(g, animation.bag);
	}
}