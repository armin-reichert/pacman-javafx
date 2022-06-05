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

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameModel;
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

	public static final int SCENE_2D = 0, SCENE_3D = 1;

	private final GameScene[][] scenes_MrPacMan = {
		//@formatter:off
		{ new PacMan_IntroScene(), null },
		{ new PacMan_CreditScene(), null },
		{ new PacMan_IntermissionScene1(), null },
		{ new PacMan_IntermissionScene2(), null },
		{ new PacMan_IntermissionScene3(), null },
		{ new PlayScene2D(), new PlayScene3D() },
		//@formatter:on
	};

	private final GameScene[][] scenes_MsPacMan = {
		//@formatter:off
		{ new MsPacMan_IntroScene(), null },
		{ new MsPacMan_CreditScene(), null },
		{ new MsPacMan_IntermissionScene1(), null },
		{ new MsPacMan_IntermissionScene2(), null },
		{ new MsPacMan_IntermissionScene3(), null },
		{ new PlayScene2D(), new PlayScene3D() },
		//@formatter:on
	};

	public void defineResizingBehavior(Scene parent) {
		defineResizingBehavior(parent, scenes_MsPacMan);
		defineResizingBehavior(parent, scenes_MrPacMan);
	}

	public void defineResizingBehavior(Scene parent, GameScene[][] scenes) {
		for (int scene = 0; scene < 6; ++scene) {
			// 2D scenes adapt to parent scene height keeping aspect ratio
			GameScene scene2D = scenes[scene][SCENE_2D];
			parent.heightProperty().addListener(($height, oldHeight, newHeight) -> scene2D.resize(newHeight.doubleValue()));
			// 3D scenes adapt to parent scene size
			GameScene scene3D = scenes[scene][SCENE_3D];
			if (scene3D != null) {
				scene3D.getFXSubScene().widthProperty().bind(parent.widthProperty());
				scene3D.getFXSubScene().heightProperty().bind(parent.heightProperty());
			}
		}
	}

	/**
	 * Returns the scene that fits the current game state.
	 *
	 * @param game      the game model (Pac-Man or Ms. Pac-Man)
	 * @param gameState the current game state
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public GameScene getFittingScene(GameModel game, GameState gameState, int dimension) {
		var scenes = switch (game.variant) {
		case MS_PACMAN -> scenes_MsPacMan;
		case PACMAN -> scenes_MrPacMan;
		};
		var sceneIndex = switch (gameState) {
		case INTRO -> 0;
		case CREDIT -> 1;
		case INTERMISSION -> 1 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 1 + game.intermissionTestNumber;
		default -> 5;
		};
		if (scenes[sceneIndex][dimension] == null) { // no 3D version exists, use 2D
			return scenes[sceneIndex][SCENE_2D];
		}
		return scenes[sceneIndex][dimension];
	}
}