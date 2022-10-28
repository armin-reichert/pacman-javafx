/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.Steering;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.shell.info.Dashboard;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.KeyboardSteering;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * JavaFX UI for Pac-Man / Ms. Pac-Man game.
 * <p>
 * The play scene is available in 2D and 3D. The intro scenes and intermission scenes are all 2D.
 * 
 * @author Armin Reichert
 */
public class GameUI {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final Image APP_ICON = Ufx.image("icons/pacman.png");

	private final GameLoop gameLoop = new GameLoop(60);
	private final GameController gameController;
	private final Stage stage;
	private final GameSceneManager sceneManager = new GameSceneManager();
	private final Scene mainScene;

	private StackPane gameSceneParent;
	private Dashboard dashboard;
	private FlashMessageView flashMessageView;
	private PiPView pipView;

	private Steering currentSteering;
	private GameScene currentGameScene;

	// In MAME, window is about 4% smaller than the 28x36 aspect ratio. Why?
	public GameUI(GameController gameController, Stage stage, double width, double height) {
		this.gameController = gameController;
		this.stage = stage;
		this.mainScene = new Scene(createSceneContent(), width, height, true, SceneAntialiasing.BALANCED);
		Env.drawModePy.addListener((x, y, z) -> updateBackground());
		Env.bgColorPy.addListener((x, y, z) -> updateBackground());
		initKeyboardInput();
		initGameEventing();
		initAnimations();
		initStage();
		Actions.setUI(this);
		Actions.playHelpMessageAfterSeconds(0.5);
		sceneManager.embedGameScenes(mainScene);
		updateGameScene(true);
		stage.show();
	}

	private void initStage() {
		stage.setOnCloseRequest(e -> gameLoop.stop());
		stage.setScene(mainScene);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setTitle("Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(APP_ICON);
		stage.centerOnScreen();
	}

	private Parent createSceneContent() {
		var root = new StackPane();
		dashboard = new Dashboard();
		dashboard.build(this);
		pipView = new PiPView();
		pipView.heightPy.bind(Env.pipSceneHeightPy);
		pipView.getRoot().opacityProperty().bind(Env.pipOpacityPy);
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView.getRoot()));
		flashMessageView = new FlashMessageView();
		gameSceneParent = new StackPane();
		root.getChildren().addAll(gameSceneParent, flashMessageView, overlayPane);
		return root;
	}

	private void initAnimations() {
		for (var gameVariant : GameVariant.values()) {
			var game = gameController.game(gameVariant);
			var r2D = switch (gameVariant) {
			case MS_PACMAN -> new RendererMsPacManGame();
			case PACMAN -> new RendererPacManGame();
			};
			game.pac.setAnimationSet(r2D.createPacAnimationSet(game.pac));
			game.ghosts().forEach(ghost -> ghost.setAnimationSet(r2D.createGhostAnimationSet(ghost)));
			LOGGER.info("Game variant %s: created 2D animations for maze, Pac-Man and the ghosts.", gameVariant);
		}
	}

	private void initGameEventing() {
		GameEvents.addEventListener(new GameEventAdapter() {
			@Override
			public void onGameEvent(GameEvent event) {
				GameEventAdapter.super.onGameEvent(event);
				currentGameScene.onGameEvent(event);
			}

			@Override
			public void onGameStateChange(GameStateChangeEvent e) {
				updateGameScene(false);
			}

			@Override
			public void onUIForceUpdate(GameEvent e) {
				updateGameScene(true);
			}
		});
	}

	private void initKeyboardInput() {
		mainScene.setOnKeyPressed(Keyboard::processEvent);
		Keyboard.addHandler(this::onKeyPressed);
		Keyboard.addHandler(() -> currentGameScene.onKeyPressed());
	}

	public void setPacSteering(Steering steering) {
		Objects.requireNonNull(steering);
		if (currentSteering instanceof KeyboardSteering keySteering) {
			mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, keySteering::onKeyPressed);
		}
		currentSteering = steering;
		if (steering instanceof KeyboardSteering keySteering) {
			mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keySteering::onKeyPressed);
		}
		gameController.setNormalSteering(currentSteering);
	}

	public GameLoop getGameLoop() {
		return gameLoop;
	}

	public void startGameLoop() {
		gameLoop.setUpdateTask(() -> {
			gameController.update();
			currentGameScene.updateAndRender();
		});
		gameLoop.setRenderTask(() -> {
			flashMessageView.update();
			dashboard.update();
			updatePipView();
		});
		gameLoop.pausedPy.bind(Env.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.timeMeasuredPy);
		gameLoop.start();
	}

	void updateGameScene(boolean forcedReload) {
		var newGameScene = sceneManager.selectGameScene(gameController, currentGameScene, forcedReload);
		newGameScene.resize(mainScene.getHeight());
		if (newGameScene != currentGameScene) {
			currentGameScene = newGameScene;
			gameSceneParent.getChildren().setAll(currentGameScene.getFXSubScene());
			updateBackground();
			pipView.init(newGameScene.getSceneContext());
		}
	}

	private void updatePipView() {
		if (Env.pipEnabledPy.get()
				&& (currentGameScene instanceof PlayScene2D || currentGameScene instanceof PlayScene3D)) {
			pipView.getRoot().setVisible(true);
			pipView.draw();
		} else {
			pipView.getRoot().setVisible(false);
		}
	}

	private void updateBackground() {
		var mode = Env.drawModePy.get();
		var bgColor = Env.bgColorPy.get();
		gameSceneParent.setBackground(Ufx.colorBackground(mode == DrawMode.FILL ? bgColor : Color.BLACK));
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Modifier.ALT, KeyCode.A)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.B)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.D)) {
			Env.toggle(Env.showDebugInfoPy);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.I)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.M)) {
			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(KeyCode.P)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Modifier.SHIFT, KeyCode.P) || Keyboard.pressed(KeyCode.SPACE)) {
			Actions.oneSimulationStep();
		} else if (Keyboard.pressed(Modifier.SHIFT, KeyCode.SPACE)) {
			Actions.tenSimulationSteps();
		} else if (Keyboard.pressed(KeyCode.Q)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.DIGIT3)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(KeyCode.F1)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(KeyCode.F2)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(KeyCode.F11)) {
			stage.setFullScreen(true);
		}
	}

	public GameController getGameController() {
		return gameController;
	}

	public Stage getStage() {
		return stage;
	}

	public GameSceneManager getSceneManager() {
		return sceneManager;
	}

	public GameScene getCurrentGameScene() {
		return currentGameScene;
	}

	public Scene getMainScene() {
		return mainScene;
	}

	public FlashMessageView getFlashMessageView() {
		return flashMessageView;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}
}