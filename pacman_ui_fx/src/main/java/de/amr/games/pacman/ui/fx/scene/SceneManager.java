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

	private enum SceneDimension {
		TWO_D, THREE_D;
	}

	private record SceneVariants(GameScene scene2D, GameScene scene3D) {
	}

	//@formatter:off
	private static final SceneVariants[] SCENE_PAIRS_PACMAN = {
			new SceneVariants(new BootScene(), null),
			new SceneVariants(new PacManIntroScene(), null),
			new SceneVariants(new PacManCreditScene(), null),
			new SceneVariants(new PacManCutscene1(), null),
			new SceneVariants(new PacManCutscene2(), null),
			new SceneVariants(new PacManCutscene3(), null),
			new SceneVariants(new PlayScene2D(), new PlayScene3D()),
	};

	private static final SceneVariants[] SCENE_PAIRS_MS_PACMAN = { 
			new SceneVariants(new BootScene(), null),
			new SceneVariants(new MsPacManIntroScene(), null),
			new SceneVariants(new MsPacManCreditScene(), null),
			new SceneVariants(new MsPacManIntermissionScene1(), null),
			new SceneVariants(new MsPacManIntermissionScene2(), null),
			new SceneVariants(new MsPacManIntermissionScene3(), null),
			new SceneVariants(new PlayScene2D(), new PlayScene3D()),
	};
	//@formatter:on

	public static void setMainScene(Scene mainScene) {
		for (var scenes : SCENE_PAIRS_MS_PACMAN) {
			setMainScene(mainScene, scenes.scene2D);
			setMainScene(mainScene, scenes.scene3D);
		}
		for (var scenes : SCENE_PAIRS_PACMAN) {
			setMainScene(mainScene, scenes.scene2D);
			setMainScene(mainScene, scenes.scene3D);
		}
	}

	private static void setMainScene(Scene mainScene, GameScene gameScene) {
		if (gameScene != null) {
			gameScene.setResizeBehavior(mainScene.widthProperty(), mainScene.heightProperty());
		}
	}

	public static boolean hasDifferentScenesFor2DAnd3D(GameController gameController) {
		var scene2D = findGameScene(gameController, SceneDimension.TWO_D);
		var scene3D = findGameScene(gameController, SceneDimension.THREE_D);
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
		var dimension = Env.use3DScenePy.get() ? SceneDimension.THREE_D : SceneDimension.TWO_D;
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
		var gameVariant = gameController.game().variant;
		var r2D = switch (gameVariant) {
		case MS_PACMAN -> ArcadeRendererMsPacManGame.get();
		case PACMAN -> ArcadeRendererPacManGame.get();
		};
		var model3D = Model3D.get(); // no game variant-specific 3D models yet
		scene.setSceneContext(new SceneContext(gameController, r2D, model3D));
		LOGGER.info("Scene context updated for '%s'.", scene);
		var sounds = Env.SOUND_DISABLED ? GameSounds.NO_SOUNDS : switch (gameVariant) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		};
		gameController.setSounds(sounds);
	}

	private static Optional<GameScene> findGameScene(GameController gameController, SceneDimension dimension) {
		var game = gameController.game();

		var scenes = switch (game.variant) {
		case MS_PACMAN -> SCENE_PAIRS_MS_PACMAN;
		case PACMAN -> SCENE_PAIRS_PACMAN;
		};

		var variants = scenes[switch (gameController.state()) {
		case BOOT -> 0;
		case INTRO -> 1;
		case CREDIT -> 2;
		case INTERMISSION -> 2 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 2 + game.intermissionTestNumber;
		default -> 6;
		}];

		var scene = switch (dimension) {
		case TWO_D -> variants.scene2D;
		case THREE_D -> variants.scene3D;
		};
		if (scene == null) {
			scene = variants.scene2D; // default, should always exist
		}

		return Optional.ofNullable(scene);
	}
}