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
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.SpritesheetMsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.SpritesheetPacMan;
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

	private static final Logger logger = LogManager.getFormatterLogger();

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

	private final GameController gameController;
	private GameScene currentGameScene;

	public SceneManager(GameController gameController) {
		this.gameController = gameController;
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public Stream<GameScene> allGameScenes() {
		return Stream.concat(Stream.of(SCENES_MS_PACMAN), Stream.of(SCENES_PACMAN)).flatMap(Stream::of)
				.filter(Objects::nonNull);
	}

	/**
	 * Finds the game scene that applies to the current game state. If a new scene is selected, the old scene's
	 * {@link GameScene#end()} method is called, the new scene's context is updated and its {@link GameScene#init()}
	 * method is called.
	 * 
	 * @param forced if {@code true} the scene is reloaded (end + update context + init) even if no scene change would be
	 *               required for the current game state
	 * 
	 * @return {@code true} if the current scene changed
	 */
	public boolean selectGameScene(boolean forced) {
		int dimension = Env.use3DScenePy.get() ? SceneManager.SCENE_3D : SceneManager.SCENE_2D;
		GameScene nextGameScene = findGameScene(dimension).orElse(null);
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (nextGameScene == currentGameScene && !forced) {
			return false; // game scene is up-to-date
		}
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		updateSceneContext(nextGameScene);
		nextGameScene.init();
		if (currentGameScene != nextGameScene) {
			logger.info("Current scene changed from %s to %s", currentGameScene, nextGameScene);
			currentGameScene = nextGameScene;
			return true;
		}
		return false;
	}

	public void updateSceneContext(GameScene scene) {
		var context = new SceneContext(gameController);
		context.r2D = switch (context.gameVariant()) {
		case MS_PACMAN -> SpritesheetMsPacMan.get();
		case PACMAN -> SpritesheetPacMan.get();
		};
		context.model3D = Model3D.get(); // no game variant-specific 3D models yet
		var sounds = GameSounds.SOUND_DISABLED ? GameSounds.NO_SOUNDS : switch (context.gameVariant()) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		};
		gameController.setSounds(sounds);
		scene.setSceneContext(context);
		logger.info("Scene context updated for '%s'.", scene);
	}

	/**
	 * Returns the game scene that fits the current game state.
	 *
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	private Optional<GameScene> findGameScene(int dimension) {
		var game = gameController.game();
		var state = gameController.state();
		var scenes = switch (game.variant) {
		case MS_PACMAN -> SCENES_MS_PACMAN;
		case PACMAN -> SCENES_PACMAN;
		};
		var sceneIndex = switch (state) {
		case BOOT -> 0;
		case INTRO -> 1;
		case CREDIT -> 2;
		case INTERMISSION -> 2 + game.intermissionNumber(game.level.number);
		case INTERMISSION_TEST -> 2 + game.intermissionTestNumber;
		default -> 6;
		};
		var gameScene = scenes[sceneIndex][dimension];
		if (gameScene == null) {
			gameScene = scenes[sceneIndex][SCENE_2D]; // use 2D as default:
		}
		return Optional.ofNullable(gameScene);
	}

	public boolean hasSceneInBothDimensions() {
		var scene2D = findGameScene(SCENE_2D);
		var scene3D = findGameScene(SCENE_3D);
		return scene2D.isPresent() && scene3D.isPresent() && !scene2D.equals(scene3D);
	}

	public void defineResizingBehavior(Scene parentScene) {
		allGameScenes()
				.forEach(gameScene -> gameScene.setResizeBehavior(parentScene.widthProperty(), parentScene.heightProperty()));
	}
}