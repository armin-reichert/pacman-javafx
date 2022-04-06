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
import static de.amr.games.pacman.ui.fx.shell.FlashMessageView.showFlashMessage;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameScenes;
import de.amr.games.pacman.ui.fx.shell.info.InfoLayer;
import de.amr.games.pacman.ui.fx.sound.mspacman.Sounds_MsPacMan;
import de.amr.games.pacman.ui.fx.sound.pacman.Sounds_PacMan;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
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

	public final GameController gameController;
	public final Stage stage;

	private final StackPane mainSceneRoot;
	private final GameScenes gameScenes;
	private final InfoLayer infoLayer;
	private final Wallpapers wallpapers;

	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage stage, double width, double height) {
		this.gameController = gameController;
		this.stage = stage;
		this.infoLayer = new InfoLayer(this);
		this.wallpapers = new Wallpapers();

		// first child will be updated by subscene assigned to current game scene
		mainSceneRoot = new StackPane(new Region(), FlashMessageView.get(), infoLayer);
		var mainScene = new Scene(mainSceneRoot, width, height);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> GameLoop.get().stop());
		stage.getIcons().add(U.image("/pacman/graphics/pacman.png"));
		stage.setScene(mainScene);

		gameController.addGameEventListener(this);
		Env.$drawMode3D.addListener(($drawMode, oldDrawMode, newDrawMode) -> updateMainSceneBackground(currentGameScene));

		gameScenes = new GameScenes(mainScene, gameController, GianmarcosModel3D.get());
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public void show(boolean fullscreen) {
		selectGameScene();
		stage.setFullScreen(fullscreen);
		stage.centerOnScreen();
		stage.show();
	}

	/**
	 * Called on every tick (if simulation is not paused).
	 */
	public void update() {
		gameController.updateState();
		currentGameScene.update();
	}

	/**
	 * Called on every tick (also if simulation is paused).
	 */
	public void render() {
		FlashMessageView.get().update();
		infoLayer.update();
		stage.setTitle(gameController.gameVariant == GameVariant.PACMAN ? "Pac-Man" : "Ms. Pac-Man");
	}

	@Override
	public void onGameEvent(GameEvent event) {
		super.onGameEvent(event);
		selectGameScene();
		currentGameScene.onGameEvent(event);
	}

	private void selectGameScene() {
		var nextGameScene = gameScenes.getScene(Env.$3D.get() ? GameScenes.SCENE_3D : GameScenes.SCENE_2D);
		if (nextGameScene != currentGameScene) {
			if (currentGameScene != null) {
				currentGameScene.end();
			}
			displayGameScene(nextGameScene);
			nextGameScene.init();
			currentGameScene = nextGameScene;
		}
	}

	private void displayGameScene(GameScene gameScene) {
		gameScene.resizeFXSubScene(stage.getScene().getHeight());
		mainSceneRoot.getChildren().set(0, gameScene.getFXSubScene());
		updateMainSceneBackground(gameScene);
		if (gameController.gameVariant == GameVariant.MS_PACMAN) {
			gameScene.setContext(gameController.game, Rendering2D_MsPacMan.get(), Sounds_MsPacMan.get());
		} else {
			gameScene.setContext(gameController.game, Rendering2D_PacMan.get(), Sounds_PacMan.get());
		}
		log("Game scene is now '%s'", gameScene.getClass());
	}

	private void updateMainSceneBackground(GameScene gameScene) {
		if (gameScene.is3D()) {
			wallpapers.next();
			mainSceneRoot.setBackground(Env.$drawMode3D.get() == DrawMode.LINE //
					? U.colorBackground(Color.BLACK)
					: wallpapers.getCurrent());
		} else {
			mainSceneRoot.setBackground(U.colorBackground(Color.CORNFLOWERBLUE));
		}
	}

	public void handleKeyPressed(KeyEvent e) {
		boolean shift = e.isShiftDown();

		if (e.isAltDown()) {
			switch (e.getCode()) {
			case A -> toggleAutopilot();
			case E -> gameController.cheatEatAllPellets();
			case I -> toggleImmunity();
			case L -> addLives(3);
			case N -> enterNextLevel();
			case Q -> quitCurrentGameScene();
			case S -> changeTargetFramerate(shift ? -10 : 10);
			case V -> toggleGameVariant();
			case X -> gameController.cheatKillGhosts();
			case Z -> startIntermissionScenesTest();
			case LEFT -> changePerspective(-1);
			case RIGHT -> changePerspective(1);
			case DIGIT3 -> toggleUse3DScene();
			default -> {
			}
			}
		}

		else if (e.isControlDown()) {
			switch (e.getCode()) {
			case I -> toggleInfoPanelsVisible();
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

	private void changePerspective(int delta) {
		if (currentGameScene.is3D()) {
			Env.$perspective.set(Env.perspectiveShifted(delta));
			String perspectiveName = Env.message(Env.$perspective.get().name());
			showFlashMessage(1, Env.message("camera_perspective", perspectiveName));
		}
	}

	private void changeTargetFramerate(int delta) {
		GameLoop.get().setTargetFrameRate(U.clamp(GameLoop.get().getTargetFrameRate() + delta, 10, 120));
		showFlashMessage(1, "Target FPS set to %d Hz", GameLoop.get().getTargetFrameRate());
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

	public void enterLevel(int levelNumber) {
		if (gameController.game.levelNumber == levelNumber) {
			return;
		}
		currentGameScene.getSounds().stopAll();
		if (levelNumber == 1) {
			gameController.game.reset();
			gameController.changeState(GameState.READY);
		} else {
			// TODO game model should be able to switch directly to any level
			int start = levelNumber > gameController.game.levelNumber ? gameController.game.levelNumber + 1 : 1;
			for (int n = start; n < levelNumber; ++n) {
				gameController.game.setLevel(n);
			}
			gameController.changeState(GameState.LEVEL_STARTING);
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

	public void toggleInfoPanelsVisible() {
		infoLayer.setVisible(!infoLayer.isVisible());
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
		gameController.playerImmune = !gameController.playerImmune;
		String message = Env.message(gameController.playerImmune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(1, message);
	}

	public void toggleUse3DScene() {
		Env.$3D.set(!Env.$3D.get());
		if (gameScenes.getScene(GameScenes.SCENE_2D) != gameScenes.getScene(GameScenes.SCENE_3D)) {
			currentGameScene.getSounds().stopAll();
			selectGameScene();
			if (currentGameScene instanceof PlayScene2D) {
				((PlayScene2D) currentGameScene).onSwitchBetween2DAnd3D();
			}
		}
		showFlashMessage(1, Env.message(Env.$3D.get() ? "use_3D_scene" : "use_2D_scene"));
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