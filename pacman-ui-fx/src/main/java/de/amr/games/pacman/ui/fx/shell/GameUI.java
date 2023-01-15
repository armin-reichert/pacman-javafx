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
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
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
	private final Group gameSceneParent = new Group(); // single child is current game scenes' JavaFX subscene
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final PiPView pipView = new PiPView();
	private final KeyboardSteering manualPacSteering = new KeyboardSteering(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT,
			KeyCode.RIGHT);

	private GameScene currentGameScene;

	public GameUI(GameController gameController, Stage primaryStage, float zoom) {
		Objects.requireNonNull(gameController);
		Objects.requireNonNull(primaryStage);
		if (zoom < 0) {
			throw new IllegalArgumentException("Zoom value must not be negative but is " + zoom);
		}

		this.gameController = gameController;
		gameController.setManualPacSteering(manualPacSteering);

		GameEvents.addListener(this);
		Keyboard.addHandler(this::onKeyPressed);
		Actions.attachTo(this);
		dashboard.attachTo(this);
		configureGameLoop();

		stage = primaryStage;
		stage.setScene(createMainScene(zoom));
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.setOnCloseRequest(e -> endApp());
		stage.centerOnScreen();
		stage.show();

		Ufx.pause(0.5, Actions::playHelpVoiceMessage).play();
		gameController.boot();
	}

	private void endApp() {
		gameLoop.stop();
		LOGGER.trace("Application ended.");
	}

	private Scene createMainScene(float zoom) {
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(new VBox(pipView));

		var content = new StackPane(gameSceneParent, flashMessageView, overlayPane);
		var size = ArcadeWorld.SIZE_PX.toFloatVec().scaled(zoom);
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

		scene.addEventHandler(KeyEvent.KEY_PRESSED, manualPacSteering::onKeyPressed);

		return scene;
	}

	private void configureGameLoop() {
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

	// package visible such that Actions class can call it
	void updateGameScene(boolean forcedReload) {
		int dim = Env.threeDScenesPy.get() ? 3 : 2;
		var gameScene = sceneManager.selectGameScene(gameController, dim, currentGameScene, forcedReload);
		if (gameScene != currentGameScene) {
			setGameScene(gameScene);
			gameController.setSounds(Env.SOUND_DISABLED ? GameSounds.NO_SOUNDS : sounds());
			pipView.init(gameScene.ctx());
		}
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

	private Rendering2D renderer() {
		return switch (gameController.game().variant()) {
		case MS_PACMAN -> RendererMsPacManGame.THE_ONE_AND_ONLY;
		case PACMAN -> RendererPacManGame.THE_ONE_AND_ONLY;
		};
	}

	private GameSounds sounds() {
		return switch (gameController.game().variant()) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		};
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
	}

	@Override
	public void onUnspecifiedChange(GameEvent e) {
		updateGameScene(true);
	}

	@Override
	public void onLevelStarting(GameEvent e) {
		gameController.game().level().ifPresent(level -> {
			var r = renderer();
			level.pac().setAnimationSet(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimationSet(r.createGhostAnimations(ghost)));
			if (level.world() instanceof ArcadeWorld arcadeWorld) {
				arcadeWorld.setFlashingAnimation(r.createMazeFlashingAnimation());
			}
		});
		updateGameScene(true);
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