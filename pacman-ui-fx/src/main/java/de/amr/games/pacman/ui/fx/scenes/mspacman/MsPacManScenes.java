package de.amr.games.pacman.ui.fx.scenes.mspacman;

import de.amr.games.pacman.ui.fx.model3D.GianmarcosPacManModel3D;
import de.amr.games.pacman.ui.fx.model3D.PacManModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Scenes of the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManScenes {

	public static final PacManModel3D MODEL_3D = new GianmarcosPacManModel3D();
	public static final Rendering2D_MsPacMan RENDERING = new Rendering2D_MsPacMan();
	public static final SoundManager SOUNDS = new SoundManager(PacManGameSounds::msPacManSoundURL);
	public static final GameScene SCENES[][] = new GameScene[5][2];

	static {
		//@formatter:off
		SCENES[0][0] = 
		SCENES[0][1] = new MsPacMan_IntroScene();
		SCENES[1][0] = 
		SCENES[1][1] = new MsPacMan_IntermissionScene1();
		SCENES[2][0] = 
		SCENES[2][1] = new MsPacMan_IntermissionScene2();
		SCENES[3][0] = 
		SCENES[3][1] = new MsPacMan_IntermissionScene3();
		SCENES[4][0] = new PlayScene2D(RENDERING, SOUNDS);
		SCENES[4][1] = new PlayScene3D(MODEL_3D, SOUNDS);
		//@formatter:on
	}
}