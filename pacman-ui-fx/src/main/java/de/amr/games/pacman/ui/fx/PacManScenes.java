package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.entity.GianmarcosPacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3DWithAnimations;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManScenes {

	public static final GameScene SCENES[][] = new GameScene[5][2];

	public static final Rendering2D_PacMan RENDERING = new Rendering2D_PacMan();
	public static final SoundManager SOUNDS = new SoundManager(PacManGameSounds::pacManSoundURL);

	static {
		//@formatter:off
		SCENES[0][0] = 
		SCENES[0][1] = new PacMan_IntroScene();
		SCENES[1][0] = 
		SCENES[1][1] = new PacMan_IntermissionScene1();
		SCENES[2][0] = 
		SCENES[2][1] = new PacMan_IntermissionScene2();
		SCENES[3][0] = 
		SCENES[3][1] = new PacMan_IntermissionScene3();
		SCENES[4][0] = new PlayScene2D(RENDERING, SOUNDS);
		SCENES[4][1] = new PlayScene3DWithAnimations(GianmarcosPacManModel3D.get(), SOUNDS);
		//@formatter:on
	}
}