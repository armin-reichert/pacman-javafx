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
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.Env3D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
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

	private GameSceneManager() {
		// only static
	}

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final int BOOT_SCENE_INDEX = 0;
	private static final int INTRO_SCENE_INDEX = 1;
	private static final int CREDIT_SCENE_INDEX = 2;
	private static final int PLAY_SCENE_INDEX = 3;

	//@formatter:off
	private static final GameSceneVariants[] SCENES_PACMAN = {
		new GameSceneVariants(createScene2D(BootScene.class), null),
		new GameSceneVariants(createScene2D(PacManIntroScene.class), null),
		new GameSceneVariants(createScene2D(PacManCreditScene.class), null),
		new GameSceneVariants(createScene2D(PlayScene2D.class), createPlayScene3D()),
		new GameSceneVariants(createScene2D(PacManCutscene1.class), null),
		new GameSceneVariants(createScene2D(PacManCutscene2.class), null),
		new GameSceneVariants(createScene2D(PacManCutscene3.class), null),
	};

	private static final GameSceneVariants[] SCENES_MS_PACMAN = { 
		new GameSceneVariants(createScene2D(BootScene.class), null),
		new GameSceneVariants(createScene2D(MsPacManIntroScene.class), null),
		new GameSceneVariants(createScene2D(MsPacManCreditScene.class), null),
		new GameSceneVariants(createScene2D(PlayScene2D.class), createPlayScene3D()),
		new GameSceneVariants(createScene2D(MsPacManIntermissionScene1.class), null),
		new GameSceneVariants(createScene2D(MsPacManIntermissionScene2.class), null),
		new GameSceneVariants(createScene2D(MsPacManIntermissionScene3.class), null),
	};
	//@formatter:on

	private static GameScene2D createScene2D(Class<? extends GameScene2D> clazz) {
		try {
			GameScene2D scene2D = clazz.getDeclaredConstructor().newInstance();
			scene2D.overlayPaneVisiblePy.bind(Env.showDebugInfoPy);
			LOG.trace("2D game scene created: '%s'", scene2D.getClass().getName());
			return scene2D;
		} catch (Exception e) {
			LOG.error("Could not create 2D game scene of class '%s'", clazz.getName());
			throw new IllegalArgumentException(e);
		}
	}

	private static PlayScene3D createPlayScene3D() {
		var playScene3D = new PlayScene3D();
		playScene3D.floorColorPy.bind(Env3D.floorColorPy);
		playScene3D.floorTexturePy.bind(Env3D.floorTexturePy);
		playScene3D.mazeWallHeightPy.bind(Env3D.mazeWallHeightPy);
		playScene3D.mazeWallThicknessPy.bind(Env3D.mazeWallThicknessPy);
		playScene3D.perspectivePy.bind(Env3D.perspectivePy);
		playScene3D.squirtingEffectPy.bind(Env3D.squirtingEnabledPy);
		playScene3D.coordSystem().visibleProperty().bind(Env3D.axesVisiblePy);
		playScene3D.ambientLight().colorProperty().bind(Env3D.lightColorPy);
		LOG.trace("3D game scene created: '%s'", playScene3D.getClass().getName());
		return playScene3D;
	}

	public static boolean isPlayScene(GameScene gameScene) {
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
	public static Optional<GameScene> findGameScene(GameController gameController, int dim) {
		if (dim != 2 && dim != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dim));
		}
		var variants = getSceneVariantsMatchingGameState(gameController);
		return Optional.ofNullable(dim == 3 ? variants.scene3D() : variants.scene2D());
	}

	public static GameSceneVariants getSceneVariantsMatchingGameState(GameController gameController) {
		var game = gameController.game();
		var level = game.level();
		var state = gameController.state();

		int index = switch (state) {
		case BOOT -> BOOT_SCENE_INDEX;
		case CREDIT -> CREDIT_SCENE_INDEX;
		case INTRO -> INTRO_SCENE_INDEX;
		case GAME_OVER, GHOST_DYING, HUNTING, LEVEL_COMPLETE, CHANGING_TO_NEXT_LEVEL, PACMAN_DYING, READY -> PLAY_SCENE_INDEX;
		case INTERMISSION -> {
			if (level.isPresent()) {
				yield PLAY_SCENE_INDEX + level.get().params().intermissionNumber();
			} else {
				throw new IllegalStateException();
			}
		}
		case INTERMISSION_TEST -> PLAY_SCENE_INDEX + gameController.intermissionTestNumber;
		default -> throw new IllegalStateException();
		};

		return switch (game.variant()) {
		case MS_PACMAN -> SCENES_MS_PACMAN[index];
		case PACMAN -> SCENES_PACMAN[index];
		};
	}

	public static void setSceneContext(GameController gameController, GameScene scene) {
		var gameVariant = gameController.game().variant();
		var r2D = switch (gameVariant) {
		case MS_PACMAN -> MsPacManGameRenderer.THE_ONE_AND_ONLY;
		case PACMAN -> PacManGameRenderer.THE_ONE_AND_ONLY;
		};
		scene.setContext(new GameSceneContext(gameController, r2D));
	}
}