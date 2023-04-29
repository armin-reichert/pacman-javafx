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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx._2d.rendering.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.PacManTestRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.scene.BootScene;
import de.amr.games.pacman.ui.fx._2d.scene.GameScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.MsPacManCreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.MsPacManIntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.MsPacManIntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.MsPacManIntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.MsPacManIntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.PacManCreditScene;
import de.amr.games.pacman.ui.fx._2d.scene.PacManCutscene1;
import de.amr.games.pacman.ui.fx._2d.scene.PacManCutscene2;
import de.amr.games.pacman.ui.fx._2d.scene.PacManCutscene3;
import de.amr.games.pacman.ui.fx._2d.scene.PacManIntroScene;
import de.amr.games.pacman.ui.fx._2d.scene.PlayScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.AppRes.Graphics;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.dashboard.Dashboard;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
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

	private static final byte TILES_X = 28;
	private static final byte TILES_Y = 36;

	public static final float PIP_MIN_HEIGHT = TILES_Y * 8;
	public static final float PIP_MAX_HEIGHT = 2.5f * PIP_MIN_HEIGHT;

	private static final byte INDEX_BOOT_SCENE = 0;
	private static final byte INDEX_INTRO_SCENE = 1;
	private static final byte INDEX_CREDIT_SCENE = 2;
	private static final byte INDEX_PLAY_SCENE = 3;

	private static final Background BACKGROUND_3D = new Background(
			new BackgroundImage(Graphics.skyImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, null));

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
			pipGameScene.render();
			currentGameScene.render();
		}
	}

	private static Image appIcon(GameVariant variant) {
		return switch (variant) {
		case MS_PACMAN -> AppRes.Graphics.iconMsPacManGame;
		case PACMAN -> AppRes.Graphics.iconPacManGame;
		default -> throw new IllegalGameVariantException(variant);
		};
	}

	private final GameController gameController;
	private final Simulation simulation = new Simulation();
	private final Map<GameVariant, Rendering2D> renderers = new EnumMap<>(GameVariant.class);
	private final Map<GameVariant, List<GameSceneSelection>> scenes = new EnumMap<>(GameVariant.class);
	private final Stage stage;
	private final StackPane root = new StackPane();
	private final PlayScene2D pipGameScene;
	private final Dashboard dashboard = new Dashboard();
	private final FlashMessageView flashMessageView = new FlashMessageView();

	private GameScene currentGameScene;

	public GameUI(final Stage stage, final Settings settings) {
		this.stage = Objects.requireNonNull(stage);
		Objects.requireNonNull(settings);

		// game controller
		gameController = new GameController(settings.variant);
		var keyboardSteering = new KeyboardSteering(//
				settings.keyMap.get(Direction.UP), settings.keyMap.get(Direction.DOWN), //
				settings.keyMap.get(Direction.LEFT), settings.keyMap.get(Direction.RIGHT));
		gameController.setManualPacSteering(keyboardSteering);

		// renderers must be created before game scenes
		renderers.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		renderers.put(GameVariant.PACMAN, settings.useTestRenderer ? new PacManTestRenderer() : new PacManGameRenderer());

		// game scenes
		createGameScenes();
		pipGameScene = new PlayScene2D(gameController);

		// main scene
		var mainScene = createMainScene(TILES_X * 8 * settings.zoom, TILES_Y * 8 * settings.zoom);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
		stage.setScene(mainScene);

		GameEvents.addListener(this);
		dashboard.populate(this);
		initEnv(settings);
		Actions.setUI(this);
		Actions.reboot();

		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();

		Logger.info("Game UI created. Locale: {}. Application settings: {}", Locale.getDefault(), settings);
		Logger.info("Window size: {} x {}, 3D: {}, perspective: {}", stage.getWidth(), stage.getHeight(),
				Env.d3_enabledPy.get(), Env.d3_perspectivePy.get());
	}

	private void createGameScenes() {
		//@formatter:off
		scenes.put(GameVariant.PACMAN, List.of(
			new GameSceneSelection(createScene2D(BootScene.class), null),
			new GameSceneSelection(createScene2D(PacManIntroScene.class), null),
			new GameSceneSelection(createScene2D(PacManCreditScene.class), null),
			new GameSceneSelection(createScene2D(PlayScene2D.class), new PlayScene3D(gameController)),
			new GameSceneSelection(createScene2D(PacManCutscene1.class), null),
			new GameSceneSelection(createScene2D(PacManCutscene2.class), null),
			new GameSceneSelection(createScene2D(PacManCutscene3.class), null)
		));

		scenes.put(GameVariant.MS_PACMAN, List.of(
			new GameSceneSelection(createScene2D(BootScene.class), null),
			new GameSceneSelection(createScene2D(MsPacManIntroScene.class), null),
			new GameSceneSelection(createScene2D(MsPacManCreditScene.class), null),
			new GameSceneSelection(createScene2D(PlayScene2D.class), new PlayScene3D(gameController)),
			new GameSceneSelection(createScene2D(MsPacManIntermissionScene1.class), null),
			new GameSceneSelection(createScene2D(MsPacManIntermissionScene2.class), null),
			new GameSceneSelection(createScene2D(MsPacManIntermissionScene3.class), null)
		));
		//@formatter:on
	}

	private Scene createMainScene(float sizeX, float sizeY) {
		var scene = new Scene(root, sizeX, sizeY);
		scene.heightProperty().addListener((py, ov, nv) -> currentGameScene.onParentSceneResize(scene));
		scene.setOnKeyPressed(this::handleKeyPressed);
		scene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToOptimalSize();
			}
		});
		var topLayer = new BorderPane();
		topLayer.setLeft(dashboard);
		topLayer.setRight(pipGameScene.fxSubScene());

		root.getChildren().add(new Label("Game scene comes here"));
		root.getChildren().add(flashMessageView);
		root.getChildren().add(topLayer);

		return scene;
	}

	private void resizeStageToOptimalSize() {
		if (currentGameScene != null && !currentGameScene.is3D() && !stage.isFullScreen()) {
			stage.setWidth(currentGameScene.fxSubScene().getWidth() + 16); // don't ask me why
		}
	}

	private void updateMainView() {
		var variant = gameController.game().variant();
		var paused = Env.simulationPausedPy.get();
		var dimensionMsg = AppRes.Texts.message(Env.d3_enabledPy.get() ? "threeD" : "twoD");
		var messageKey = switch (variant) {
		case MS_PACMAN -> paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
		case PACMAN -> paused ? "app.title.pacman.paused" : "app.title.pacman";
		default -> throw new IllegalGameVariantException(variant);
		};
		stage.getIcons().setAll(appIcon(variant));
		stage.setTitle(AppRes.Texts.message(messageKey, dimensionMsg));
		var bgColor = Env.d3_drawModePy.get() == DrawMode.LINE ? Color.BLACK : Env.mainSceneBgColorPy.get();
		root.setBackground(ResourceMgr.colorBackground(bgColor));
		if (currentGameScene != null && currentGameScene.is3D()) {
			root.setBackground(BACKGROUND_3D);
		}
	}

	/**
	 * The picture-in-picture view shows the 2D version of the current game scene (in case this is the play scene). It is
	 * activated/deactivated by pressing key F2. Size and transparency can be controlled using the dashboard.
	 */
	private void updatePictureInPictureView() {
		boolean visible = Env.pipVisiblePy.get() && isPlayScene(currentGameScene);
		pipGameScene.fxSubScene().setVisible(visible);
		pipGameScene.context().setCreditVisible(false);
		pipGameScene.context().setScoreVisible(true);
		pipGameScene.context().setRendering2D(currentGameScene.context().rendering2D());
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	private void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateMainView());

		Env.pipVisiblePy.addListener((py, oldVal, newVal) -> updatePictureInPictureView());
		Env.pipSceneHeightPy.addListener((py, oldVal, newVal) -> pipGameScene.resize(newVal.doubleValue()));
		pipGameScene.fxSubScene().opacityProperty().bind(Env.pipOpacityPy);

		Env.simulationPausedPy.addListener((py, oldVal, newVal) -> updateMainView());
		simulation.pausedPy.bind(Env.simulationPausedPy);
		simulation.targetFrameratePy.bind(Env.simulationSpeedPy);
		simulation.measuredPy.bind(Env.simulationTimeMeasuredPy);

		Env.d3_drawModePy.addListener((py, oldVal, newVal) -> updateMainView());
		Env.d3_enabledPy.addListener((py, oldVal, newVal) -> updateMainView());
		Env.d3_enabledPy.set(settings.use3D);
		Env.d3_perspectivePy.set(settings.perspective);
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
			Logger.trace("2D game scene created: '{}'", scene2D.getClass().getName());
			return scene2D;
		} catch (Exception e) {
			Logger.error("Could not create 2D game scene of class '{}'", clazz.getName());
			throw new IllegalArgumentException(e);
		}
	}

	private boolean isPlayScene(GameScene gameScene) {
		return gameScene == scenes.get(GameVariant.PACMAN).get(INDEX_PLAY_SCENE).scene2D()
				|| gameScene == scenes.get(GameVariant.PACMAN).get(INDEX_PLAY_SCENE).scene3D()
				|| gameScene == scenes.get(GameVariant.MS_PACMAN).get(INDEX_PLAY_SCENE).scene2D()
				|| gameScene == scenes.get(GameVariant.MS_PACMAN).get(INDEX_PLAY_SCENE).scene3D();
	}

	private GameSceneSelection sceneSelectionMatchingCurrentGameState() {
		var game = gameController.game();
		var gameState = gameController.state();
		int index = switch (gameState) {
		case BOOT -> INDEX_BOOT_SCENE;
		case CREDIT -> INDEX_CREDIT_SCENE;
		case INTRO -> INDEX_INTRO_SCENE;
		case GAME_OVER, GHOST_DYING, HUNTING, LEVEL_COMPLETE, LEVEL_TEST, CHANGING_TO_NEXT_LEVEL, PACMAN_DYING, READY -> INDEX_PLAY_SCENE;
		case INTERMISSION -> INDEX_PLAY_SCENE + game.level().orElseThrow(IllegalStateException::new).intermissionNumber;
		case INTERMISSION_TEST -> INDEX_PLAY_SCENE + game.intermissionTestNumber;
		default -> throw new IllegalArgumentException("Unknown game state: %s".formatted(gameState));
		};
		return scenes.get(game.variant()).get(index);
	}

	public void updateGameScene(boolean reload) {
		var matching = sceneSelectionMatchingCurrentGameState();
		var use3D = Env.d3_enabledPy.get();
		var nextGameScene = (use3D && matching.scene3D() != null) ? matching.scene3D() : matching.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updatePictureInPictureView();
		updateMainView();
	}

	private void changeGameScene(GameScene nextGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		var renderer = renderers.get(gameController.game().variant());
		nextGameScene.context().setRendering2D(renderer);
		nextGameScene.init();
		root.getChildren().set(0, nextGameScene.fxSubScene());
		nextGameScene.onEmbedIntoParentScene(mainScene());
		currentGameScene = nextGameScene;
		Logger.trace("Game scene changed to {}", nextGameScene);
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
		} else if (Keyboard.pressed(Keys.SIMULATION_FASTER)) {
			Actions.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(Keys.SIMULATION_SLOWER)) {
			Actions.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(Keys.SIMULATION_NORMAL)) {
			Actions.resetSimulationSpeed();
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
		Logger.trace("Event received: {}", e);
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
			Logger.trace("Created creature and world animations for level #{}", level.number());
		});
		updateGameScene(true);
	}

	@Override
	public void onSoundEvent(SoundEvent event) {
		var sounds = AppRes.Sounds.soundClips(event.game.variant());
		switch (event.id) {
		case GameModel.SE_BONUS_EATEN -> sounds.play(AudioClipID.BONUS_EATEN);
		case GameModel.SE_CREDIT_ADDED -> sounds.play(AudioClipID.CREDIT);
		case GameModel.SE_EXTRA_LIFE -> sounds.play(AudioClipID.EXTRA_LIFE);
		case GameModel.SE_GHOST_EATEN -> sounds.play(AudioClipID.GHOST_EATEN);
		case GameModel.SE_HUNTING_PHASE_STARTED_0 -> sounds.ensureSirenStarted(0);
		case GameModel.SE_HUNTING_PHASE_STARTED_2 -> sounds.ensureSirenStarted(1);
		case GameModel.SE_HUNTING_PHASE_STARTED_4 -> sounds.ensureSirenStarted(2);
		case GameModel.SE_HUNTING_PHASE_STARTED_6 -> sounds.ensureSirenStarted(3);
		case GameModel.SE_READY_TO_PLAY -> sounds.play(AudioClipID.GAME_READY);
		case GameModel.SE_PACMAN_DEATH -> sounds.play(AudioClipID.PACMAN_DEATH);
		// TODO this does not sound as in the original game
		case GameModel.SE_PACMAN_FOUND_FOOD -> sounds.ensureLoop(AudioClipID.PACMAN_MUNCH, AudioClip.INDEFINITE);
		case GameModel.SE_PACMAN_POWER_ENDS -> {
			sounds.stop(AudioClipID.PACMAN_POWER);
			event.game.level().ifPresent(level -> sounds.ensureSirenStarted(level.huntingPhase() / 2));
		}
		case GameModel.SE_PACMAN_POWER_STARTS -> {
			sounds.stopSirens();
			sounds.stop(AudioClipID.PACMAN_POWER);
			sounds.loop(AudioClipID.PACMAN_POWER, AudioClip.INDEFINITE);
		}
		case GameModel.SE_START_INTERMISSION_1 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(AudioClipID.INTERMISSION_1);
			case PACMAN -> sounds.loop(AudioClipID.INTERMISSION_1, 2);
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_START_INTERMISSION_2 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(AudioClipID.INTERMISSION_2);
			case PACMAN -> sounds.play(AudioClipID.INTERMISSION_1);
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_START_INTERMISSION_3 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> sounds.play(AudioClipID.INTERMISSION_3);
			case PACMAN -> sounds.loop(AudioClipID.INTERMISSION_1, 2);
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_STOP_ALL_SOUNDS -> sounds.stopAll();
		default -> {
			// ignore
		}
		}
	}

	public GameController gameController() {
		return gameController;
	}

	public Scene mainScene() {
		return stage.getScene();
	}

	public GameScene currentGameScene() {
		return currentGameScene;
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
}