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

import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Scenes of the Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class ScenesPacMan {

	public static final GameScene SCENES[][] = new GameScene[5][2];
	public static final Rendering2D_PacMan RENDERING = new Rendering2D_PacMan();
	public static final PacManModel3D MODEL_3D = GianmarcosModel3D.get();
	public static final SoundManager SOUNDS = new SoundManager();

	static {
		//@formatter:off
		SOUNDS.put(GameSounds.CREDIT,          "/pacman/sound/credit.mp3");
		SOUNDS.put(GameSounds.EXTRA_LIFE,      "/pacman/sound/extend.mp3");
		SOUNDS.put(GameSounds.GAME_READY,      "/pacman/sound/game_start.mp3");
		SOUNDS.put(GameSounds.BONUS_EATEN,     "/pacman/sound/eat_fruit.mp3");
		SOUNDS.put(GameSounds.PACMAN_MUNCH,    "/pacman/sound/munch_1.wav");
		SOUNDS.put(GameSounds.PACMAN_DEATH,    "/pacman/sound/pacman_death.wav");
		SOUNDS.put(GameSounds.PACMAN_POWER,    "/pacman/sound/power_pellet.mp3");
		SOUNDS.put(GameSounds.GHOST_EATEN,     "/pacman/sound/eat_ghost.mp3");
		SOUNDS.put(GameSounds.GHOST_RETURNING, "/pacman/sound/retreating.mp3");
		SOUNDS.put(GameSounds.SIREN_1,   "/pacman/sound/siren_1.mp3");
		SOUNDS.put(GameSounds.SIREN_2,   "/pacman/sound/siren_2.mp3");
		SOUNDS.put(GameSounds.SIREN_3,   "/pacman/sound/siren_3.mp3");
		SOUNDS.put(GameSounds.SIREN_4,   "/pacman/sound/siren_4.mp3");
		SOUNDS.put(GameSounds.INTERMISSION_1,  "/pacman/sound/intermission.mp3");
		SOUNDS.put(GameSounds.INTERMISSION_2,  "/pacman/sound/intermission.mp3");
		SOUNDS.put(GameSounds.INTERMISSION_3,  "/pacman/sound/intermission.mp3");
	}
	
	public static void createScenes(PacManGameUI_JavaFX ui) {
		//@formatter:off
		SCENES[0][0] = 
		SCENES[0][1] = new PacMan_IntroScene(ui.gameController, ui.canvas);
		SCENES[1][0] = 
		SCENES[1][1] = new PacMan_IntermissionScene1(ui.gameController, ui.canvas);
		SCENES[2][0] = 
		SCENES[2][1] = new PacMan_IntermissionScene2(ui.gameController, ui.canvas);
		SCENES[3][0] = 
		SCENES[3][1] = new PacMan_IntermissionScene3(ui.gameController, ui.canvas);
		SCENES[4][0] = new PlayScene2D(ui.gameController, ui.canvas, RENDERING);
		SCENES[4][1] = new PlayScene3D(ui, MODEL_3D);
		//@formatter:on
	}
}