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
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.BootScene;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
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
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;

/**
 * @author Armin Reichert
 */
public class GameSceneManager {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final int BOOT_SCENE_INDEX = 0;
	private static final int INTRO_SCENE_INDEX = 1;
	private static final int CREDIT_SCENE_INDEX = 2;
	private static final int PLAY_SCENE_INDEX = 3;

	//@formatter:off
	private static final SceneVariants[] SCENES_PACMAN = {
			new SceneVariants(createScene2D(BootScene.class), null),
			new SceneVariants(createScene2D(PacManIntroScene.class), null),
			new SceneVariants(createScene2D(PacManCreditScene.class), null),
			new SceneVariants(createScene2D(PlayScene2D.class), createPlayScene3D()),
			new SceneVariants(createScene2D(PacManCutscene1.class), null),
			new SceneVariants(createScene2D(PacManCutscene2.class), null),
			new SceneVariants(createScene2D(PacManCutscene3.class), null),
	};

	private static final SceneVariants[] SCENES_MS_PACMAN = { 
			new SceneVariants(createScene2D(BootScene.class), null),
			new SceneVariants(createScene2D(MsPacManIntroScene.class), null),
			new SceneVariants(createScene2D(MsPacManCreditScene.class), null),
			new SceneVariants(createScene2D(PlayScene2D.class), createPlayScene3D()),
			new SceneVariants(createScene2D(MsPacManIntermissionScene1.class), null),
			new SceneVariants(createScene2D(MsPacManIntermissionScene2.class), null),
			new SceneVariants(createScene2D(MsPacManIntermissionScene3.class), null),
	};
	//@formatter:on

	private static GameScene2D createScene2D(Class<? extends GameScene2D> clazz) {
		GameScene2D scene2D;
		try {
			scene2D = clazz.getDeclaredConstructor().newInstance();
			LOGGER.info("2D game scene created, class='%s'", clazz.getName());
			scene2D.overlayPaneVisiblePy.bind(Env.showDebugInfoPy);
			return scene2D;
		} catch (Exception e) {
			LOGGER.error("Could not create 2D game scene of class '%s'", clazz.getName());
			throw new IllegalArgumentException(e);
		}
	}

	private static PlayScene3D createPlayScene3D() {
		var playScene3D = new PlayScene3D();
		playScene3D.drawModePy.bind(Env.drawModePy);
		playScene3D.floorColorPy.bind(Env.floorColorPy);
		playScene3D.floorTexturePy.bind(Env.floorTexturePy);
		playScene3D.mazeResolutionPy.bind(Env.mazeResolutionPy);
		playScene3D.mazeWallHeightPy.bind(Env.mazeWallHeightPy);
		playScene3D.mazeWallThicknessPy.bind(Env.mazeWallThicknessPy);
		playScene3D.pac3DLightedPy.bind(Env.pac3DLightedPy);
		playScene3D.perspectivePy.bind(Env.perspectivePy);
		playScene3D.squirtingEffectPy.bind(Env.squirtingEffectPy);
		playScene3D.coordSystem().visibleProperty().bind(Env.axesVisiblePy);
		playScene3D.ambientLight().colorProperty().bind(Env.lightColorPy);
		LOGGER.info("3D game scene created, class='%s'", playScene3D.getClass().getName());
		return playScene3D;
	}

	/**
	 * Selects the game scene that applies to the current game state. If a new scene is selected, the old scene's
	 * {@link GameScene#end()} method is called, the new scene's context is updated and its {@link GameScene#init()}
	 * method is called.
	 * 
	 * @param gameController   the game controller
	 * @param dim              dimension (2 or 3)
	 * @param currentGameScene current game scene
	 * @param reload           if {@code true} the scene is reloaded (end + update context + init) even if no scene change
	 *                         would be required for the current game state
	 * 
	 * @return the selected game scene
	 */
	public GameScene selectGameScene(GameController gameController, int dim, GameScene currentGameScene, boolean reload) {
		if (dim != 2 && dim != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dim));
		}
		var variants = getSceneVariantsMatchingGameState(gameController.game(), gameController.state());
		var nextGameScene = (dim == 3 && variants.scene3D() != null) ? variants.scene3D() : variants.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (nextGameScene == currentGameScene && !reload) {
			return currentGameScene;
		}
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		setSceneContext(gameController, nextGameScene);
		nextGameScene.init();
		return nextGameScene;
	}

	public boolean isPlayScene(GameScene gameScene) {
		return gameScene == SCENES_MS_PACMAN[PLAY_SCENE_INDEX].scene2D()
				|| gameScene == SCENES_MS_PACMAN[PLAY_SCENE_INDEX].scene3D()
				|| gameScene == SCENES_PACMAN[PLAY_SCENE_INDEX].scene2D()
				|| gameScene == SCENES_PACMAN[PLAY_SCENE_INDEX].scene3D();
	}

	/**
	 * @param gameController the game controller
	 * @param dim            scene variant dimension (2 or 3)
	 * @return (optional) game scene matching current game state and specified dimension
	 */
	public Optional<GameScene> findGameScene(GameController gameController, int dim) {
		if (dim != 2 && dim != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dim));
		}
		var variants = getSceneVariantsMatchingGameState(gameController.game(), gameController.state());
		return Optional.ofNullable(dim == 3 ? variants.scene3D() : variants.scene2D());
	}

	private SceneVariants getSceneVariantsMatchingGameState(GameModel game, GameState state) {
		int index = switch (state) {
		case BOOT -> BOOT_SCENE_INDEX;
		case INTRO -> INTRO_SCENE_INDEX;
		case GAME_OVER, GHOST_DYING, HUNTING, LEVEL_COMPLETE, LEVEL_STARTING, PACMAN_DYING, READY -> PLAY_SCENE_INDEX;
		case CREDIT -> CREDIT_SCENE_INDEX;
		case INTERMISSION -> PLAY_SCENE_INDEX + game.intermissionNumber(game.level.number());
		case INTERMISSION_TEST -> PLAY_SCENE_INDEX + game.intermissionTestNumber;
		};
		return switch (game.variant()) {
		case MS_PACMAN -> SCENES_MS_PACMAN[index];
		case PACMAN -> SCENES_PACMAN[index];
		};
	}

	private void setSceneContext(GameController gameController, GameScene scene) {
		var gameVariant = gameController.game().variant();
		var r2D = switch (gameVariant) {
		case MS_PACMAN -> new RendererMsPacManGame();
		case PACMAN -> new RendererPacManGame();
		};
		scene.setContext(new SceneContext(gameController, r2D));
		LOGGER.info("Scene context set for '%s'.", scene);
	}
}