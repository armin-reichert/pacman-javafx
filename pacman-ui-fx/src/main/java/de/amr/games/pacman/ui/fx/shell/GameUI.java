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
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
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
 * JavaFX UI for Pac-Man and Ms. Pac-Man game.
 * <p>
 * The play scene is available in 2D and 3D. All others scenes are 2D only.
 * 
 * @author Armin Reichert
 */
public class GameUI implements GameEventListener {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final Image APP_ICON_PACMAN = Ufx.image("icons/pacman.png");
	private static final Image APP_ICON_MSPACMAN = Ufx.image("icons/mspacman.png");

	private final GameController gameController;
	private final Stage stage;
	private final GameLoop gameLoop = new GameLoop(GameModel.FPS);
	private final GameSceneManager sceneManager = new GameSceneManager();

	private Scene mainScene;
	private Group gameSceneParent;
	private BorderPane overlayPane;
	private Dashboard dashboard;
	private FlashMessageView flashMessageView;
	private PiPView pipView;
	private KeyboardSteering kbSteering;
	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage primaryStage, float zoom, boolean fullScreen) {
		this.gameController = Objects.requireNonNull(gameController);
		this.stage = Objects.requireNonNull(primaryStage);
		Keyboard.addHandler(this::onKeyPressed);
		GameEvents.addListener(this);
		Actions.setUI(this);
		createMainScene(zoom);
		configureStage(fullScreen);
		configureGameLoop();
		bindWithEnv();
	}

	public void setSteeringKeys(KeyCode keyUp, KeyCode keyDown, KeyCode keyLeft, KeyCode keyRight) {
		if (kbSteering != null) {
			mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, kbSteering::onKeyPressed);
		}
		kbSteering = new KeyboardSteering(keyUp, keyDown, keyLeft, keyRight);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, kbSteering::onKeyPressed);
		gameController.setManualPacSteering(kbSteering);
	}

	public void start() {
		gameController.boot();
		stage.centerOnScreen();
		stage.show();
		stage.requestFocus();
		playGreetingVoiceAfterSec(1.0);
		gameLoop().start();
	}

	public void playGreetingVoiceAfterSec(double sec) {
		Ufx.pause(sec, Actions::playHelpVoiceMessage).play();
	}

	private void configureGameLoop() {
		gameLoop.setUpdateTask(() -> {
			gameController.update();
			currentGameScene.onTick();
			Keyboard.clear();
		});
		gameLoop.setRenderTask(this::updateUI);
	}

	private void bindWithEnv() {
		Env.drawModePy.addListener((property, oldVal, newVal) -> updateMainSceneBackground());
		Env.bgColorPy.addListener((property, oldVal, newVal) -> updateMainSceneBackground());
		Env.pausedPy.addListener((property, oldVal, newVal) -> updateStageFrame());
		pipView.heightPy.bind(Env.pipSceneHeightPy);
		pipView.opacityProperty().bind(Env.pipOpacityPy);
		gameLoop.pausedPy.bind(Env.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.timeMeasuredPy);
	}

	private void configureStage(boolean fullScreen) {
		stage.setFullScreen(fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setOnCloseRequest(e -> {
			gameLoop.stop();
			LOGGER.info("Game loop stopped. Application closed.");
		});
		stage.setScene(mainScene);
	}

	private void createMainScene(float zoom) {
		if (zoom <= 0) {
			throw new IllegalArgumentException("Zoom value must be positive, but is " + zoom);
		}

		gameSceneParent = new Group(); // single child is current game scenes' JavaFX subscene
		flashMessageView = new FlashMessageView();
		overlayPane = new BorderPane();
		pipView = new PiPView(ArcadeWorld.SIZE_PX.toFloatVec(), 2.0f);
		dashboard = new Dashboard(this);
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView));

		var root = new StackPane();
		root.getChildren().addAll(gameSceneParent, flashMessageView, overlayPane);

		var size = ArcadeWorld.SIZE_PX.toFloatVec().scaled(zoom);
		mainScene = new Scene(root, size.x(), size.y());

		mainScene.setOnKeyPressed(Keyboard::processEvent);
		mainScene.heightProperty()
				.addListener((heightPy, oldHeight, newHeight) -> currentGameScene.resizeToHeight(newHeight.floatValue()));
	}

	private void updateUI() {
		flashMessageView.update();
		dashboard.update();
		pipView.setVisible(Env.pipVisiblePy.get() && sceneManager.isPlayScene(currentGameScene));
		if (pipView.isVisible()) {
			pipView.update();
		}
	}

	private void updateStageFrame() {
		var pausedText = Env.pausedPy.get() ? " (paused)" : "";
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			stage.setTitle("Ms. Pac-Man" + pausedText);
			stage.getIcons().setAll(APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			stage.setTitle("Pac-Man" + pausedText);
			stage.getIcons().setAll(APP_ICON_PACMAN);
		}
		default -> throw new IllegalStateException();
		}
	}

	private void updateMainSceneBackground() {
		var bgColor = Env.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.bgColorPy.get();
		var sceneRoot = (Region) mainScene.getRoot();
		sceneRoot.setBackground(Ufx.colorBackground(bgColor));
	}

	// public visible such that Actions class can call it
	public void updateGameScene(boolean reload) {
		int dim = Env.threeDScenesPy.get() ? 3 : 2;
		var gameScene = sceneManager.selectGameScene(gameController, dim, currentGameScene, reload);
		if (gameScene != currentGameScene) {
			currentGameScene = gameScene;
			gameSceneParent.getChildren().setAll(currentGameScene.fxSubScene());
			currentGameScene.embedInto(mainScene);
			pipView.setContext(currentGameScene.ctx());
			LOGGER.trace("Game scene is now: %s", gameScene);
		}
		updateSounds();
		updateMainSceneBackground();
		updateStageFrame();
	}

	private void updateSounds() {
		var variant = gameController.game().variant();
		var gameSounds = switch (variant) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		default -> throw new IllegalStateException();
		};
		var sounds = Env.SOUND_DISABLED ? GameSounds.NO_SOUNDS : gameSounds;
		gameController.setSounds(sounds);
		LOGGER.info("Using sounds for game variant %s", variant);
	}

	@Override
	public void onGameEvent(GameEvent event) {
		switch (event.type) {
		case GAME_STATE_CHANGED -> updateGameScene(false);
		case UNSPECIFIED_CHANGE -> updateGameScene(true);
		case LEVEL_STARTING -> onLevelStarting(event);
		default -> { // let current scene handle event
		}
		}
		currentGameScene.onGameEvent(event);
		LOGGER.trace("Game UI received game event %s", event);
	}

	// this is dubious but we need some point in time where the animations are created
	@Override
	public void onLevelStarting(GameEvent e) {
		gameController.game().level().ifPresent(level -> {
			var r = currentGameScene.ctx().r2D();
			level.pac().setAnimations(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
			if (level.world() instanceof ArcadeWorld arcadeWorld) {
				arcadeWorld.setFlashingAnimation(r.createMazeFlashingAnimation());
			}
		});
		updateGameScene(true);
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
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.T)) {
			Actions.toggleLevelTestMode();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.DIGIT3)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(KeyCode.F1)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(KeyCode.F2)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(KeyCode.F3)) {
			Actions.reboot();
		} else if (Keyboard.pressed(KeyCode.F11)) {
			stage.setFullScreen(true);
		}
		currentGameScene.onKeyPressed();
	}

	public GameController gameController() {
		return gameController;
	}

	public Scene mainScene() {
		return stage.getScene();
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

	public PiPView pipView() {
		return pipView;
	}
}