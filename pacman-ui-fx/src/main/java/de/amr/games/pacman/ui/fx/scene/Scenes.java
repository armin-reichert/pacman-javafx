/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.entity.Gianmarcos3DModel;
import de.amr.games.pacman.ui.fx._3d.entity.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3DWithAnimations;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Creates and stores all scenes used in the game. Each game variants has its own set of scenes.
 * 
 * @author Armin Reichert
 */
public class Scenes {

	public static final GameScene PACMAN_SCENES[][] = new GameScene[5][2];
	public static final Rendering2D_PacMan PACMAN_RENDERING = new Rendering2D_PacMan();
	public static final PacManModel3D MODEL_3D = new Gianmarcos3DModel("/common/gianmarco/pacman.obj");
	public static final SoundManager PACMAN_SOUNDS = new SoundManager(PacManGameSounds::pacManSoundURL);

	static {
		//@formatter:off
		PACMAN_SCENES[0][0] = 
		PACMAN_SCENES[0][1] = new PacMan_IntroScene();
		PACMAN_SCENES[1][0] = 
		PACMAN_SCENES[1][1] = new PacMan_IntermissionScene1();
		PACMAN_SCENES[2][0] = 
		PACMAN_SCENES[2][1] = new PacMan_IntermissionScene2();
		PACMAN_SCENES[3][0] = 
		PACMAN_SCENES[3][1] = new PacMan_IntermissionScene3();
		PACMAN_SCENES[4][0] = new PlayScene2D(PACMAN_RENDERING, PACMAN_SOUNDS);
		PACMAN_SCENES[4][1] = new PlayScene3DWithAnimations(MODEL_3D, PACMAN_SOUNDS);
		//@formatter:on
	}

	public static final GameScene MS_PACMAN_SCENES[][] = new GameScene[5][2];
	public static final Rendering2D_MsPacMan MS_PACMAN_RENDERING = new Rendering2D_MsPacMan();
	public static final SoundManager MS_PACMAN_SOUNDS = new SoundManager(PacManGameSounds::msPacManSoundURL);

	static {
		//@formatter:off
		MS_PACMAN_SCENES[0][0] = 
		MS_PACMAN_SCENES[0][1] = new MsPacMan_IntroScene();
		MS_PACMAN_SCENES[1][0] = 
		MS_PACMAN_SCENES[1][1] = new MsPacMan_IntermissionScene1();
		MS_PACMAN_SCENES[2][0] = 
		MS_PACMAN_SCENES[2][1] = new MsPacMan_IntermissionScene2();
		MS_PACMAN_SCENES[3][0] = 
		MS_PACMAN_SCENES[3][1] = new MsPacMan_IntermissionScene3();
		MS_PACMAN_SCENES[4][0] = new PlayScene2D(MS_PACMAN_RENDERING, MS_PACMAN_SOUNDS);
		MS_PACMAN_SCENES[4][1] = new PlayScene3DWithAnimations(MODEL_3D, MS_PACMAN_SOUNDS);
		//@formatter:on
	}
}