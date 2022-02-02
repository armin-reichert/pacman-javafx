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
import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
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
import javafx.beans.binding.Bindings;
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

	public static final int TILES_X = 28;
	public static final int TILES_Y = 36;
	public static final double ASPECT_RATIO = (double) TILES_X / TILES_Y;

	private final Background BG_BEACH = U.imageBackground("/common/beach.jpg");
	private final Background BG_BLACK = U.colorBackground(Color.BLACK);
	private final Background BG_BLUE = U.colorBackground(Color.CORNFLOWERBLUE);

	private final PacManModel3D MODEL_3D = GianmarcosModel3D.get();

	private final Rendering2D_PacMan RENDERING_PACMAN = new Rendering2D_PacMan();
	private final Rendering2D_MsPacMan RENDERING_MSPACMAN = new Rendering2D_MsPacMan();

	private final SoundManager SOUNDS_PACMAN = new SoundManager();
	{
		//@formatter:off
		SOUNDS_PACMAN.put(GameSounds.CREDIT,          "/pacman/sound/credit.mp3");
		SOUNDS_PACMAN.put(GameSounds.EXTRA_LIFE,      "/pacman/sound/extend.mp3");
		SOUNDS_PACMAN.put(GameSounds.GAME_READY,      "/pacman/sound/game_start.mp3");
		SOUNDS_PACMAN.put(GameSounds.BONUS_EATEN,     "/pacman/sound/eat_fruit.mp3");
		SOUNDS_PACMAN.put(GameSounds.PACMAN_MUNCH,    "/pacman/sound/munch_1.wav");
		SOUNDS_PACMAN.put(GameSounds.PACMAN_DEATH,    "/pacman/sound/pacman_death.wav");
		SOUNDS_PACMAN.put(GameSounds.PACMAN_POWER,    "/pacman/sound/power_pellet.mp3");
		SOUNDS_PACMAN.put(GameSounds.GHOST_EATEN,     "/pacman/sound/eat_ghost.mp3");
		SOUNDS_PACMAN.put(GameSounds.GHOST_RETURNING, "/pacman/sound/retreating.mp3");
		SOUNDS_PACMAN.put(GameSounds.SIREN_1,         "/pacman/sound/siren_1.mp3");
		SOUNDS_PACMAN.put(GameSounds.SIREN_2,         "/pacman/sound/siren_2.mp3");
		SOUNDS_PACMAN.put(GameSounds.SIREN_3,         "/pacman/sound/siren_3.mp3");
		SOUNDS_PACMAN.put(GameSounds.SIREN_4,         "/pacman/sound/siren_4.mp3");
		SOUNDS_PACMAN.put(GameSounds.INTERMISSION_1,  "/pacman/sound/intermission.mp3");
		SOUNDS_PACMAN.put(GameSounds.INTERMISSION_2,  "/pacman/sound/intermission.mp3");
		SOUNDS_PACMAN.put(GameSounds.INTERMISSION_3,  "/pacman/sound/intermission.mp3");
		//@formatter:on
	}

	private final SoundManager SOUNDS_MSPACMAN = new SoundManager();
	{
		//@formatter:off
		SOUNDS_MSPACMAN.put(GameSounds.CREDIT,          "/mspacman/sound/Coin Credit.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.EXTRA_LIFE,      "/mspacman/sound/Extra Life.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.GAME_READY,      "/mspacman/sound/Start.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.BONUS_EATEN,     "/mspacman/sound/Fruit.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.PACMAN_MUNCH,    "/mspacman/sound/Ms. Pac Man Pill.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.PACMAN_DEATH,    "/mspacman/sound/Died.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.PACMAN_POWER,    "/mspacman/sound/Scared Ghost.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.GHOST_EATEN,     "/mspacman/sound/Ghost.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.GHOST_RETURNING, "/mspacman/sound/Ghost Eyes.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.SIREN_1,         "/mspacman/sound/Ghost Noise 1.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.SIREN_2,         "/mspacman/sound/Ghost Noise 2.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.SIREN_3,         "/mspacman/sound/Ghost Noise 3.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.SIREN_4,         "/mspacman/sound/Ghost Noise 4.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.INTERMISSION_1,  "/mspacman/sound/They Meet Act 1.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.INTERMISSION_2,  "/mspacman/sound/The Chase Act 2.mp3");
		SOUNDS_MSPACMAN.put(GameSounds.INTERMISSION_3,  "/mspacman/sound/Junior Act 3.mp3");
		//@formatter:on
	}

	private final GameScene SCENES_PACMAN[][] = new GameScene[5][2];
	private final GameScene SCENES_MSPACMAN[][] = new GameScene[5][2];

	final GameController gameController;
	final Canvas canvas;
	final Scene mainScene;
	final Stage stage;
	final Group gameSceneContainer;
	final StackPane mainSceneContainer;

	GameScene currentScene;

	public GameUI(Stage stage, GameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		gameSceneContainer = new Group();
		StackPane.setAlignment(HUD.get(), Pos.TOP_LEFT);

		mainSceneContainer = new StackPane(gameSceneContainer, FlashMessageView.get(), HUD.get());
		mainScene = new Scene(mainSceneContainer, ASPECT_RATIO * height, height);

		// all 2D scenes render into this canvas
		canvas = new Canvas();
		canvas.heightProperty().bind(mainScene.heightProperty());
		canvas.widthProperty().bind(Bindings.createDoubleBinding(() -> {
			double scaling = canvas.getHeight() / t(TILES_Y);
			canvas.getTransforms().setAll(new Scale(scaling, scaling));
			return canvas.getHeight() * ASPECT_RATIO;
		}, canvas.heightProperty()));

		stage.setScene(mainScene);
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.titleProperty().bind(Bindings.createStringBinding(() -> {
			String gameName = gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man";
			return Env.$paused.get() ? String.format("%s (PAUSED, CTRL+P: resume, P: Step)", gameName)
					: String.format("%s", gameName);
		}, Env.gameLoop.$fps));

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> Env.gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

		Env.$drawMode3D.addListener($1 -> selectBackground(currentScene));

		//@formatter:off
		SCENES_PACMAN[0][0] = 
		SCENES_PACMAN[0][1] = new PacMan_IntroScene(gameController, canvas, RENDERING_PACMAN);
		SCENES_PACMAN[1][0] = 
		SCENES_PACMAN[1][1] = new PacMan_IntermissionScene1(gameController, canvas, RENDERING_PACMAN);
		SCENES_PACMAN[2][0] = 
		SCENES_PACMAN[2][1] = new PacMan_IntermissionScene2(gameController, canvas, RENDERING_PACMAN);
		SCENES_PACMAN[3][0] = 
		SCENES_PACMAN[3][1] = new PacMan_IntermissionScene3(gameController, canvas, RENDERING_PACMAN);
		SCENES_PACMAN[4][0] = new PlayScene2D(gameController, canvas, RENDERING_PACMAN);
		SCENES_PACMAN[4][1] = new PlayScene3D(gameController, MODEL_3D);
		
		SCENES_MSPACMAN[0][0] = 
		SCENES_MSPACMAN[0][1] = new MsPacMan_IntroScene(gameController, canvas, RENDERING_MSPACMAN);
		SCENES_MSPACMAN[1][0] = 
		SCENES_MSPACMAN[1][1] = new MsPacMan_IntermissionScene1(gameController, canvas, RENDERING_MSPACMAN);
		SCENES_MSPACMAN[2][0] = 
		SCENES_MSPACMAN[2][1] = new MsPacMan_IntermissionScene2(gameController, canvas, RENDERING_MSPACMAN);
		SCENES_MSPACMAN[3][0] = 
		SCENES_MSPACMAN[3][1] = new MsPacMan_IntermissionScene3(gameController, canvas, RENDERING_MSPACMAN);
		SCENES_MSPACMAN[4][0] = new PlayScene2D(gameController, canvas, RENDERING_MSPACMAN);
		SCENES_MSPACMAN[4][1] = new PlayScene3D(gameController, MODEL_3D);
		//@formatter:on
		selectGameScene();

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
		final var game = gameController.game;
		final int sceneVariant = _3D ? 1 : 0;

		int sceneIndex;
		switch (gameController.currentStateID) {
		case INTRO:
			sceneIndex = 0;
			break;
		case INTERMISSION:
			sceneIndex = game.intermissionNumber(game.levelNumber);
			break;
		case INTERMISSION_TEST:
			sceneIndex = gameController.intermissionTestNumber;
			break;
		default:
			sceneIndex = 4; // Play Scene
			break;
		}

		return gameController.gameVariant == GameVariant.MS_PACMAN //
				? SCENES_MSPACMAN[sceneIndex][sceneVariant]
				: SCENES_PACMAN[sceneIndex][sceneVariant];
	}

	private void selectGameScene() {
		GameScene nextScene = gameSceneForCurrentState(Env.$3D.get());
		if (currentScene != nextScene) {
			if (currentScene != null) {
				log("Change scene from '%s' to '%s'", currentScene.getClass().getName(),
						nextScene.getClass().getName());
				currentScene.end();
			} else {
				log("Set scene to '%s'", nextScene.getClass().getName());
			}
			selectBackground(nextScene);
			selectRendering(nextScene);
			selectSounds(nextScene);
			// TODO: why do I always have to create a new subscene in the 2D case?
			gameSceneContainer.getChildren().setAll(nextScene.createSubScene(mainScene));
			nextScene.init();
			currentScene = nextScene;
		}
	}

	private void selectSounds(GameScene scene) {
		Env.sounds = gameController.gameVariant == GameVariant.MS_PACMAN ? SOUNDS_MSPACMAN : SOUNDS_PACMAN;
	}

	private void selectRendering(GameScene scene) {
		Env.r2D = gameController.gameVariant == GameVariant.MS_PACMAN ? RENDERING_MSPACMAN : RENDERING_PACMAN;
	}

	private void selectBackground(GameScene scene) {
		if (scene.is3D()) {
			mainSceneContainer.setBackground(Env.$drawMode3D.get() == DrawMode.LINE ? BG_BLACK : BG_BEACH);
		} else {
			mainSceneContainer.setBackground(BG_BLUE);
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
			} else {
				FlashMessageView.showFlashMessage(2, "Game resumed");
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