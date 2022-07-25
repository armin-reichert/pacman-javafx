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

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.ArcadeRendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.ArcadeRendererPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.BootScene;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManCreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManIntroScene;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import javafx.scene.Scene;

/**
 * @author Armin Reichert
 */
public class SceneManager {

	private SceneManager() {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static final int SCENE_2D = 0;
	public static final int SCENE_3D = 1;

	private static final GameScene[][] SCENES_PACMAN = {
		//@formatter:off
		{ new BootScene(),                null },
		{ new PacManIntroScene(),         null },
		{ new PacManCreditScene(),        null },
		{ new PacManCutscene1(),          null },
		{ new PacManCutscene2(),          null },
		{ new PacManCutscene3(),          null },
		{ new PlayScene2D(),              new PlayScene3D() },
		//@formatter:on
	};

	private static final GameScene[][] SCENES_MS_PACMAN = {
		//@formatter:off
		{ new BootScene(),                  null },
		{ new MsPacManIntroScene(),         null },
		{ new MsPacManCreditScene(),        null },
		{ new MsPacManIntermissionScene1(), null },
		{ new MsPacManIntermissionScene2(), null },
		{ new MsPacManIntermissionScene3(), null },
		{ new PlayScene2D(),                new PlayScene3D() },
		//@formatter:on
	};

	public static void setMainScene(Scene mainScene) {
		for (var gameScenes : SCENES_MS_PACMAN) {
			for (var gameScene : gameScenes) {
				setMainScene(mainScene, gameScene);
			}
		}
		for (var gameScenes : SCENES_PACMAN) {
			for (var gameScene : gameScenes) {
				setMainScene(mainScene, gameScene);
			}
		}
	}

	private static void setMainScene(Scene mainScene, GameScene gameScene) {
		if (gameScene != null) {
			gameScene.setResizeBehavior(mainScene.widthProperty(), mainScene.heightProperty());
		}
	}

	public static boolean hasDifferentScenesFor2DAnd3D(GameController gameController) {
		var scene2D = findGameScene(gameController, SCENE_2D);
		var scene3D = findGameScene(gameController, SCENE_3D);
		return scene2D.isPresent() && scene3D.isPresent() && !scene2D.equals(scene3D);
	}

	/**
	 * Finds the game scene that applies to the current game state. If a new scene is selected, the old scene's
	 * {@link GameScene#end()} method is called, the new scene's context is updated and its {@link GameScene#init()}
	 * method is called.
	 * 
	 * @param gameController the game controller
	 * @param forceReload    if {@code true} the scene is reloaded (end + update context + init) even if no scene change
	 *                       would be required for the current game state
	 * 
	 * @return the selected game scene
	 */
	public static GameScene selectGameScene(GameController gameController, GameScene currentGameScene,
			boolean forceReload) {
		int dimension = Env.use3DScenePy.get() ? SceneManager.SCENE_3D : SceneManager.SCENE_2D;
		GameScene nextGameScene = findGameScene(gameController, dimension).orElse(null);
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (nextGameScene == currentGameScene && !forceReload) {
			return currentGameScene;
		}
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		updateSceneContext(gameController, nextGameScene);
		nextGameScene.init();
		return nextGameScene;
	}

	private static void updateSceneContext(GameController gameController, GameScene scene) {
		var context = new SceneContext(gameController);
		context.r2D = switch (context.gameVariant()) {
		case MS_PACMAN -> ArcadeRendererMsPacManGame.get();
		case PACMAN -> ArcadeRendererPacManGame.get();
		};
		context.model3D = Model3D.get(); // no game variant-specific 3D models yet
		var sounds = Env.SOUND_DISABLED ? GameSounds.NO_SOUNDS : switch (context.gameVariant()) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		};
		gameController.setSounds(sounds);
		scene.setSceneContext(context);
		LOGGER.info("Scene context updated for '%s'.", scene);
	}

	private static Optional<GameScene> findGameScene(GameController gameController, int dimension) {
		var game = gameController.game();
		var state = gameController.state();
		var scenes = switch (game.variant) {
		case MS_PACMAN -> SCENES_MS_PACMAN;
		case PACMAN -> SCENES_PACMAN;
		};
		var index = switch (state) {
		case BOOT -> 0;
		case INTRO -> 1;
		case CREDIT -> 2;
		case INTERMISSION -> 2 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 2 + game.intermissionTestNumber;
		default -> 6;
		};
		var gameScene = Optional.ofNullable(scenes[index][dimension]).orElse(scenes[index][SCENE_2D]);
		return Optional.ofNullable(gameScene);
	}
}