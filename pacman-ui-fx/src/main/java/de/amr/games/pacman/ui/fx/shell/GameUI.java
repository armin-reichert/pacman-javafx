/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.shell;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.World.TS;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class GameUI extends DefaultGameEventHandler {

	private static final int TILES_X = 28;
	private static final int TILES_Y = 36;
	private static final double ASPECT_RATIO = (double) TILES_X / TILES_Y;

	private final Background bg_beach = U.imageBackground("/common/beach.jpg");
	private final Background bg_black = U.colorBackground(Color.BLACK);
	private final Background bg_blue = U.colorBackground(Color.CORNFLOWERBLUE);

	private final PacManModel3D model3D = GianmarcosModel3D.get();

	private final SoundManager sounds_PacMan = new SoundManager();
	{
		//@formatter:off
		sounds_PacMan.put(GameSounds.CREDIT,          "/pacman/sound/credit.mp3");
		sounds_PacMan.put(GameSounds.EXTRA_LIFE,      "/pacman/sound/extend.mp3");
		sounds_PacMan.put(GameSounds.GAME_READY,      "/pacman/sound/game_start.mp3");
		sounds_PacMan.put(GameSounds.BONUS_EATEN,     "/pacman/sound/eat_fruit.mp3");
		sounds_PacMan.put(GameSounds.PACMAN_MUNCH,    "/pacman/sound/munch_1.wav");
		sounds_PacMan.put(GameSounds.PACMAN_DEATH,    "/pacman/sound/pacman_death.wav");
		sounds_PacMan.put(GameSounds.PACMAN_POWER,    "/pacman/sound/power_pellet.mp3");
		sounds_PacMan.put(GameSounds.GHOST_EATEN,     "/pacman/sound/eat_ghost.mp3");
		sounds_PacMan.put(GameSounds.GHOST_RETURNING, "/pacman/sound/retreating.mp3");
		sounds_PacMan.put(GameSounds.SIREN_1,         "/pacman/sound/siren_1.mp3");
		sounds_PacMan.put(GameSounds.SIREN_2,         "/pacman/sound/siren_2.mp3");
		sounds_PacMan.put(GameSounds.SIREN_3,         "/pacman/sound/siren_3.mp3");
		sounds_PacMan.put(GameSounds.SIREN_4,         "/pacman/sound/siren_4.mp3");
		sounds_PacMan.put(GameSounds.INTERMISSION_1,  "/pacman/sound/intermission.mp3");
		sounds_PacMan.put(GameSounds.INTERMISSION_2,  "/pacman/sound/intermission.mp3");
		sounds_PacMan.put(GameSounds.INTERMISSION_3,  "/pacman/sound/intermission.mp3");
		//@formatter:on
	}

	private final SoundManager sounds_MsPacMan = new SoundManager();
	{
		//@formatter:off
		sounds_MsPacMan.put(GameSounds.CREDIT,          "/mspacman/sound/Coin Credit.mp3");
		sounds_MsPacMan.put(GameSounds.EXTRA_LIFE,      "/mspacman/sound/Extra Life.mp3");
		sounds_MsPacMan.put(GameSounds.GAME_READY,      "/mspacman/sound/Start.mp3");
		sounds_MsPacMan.put(GameSounds.BONUS_EATEN,     "/mspacman/sound/Fruit.mp3");
		sounds_MsPacMan.put(GameSounds.PACMAN_MUNCH,    "/mspacman/sound/Ms. Pac Man Pill.mp3");
		sounds_MsPacMan.put(GameSounds.PACMAN_DEATH,    "/mspacman/sound/Died.mp3");
		sounds_MsPacMan.put(GameSounds.PACMAN_POWER,    "/mspacman/sound/Scared Ghost.mp3");
		sounds_MsPacMan.put(GameSounds.GHOST_EATEN,     "/mspacman/sound/Ghost.mp3");
		sounds_MsPacMan.put(GameSounds.GHOST_RETURNING, "/mspacman/sound/Ghost Eyes.mp3");
		sounds_MsPacMan.put(GameSounds.SIREN_1,         "/mspacman/sound/Ghost Noise 1.mp3");
		sounds_MsPacMan.put(GameSounds.SIREN_2,         "/mspacman/sound/Ghost Noise 2.mp3");
		sounds_MsPacMan.put(GameSounds.SIREN_3,         "/mspacman/sound/Ghost Noise 3.mp3");
		sounds_MsPacMan.put(GameSounds.SIREN_4,         "/mspacman/sound/Ghost Noise 4.mp3");
		sounds_MsPacMan.put(GameSounds.INTERMISSION_1,  "/mspacman/sound/They Meet Act 1.mp3");
		sounds_MsPacMan.put(GameSounds.INTERMISSION_2,  "/mspacman/sound/The Chase Act 2.mp3");
		sounds_MsPacMan.put(GameSounds.INTERMISSION_3,  "/mspacman/sound/Junior Act 3.mp3");
		//@formatter:on
	}

	private final GameScene scenes_PacMan[][] = new GameScene[5][2];
	private final GameScene scenes_MsPacMan[][] = new GameScene[5][2];

	final GameController gameController;
	final Canvas canvas; // common canvas of all 2D scenes
	final Scene mainScene;
	final Stage stage;
	final Group gameSceneRoot;
	final StackPane mainSceneRoot;

	GameScene currentScene;

	public GameUI(Stage stage, GameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		gameSceneRoot = new Group();
		StackPane.setAlignment(HUD.get(), Pos.TOP_LEFT);

		canvas = new Canvas();

		V2i sceneSize = new V2i(TILES_X, TILES_Y).scaled(TS);
		//@formatter:off
		scenes_PacMan  [0][0] = 
		scenes_PacMan  [0][1] = new PacMan_IntroScene(gameController, sceneSize, canvas,  Rendering2D_PacMan.get());
		scenes_PacMan  [1][0] = 
		scenes_PacMan  [1][1] = new PacMan_IntermissionScene1(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [2][0] = 
		scenes_PacMan  [2][1] = new PacMan_IntermissionScene2(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [3][0] = 
		scenes_PacMan  [3][1] = new PacMan_IntermissionScene3(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [4][0] = new PlayScene2D(gameController, sceneSize, canvas, Rendering2D_PacMan.get());
		scenes_PacMan  [4][1] = new PlayScene3D(gameController, model3D);
		
		scenes_MsPacMan[0][0] = 
		scenes_MsPacMan[0][1] = new MsPacMan_IntroScene(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[1][0] = 
		scenes_MsPacMan[1][1] = new MsPacMan_IntermissionScene1(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[2][0] = 
		scenes_MsPacMan[2][1] = new MsPacMan_IntermissionScene2(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[3][0] = 
		scenes_MsPacMan[3][1] = new MsPacMan_IntermissionScene3(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[4][0] = new PlayScene2D(gameController, sceneSize, canvas, Rendering2D_MsPacMan.get());
		scenes_MsPacMan[4][1] = new PlayScene3D(gameController, model3D);
		//@formatter:on

		mainSceneRoot = new StackPane(gameSceneRoot, FlashMessageView.get(), HUD.get());
		mainScene = new Scene(mainSceneRoot, ASPECT_RATIO * height, height);

		mainScene.heightProperty().addListener($1 -> adaptCanvasSize(mainScene.getHeight()));
		Env.$drawMode3D.addListener($1 -> updateBackground(currentScene));
		Env.gameLoop.$fps.addListener($1 -> updateStageTitle());

		selectGameScene();
		adaptCanvasSize(mainScene.getHeight());

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> Env.gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

		updateStageTitle();
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.setFullScreen(fullscreen);
		stage.show();
	}

	public void update() {
		FlashMessageView.get().update();
		HUD.get().update(this);
	}

	public void updateGameScene() {
		currentScene.update();
	}

	private GameScene gameSceneForCurrentState(boolean _3D) {
		var game = gameController.game;
		int variant = _3D ? 1 : 0;
		int index = 4; // default = Play Scene
		switch (gameController.currentStateID) {
		case INTRO:
			index = 0;
			break;
		case INTERMISSION:
			index = game.intermissionNumber(game.levelNumber);
			break;
		case INTERMISSION_TEST:
			index = gameController.intermissionTestNumber;
			break;
		default:
			break;
		}
		return gameController.gameVariant == GameVariant.MS_PACMAN ? //
				scenes_MsPacMan[index][variant] : scenes_PacMan[index][variant];
	}

	private void selectGameScene() {
		GameScene nextScene = gameSceneForCurrentState(Env.$3D.get());
		if (currentScene != nextScene) {
			if (currentScene != null) {
				log("Change scene from '%s' to '%s'", currentScene.getClass().getName(), nextScene.getClass().getName());
				currentScene.end();
			} else {
				log("Set scene to '%s'", nextScene.getClass().getName());
			}
			updateSceneContext(nextScene);
			// TODO: when the 2D subscene is cached (as is in the 3D case), strange things happen. Why?
			gameSceneRoot.getChildren().setAll(nextScene.createSubScene(mainScene));
			nextScene.init();
			currentScene = nextScene;
		}
	}

	private void adaptCanvasSize(double height) {
		canvas.setHeight(height);
		canvas.setWidth(canvas.getHeight() * ASPECT_RATIO);
		double scaling = canvas.getHeight() / (TILES_Y * TS);
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
	}

	private void updateSceneContext(GameScene gameScene) {
		updateBackground(gameScene);
		if (gameController.gameVariant == GameVariant.MS_PACMAN) {
			Env.r2D = Rendering2D_MsPacMan.get();
			Env.sounds = sounds_MsPacMan;
		} else {
			Env.r2D = Rendering2D_PacMan.get();
			Env.sounds = sounds_PacMan;
		}
	}

	private void updateStageTitle() {
		String gameName = gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man";
		String title = Env.$paused.get() ? String.format("%s (PAUSED, CTRL+P: resume, P: Step)", gameName)
				: String.format("%s", gameName);
		stage.setTitle(title);
	}

	private void updateBackground(GameScene scene) {
		if (scene.is3D()) {
			mainSceneRoot.setBackground(Env.$drawMode3D.get() == DrawMode.LINE ? bg_black : bg_beach);
		} else {
			mainSceneRoot.setBackground(bg_blue);
		}
	}

	private void toggle3D() {
		Env.$3D.set(!Env.$3D.get());
		if (gameSceneForCurrentState(false) != gameSceneForCurrentState(true)) {
			Env.sounds.stopAll();
			selectGameScene();
		}
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		currentScene.onGameEvent(event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		selectGameScene();
	}

	private void onKeyPressed(KeyEvent e) {
		final GameModel game = gameController.game;
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		switch (e.getCode()) {
		case A: {
			gameController.autoControlled = !gameController.autoControlled;
			String message = Env.message(gameController.autoControlled ? "autopilot_on" : "autopilot_off");
			FlashMessageView.showFlashMessage(1, message);
			break;
		}

		case E:
			if (gameController.gameRunning) {
				gameController.cheatEatAllPellets();
			}
			break;

		case I: {
			game.player.immune = !game.player.immune;
			String message = Env.message(game.player.immune ? "player_immunity_on" : "player_immunity_off");
			FlashMessageView.showFlashMessage(1, message);
			break;
		}

		case L:
			if (gameController.gameRunning) {
				game.player.lives += 3;
				FlashMessageView.showFlashMessage(2, "You have %d lives", game.player.lives);
			}
			break;

		case N:
			if (gameController.gameRunning) {
				FlashMessageView.showFlashMessage(1, Env.CHEAT_TALK.next());
				gameController.changeState(GameState.LEVEL_COMPLETE);
			}
			break;

		case P:
			if (Env.$paused.get()) {
				Env.gameLoop.runSingleStep(true);
			}
			break;

		case Q:
			if (gameController.currentStateID != GameState.INTRO) {
				currentScene.end();
				Env.sounds.stopAll();
				gameController.changeState(GameState.INTRO);
			}
			break;

		case V:
			if (gameController.currentStateID == GameState.INTRO) {
				gameController.selectGameVariant(gameController.gameVariant.succ());
			}
			break;

		case X:
			if (gameController.gameRunning) {
				gameController.cheatKillGhosts();
			}
			break;

		case Z:
			if (gameController.currentStateID == GameState.INTRO) {
				FlashMessageView.showFlashMessage(1, "Intermission Scene Test");
				gameController.startIntermissionTest();
			}
			break;

		case SPACE:
			gameController.requestGame();
			break;

		case F11:
			stage.setFullScreen(true);
			break;

		default:
			break;
		}
	}

	private void onControlKeyPressed(KeyEvent e) {
		switch (e.getCode()) {

		case C:
			if (currentScene.is3D()) {
				Env.nextPerspective();
				String perspective_key = Env.message(Env.$perspective.get().name().toLowerCase());
				String message = Env.message("camera_perspective", perspective_key);
				FlashMessageView.showFlashMessage(1, message);
			}
			break;

		case H:
			if (currentScene.is3D()) {
				Env.changeMazeWallHeight(!e.isShiftDown());
			}
			break;

		case I:
			if (!HUD.get().isVisible()) {
				HUD.get().show();
			} else {
				HUD.get().hide();
			}
			break;

		case L:
			if (currentScene.is3D()) {
				Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			}
			break;

		case P:
			Env.$paused.set(!Env.$paused.get());
			if (Env.$paused.get()) {
				FlashMessageView.showFlashMessage(3, "Game paused");
				log("Game paused.");
			} else {
				FlashMessageView.showFlashMessage(2, "Game resumed");
				log("Game resumed.");
			}
			break;

		case R:
			if (currentScene.is3D()) {
				Env.changeMazeResolution(!e.isShiftDown());
			}
			break;

		case S:
			int currentTargetFrameRate = Env.gameLoop.getTargetFrameRate();
			if (!e.isShiftDown()) {
				Env.gameLoop.setTargetFrameRate(currentTargetFrameRate + 10);
			} else {
				Env.gameLoop.setTargetFrameRate(Math.max(10, currentTargetFrameRate - 10));
			}
			FlashMessageView.showFlashMessage(1, "Target FPS set to %d Hz", Env.gameLoop.getTargetFrameRate());
			break;

		case T:
			Env.$isTimeMeasured.set(!Env.$isTimeMeasured.get());
			break;

		case X:
			if (currentScene.is3D()) {
				Env.$axesVisible.set(!Env.$axesVisible.get());
			}
			break;

		case Y:
			if (!currentScene.is3D()) {
				Env.$tilesVisible.set(!Env.$tilesVisible.get());
			}
			break;

		case DIGIT3: {
			toggle3D();
			String message = Env.$3D.get() ? "Using 3D play scene\nCTRL+C changes perspective" : "Using 2D play scene";
			FlashMessageView.showFlashMessage(2, message);
			break;
		}

		default:
			break;
		}
	}
}