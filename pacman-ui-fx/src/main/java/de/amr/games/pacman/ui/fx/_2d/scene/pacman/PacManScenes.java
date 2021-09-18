package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.entity.GianmarcosPacManModel3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3DWithAnimations;
import de.amr.games.pacman.ui.fx.shell.GameScene;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManScenes {

	public static final PacManModel3D MODEL_3D = new GianmarcosPacManModel3D();
	public static final Rendering2D_PacMan RENDERING = new Rendering2D_PacMan();
	public static final SoundManager SOUNDS = new SoundManager(PacManGameSounds::pacManSoundURL);
	public static final GameScene SCENES[][] = new GameScene[5][2];

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
		SCENES[4][1] = new PlayScene3DWithAnimations(MODEL_3D, SOUNDS);
		//@formatter:on
	}
}