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
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.shell.info.Dashboard;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.KeyboardSteering;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
public class GameUI implements GameEventListener {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final Image APP_ICON_PACMAN = Ufx.image("icons/pacman.png");
	private static final Image APP_ICON_MSPACMAN = Ufx.image("icons/mspacman.png");

	private final GameLoop gameLoop = new GameLoop(GameModel.FPS);
	private final GameSceneManager sceneManager = new GameSceneManager();
	private final GameController gameController;
	private final Stage stage;
	/** Game scene placeholder, single child will be the game scene's FX subscene. */
	private final Group gameSceneParent = new Group();
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final PiPView pipView = new PiPView();

	private Steering currentSteering;
	private GameScene currentGameScene;

	// In MAME, window is about 4% smaller than the 28x36 aspect ratio. Why?
	public GameUI(GameController gameController, Stage stage, float zoom) {
		this.gameController = gameController;
		this.stage = stage;
		stage.setScene(createScene(zoom));
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setOnCloseRequest(e -> endApp());
		Keyboard.addHandler(this::onKeyPressed);
		GameEvents.addListener(this);
		Actions.init(this);
		initGameLoop();
		updateGameScene(true);
		updateSound(gameController);
		updateStageTitle();
		dashboard.init(this);
		stage.centerOnScreen();
		stage.show();
		Ufx.pause(0.5, Actions::playHelpVoiceMessage).play();
	}

	private void endApp() {
		gameLoop.stop();
		LOGGER.trace("Application ended.");
	}

	private Scene createScene(float zoom) {
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView));

		var content = new StackPane(gameSceneParent, flashMessageView, overlayPane);
		var size = GameScene.DEFAULT_SIZE.toFloatVec().scaled(zoom);
		var scene = new Scene(content, size.x(), size.y());
		scene.setOnKeyPressed(Keyboard::processEvent);
		scene.heightProperty().addListener((heightPy, oldHeight, newHeight) -> {
			if (currentGameScene instanceof GameScene2D scene2D) {
				scene2D.resizeToHeight(newHeight.floatValue());
			}
		});

		Env.drawModePy.addListener((x, y, z) -> updateMainSceneBackground());
		Env.bgColorPy.addListener((x, y, z) -> updateMainSceneBackground());
		Env.pausedPy.addListener((x, y, z) -> updateStageTitle());

		return scene;
	}

	private void initGameLoop() {
		gameLoop.setUpdateTask(() -> {
			gameController.update();
			currentGameScene.onTick();
			Keyboard.clear();
		});
		gameLoop.setRenderTask(() -> {
			flashMessageView.update();
			dashboard.update();
			pipView.update();
			pipView.setVisible(Env.pipEnabledPy.get() && sceneManager.isPlayScene(currentGameScene));
		});
		gameLoop.pausedPy.bind(Env.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.timeMeasuredPy);
	}

	void updateGameScene(boolean forcedReload) {
		int dim = Env.threeDScenesPy.get() ? 3 : 2;
		var gameScene = sceneManager.selectGameScene(gameController, dim, currentGameScene, forcedReload);
		if (gameScene != currentGameScene) {
			setGameScene(gameScene);
		}
		pipView.init(gameScene.ctx());
	}

	private void updateStageTitle() {
		var paused = Env.pausedPy.get() ? " (paused)" : "";
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			stage.setTitle("Ms. Pac-Man" + paused);
			stage.getIcons().setAll(APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			stage.setTitle("Pac-Man" + paused);
			stage.getIcons().setAll(APP_ICON_PACMAN);
		}
		default -> throw new IllegalStateException();
		}
	}

	private void setGameScene(GameScene gameScene) {
		currentGameScene = gameScene;
		gameSceneParent.getChildren().setAll(gameScene.fxSubScene());
		gameScene.embedInto(stage.getScene());
		updateMainSceneBackground();
		updateStageTitle();
		LOGGER.trace("Game scene is now %s", gameScene);
	}

	private void updateMainSceneBackground() {
		var bgColor = Env.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.bgColorPy.get();
		var sceneRoot = (Region) stage.getScene().getRoot();
		sceneRoot.setBackground(Ufx.colorBackground(bgColor));
	}

	@Override
	public void onGameEvent(GameEvent event) {
		GameEventListener.super.onGameEvent(event);
		currentGameScene.onGameEvent(event);
		LOGGER.trace("Game UI received game event %s", event);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateGameScene(false);
		updateSound(gameController);
	}

	@Override
	public void onUIForceUpdate(GameEvent e) {
		updateGameScene(true);
	}

	private void updateSound(GameController gameController) {
		var gameVariant = gameController.game().variant();
		var sounds = Env.SOUND_DISABLED ? GameSounds.NO_SOUNDS : switch (gameVariant) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		};
		gameController.setSounds(sounds);
	}

	public void setPacSteering(Steering steering) {
		Objects.requireNonNull(steering);
		if (currentSteering instanceof KeyboardSteering keySteering) {
			stage.getScene().removeEventHandler(KeyEvent.KEY_PRESSED, keySteering::onKeyPressed);
		}
		currentSteering = steering;
		if (steering instanceof KeyboardSteering keySteering) {
			stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, keySteering::onKeyPressed);
		}
		gameController.setNormalSteering(currentSteering);
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Modifier.ALT, KeyCode.A)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.B) || Keyboard.pressed(KeyCode.F3)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.D)) {
			Env.toggle(Env.showDebugInfoPy);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.I)) {
			Actions.toggleImmunity();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.M)) {
			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.T)) {
			Actions.toggleLevelTestModel();
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
		currentGameScene.onKeyPressed();
	}

	public GameController gameController() {
		return gameController;
	}

	public Stage stage() {
		return stage;
	}

	public GameSceneManager sceneManager() {
		return sceneManager;
	}

	public GameLoop gameLoop() {
		return gameLoop;
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public Dashboard dashboard() {
		return dashboard;
	}
}