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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManTestRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.BootScene;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManCreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacManIntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene1;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene2;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene3;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManIntroScene;
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
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The play scene is available in 2D and 3D. All others scenes are 2D only.
 * 
 * @author Armin Reichert
 */
public class GameUI implements GameEventListener {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public static final double PIP_MIN_HEIGHT = World.t(36);
	public static final double PIP_MAX_HEIGHT = 2 * PIP_MIN_HEIGHT;

	private static final int BOOT_SCENE_INDEX = 0;
	private static final int INTRO_SCENE_INDEX = 1;
	private static final int CREDIT_SCENE_INDEX = 2;
	private static final int PLAY_SCENE_INDEX = 3;

	private record GameSceneSelection(GameScene scene2D, GameScene scene3D) {
	}

	public class Simulation extends GameLoop {

		public Simulation() {
			super(GameModel.FPS);
		}

		@Override
		public void doUpdate() {
			gameController.update();
			currentGameScene.update();
		}

		@Override
		public void doRender() {
			flashMessageView.update();
			dashboard.update();
			updatePiPView();
			currentGameScene.render();
		}
	}

	private final Stage stage;
	private final Scene mainScene;
	private final GameController gameController;
	private final Simulation simulation = new Simulation();
	private final SoundHandler soundHandler = new SoundHandler();
	private final Map<GameVariant, Rendering2D> rendererMap = new EnumMap<>(GameVariant.class);
	private PlayScene2D pipViewScene;
	private KeyboardSteering manualSteering;
	private final GameSceneSelection[] scenesMsPacMan;
	private final GameSceneSelection[] scenesPacMan;
	private GameScene currentGameScene;
	private Dashboard dashboard;
	private FlashMessageView flashMessageView;

	public GameUI(Stage primaryStage, Settings settings) {
		gameController = new GameController(settings.variant);

		stage = primaryStage;
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);

		mainScene = createMainScene(settings);
		stage.setScene(mainScene);

		rendererMap.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		rendererMap.put(GameVariant.PACMAN, settings.useTestRenderer ? new PacManTestRenderer() : new PacManGameRenderer());

		//@formatter:off
		scenesMsPacMan = new GameSceneSelection[] {
			new GameSceneSelection(createScene2D(BootScene.class), null),
			new GameSceneSelection(createScene2D(PacManIntroScene.class), null),
			new GameSceneSelection(createScene2D(PacManCreditScene.class), null),
			new GameSceneSelection(createScene2D(PlayScene2D.class), new PlayScene3D(gameController)),
			new GameSceneSelection(createScene2D(PacManCutscene1.class), null),
			new GameSceneSelection(createScene2D(PacManCutscene2.class), null),
			new GameSceneSelection(createScene2D(PacManCutscene3.class), null),
		};

		scenesPacMan = new GameSceneSelection[] { 
			new GameSceneSelection(createScene2D(BootScene.class), null),
			new GameSceneSelection(createScene2D(MsPacManIntroScene.class), null),
			new GameSceneSelection(createScene2D(MsPacManCreditScene.class), null),
			new GameSceneSelection(createScene2D(PlayScene2D.class), new PlayScene3D(gameController)),
			new GameSceneSelection(createScene2D(MsPacManIntermissionScene1.class), null),
			new GameSceneSelection(createScene2D(MsPacManIntermissionScene2.class), null),
			new GameSceneSelection(createScene2D(MsPacManIntermissionScene3.class), null),
		};

		manualSteering = new KeyboardSteering(
			settings.keyMap.get(Direction.UP),
			settings.keyMap.get(Direction.DOWN),
			settings.keyMap.get(Direction.LEFT),
			settings.keyMap.get(Direction.RIGHT)
		);
		//@formatter:on
		gameController.setManualPacSteering(manualSteering);

		dashboard.init(this);
		initEnv(settings);
		GameEvents.addListener(this);
		Actions.setUI(this);

