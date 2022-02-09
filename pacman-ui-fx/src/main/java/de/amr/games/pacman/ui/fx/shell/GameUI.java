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
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.GameScenes;
import de.amr.games.pacman.ui.fx.sound.mspacman.SoundManager_MsPacMan;
import de.amr.games.pacman.ui.fx.sound.pacman.SoundManager_PacMan;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
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

	private static final int TILES_X = 28, TILES_Y = 36;
	private static final double ASPECT_RATIO = (double) TILES_X / TILES_Y;

	private final GameScenes gameScenes = new GameScenes();

	private final Background bg_beach = U.imageBackground("/common/beach.jpg");
	private final Background bg_black = U.colorBackground(Color.BLACK);
	private final Background bg_blue = U.colorBackground(Color.CORNFLOWERBLUE);

	protected final GameController gameController;
	protected final Stage stage;
	protected final Canvas canvas = new Canvas(); // common canvas of all 2D scenes

	private final Scene mainScene;
	private final Group gameSceneRoot = new Group();
	private final StackPane mainSceneRoot = new StackPane();

	protected AbstractGameScene currentScene;

	public GameUI(Stage stage, GameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		mainSceneRoot.getChildren().addAll(gameSceneRoot, FlashMessageView.get(), HUD.get());
		StackPane.setAlignment(HUD.get(), Pos.TOP_LEFT);
		mainScene = new Scene(mainSceneRoot, ASPECT_RATIO * height, height);
		mainScene.heightProperty().addListener($1 -> resizeCanvas(mainScene.getHeight()));
		resizeCanvas(mainScene.getHeight());

		Env.$drawMode3D.addListener($1 -> updateBackground(currentScene));
		Env.gameLoop.$fps.addListener($1 -> stage.setTitle(computeStageTitle()));

		selectGameScene();

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> Env.gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);

		stage.setTitle(computeStageTitle());
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

	private AbstractGameScene gameSceneForCurrentState(boolean _3D) {
		var game = gameController.game;
		int sceneIndex = switch (gameController.state) {
		case INTRO -> 0;
		case INTERMISSION -> game.intermissionNumber(game.levelNumber);
		case INTERMISSION_TEST -> gameController.intermissionTestNumber;
		default -> 4;
		};
		return gameScenes.getScene(gameController.gameVariant, sceneIndex, _3D ? 1 : 0);
	}

	private void selectGameScene() {
		AbstractGameScene nextScene = gameSceneForCurrentState(Env.$3D.get());
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

	private void updateSceneContext(AbstractGameScene gameScene) {
		switch (gameController.gameVariant) {
		case MS_PACMAN -> {
			gameScene.setContext(gameController, gameController.game, Rendering2D_MsPacMan.get(),
					SoundManager_MsPacMan.get());
			if (gameScene instanceof AbstractGameScene2D) {
				((AbstractGameScene2D) gameScene).setDrawingContext(canvas, new V2i(TILES_X, TILES_Y).scaled(TS));
			}
		}
		case PACMAN -> {
			gameScene.setContext(gameController, gameController.game, Rendering2D_PacMan.get(), SoundManager_PacMan.get());
			if (gameScene instanceof AbstractGameScene2D) {
				((AbstractGameScene2D) gameScene).setDrawingContext(canvas, new V2i(TILES_X, TILES_Y).scaled(TS));
			}
		}
		}
		updateBackground(gameScene);
	}

	private void resizeCanvas(double height) {
		canvas.setHeight(height);
		canvas.setWidth(height * ASPECT_RATIO);
		double scaling = height / (TILES_Y * TS);
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
	}

	private String computeStageTitle() {
		String gameName = gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man";
		return Env.$paused.get() ? String.format("%s (PAUSED, CTRL+P: resume, P: Step)", gameName)
				: String.format("%s", gameName);
	}

	private void updateBackground(AbstractGameScene scene) {
		if (scene.is3D()) {
			mainSceneRoot.setBackground(Env.$drawMode3D.get() == DrawMode.LINE ? bg_black : bg_beach);
		} else {
			mainSceneRoot.setBackground(bg_blue);
		}
	}

	private void toggle3D() {
		Env.$3D.set(!Env.$3D.get());
		if (gameSceneForCurrentState(false) != gameSceneForCurrentState(true)) {
			currentScene.getSounds().stopAll();
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
		if (e.isControlDown()) {
			onControlKeyPressed(e);
			return;
		}

		if (e.isShiftDown() || e.isAltDown()) {
			return;
		}

		final GameModel game = gameController.game;
		final GameState state = gameController.state;

		switch (e.getCode()) {

		case A -> {
			gameController.autoControlled = !gameController.autoControlled;
			String message = Env.message(gameController.autoControlled ? "autopilot_on" : "autopilot_off");
			FlashMessageView.showFlashMessage(1, message);
		}

		case E -> {
			if (gameController.gameRunning) {
				gameController.cheatEatAllPellets();
			}
		}

		case I -> {
			game.player.immune = !game.player.immune;
			String message = Env.message(game.player.immune ? "player_immunity_on" : "player_immunity_off");
			FlashMessageView.showFlashMessage(1, message);
		}

		case L -> {
			if (gameController.gameRunning) {
				game.player.lives += 3;
				FlashMessageView.showFlashMessage(2, "You have %d lives", game.player.lives);
			}
		}

		case N -> {
			if (gameController.gameRunning) {
				FlashMessageView.showFlashMessage(1, Env.CHEAT_TALK.next());
				gameController.changeState(GameState.LEVEL_COMPLETE);
			}
		}

		case P -> {
			if (Env.$paused.get()) {
				Env.gameLoop.runSingleStep(true);
			}
		}

		case Q -> {
			if (state != GameState.INTRO) {
				currentScene.end();
				currentScene.getSounds().stopAll();
				gameController.changeState(GameState.INTRO);
			}
		}

		case V -> {
			if (state == GameState.INTRO) {
				gameController.selectGameVariant(gameController.gameVariant.succ());
			}
		}

		case X -> {
			if (gameController.gameRunning) {
				gameController.cheatKillGhosts();
			}
		}

		case Z -> {
			if (state == GameState.INTRO) {
				gameController.startIntermissionTest();
				FlashMessageView.showFlashMessage(1, "Intermission Scene Test");
			}
		}

		case SPACE -> gameController.requestGame();

		case F11 -> stage.setFullScreen(true);

		default -> {
		}

		}
	}

	private void onControlKeyPressed(KeyEvent e) {
		switch (e.getCode()) {

		case LEFT, RIGHT -> {
			if (currentScene.is3D()) {
				if (e.getCode() == KeyCode.LEFT) {
					Env.selectPrevPerspective();
				} else {
					Env.selectNextPerspective();
				}
				String perspective_key = Env.message(Env.$perspective.get().name().toLowerCase());
				String message = Env.message("camera_perspective", perspective_key);
				FlashMessageView.showFlashMessage(1, message);
			}
		}

		case H -> {
			if (currentScene.is3D()) {
				Env.changeMazeWallHeight(!e.isShiftDown());
			}
		}

		case I -> {
			if (HUD.get().isVisible()) {
				HUD.get().hide();
			} else {
				HUD.get().show();
			}
		}

		case L -> {
			if (currentScene.is3D()) {
				Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
			}
		}

		case P -> {
			Env.$paused.set(!Env.$paused.get());
			FlashMessageView.showFlashMessage(2, Env.$paused.get() ? "Game paused" : "Game resumed");
			log(Env.$paused.get() ? "Game paused." : "Game resumed.");
		}

		case R -> {
			if (currentScene.is3D()) {
				Env.changeMazeResolution(!e.isShiftDown());
			}
		}

		case S -> {
			int targetFrameRate = Env.gameLoop.getTargetFrameRate();
			if (!e.isShiftDown()) {
				Env.gameLoop.setTargetFrameRate(targetFrameRate + 10);
			} else {
				Env.gameLoop.setTargetFrameRate(Math.max(10, targetFrameRate - 10));
			}
			FlashMessageView.showFlashMessage(1, "Target FPS set to %d Hz", Env.gameLoop.getTargetFrameRate());
		}

		case T -> {
			Env.$isTimeMeasured.set(!Env.$isTimeMeasured.get());
		}

		case X -> {
			if (currentScene.is3D()) {
				Env.$axesVisible.set(!Env.$axesVisible.get());
			}
		}

		case Y -> {
			if (!currentScene.is3D()) {
				Env.$tilesVisible.set(!Env.$tilesVisible.get());
			}
		}

		case DIGIT3 -> {
			toggle3D();
			String message = Env.$3D.get() ? "Using 3D play scene\nCTRL+C changes perspective" : "Using 2D play scene";
			FlashMessageView.showFlashMessage(2, message);
		}

		default -> {
		}

		}
	}
}