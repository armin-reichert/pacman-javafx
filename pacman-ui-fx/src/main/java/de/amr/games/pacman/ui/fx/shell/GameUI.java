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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.input.Modifier;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final Image APP_ICON_PACMAN = ResourceMgr.image("icons/pacman.png");
	private static final Image APP_ICON_MSPACMAN = ResourceMgr.image("icons/mspacman.png");

	private static final float PIP_VIEW_MIN_HEIGHT = ArcadeWorld.SIZE_PX.y();

	private final GameController gameController;
	private final Stage stage;
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final PipView pipView = new PipView();

	private final GameLoop gameLoop = new GameLoop(GameModel.FPS) {
		@Override
		public void doUpdate() {
			gameController.update();
			currentGameScene.onTick();
			Keyboard.clear();
		}

		@Override
		public void doRender() {
			flashMessageView.update();
			dashboard.update();
			pipView.update(currentGameScene);
		}
	};

	private Scene mainScene;
	private GameScene currentGameScene;

	public GameUI(Stage primaryStage, Settings settings) {
		stage = primaryStage;
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		createAndSetMainScene(ArcadeWorld.SIZE_PX.x(), ArcadeWorld.SIZE_PX.y(), settings.zoom);
		var kbSteering = new KeyboardSteering(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, kbSteering::onKeyPressed);
		gameController = new GameController(settings.variant);
		gameController.setManualPacSteering(kbSteering);
		Keyboard.addHandler(this::onKeyPressed);
		GameEvents.addListener(this);
		Actions.setUI(this);
		initEnv(settings);
		dashboard.init(this);
		LOG.info("Created game UI, Application settings: %s", settings);
	}

	private void createAndSetMainScene(int width, int height, float zoom) {
		if (zoom <= 0) {
			throw new IllegalArgumentException("Zoom value must be positive but is: %.2f".formatted(zoom));
		}
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(pipView);
		var root = new StackPane(new Group() /* placeholder for current game scene */, flashMessageView, overlayPane);
		mainScene = new Scene(root, width * zoom, height * zoom);
		mainScene.setOnKeyPressed(Keyboard::processEvent);
		mainScene.heightProperty()
				.addListener((heightPy, oldHeight, newHeight) -> currentGameScene.resizeToHeight(newHeight.floatValue()));
		stage.setScene(mainScene);
	}

	public void start() {
		Ufx.afterSeconds(1.0, Actions::playHelpVoiceMessage).play();
		gameController.boot(); // after booting, current game scene is initialized
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
		gameLoop().start();
		LOG.info("Game started. Game loop target frame rate: %d", gameLoop.getTargetFramerate());
		LOG.info("Window size: %.0f x %.0f, 3D: %s, perspective: %s".formatted(stage.getWidth(), stage.getHeight(),
				U.onOff(Env.ThreeD.enabledPy.get()), Env.ThreeD.perspectivePy.get()));
	}

	public void stop() {
		gameLoop.stop();
		LOG.info("Game stopped");
	}

	private void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateMainSceneBackground());

		Env.ThreeD.drawModePy.addListener((py, oldVal, newVal) -> updateMainSceneBackground());
		Env.ThreeD.enabledPy.set(settings.use3D);
		Env.ThreeD.perspectivePy.set(settings.perspective);

		Env.PiP.sceneHeightPy.addListener((py, oldVal, newVal) -> pipView.resizeToHeight(newVal.doubleValue()));
		pipView.opacityProperty().bind(Env.PiP.opacityPy);

		Env.Simulation.pausedPy.addListener((py, oldVal, newVal) -> updateStageFrame());
		gameLoop.pausedPy.bind(Env.Simulation.pausedPy);
		gameLoop.targetFrameratePy.bind(Env.Simulation.targetFrameratePy);
		gameLoop.measuredPy.bind(Env.Simulation.timeMeasuredPy);
	}

	private void updateStageFrame() {
		var pausedText = Env.Simulation.pausedPy.get() ? " (paused)" : "";
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
		var bgColor = Env.ThreeD.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.mainSceneBgColorPy.get();
		var sceneRoot = (Region) mainScene.getRoot();
		sceneRoot.setBackground(ResourceMgr.colorBackground(bgColor));
	}

	// public visible such that Actions class can call it
	public void updateGameScene(boolean reload) {
		var use3D = Env.ThreeD.enabledPy.get();
		var variants = GameSceneManager.getSceneVariantsMatchingGameState(gameController);
		var nextGameScene = (use3D && variants.scene3D() != null) ? variants.scene3D() : variants.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found.");
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateSounds();
		updateMainSceneBackground();
		updateStageFrame();
	}

	private void changeGameScene(GameScene nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		GameSceneManager.setSceneContext(gameController, nextGameScene);
		nextGameScene.init();
		currentGameScene = nextGameScene;
		// embed game scene into main scene
		StackPane root = (StackPane) mainScene.getRoot();
		root.getChildren().set(0, currentGameScene.fxSubScene());
		currentGameScene.embedInto(mainScene);
	}

	private void updateSounds() {
		var variant = gameController.game().variant();
		var gameSounds = switch (variant) {
		case MS_PACMAN -> GameSounds.MS_PACMAN_SOUNDS;
		case PACMAN -> GameSounds.PACMAN_SOUNDS;
		default -> throw new IllegalStateException();
		};
		gameController.setSounds(gameSounds);
		LOG.info("Using sounds for game variant %s", variant);
	}

	@Override
	public void onGameEvent(GameEvent event) {
		LOG.trace("Game UI received game event %s", event);
		switch (event.type) {
		case GAME_STATE_CHANGED -> updateGameScene(false);
		case LEVEL_STARTING -> {
			gameController.game().level().ifPresent(this::createLevelAnimations);
			updateGameScene(true);
		}
		case UNSPECIFIED_CHANGE -> updateGameScene(true);
		default -> {
			// ignore
		}
		}
		currentGameScene.onGameEvent(event);
	}

	private void createLevelAnimations(GameLevel level) {
		var r = currentGameScene.ctx().r2D();
		level.pac().setAnimations(r.createPacAnimations(level.pac()));
		level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
		level.world().addAnimation("flashing", r.createMazeFlashingAnimation());
		LOG.trace("Created level animations for level #%d", level.number());
	}

	private void onKeyPressed() {
		if (Keyboard.pressed(Modifier.ALT, KeyCode.A)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.B)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.D)) {
			Ufx.toggle(Env.showDebugInfoPy);
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

	public double pipViewMinHeight() {
		return PIP_VIEW_MIN_HEIGHT;
	}

	public double pipViewMaxHeight() {
		return pipViewMinHeight() * 2;
	}

	/**
	 * Picture-in-picture view.
	 * <p>
	 * Displays a mini 2D view of the running play scene when activated (key F2). Size and transparency can be controlled
	 * using the dashboard.
	 */
	private static class PipView extends Pane {

		private final PlayScene2D playScene;

		public PipView() {
			playScene = new PlayScene2D();
			playScene.resizeToHeight(PIP_VIEW_MIN_HEIGHT);
			getChildren().add(playScene.fxSubScene());
			setFocusTraversable(false);
		}

		public void resizeToHeight(double height) {
			playScene.resizeToHeight(height);
		}

		public void update(GameScene currentGameScene) {
			boolean visible = Env.PiP.visiblePy.get() && GameSceneManager.isPlayScene(currentGameScene);
			setVisible(visible);
			if (visible) {
				LOG.trace("Update PiP view");
				playScene.setContext(currentGameScene.ctx());
				playScene.clear();
				playScene.drawSceneContent();
				playScene.drawHUD();
			}
		}
	}
}