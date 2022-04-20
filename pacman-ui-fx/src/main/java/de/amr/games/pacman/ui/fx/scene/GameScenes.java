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
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import javafx.scene.Scene;

/**
 * The game scenes.
 * 
 * @author Armin Reichert
 */
public class GameScenes {

	public static final int SCENE_2D = 0, SCENE_3D = 1;

	private final GameController gameController;
	private final GameScene scenes[][][] = new GameScene[2][5][2];

	/**
	 * Creates all game scenes.
	 * 
	 * @param parent         the parent scene (main scene)
	 * @param gameController the game controller
	 * @param model3D        the used 3D model for the actors
	 * @param size           logical scene size (number of tiles times tile size)
	 */
	public GameScenes(Scene parent, GameController gameController, V2i size) {
		this.gameController = gameController;
		//@formatter:off
		scenes[0][0][SCENE_2D] = new MsPacMan_IntroScene(gameController, size);
		scenes[0][0][SCENE_3D] = null;
		scenes[0][1][SCENE_2D] = new MsPacMan_IntermissionScene1(gameController, size);
		scenes[0][1][SCENE_3D] = null;
		scenes[0][2][SCENE_2D] = new MsPacMan_IntermissionScene2(gameController, size);
		scenes[0][2][SCENE_3D] = null;
		scenes[0][3][SCENE_2D] = new MsPacMan_IntermissionScene3(gameController, size);
		scenes[0][3][SCENE_3D] = null;
		scenes[0][4][SCENE_2D] = new PlayScene2D(gameController, size);
		scenes[0][4][SCENE_3D] = new PlayScene3D(gameController, size);
		
		scenes[1][0][SCENE_2D] = new PacMan_IntroScene(gameController, size);
		scenes[1][0][SCENE_3D] = null;
		scenes[1][1][SCENE_2D] = new PacMan_IntermissionScene1(gameController, size);
		scenes[1][1][SCENE_3D] = null;
		scenes[1][2][SCENE_2D] = new PacMan_IntermissionScene2(gameController, size);
		scenes[1][2][SCENE_3D] = null;
		scenes[1][3][SCENE_2D] = new PacMan_IntermissionScene3(gameController, size);
		scenes[1][3][SCENE_3D] = null;
		scenes[1][4][SCENE_2D] = new PlayScene2D(gameController, size);
		scenes[1][4][SCENE_3D] = new PlayScene3D(gameController, size);
		//@formatter:on

		// define resize behavior
		for (int variantIndex = 0; variantIndex <= 1; ++variantIndex) {
			for (int sceneIndex = 0; sceneIndex <= 4; ++sceneIndex) {
				// 2D scenes adapt to parent scene height keeping aspect ratio
				GameScene scene2D = scenes[variantIndex][sceneIndex][SCENE_2D];
				parent.heightProperty().addListener(($height, oldHeight, newHeight) -> scene2D.resize(newHeight.doubleValue()));
				// 3D scenes adapt to parent scene size
				GameScene scene3D = scenes[variantIndex][sceneIndex][SCENE_3D];
				if (scene3D != null) {
					scene3D.getFXSubScene().widthProperty().bind(parent.widthProperty());
					scene3D.getFXSubScene().heightProperty().bind(parent.heightProperty());
				}
			}
		}
	}

	/**
	 * Returns the scene that fits the current game state.
	 * 
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public GameScene getScene(int dimension) {
		int sceneIndex = switch (gameController.state) {
		case INTRO -> 0;
		case INTERMISSION -> gameController.game.intermissionNumber(gameController.game.levelNumber);
		case INTERMISSION_TEST -> gameController.intermissionTestNumber;
		default -> 4; // play scene
		};
		int variantIndex = gameController.gameVariant.ordinal();
		if (scenes[variantIndex][sceneIndex][dimension] == null) {
			// no 3D version exists, use 2D version
			return scenes[variantIndex][sceneIndex][SCENE_2D];
		}
		return scenes[variantIndex][sceneIndex][dimension];
	}
}