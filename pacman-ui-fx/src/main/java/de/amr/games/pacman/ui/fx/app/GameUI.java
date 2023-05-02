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
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManTestRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
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

	private static final byte INDEX_BOOT_SCENE = 0;
	private static final byte INDEX_INTRO_SCENE = 1;
	private static final byte INDEX_CREDIT_SCENE = 2;
	private static final byte INDEX_PLAY_SCENE = 3;

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
			currentGameScene.render();
		}
	}

	private final GameController gameController;
	private final Simulation simulation = new Simulation();
	private final Map<GameVariant, Rendering2D> renderers = new EnumMap<>(GameVariant.class);
	private final Map<GameVariant, List<GameSceneChoice>> scenes = new EnumMap<>(GameVariant.class);
	private final Stage stage;
	private final StackPane root = new StackPane();
	private final FlashMessageView flashMessageView = new FlashMessageView();
	private final SoundHandler soundHandler = new SoundHandler();

	private GameScene currentGameScene;

	public GameUI(final Stage stage, final Settings settings, GameController gameController,
			List<GameSceneChoice> msPacManScenes, List<GameSceneChoice> pacManScenes) {

		checkNotNull(stage);
		checkNotNull(settings);

		this.stage = stage;
		this.gameController = gameController;
		var keyboardSteering = new KeyboardSteering(//
				settings.keyMap.get(Direction.UP), settings.keyMap.get(Direction.DOWN), //
				settings.keyMap.get(Direction.LEFT), settings.keyMap.get(Direction.RIGHT));
		gameController.setManualPacSteering(keyboardSteering);

		// renderers must be created before game scenes
		renderers.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		scenes.put(GameVariant.MS_PACMAN, msPacManScenes);

		renderers.put(GameVariant.PACMAN, settings.useTestRenderer ? new PacManTestRenderer() : new PacManGameRenderer());
		scenes.put(GameVariant.PACMAN, pacManScenes);

		var mainScene = createMainScene(TILES_X * 8 * settings.zoom, TILES_Y * 8 * settings.zoom);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
		stage.setScene(mainScene);

		GameEvents.addListener(this);
		initEnv(settings);
		Actions.init(new ActionContext(simulation, gameController, this::currentGameScene, flashMessageView));
		Actions.reboot();

		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();

		Logger.info("Game UI created. Locale: {}. Application settings: {}", Locale.getDefault(), settings);
		Logger.info("Window size: {} x {}", stage.getWidth(), stage.getHeight());
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
		root.setBackground(AppRes.Manager.colorBackground(Env.mainSceneBgColorPy.get()));
		var paused = Env.simulationPausedPy.get();
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = paused ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(AppRes.Texts.message(messageKey, "")); // TODO
			stage.getIcons().setAll(AppRes.Graphics.MsPacManGame.icon);
		}
		case PACMAN -> {
			var messageKey = paused ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(AppRes.Texts.message(messageKey, "")); // TODO
			stage.getIcons().setAll(AppRes.Graphics.PacManGame.icon);
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		Keyboard.clearState();
	}

	private void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateMainView());

		Env.simulationPausedPy.addListener((py, oldVal, newVal) -> updateMainView());
		simulation.pausedPy.bind(Env.simulationPausedPy);
		simulation.targetFrameratePy.bind(Env.simulationSpeedPy);
		simulation.measuredPy.bind(Env.simulationTimeMeasuredPy);
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

	private GameSceneChoice sceneSelectionMatchingCurrentGameState() {
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
		var nextGameScene = matching.scene2D();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
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
		soundHandler.onSoundEvent(event);
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

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}
}