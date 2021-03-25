package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundAssets;
import de.amr.games.pacman.ui.mspacman.MsPacMan_IntermissionScene2_Controller;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they
 * both rapidly run from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D {

	private MsPacMan_IntermissionScene2_Controller sceneController;

	public MsPacMan_IntermissionScene2() {
		super(Assets2D.RENDERING_2D.get(GameVariant.MS_PACMAN), SoundAssets.get(GameVariant.MS_PACMAN));
	}

	@Override
	public void start() {
		sceneController = new MsPacMan_IntermissionScene2_Controller(gameController, rendering, sounds);
		sceneController.start();
	}

	@Override
	public void end() {
	}

	@Override
	public void update() {
		super.update();
		sceneController.update();
		rendering.drawFlap(gc, sceneController.flap);
		rendering.drawPlayer(gc, sceneController.msPacMan);
		rendering.drawSpouse(gc, sceneController.pacMan);
	}
}