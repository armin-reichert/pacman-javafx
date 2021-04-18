package de.amr.games.pacman.ui.fx.scenes.pacman;

import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

public class PacManScenes {

	public static final SoundManager SOUNDS = new SoundManager(PacManGameSounds::mrPacManSoundURL);

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
		SCENES[4][0] = new PlayScene2D<>(Rendering2D_Impl.RENDERING_PACMAN, SOUNDS);
		SCENES[4][1] = new PlayScene3D(SOUNDS);
		//@formatter:on
	}
}