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

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.Arrays;
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
import de.amr.games.pacman.ui.fx.scene2d.BootScene;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManCreditScene;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntermissionScene1;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntermissionScene2;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntermissionScene3;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntroScene;
import de.amr.games.pacman.ui.fx.scene2d.PacManCreditScene;
import de.amr.games.pacman.ui.fx.scene2d.PacManCutscene1;
import de.amr.games.pacman.ui.fx.scene2d.PacManCutscene2;
import de.amr.games.pacman.ui.fx.scene2d.PacManCutscene3;
import de.amr.games.pacman.ui.fx.scene2d.PacManIntroScene;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.GameLoop;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class GameUI extends GameLoop implements GameEventListener {

	public static final byte TILES_X = 28;
	public static final byte TILES_Y = 36;

	public static final byte INDEX_BOOT_SCENE = 0;
	public static final byte INDEX_INTRO_SCENE = 1;
	public static final byte INDEX_CREDIT_SCENE = 2;
	public static final byte INDEX_PLAY_SCENE = 3;

	protected final GameController gameController;
	protected final Map<GameVariant, Rendering2D> renderers = new EnumMap<>(GameVariant.class);
	protected final Map<GameVariant, List<GameSceneChoice>> scenes = new EnumMap<>(GameVariant.class);
	protected final Stage stage;
	protected final Scene mainScene;
	protected final StackPane root = new StackPane();
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final SoundHandler soundHandler = new SoundHandler();
	protected KeyboardSteering keyboardSteering;
	protected GameScene currentGameScene;

	public GameUI(Stage stage, Settings settings, GameController gameController) {
		checkNotNull(stage);
		checkNotNull(settings);
		checkNotNull(gameController);

		this.stage = stage;
		this.gameController = gameController;

		// renderers must be created before game scenes
		renderers.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		renderers.put(GameVariant.PACMAN, settings.useTestRenderer ? new PacManTestRenderer() : new PacManGameRenderer());

		scenes.put(GameVariant.MS_PACMAN, createMsPacManScenes(gameController));
		scenes.put(GameVariant.PACMAN, createPacManScenes(gameController));

		createLayout();

		// main scene
		mainScene = new Scene(root, TILES_X * TS * settings.zoom, TILES_Y * TS * settings.zoom);
		mainScene.heightProperty().addListener((py, ov, nv) -> currentGameScene.onParentSceneResize(mainScene));
		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToOptimalSize();
			}
		});

		// stage
		stage.setScene(mainScene);
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();

		// game loop
		Env.simulationPausedPy.addListener((py, oldVal, newVal) -> updateUI());
		targetFrameratePy.bind(Env.simulationSpeedPy);
		measuredPy.bind(Env.simulationTimeMeasuredPy);
		pausedPy.bind(Env.simulationPausedPy);

		// keyboard
		keyboardSteering = new KeyboardSteering(//
				settings.keyMap.get(Direction.UP), settings.keyMap.get(Direction.DOWN), //
				settings.keyMap.get(Direction.LEFT), settings.keyMap.get(Direction.RIGHT));

		gameController.setManualPacSteering(keyboardSteering);
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);

		initEnv(settings);
		GameEvents.addListener(this);
		Actions.init(new ActionContext(this, gameController, this::currentGameScene, flashMessageView));
		Actions.reboot();

		Logger.info("Game UI created. Locale: {}. Application settings: {}", Locale.getDefault(), settings);
		Logger.info("Window size: {} x {}", stage.getWidth(), stage.getHeight());
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

	protected void createLayout() {
		root.getChildren().add(new Label("Game scene comes here"));
		root.getChildren().add(flashMessageView);
		root.getChildren().add(new BorderPane());
	}

	protected void resizeStageToOptimalSize() {
		if (currentGameScene != null && !currentGameScene.is3D() && !stage.isFullScreen()) {
			stage.setWidth(currentGameScene.fxSubScene().getWidth() + 16); // don't ask me why
		}
	}

	protected void updateUI() {
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

	protected void initEnv(Settings settings) {
		Env.mainSceneBgColorPy.addListener((py, oldVal, newVal) -> updateUI());
	}

	protected List<GameSceneChoice> createPacManScenes(GameController gc) {
		return Arrays.asList(
		//@formatter:off
			new GameSceneChoice(new BootScene(gc)),
			new GameSceneChoice(new PacManIntroScene(gc)),
			new GameSceneChoice(new PacManCreditScene(gc)),
			new GameSceneChoice(new PlayScene2D(gc)),
			new GameSceneChoice(new PacManCutscene1(gc)), 
			new GameSceneChoice(new PacManCutscene2(gc)),
			new GameSceneChoice(new PacManCutscene3(gc))
		//@formatter:on
		);
	}

	protected List<GameSceneChoice> createMsPacManScenes(GameController gc) {
		return Arrays.asList(
		//@formatter:off
			new GameSceneChoice(new BootScene(gc)),
			new GameSceneChoice(new MsPacManIntroScene(gc)), 
			new GameSceneChoice(new MsPacManCreditScene(gc)),
			new GameSceneChoice(new PlayScene2D(gc)),
			new GameSceneChoice(new MsPacManIntermissionScene1(gc)), 
			new GameSceneChoice(new MsPacManIntermissionScene2(gc)),
			new GameSceneChoice(new MsPacManIntermissionScene3(gc))
		//@formatter:on
		);
	}

	/**
	 * @param dimension scene dimension (2 or 3)
	 * @return (optional) game scene matching current game state and specified dimension
	 */
	public Optional<GameScene> findGameScene(int dimension) {
		if (dimension != 2 && dimension != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dimension));
		}
		var choice = sceneChoiceMatchingCurrentGameState();
		return Optional.ofNullable(dimension == 3 ? choice.scene3D() : choice.scene2D());
	}

	protected GameSceneChoice sceneChoiceMatchingCurrentGameState() {
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
		var nextGameScene = chooseGameScene(sceneChoiceMatchingCurrentGameState());
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateUI();
	}

	protected GameScene chooseGameScene(GameSceneChoice choice) {
		return choice.scene2D();
	}

	protected final void changeGameScene(GameScene newGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		newGameScene.context().setRendering2D(renderers.get(gameController.game().variant()));
		newGameScene.init();
		root.getChildren().set(0, newGameScene.fxSubScene());
		newGameScene.onEmbedIntoParentScene(stage.getScene());
		currentGameScene = newGameScene;
		Logger.trace("Game scene changed to {}", currentGameScene);
	}

	protected void handleKeyboardInput() {
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

	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}
}