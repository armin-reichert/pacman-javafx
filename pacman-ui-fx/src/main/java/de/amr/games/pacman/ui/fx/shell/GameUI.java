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
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.ui.fx.shell.FlashMessageView.showFlashMessage;

import java.util.Random;

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
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameScenes;
import de.amr.games.pacman.ui.fx.sound.mspacman.Sounds_MsPacMan;
import de.amr.games.pacman.ui.fx.sound.pacman.Sounds_PacMan;
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

	private static final Background[] WALLPAPERS = { //
			U.imageBackground("/common/wallpapers/beach.jpg"), //
			U.imageBackground("/common/wallpapers/space.jpg"), //
			U.imageBackground("/common/wallpapers/easter_island.jpg"), //
	};

	public final GameController gameController;
	private final GameScenes gameScenes;
	private final Stage stage;
	private final Canvas canvas;
	private final Scene mainScene;
	private final StackPane mainSceneRoot;
	private final InfoPanel infoPanel;
	private final SettingsPanel settingsPanel;

	private int wallpaperIndex;
	private GameScene currentGameScene;

	public GameUI(Stage stage, GameController gameController, double height, boolean fullscreen) {
		this.stage = stage;
		this.gameController = gameController;

		canvas = new Canvas(); // common canvas of all 2D scenes
		infoPanel = new InfoPanel(this, 400);
		settingsPanel = new SettingsPanel(this, 400);

		// first child will get replaced by subscene representing current game scene
		mainSceneRoot = new StackPane(new Group(), FlashMessageView.get(), infoPanel, settingsPanel);
		StackPane.setAlignment(infoPanel, Pos.TOP_LEFT);
		StackPane.setAlignment(settingsPanel, Pos.TOP_LEFT);

		mainScene = new Scene(mainSceneRoot, ASPECT_RATIO * height, height);
		mainScene.heightProperty().addListener($1 -> resizeCanvas(mainScene.getHeight()));
		resizeCanvas(mainScene.getHeight());

		Env.$drawMode3D.addListener($1 -> updateBackground(currentGameScene));

		gameScenes = new GameScenes(mainScene, gameController, canvas, new V2i(TILES_X, TILES_Y).scaled(TS));
		updateGameScene();

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> Env.gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);

		stage.setTitle(computeStageTitle());
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setScene(mainScene);
		stage.centerOnScreen();
		stage.setFullScreen(fullscreen);
		stage.show();
	}

	public void update() {
		FlashMessageView.get().update();
		if (infoPanel.isVisible()) {
			infoPanel.update(gameController, currentGameScene, stage, canvas);
		}
		if (settingsPanel.isVisible()) {
			settingsPanel.update();
		}
		stage.setTitle(computeStageTitle());
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	private GameScene gameSceneForCurrentState(boolean _3D) {
		var game = gameController.game;
		int sceneIndex = switch (gameController.state) {
		case INTRO -> 0;
		case INTERMISSION -> game.intermissionNumber(game.levelNumber);
		case INTERMISSION_TEST -> gameController.intermissionTestNumber;
		default -> 4;
		};
		return gameScenes.getScene(gameController.gameVariant, sceneIndex, _3D ? 1 : 0);
	}

	private void updateGameScene() {
		GameScene nextGameScene = gameSceneForCurrentState(Env.$3D.get());
		if (currentGameScene != nextGameScene) {
			if (currentGameScene != null) {
				log("Change scene from '%s' to '%s'", currentGameScene.getClass().getName(),
						nextGameScene.getClass().getName());
				currentGameScene.end();
			} else {
				log("Set scene to '%s'", nextGameScene.getClass().getName());
			}
			mainSceneRoot.getChildren().set(0, nextGameScene.getFXSubScene());
			updateSceneContext(nextGameScene);
			nextGameScene.init();
			currentGameScene = nextGameScene;
		}
	}

	private void updateSceneContext(GameScene gameScene) {
		switch (gameController.gameVariant) {
		case MS_PACMAN -> gameScene.setContext(gameController.game, Rendering2D_MsPacMan.get(), Sounds_MsPacMan.get());
		case PACMAN -> gameScene.setContext(gameController.game, Rendering2D_PacMan.get(), Sounds_PacMan.get());
		default -> throw new IllegalArgumentException();
		}
		updateBackground(gameScene);
	}

	private void updateBackground(GameScene gameScene) {
		if (gameScene.is3D()) {
			selectRandomWallpaper();
			mainSceneRoot.setBackground(Env.$drawMode3D.get() == DrawMode.LINE //
					? U.colorBackground(Color.BLACK)
					: WALLPAPERS[wallpaperIndex]);
		} else {
			mainSceneRoot.setBackground(U.colorBackground(Color.CORNFLOWERBLUE));
		}
	}

	private void selectRandomWallpaper() {
		int next = wallpaperIndex;
		while (next == wallpaperIndex) {
			next = new Random().nextInt(WALLPAPERS.length);
		}
		wallpaperIndex = next;
	}

	private void resizeCanvas(double height) {
		canvas.setHeight(height);
		canvas.setWidth(height * ASPECT_RATIO);
		double scaling = height / (TILES_Y * TS);
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
	}

	private String computeStageTitle() {
		return gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man";
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene();
	}

	private void handleKeyPressed(KeyEvent e) {
		final boolean shift = e.isShiftDown();
		if (e.isAltDown()) {
			onAltKeyPressed(e, shift);
		} else if (e.isControlDown()) {
			onControlKeyPressed(e, shift);
		} else {
			onKeyPressed(e, shift);
		}
	}

	private void onAltKeyPressed(KeyEvent e, boolean shift) {
		switch (e.getCode()) {
		case A -> toggleAutopilot();
		case E -> gameController.cheatEatAllPellets();
		case I -> toggleImmunity();
		case L -> addLives(3);
		case N -> enterNextLevel();
		case Q -> quitCurrentGameScene();
		case V -> toggleGameVariant();
		case X -> gameController.cheatKillGhosts();
		case Z -> startIntermissionTest();
		case LEFT, RIGHT -> {
			if (currentGameScene.is3D()) {
				if (e.getCode() == KeyCode.LEFT) {
					Env.selectPrevPerspective();
				} else {
					Env.selectNextPerspective();
				}
				String perspectiveName = Env.message(Env.$perspective.get().name());
				showFlashMessage(1, Env.message("camera_perspective", perspectiveName));
			}
		}
		case S -> {
			int rate = Env.gameLoop.getTargetFrameRate();
			setTargetFrameRate(shift ? Math.max(10, rate - 10) : rate + 10);
			showFlashMessage(1, "Target FPS set to %d Hz", Env.gameLoop.getTargetFrameRate());
		}
		case DIGIT3 -> togglePlayScene3D();
		default -> {
		}
		}
	}

	private void onControlKeyPressed(KeyEvent e, boolean shift) {
		switch (e.getCode()) {
		case I -> toggleInfoPanelVisibility();
		case J -> toggleSettingsPanelVisibility();
		default -> {
		}
		}
	}

	private void onKeyPressed(KeyEvent e, boolean shift) {
		switch (e.getCode()) {
		case SPACE -> gameController.requestGame();
		case F11 -> stage.setFullScreen(true);
		default -> {
		}
		}
	}

	public void quitCurrentGameScene() {
		currentGameScene.end();
		currentGameScene.getSounds().stopAll();
		gameController.changeState(GameState.INTRO);
	}

	public void enterNextLevel() {
		if (gameController.gameRunning) {
			gameController.changeState(GameState.LEVEL_COMPLETE);
			showFlashMessage(1, Env.CHEAT_TALK.next());
		}
	}

	public void addLives(int lives) {
		if (gameController.gameRunning) {
			gameController.game.player.lives += lives;
			showFlashMessage(2, "You have %d lives", gameController.game.player.lives);
		}
	}

	public void startIntermissionTest() {
		if (gameController.state == GameState.INTRO) {
			gameController.startIntermissionTest();
			showFlashMessage(1, "Intermission Scene Test");
		}
	}

	public void setTargetFrameRate(int value) {
		Env.gameLoop.setTargetFrameRate(value);
	}

	public void toggleInfoPanelVisibility() {
		infoPanel.setVisible(!infoPanel.isVisible());
		if (infoPanel.isVisible()) {
			settingsPanel.setVisible(false);
		}
	}

	public void toggleSettingsPanelVisibility() {
		settingsPanel.setVisible(!settingsPanel.isVisible());
		if (settingsPanel.isVisible()) {
			infoPanel.setVisible(false);
		}
	}

	public void togglePaused() {
		Env.$paused.set(!Env.$paused.get());
		showFlashMessage(2, Env.$paused.get() ? "Paused" : "Resumed");
		log(Env.$paused.get() ? "Simulation paused." : "Simulation resumed.");
	}

	public void toggleGameVariant() {
		if (!gameController.gameRunning) {
			gameController.selectGameVariant(gameController.gameVariant.succ());
		}
	}

	public void toggleAutopilot() {
		gameController.autoControlled = !gameController.autoControlled;
		String message = Env.message(gameController.autoControlled ? "autopilot_on" : "autopilot_off");
		showFlashMessage(1, message);
	}

	public void toggleImmunity() {
		GameModel game = gameController.game;
		game.player.immune = !game.player.immune;
		String message = Env.message(game.player.immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(1, message);
	}

	public void togglePlayScene3D() {
		Env.$3D.set(!Env.$3D.get());
		if (gameSceneForCurrentState(false) != gameSceneForCurrentState(true)) {
			currentGameScene.getSounds().stopAll();
			updateGameScene();
			if (currentGameScene instanceof PlayScene2D) {
				((PlayScene2D) currentGameScene).onSwitchFrom3DTo2D();
			}
		}
		String message = Env.$3D.get() ? "Using 3D Play Scene" : "Using 2D Play Scene";
		showFlashMessage(2, message);
	}

	public void toggleAxesVisible() {
		if (currentGameScene.is3D()) {
			Env.$axesVisible.set(!Env.$axesVisible.get());
		}
	}

	public void toggleDrawMode() {
		if (currentGameScene.is3D()) {
			Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
		}
	}

	public void toggleUseMazeFloorTexture() {
		if (currentGameScene.is3D()) {
			Env.$useMazeFloorTexture.set(!Env.$useMazeFloorTexture.get());
		}
	}

	public void toggleTilesVisible() {
		if (!currentGameScene.is3D()) {
			Env.$tilesVisible.set(!Env.$tilesVisible.get());
		}
	}
}