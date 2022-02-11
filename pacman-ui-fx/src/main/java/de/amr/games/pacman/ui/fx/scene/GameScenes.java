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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.common.GameVariant;
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

/**
 * The game scenes.
 * 
 * @author Armin Reichert
 */
public class GameScenes {

	private final AbstractGameScene scenes[][][] = new AbstractGameScene[2][5][2];

	public GameScenes(GameController gameController) {
		//@formatter:off
		scenes[0][0][0] = 
		scenes[0][0][1] = new MsPacMan_IntroScene(gameController);
		scenes[0][1][0] = 
		scenes[0][1][1] = new MsPacMan_IntermissionScene1(gameController);
		scenes[0][2][0] = 
		scenes[0][2][1] = new MsPacMan_IntermissionScene2(gameController);
		scenes[0][3][0] = 
		scenes[0][3][1] = new MsPacMan_IntermissionScene3(gameController);
		scenes[0][4][0] = new PlayScene2D(gameController);
		scenes[0][4][1] = new PlayScene3D(gameController, GianmarcosModel3D.get());
		
		scenes[1][0][0] = 
		scenes[1][0][1] = new PacMan_IntroScene(gameController);
		scenes[1][1][0] = 
		scenes[1][1][1] = new PacMan_IntermissionScene1(gameController);
		scenes[1][2][0] = 
		scenes[1][2][1] = new PacMan_IntermissionScene2(gameController);
		scenes[1][3][0] = 
		scenes[1][3][1] = new PacMan_IntermissionScene3(gameController);
		scenes[1][4][0] = new PlayScene2D(gameController);
		scenes[1][4][1] = new PlayScene3D(gameController, GianmarcosModel3D.get());
		//@formatter:on
	}

	public AbstractGameScene getScene(GameVariant gameVariant, int sceneIndex, int sceneVariant) {
		return scenes[gameVariant.ordinal()][sceneIndex][sceneVariant];
	}
}