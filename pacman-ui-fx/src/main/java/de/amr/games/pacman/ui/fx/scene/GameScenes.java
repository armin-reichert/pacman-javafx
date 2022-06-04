/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_CreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_CreditScene;
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

	public static final int NUM_SCENES = 6;
	public static final int SCENE_2D = 0, SCENE_3D = 1;

	private final GameScene scenes[][][] = new GameScene[GameVariant.numValues()][NUM_SCENES][2];

	/**
	 * Creates all game scenes.
	 * 
	 * @param parent         the parent scene (main scene)
	 * @param gameController the game controller
	 * @param model3D        the used 3D model for the actors
	 */
	public GameScenes(GameController gameController) {
		//@formatter:off
		scenes[0][0][SCENE_2D] = new MsPacMan_IntroScene(gameController);
		scenes[0][0][SCENE_3D] = null;
		scenes[0][1][SCENE_2D] = new MsPacMan_CreditScene(gameController);
		scenes[0][1][SCENE_3D] = null;
		scenes[0][2][SCENE_2D] = new MsPacMan_IntermissionScene1(gameController);
		scenes[0][2][SCENE_3D] = null;
		scenes[0][3][SCENE_2D] = new MsPacMan_IntermissionScene2(gameController);
		scenes[0][3][SCENE_3D] = null;
		scenes[0][4][SCENE_2D] = new MsPacMan_IntermissionScene3(gameController);
		scenes[0][4][SCENE_3D] = null;
		scenes[0][5][SCENE_2D] = new PlayScene2D(gameController);
		scenes[0][5][SCENE_3D] = new PlayScene3D(gameController);
		
		scenes[1][0][SCENE_2D] = new PacMan_IntroScene(gameController);
		scenes[1][0][SCENE_3D] = null;
		scenes[1][1][SCENE_2D] = new PacMan_CreditScene(gameController);
		scenes[1][1][SCENE_3D] = null;
		scenes[1][2][SCENE_2D] = new PacMan_IntermissionScene1(gameController);
		scenes[1][2][SCENE_3D] = null;
		scenes[1][3][SCENE_2D] = new PacMan_IntermissionScene2(gameController);
		scenes[1][3][SCENE_3D] = null;
		scenes[1][4][SCENE_2D] = new PacMan_IntermissionScene3(gameController);
		scenes[1][4][SCENE_3D] = null;
		scenes[1][5][SCENE_2D] = new PlayScene2D(gameController);
		scenes[1][5][SCENE_3D] = new PlayScene3D(gameController);
		//@formatter:on
	}

	public void defineResizingBehavior(Scene parent) {
		for (int variant = 0; variant < GameVariant.numValues(); ++variant) {
			for (int scene = 0; scene < NUM_SCENES; ++scene) {
				// 2D scenes adapt to parent scene height keeping aspect ratio
				GameScene scene2D = scenes[variant][scene][SCENE_2D];
				parent.heightProperty().addListener(($height, oldHeight, newHeight) -> scene2D.resize(newHeight.doubleValue()));
				// 3D scenes adapt to parent scene size
				GameScene scene3D = scenes[variant][scene][SCENE_3D];
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
	 * @param gameController the game controller
	 * @param dimension      {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public GameScene getScene(GameController gameController, int dimension) {
		var game = gameController.game();
		var variantIndex = game.variant.ordinal();
		var gameState = gameController.state();
		int sceneIndex = switch (gameState) {
		case INTRO -> 0;
		case CREDIT -> 1;
		case INTERMISSION -> 1 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 1 + game.intermissionTestNumber;
		default -> 5;
		};
		if (scenes[variantIndex][sceneIndex][dimension] == null) {
			// no 3D version exists, use 2D version
			return scenes[variantIndex][sceneIndex][SCENE_2D];
		}
		return scenes[variantIndex][sceneIndex][dimension];
	}
}