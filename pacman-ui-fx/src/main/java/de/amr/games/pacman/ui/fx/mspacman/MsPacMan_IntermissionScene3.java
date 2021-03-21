package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.common.scene2d.Assets2D;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene3_Controller;

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
public class MsPacMan_IntermissionScene3 extends AbstractGameScene2D {

	private MsPacMan_IntermissionScene3_Controller sceneController;

	public MsPacMan_IntermissionScene3() {
		super(Assets2D.RENDERING_2D.get(GameVariant.MS_PACMAN), Assets2D.SOUND.get(GameVariant.MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new MsPacMan_IntermissionScene3_Controller(controller, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		sceneController.update();
		rendering.drawFlap(gc, sceneController.flap);
		rendering.drawStork(gc, sceneController.stork);
		rendering.drawPlayer(gc, sceneController.msPacMan);
		rendering.drawSpouse(gc, sceneController.pacMan);
		rendering.drawJuniorBag(gc, sceneController.bag);
	}
}