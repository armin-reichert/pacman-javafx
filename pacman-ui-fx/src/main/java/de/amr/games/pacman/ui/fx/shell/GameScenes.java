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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
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
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import javafx.scene.canvas.Canvas;

/**
 * The game scenes.
 * 
 * @author Armin Reichert
 */
public class GameScenes {

	private final GameScene scenes_PacMan[][] = new GameScene[5][2];
	private final GameScene scenes_MsPacMan[][] = new GameScene[5][2];

	public GameScenes(GameController gameController, V2i sceneSize, Canvas canvas) {
		//@formatter:off
		scenes_PacMan  [0][0] = 
		scenes_PacMan  [0][1] = new PacMan_IntroScene(gameController, sceneSize, canvas,  Rendering2D_PacMan.get());
		scenes_PacMan  [1][0] = 
		scenes_PacMan  [1][1] = new PacMan_IntermissionScene1(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [2][0] = 
		scenes_PacMan  [2][1] = new PacMan_IntermissionScene2(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [3][0] = 
		scenes_PacMan  [3][1] = new PacMan_IntermissionScene3(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [4][0] = new PlayScene2D(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [4][1] = new PlayScene3D(gameController, GianmarcosModel3D.get());
		
		scenes_MsPacMan[0][0] = 
		scenes_MsPacMan[0][1] = new MsPacMan_IntroScene(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[1][0] = 
		scenes_MsPacMan[1][1] = new MsPacMan_IntermissionScene1(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[2][0] = 
		scenes_MsPacMan[2][1] = new MsPacMan_IntermissionScene2(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[3][0] = 
		scenes_MsPacMan[3][1] = new MsPacMan_IntermissionScene3(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[4][0] = new PlayScene2D(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[4][1] = new PlayScene3D(gameController, GianmarcosModel3D.get());
		//@formatter:on
	}

	public GameScene getScene(GameVariant gameVariant, int sceneIndex, int sceneVariant) {
		return gameVariant == GameVariant.MS_PACMAN ? scenes_MsPacMan[sceneIndex][sceneVariant]
				: scenes_PacMan[sceneIndex][sceneVariant];
	}
}
