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
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererPacManGame;
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
import de.amr.games.pacman.ui.fx.sound.GameSounds;

/**
 * @author Armin Reichert
 */
public class GameSceneManager {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final int PLAY_SCENE_INDEX = 3;

	//@formatter:off
	private static final GameSceneVariants[] SCENES_PACMAN = {
			new GameSceneVariants(new BootScene(), null),
			new GameSceneVariants(new PacManIntroScene(), null),
			new GameSceneVariants(new PacManCreditScene(), null),
			new GameSceneVariants(new PlayScene2D(), new PlayScene3D()),
			new GameSceneVariants(new PacManCutscene1(), null),
			new GameSceneVariants(new PacManCutscene2(), null),
			new GameSceneVariants(new PacManCutscene3(), null),
	};

	private static final GameSceneVariants[] SCENES_MS_PACMAN = { 
			new GameSceneVariants(new BootScene(), null),
			new GameSceneVariants(new MsPacManIntroScene(), null),
			new GameSceneVariants(new MsPacManCreditScene(), null),
			new GameSceneVariants(new PlayScene2D(), new PlayScene3D()),
			new GameSceneVariants(new MsPacManIntermissionScene1(), null),
			new GameSceneVariants(new MsPacManIntermissionScene2(), null),
			new GameSceneVariants(new MsPacManIntermissionScene3(), null),
	};
	//@formatter:on

	private static GameSceneVariants[] scenes(GameVariant gameVariant) {
		return switch (gameVariant) {
		case MS_PACMAN -> SCENES_MS_PACMAN;
		case PACMAN -> SCENES_PACMAN;
		};
	}

	public boolean hasDifferentScenesFor2DAnd3D(GameController gameController) {
		var scene2D = findGameScene(gameController, false);
		var scene3D = findGameScene(gameController, true);
		return scene2D.isPresent() && scene3D.isPresent() && !scene2D.equals(scene3D);
	}

	/**
	 * Finds the game scene that applies to the current game state. If a new scene is selected, the old scene's
	 * {@link GameScene#end()} method is called, the new scene's context is updated and its {@link GameScene#init()}
	 * method is called.
	 * 
	 * @param gameController   the game controller
	 * @param currentGameScene current game scene
	 * @param reload           if {@code true} the scene is reloaded (end + update context + init) even if no scene change
	 *                         would be required for the current game state
	 * 
	 * @return the selected game scene
	 */
	public GameScene selectGameScene(GameController gameController, GameScene currentGameScene, boolean reload) {
		var nextGameScene = findGameScene(gameController, Env.use3DScenePy.get()).orElse(null);
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (nextGameScene == currentGameScene && !reload) {
			return currentGameScene;
		}
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		updateSceneContext(gameController, nextGameScene);
		nextGameScene.init();
		return nextGameScene;
	}

	public boolean isPlayScene(GameScene gameScene) {
		return gameScene == SCENES_MS_PACMAN[PLAY_SCENE_INDEX].scene2D()
				|| gameScene == SCENES_MS_PACMAN[PLAY_SCENE_INDEX].scene3D()
				|| gameScene == SCENES_PACMAN[PLAY_SCENE_INDEX].scene2D()
				|| gameScene == SCENES_PACMAN[PLAY_SCENE_INDEX].scene3D();
	}

	private void updateSceneContext(GameController gameController, GameScene scene) {
		var gameVariant = gameController.game().variant;
		var r2D = switch (gameVariant) {
		case MS_PACMAN -> new RendererMsPacManGame();
		case PACMAN -> new RendererPacManGame();
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

	private Optional<GameScene> findGameScene(GameController gameController, boolean threeD) {
		var game = gameController.game();
		int index = switch (gameController.state()) {
		case BOOT -> 0;
		case INTRO -> 1;
		case CREDIT -> 2;
		case INTERMISSION -> PLAY_SCENE_INDEX + game.intermissionNumber(game.level.number());
		case INTERMISSION_TEST -> PLAY_SCENE_INDEX + game.intermissionTestNumber;
		default -> PLAY_SCENE_INDEX; // play scene
		};
		var variants = scenes(game.variant)[index];
		var scene = threeD && variants.scene3D() != null ? variants.scene3D() : variants.scene2D();
		return Optional.of(scene);
	}
}