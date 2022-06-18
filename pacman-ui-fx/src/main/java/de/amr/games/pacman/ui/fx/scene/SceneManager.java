/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_CreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_CreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_Cutscene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_Cutscene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_Cutscene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;

/**
 * @author Armin Reichert
 */
public class SceneManager {

	public static final int SCENE_2D = 0, SCENE_3D = 1;

	private static final GameScene[][] SCENES_PACMAN = {
			//@formatter:off
			{ new PacMan_IntroScene(),         null },
			{ new PacMan_CreditScene(),        null },
			{ new PacMan_Cutscene1(),          null },
			{ new PacMan_Cutscene2(),          null },
			{ new PacMan_Cutscene3(),          null },
			{ new PlayScene2D(),               new PlayScene3D() },
			//@formatter:on
	};

	private static final GameScene[][] SCENES_MS_PACMAN = {
			//@formatter:off
			{ new MsPacMan_IntroScene(),         null },
			{ new MsPacMan_CreditScene(),        null },
			{ new MsPacMan_IntermissionScene1(), null },
			{ new MsPacMan_IntermissionScene2(), null },
			{ new MsPacMan_IntermissionScene3(), null },
			{ new PlayScene2D(),                 new PlayScene3D() },
			//@formatter:on
	};

	public static Stream<GameScene> allGameScenes() {
		return Stream.concat(Stream.of(SCENES_MS_PACMAN), Stream.of(SCENES_PACMAN)).flatMap(Stream::of)
				.filter(Objects::nonNull);
	}

	/**
	 * Returns the game scene that fits the current game state.
	 *
	 * @param game      the game model (Pac-Man or Ms. Pac-Man)
	 * @param gameState the current game state
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public static GameScene findGameScene(GameModel game, GameState gameState, int dimension) {
		var scenes = switch (game.variant) {
		case MS_PACMAN -> SCENES_MS_PACMAN;
		case PACMAN -> SCENES_PACMAN;
		};
		var sceneIndex = switch (gameState) {
		case INTRO -> 0;
		case CREDIT -> 1;
		case INTERMISSION -> 1 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 1 + game.intermissionTestNumber;
		default -> 5;
		};
		var gameScene = scenes[sceneIndex][dimension];
		return gameScene != null ? gameScene : scenes[sceneIndex][SCENE_2D]; // use 2D as default
	}

	public static boolean sceneExistsInBothDimensions(GameModel game, GameState state) {
		return findGameScene(game, state, SceneManager.SCENE_2D) != findGameScene(game, state, SceneManager.SCENE_3D);
	}
}