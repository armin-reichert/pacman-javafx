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
import de.amr.games.pacman.ui.fx.sound.GameSounds;

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

	private final SceneContext context;

	public SceneManager(GameController gameController) {
		context = new SceneContext(gameController);
	}

	public SceneContext getContext() {
		return context;
	}

	public Stream<GameScene> allGameScenes() {
		return Stream.concat(Stream.of(SCENES_MS_PACMAN), Stream.of(SCENES_PACMAN)).flatMap(Stream::of)
				.filter(Objects::nonNull);
	}

	public void updateSceneContext(GameScene scene) {
		var game = context.gameController.game();
		var r2D = switch (game.variant) {
		case MS_PACMAN -> SpritesheetMsPacMan.get();
		case PACMAN -> SpritesheetPacMan.get();
		};
		var sounds = GameSounds.SOUND_DISABLED ? GameSounds.NO_SOUNDS : switch (game.variant) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		};
		var model3D = Model3D.get(); // no game variant-specific 3D models yet
		context.r2D = r2D;
		context.model3D = model3D;
		context.gameController.setSounds(sounds);
		scene.setSceneContext(context);
		logger.info("Scene '%s' initialized. Game variant: %s, Rendering2D: %s", scene, game.variant, r2D);
	}

	/**
	 * Returns the game scene that fits the current game state.
	 *
	 * @param dimension {@link GameScenes#SCENE_2D} or {@link GameScenes#SCENE_3D}
	 * @return the game scene that fits the current game state
	 */
	public Optional<GameScene> findGameScene(int dimension) {
		var game = context.gameController.game();
		var state = context.gameController.state();
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
}