/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import java.util.EnumMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManTestRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene.GameSceneManager;
import de.amr.games.pacman.ui.fx.sound.common.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

	public static final double PIP_VIEW_MIN_HEIGHT = ArcadeWorld.SIZE_PX.y();
	public static final double PIP_VIEW_MAX_HEIGHT = ArcadeWorld.SIZE_PX.y() * 2;

	public class Simulation extends GameLoop {

		public Simulation() {
			super(GameModel.FPS);
		}

		@Override
		public void doUpdate() {
			gameController.update();
			currentGameScene.onTick();
		}

		@Override
		public void doRender() {
			flashMessageView.update();
			dashboard.update();
			pipView.update();
		}
	}

	/**
	 * Embedded 2D-view of the current play scene. Activated/deactivated by pressing key F2. Size and transparency can be
	 * controlled using the dashboard.
	 */
	private class PipView extends PlayScene2D {
		@Override
		public void update() {
			boolean visible = Env.PiP.visiblePy.get() && GameSceneManager.isPlayScene(currentGameScene);
			pipView.fxSubScene().setVisible(visible);
			if (visible) {
				pipView.setContext(currentGameScene.context());
				pipView.draw();
			}
		}
	}

	private final Stage stage;
	private final Scene mainScene;
	private final GameController gameController;
	private final Simulation simulation = new Simulation();
	private final SoundHandler soundHandler = new SoundHandler();
	private final Map<GameVariant, Rendering2D> rendererMap = new EnumMap<>(GameVariant.class);
	private KeyboardSteering manualSteering;
	private GameScene currentGameScene;
	private Dashboard dashboard;
	private FlashMessageView flashMessageView;
	private PipView pipView;

	public GameUI(Stage primaryStage, Settings settings) {
		gameController = new GameController(settings.variant);

		stage = primaryStage;
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);

		mainScene = createMainScene(ArcadeWorld.SIZE_PX, settings.zoom);
		stage.setScene(mainScene);

		rendererMap.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		rendererMap.put(GameVariant.PACMAN, settings.useTestRenderer ? new PacManTestRenderer() : new PacManGameRenderer());

		manualSteering = new KeyboardSteering( //
				settings.keyMap.get(Direction.UP), //
				settings.keyMap.get(Direction.DOWN), //
				settings.keyMap.get(Direction.LEFT), //
				settings.keyMap.get(Direction.RIGHT));

		gameController.setManualPacSteering(manualSteering);

		dashboard.init(this);
		initEnv(settings);
		GameEvents.addListener(this);
		Actions.setUI(this);

		LOG.info("Created game UI, Application settings: %s", settings);
	}

	private Scene createMainScene(Vector2i size, float zoom) {
		if (size.x() <= 0) {
			throw new IllegalArgumentException("Scene width must be positive but is: %d".formatted(size.x()));
		}
		if (size.y() <= 0) {
			throw new IllegalArgumentException("Scene height must be positive but is: %d".formatted(size.y()));
		}
		if (zoom <= 0) {
			throw new IllegalArgumentException("Zoom value must be positive but is: %.2f".formatted(zoom));
		}
		dashboard = new Dashboard();
		flashMessageView = new FlashMessageView();
		pipView = new PipView();
		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(pipView.fxSubScene());
		/* First child is placeholder for current game scene */
		var root = new StackPane(new Pane(), flashMessageView, overlayPane);

		var scene = new Scene(root, size.x() * zoom, size.y() * zoom);
		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.heightProperty()
				.addListener((heightPy, oldHeight, newHeight) -> currentGameScene.resizeToHeight(newHeight.floatValue()));

		return scene;
	}

	private void updateView() {
		var variant = gameController.game().variant();
		var paused = Env.Simulation.pausedPy.get();
		switch (variant) {
		case MS_PACMAN -> {
			var title = ResourceMgr.message(paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman");
			stage.setTitle(title);
			stage.getIcons().setAll(APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			var title = ResourceMgr.message(paused ? "app.title.pacman.paused" : "app.title.pacman");
			stage.setTitle(title);
			stage.getIcons().setAll(APP_ICON_PACMAN);
		}
		default -> throw new IllegalArgumentException("Unknown game variant: %s".formatted(variant));
		}
		var bgColor = Env.ThreeD.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.mainSceneBgColorPy.get();
		var sceneRoot = (Region) mainScene.getRoot();
		sceneRoot.setBackground(ResourceMgr.colorBackground(bgColor));
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.consume(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	private void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateView());

		Env.PiP.sceneHeightPy.addListener((py, oldVal, newVal) -> pipView.resizeToHeight(newVal.doubleValue()));
		pipView.fxSubScene().opacityProperty().bind(Env.PiP.opacityPy);

		Env.Simulation.pausedPy.addListener((py, oldVal, newVal) -> updateView());
		simulation.pausedPy.bind(Env.Simulation.pausedPy);
		simulation.targetFrameratePy.bind(Env.Simulation.targetFrameratePy);
		simulation.measuredPy.bind(Env.Simulation.timeMeasuredPy);

		Env.ThreeD.drawModePy.addListener((py, oldVal, newVal) -> updateView());
		Env.ThreeD.enabledPy.set(settings.use3D);
		Env.ThreeD.perspectivePy.set(settings.perspective);
	}

	public void start() {
		if (simulation.isRunning()) {
			LOG.info("Game has already been started");
			return;
		}
		Actions.reboot();
		simulation.start();
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
		LOG.info("Game started. Target frame rate: %d", simulation.targetFrameratePy.get());
		LOG.info("Window size: %.0f x %.0f, 3D: %s, perspective: %s".formatted(stage.getWidth(), stage.getHeight(),
				Env.ThreeD.enabledPy.get(), Env.ThreeD.perspectivePy.get()));
	}

	public void stop() {
		simulation.stop();
		LOG.info("Game stopped");
	}

	// public visible such that Actions class can call it
	public void updateGameScene(boolean reload) {
		var matching = GameSceneManager.sceneSelectionMatchingCurrentGameState(gameController);
		var use3D = Env.ThreeD.enabledPy.get();
		var nextGameScene = (use3D && matching.scene3D() != null) ? matching.scene3D() : matching.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateView();
	}

	private void changeGameScene(GameScene nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		var renderer = rendererMap.get(gameController.game().variant());
		var sceneContext = new GameSceneContext(gameController, renderer);
		nextGameScene.setContext(sceneContext);
		nextGameScene.init();
		updateManualPacManSteering(nextGameScene);
		var root = (StackPane) mainScene.getRoot();
		root.getChildren().set(0, nextGameScene.fxSubScene());
		nextGameScene.onEmbed(mainScene);
		currentGameScene = nextGameScene;
		LOG.trace("Game scene changed to %s".formatted(nextGameScene));
	}

	private void updateManualPacManSteering(GameScene gameScene) {
		boolean enabled = false;
		mainScene.removeEventHandler(KeyEvent.KEY_PRESSED, manualSteering);
		if (gameScene instanceof PlayScene2D || gameScene instanceof PlayScene3D) {
			mainScene.addEventHandler(KeyEvent.KEY_PRESSED, manualSteering);
			enabled = true;
		}
		LOG.trace("Manual Pac-Man steering is %s", enabled ? "enabled" : "disabled");
	}

	private void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.AUTOPILOT)) {
			Actions.toggleAutopilot();
		} else if (Keyboard.pressed(Keys.BOOT)) {
			Actions.reboot();
		} else if (Keyboard.pressed(Keys.DEBUG_INFO)) {
			Ufx.toggle(Env.showDebugInfoPy);
		} else if (Keyboard.pressed(Keys.IMMUNITIY)) {
			Actions.toggleImmunity();
//		} else if (Keyboard.pressed(Keys.MUTE)) {
//			Actions.toggleSoundMuted();
		} else if (Keyboard.pressed(Keys.PAUSE)) {
			Actions.togglePaused();
		} else if (Keyboard.pressed(Keys.PAUSE_STEP) || Keyboard.pressed(Keys.SINGLE_STEP)) {
			Actions.oneSimulationStep();
		} else if (Keyboard.pressed(Keys.TEN_STEPS)) {
			Actions.tenSimulationSteps();
		} else if (Keyboard.pressed(Keys.QUIT)) {
			Actions.restartIntro();
		} else if (Keyboard.pressed(Keys.TEST_LEVELS)) {
			Actions.startLevelTestMode();
		} else if (Keyboard.pressed(Keys.USE_3D)) {
			Actions.toggleUse3DScene();
		} else if (Keyboard.pressed(Keys.DASHBOARD) || Keyboard.pressed(Keys.DASHBOARD2)) {
			Actions.toggleDashboardVisible();
		} else if (Keyboard.pressed(Keys.PIP_VIEW)) {
			Actions.togglePipViewVisible();
		} else if (Keyboard.pressed(Keys.FULLSCREEN)) {
			stage.setFullScreen(true);
		}
		currentGameScene.handleKeyboardInput();
	}

	@Override
	public void onGameEvent(GameEvent e) {
		LOG.trace("Event received: %s", e);
		// call event specific handler
		GameEventListener.super.onGameEvent(e);
		if (currentGameScene != null) {
			currentGameScene.onGameEvent(e);
		}
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
		e.game.level().ifPresent(level -> {
			var r = currentGameScene.context().r2D();
			level.pac().setAnimations(r.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(r.createGhostAnimations(ghost)));
			level.world().setAnimations(r.createWorldAnimations(level.world()));
			LOG.trace("Created creature and world animations for level #%d", level.number());
		});
		updateGameScene(true);
	}

	@Override
	public void onSoundEvent(SoundEvent e) {
		soundHandler.onSoundEvent(e);
	}

	public GameController gameController() {
		return gameController;
	}

	public Scene mainScene() {
		return mainScene;
	}

	public Simulation simulation() {
		return simulation;
	}

	public Dashboard dashboard() {
		return dashboard;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}
}