		LOG.info("Created game UI, Locale: %s, Application settings: %s", Locale.getDefault(), settings);
	}

	private Scene createMainScene(Settings settings) {
		dashboard = new Dashboard();
		flashMessageView = new FlashMessageView();
		pipViewScene = new PlayScene2D(gameController);

		var overlayPane = new BorderPane();
		overlayPane.setLeft(dashboard);
		overlayPane.setRight(pipViewScene.fxSubScene());
		var gameScenePlaceholder = new Pane();
		var root = new StackPane(gameScenePlaceholder, flashMessageView, overlayPane);

		var size = ArcadeWorld.SIZE_PX.toFloatVec().scaled(settings.zoom);
		var scene = new Scene(root, size.x(), size.y());
		scene.heightProperty().addListener((heightPy, oldHeight, newHeight) -> currentGameScene.onParentSceneResize(scene));

		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToOptimalSize();
			}
		});

		return scene;
	}

	private void resizeStageToOptimalSize() {
		if (currentGameScene != null && !currentGameScene.is3D() && !stage.isFullScreen()) {
			stage.setWidth(currentGameScene.fxSubScene().getWidth() + 16);
		}
	}

	private void updateView() {
		var variant = gameController.game().variant();
		var paused = Env.Simulation.pausedPy.get();
		var dimension = ResourceMgr.message(Env.ThreeD.enabledPy.get() ? "threeD" : "twoD");
		switch (variant) {
		case MS_PACMAN -> {
			var title = ResourceMgr.message(paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman", dimension);
			stage.setTitle(title);
			stage.getIcons().setAll(ResourceMgr.APP_ICON_MSPACMAN);
		}
		case PACMAN -> {
			var title = ResourceMgr.message(paused ? "app.title.pacman.paused" : "app.title.pacman", dimension);
			stage.setTitle(title);
			stage.getIcons().setAll(ResourceMgr.APP_ICON_PACMAN);
		}
		default -> throw new IllegalArgumentException("Unknown game variant: %s".formatted(variant));
		}
		var bgColor = Env.ThreeD.drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.mainSceneBgColorPy.get();
		var sceneRoot = (Region) mainScene.getRoot();
		sceneRoot.setBackground(ResourceMgr.colorBackground(bgColor));
	}

	/**
	 * Embedded 2D-view of the current play scene. Activated/deactivated by pressing key F2. Size and transparency can be
	 * controlled using the dashboard.
	 */
	private void updatePiPView() {
		if (Env.PiP.visiblePy.get() && isPlayScene(currentGameScene)) {
			pipViewScene.fxSubScene().setVisible(true);
			pipViewScene.context().setCreditVisible(false);
			pipViewScene.context().setScoreVisible(true);
			pipViewScene.context().setRendering2D(currentGameScene.context().rendering2D());
			pipViewScene.render();
		} else {
			pipViewScene.fxSubScene().setVisible(false);
		}
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.consume(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	private void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateView());

		Env.PiP.sceneHeightPy.addListener((py, oldVal, newVal) -> pipViewScene.resize(newVal.doubleValue()));
		pipViewScene.fxSubScene().opacityProperty().bind(Env.PiP.opacityPy);

		Env.Simulation.pausedPy.addListener((py, oldVal, newVal) -> updateView());
		simulation.pausedPy.bind(Env.Simulation.pausedPy);
		simulation.targetFrameratePy.bind(Env.Simulation.targetFrameratePy);
		simulation.measuredPy.bind(Env.Simulation.timeMeasuredPy);

		Env.ThreeD.drawModePy.addListener((py, oldVal, newVal) -> updateView());
		Env.ThreeD.enabledPy.addListener((py, oldVal, newVal) -> updateView());
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

	/**
	 * @param dimension scene dimension (2 or 3)
	 * @return (optional) game scene matching current game state and specified dimension
	 */
	public Optional<GameScene> findGameScene(int dimension) {
		if (dimension != 2 && dimension != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dimension));
		}
		var matching = sceneSelectionMatchingCurrentGameState();
		return Optional.ofNullable(dimension == 3 ? matching.scene3D() : matching.scene2D());
	}

	private GameScene2D createScene2D(Class<? extends GameScene2D> clazz) {
		try {
			GameScene2D scene2D = clazz.getDeclaredConstructor(GameController.class).newInstance(gameController);
			scene2D.infoVisiblePy.bind(Env.showDebugInfoPy);
			LOG.trace("2D game scene created: '%s'", scene2D.getClass().getName());
			return scene2D;
		} catch (Exception e) {
			LOG.error("Could not create 2D game scene of class '%s'", clazz.getName());
			throw new IllegalArgumentException(e);
		}
	}

	private boolean isPlayScene(GameScene gameScene) {
		return gameScene == scenesPacMan[PLAY_SCENE_INDEX].scene2D()
				|| gameScene == scenesPacMan[PLAY_SCENE_INDEX].scene3D()
				|| gameScene == scenesMsPacMan[PLAY_SCENE_INDEX].scene2D()
				|| gameScene == scenesMsPacMan[PLAY_SCENE_INDEX].scene3D();
	}

	private GameSceneSelection sceneSelectionMatchingCurrentGameState() {
		int index = switch (gameController.state()) {
		case BOOT -> BOOT_SCENE_INDEX;
		case CREDIT -> CREDIT_SCENE_INDEX;
		case INTRO -> INTRO_SCENE_INDEX;
		case GAME_OVER, GHOST_DYING, HUNTING, LEVEL_COMPLETE, LEVEL_TEST, CHANGING_TO_NEXT_LEVEL, PACMAN_DYING, READY -> PLAY_SCENE_INDEX;
		case INTERMISSION -> PLAY_SCENE_INDEX + gameController.game().level().get().intermissionNumber;
		case INTERMISSION_TEST -> PLAY_SCENE_INDEX + gameController.game().intermissionTestNumber;
		default -> throw new IllegalArgumentException("Unknown game state: %s".formatted(gameController.state()));
		};
		return switch (gameController.game().variant()) {
		case MS_PACMAN -> scenesPacMan[index];
		case PACMAN -> scenesMsPacMan[index];
		};
	}

	public void updateGameScene(boolean reload) {
		var matching = sceneSelectionMatchingCurrentGameState();
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
		nextGameScene.context().setRendering2D(renderer);
		nextGameScene.init();
		updateManualPacManSteering(nextGameScene);
		var root = (StackPane) mainScene.getRoot();
		root.getChildren().set(0, nextGameScene.fxSubScene());
		nextGameScene.onEmbedIntoParentScene(mainScene);
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
			var r = currentGameScene.context().rendering2D();
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