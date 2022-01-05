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
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.entity.GianmarcosPacManModel3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3DWithAnimations;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Scenes of the Ms. Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class ScenesMsPacMan {

	public static final AbstractGameScene SCENES[][] = new AbstractGameScene[5][2];
	public static final Rendering2D_MsPacMan RENDERING = new Rendering2D_MsPacMan();
	public static final SoundManager SOUNDS = new SoundManager(PacManGameSounds::msPacManSoundURL);
	public static final PacManModel3D MODEL_3D = new GianmarcosPacManModel3D();

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
		SCENES[4][1] = new PlayScene3DWithAnimations(MODEL_3D, SOUNDS);
		//@formatter:on
	}
}