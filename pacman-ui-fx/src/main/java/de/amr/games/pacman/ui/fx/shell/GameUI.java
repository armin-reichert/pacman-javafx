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
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameScenes;
import de.amr.games.pacman.ui.fx.sound.mspacman.Sounds_MsPacMan;
import de.amr.games.pacman.ui.fx.sound.pacman.Sounds_PacMan;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class GameUI extends DefaultGameEventHandler {

	private static final V2i SIZE_IN_TILES = new V2i(28, 36);
	private static final double ASPECT_RATIO = (double) SIZE_IN_TILES.x / SIZE_IN_TILES.y;

	public final GameController gameController;
	public final GameLoop gameLoop = new GameLoop();
	public final Stage stage;
	private final Scene mainScene;
	private final StackPane mainLayout;
	private final InfoPanel infoPanel;
	private final SettingsPanel settingsPanel;

	private Background[] wallpapers = { //
			U.imageBackground("/common/wallpapers/beach.jpg"), //
			U.imageBackground("/common/wallpapers/space.jpg"), //
			U.imageBackground("/common/wallpapers/easter_island.jpg"), //
	};
	private int wallpaperIndex;

	private final GameScenes gameScenes;
	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage stage, double height) {
		this.gameController = gameController;
		this.stage = stage;
		this.settingsPanel = new SettingsPanel(this, 400);
		this.infoPanel = new InfoPanel(this, 400);
		var infoLayer = new BorderPane();
		infoLayer.setLeft(infoPanel);
		infoLayer.setRight(settingsPanel);

		// first child will dynamically get replaced by subscene representing the current game scene
		mainLayout = new StackPane(new Group(), FlashMessageView.get(), infoLayer);
		mainScene = new Scene(mainLayout, ASPECT_RATIO * height, height);
		gameScenes = new GameScenes(mainScene, gameController, GianmarcosModel3D.get(), SIZE_IN_TILES.scaled(TS));

		// Game loop triggers game controller updates, UI handles game controller events
		gameController.addGameEventListener(this);
		gameLoop.update = () -> {
			gameController.updateState();
			getCurrentGameScene().update();
		};
		gameLoop.render = this::update;

		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> gameLoop.stop());
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setScene(mainScene);

		Env.$drawMode3D.addListener(($drawMode, _old, _new) -> updateBackground(currentGameScene));
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		updateGameScene();
		currentGameScene.onGameEvent(event);
	}

	public void start(boolean fullscreen) {
		updateGameScene();
		stage.setFullScreen(fullscreen);
		stage.centerOnScreen();
		stage.show();
		gameLoop.start();
	}

	private void update() {
		FlashMessageView.get().update();
		if (infoPanel.isVisible()) {
			infoPanel.update(this);
		}
		if (settingsPanel.isVisible()) {
			settingsPanel.update();
		}
		stage.setTitle(gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	private void updateGameScene() {
		var nextGameScene = gameScenes.getScene(gameController, Env.$3D.get());
		if (currentGameScene == nextGameScene) {
			return;
		}
		if (currentGameScene != null) {
			currentGameScene.end();
			log("Change scene from '%s' to '%s'", currentGameScene.getClass(), nextGameScene.getClass());
		} else {
			log("Set scene to '%s'", nextGameScene.getClass());
		}
		mainLayout.getChildren().set(0, nextGameScene.getFXSubScene());
		updateBackground(nextGameScene);
		switch (gameController.gameVariant) {
		case MS_PACMAN -> //
				nextGameScene.setContext(gameController.game, Rendering2D_MsPacMan.get(), Sounds_MsPacMan.get());
		case PACMAN -> //
				nextGameScene.setContext(gameController.game, Rendering2D_PacMan.get(), Sounds_PacMan.get());
		default -> //
				throw new IllegalArgumentException();
		}
		nextGameScene.init();
		currentGameScene = nextGameScene;
	}

	private void updateBackground(GameScene gameScene) {
		if (gameScene.is3D()) {
			selectRandomWallpaper();
			mainLayout.setBackground(Env.$drawMode3D.get() == DrawMode.LINE //
					? U.colorBackground(Color.BLACK)
					: wallpapers[wallpaperIndex]);
		} else {
			mainLayout.setBackground(U.colorBackground(Color.CORNFLOWERBLUE));
		}
	}

	private void selectRandomWallpaper() {
		int next = wallpaperIndex;
		while (next == wallpaperIndex) {
			next = new Random().nextInt(wallpapers.length);
		}
		wallpaperIndex = next;
	}

	private void handleKeyPressed(KeyEvent e) {
		boolean shift = e.isShiftDown();

		if (e.isAltDown()) {
			switch (e.getCode()) {
			case A -> toggleAutopilot();
			case E -> gameController.cheatEatAllPellets();
			case I -> toggleImmunity();
			case L -> addLives(3);
			case N -> enterNextLevel();
			case Q -> quitCurrentGameScene();
			case S -> {
				int rate = gameLoop.getTargetFrameRate();
				gameLoop.setTargetFrameRate(shift ? Math.max(10, rate - 10) : rate + 10);
				showFlashMessage(1, "Target FPS set to %d Hz", gameLoop.getTargetFrameRate());
			}
			case V -> toggleGameVariant();
			case X -> gameController.cheatKillGhosts();
			case Z -> startIntermissionScenesTest();
			case LEFT, RIGHT -> {
				if (currentGameScene.is3D()) {
					int delta = e.getCode() == KeyCode.LEFT ? Perspective.values().length - 1 : 1;
					Env.selectPerspective(delta);
					String perspectiveName = Env.message(Env.$perspective.get().name());
					showFlashMessage(1, Env.message("camera_perspective", perspectiveName));
				}
			}
			case DIGIT3 -> toggleUsePlayScene3D();
			default -> {
			}
			}
		}

		else if (e.isControlDown()) {
			switch (e.getCode()) {
			case I -> toggleInfoPanelVisible();
			case J -> toggleSettingsPanelVisible();
			default -> {
			}
			}
		}

		else {
			switch (e.getCode()) {
			case SPACE -> gameController.requestGame();
			case F11 -> stage.setFullScreen(true);
			default -> {
			}
			}
		}
	}

	public void quitCurrentGameScene() {
		currentGameScene.getSounds().stopAll();
		currentGameScene.end();
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
			showFlashMessage(1, "You have %d lives", gameController.game.player.lives);
		}
	}

	public void startIntermissionScenesTest() {
		if (gameController.state == GameState.INTRO) {
			gameController.startIntermissionTest();
			showFlashMessage(1, "Intermission Scenes Test");
		}
	}

	public void toggleInfoPanelVisible() {
		infoPanel.setVisible(!infoPanel.isVisible());
	}

	public void toggleSettingsPanelVisible() {
		settingsPanel.setVisible(!settingsPanel.isVisible());
	}

	public void togglePaused() {
		Env.$paused.set(!Env.$paused.get());
		showFlashMessage(1, Env.$paused.get() ? "Paused" : "Resumed");
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

	public void toggleUsePlayScene3D() {
		Env.$3D.set(!Env.$3D.get());
		if (gameScenes.getScene(gameController, false) != gameScenes.getScene(gameController, true)) {
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
		Env.$axesVisible.set(!Env.$axesVisible.get());
	}

	public void toggleDrawMode() {
		Env.$drawMode3D.set(Env.$drawMode3D.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}

	public void toggleUseMazeFloorTexture() {
		Env.$useMazeFloorTexture.set(!Env.$useMazeFloorTexture.get());
	}

	public void toggleTilesVisible() {
		Env.$tilesVisible.set(!Env.$tilesVisible.get());
	}
}