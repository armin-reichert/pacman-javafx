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
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.BootScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
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
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class GameUI2d extends GameClock implements GameEventListener {

	public static final byte TILES_X = 28;
	public static final byte TILES_Y = 36;

	public static final byte INDEX_BOOT_SCENE = 0;
	public static final byte INDEX_INTRO_SCENE = 1;
	public static final byte INDEX_CREDIT_SCENE = 2;
	public static final byte INDEX_PLAY_SCENE = 3;

	private AudioClip currentVoiceMessage;

	protected final Map<GameVariant, Rendering2D> renderers = new EnumMap<>(GameVariant.class);
	protected final Map<GameVariant, List<GameSceneChoice>> sceneChoicesMap = new EnumMap<>(GameVariant.class);
	protected final Stage stage;
	protected Scene mainScene;
	protected final StackPane mainSceneRoot = new StackPane();
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final ContextSensitiveHelp contextSensitiveHelp;
	protected final SoundHandler soundHandler = new SoundHandler();
	protected final GameController gameController;

	protected KeyboardSteering keyboardSteering;
	protected GameScene currentGameScene;

	public GameUI2d(Stage stage, Settings settings) {
		checkNotNull(stage);
		checkNotNull(settings);

		this.stage = stage;
		this.gameController = new GameController(settings.variant);
		this.contextSensitiveHelp = new ContextSensitiveHelp(gameController);

		configureRenderers(settings);
		createMsPacManSceneChoices();
		createPacManSceneChoices();
		createMainSceneLayout();
		createMainScene(settings);
		configureStage(settings);
		configureGameLoop();
		configurePacSteering(settings);
		initProperties(settings);

		GameEvents.addListener(this);
	}

	@Override
	public void start() {
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
		super.start();
	}

	/**
	 * @return mutable list of Pac-Man game scene choices
	 */
	protected void createPacManSceneChoices() {
		var scenes = Arrays.asList(
		//@formatter:off
			new GameSceneChoice(new BootScene(gameController)),
			new GameSceneChoice(new PacManIntroScene(gameController)),
			new GameSceneChoice(new PacManCreditScene(gameController)),
			new GameSceneChoice(new PlayScene2D(gameController)),
			new GameSceneChoice(new PacManCutscene1(gameController)), 
			new GameSceneChoice(new PacManCutscene2(gameController)),
			new GameSceneChoice(new PacManCutscene3(gameController))
		//@formatter:on
		);
		sceneChoicesMap.put(GameVariant.PACMAN, scenes);
	}

	/**
	 * @return mutable list of Ms. Pac-Man game scene choices
	 */
	protected void createMsPacManSceneChoices() {
		var scenes = Arrays.asList(
		//@formatter:off
			new GameSceneChoice(new BootScene(gameController)),
			new GameSceneChoice(new MsPacManIntroScene(gameController)), 
			new GameSceneChoice(new MsPacManCreditScene(gameController)),
			new GameSceneChoice(new PlayScene2D(gameController)),
			new GameSceneChoice(new MsPacManIntermissionScene1(gameController)), 
			new GameSceneChoice(new MsPacManIntermissionScene2(gameController)),
			new GameSceneChoice(new MsPacManIntermissionScene3(gameController))
		//@formatter:on
		);
		sceneChoicesMap.put(GameVariant.MS_PACMAN, scenes);
	}

	/**
	 * @param settings application settings
	 */
	protected void configureRenderers(Settings settings) {
		renderers.put(GameVariant.MS_PACMAN, new MsPacManGameRenderer());
		renderers.put(GameVariant.PACMAN, new PacManGameRenderer());
	}

	protected void configurePacSteering(Settings settings) {
		keyboardSteering = new KeyboardSteering(settings.keyMap.get(Direction.UP), settings.keyMap.get(Direction.DOWN), //
				settings.keyMap.get(Direction.LEFT), settings.keyMap.get(Direction.RIGHT));
		mainScene.addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
		gameController.setManualPacSteering(keyboardSteering);
	}

	protected void configureGameLoop() {
		pausedPy.addListener((py, oldVal, newVal) -> updateStage());
	}

	protected void configureStage(Settings settings) {
		stage.setScene(mainScene);
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(241);
		stage.setMinHeight(328);
	}

	protected void createMainSceneLayout() {
		mainSceneRoot.getChildren().add(new Label("Game scene comes here"));
		mainSceneRoot.getChildren().add(flashMessageView);
	}

	public void updateContextSensitiveHelp() {
		if (currentGameScene instanceof GameScene2D scene2D) {
			if (Game2d.showHelpPy.get()) {
				var help = contextSensitiveHelp.current();
				if (help.isEmpty()) {
					scene2D.helpRoot().getChildren().clear();
				} else {
					var font = Game2d.resources.font(Game2d.resources.arcadeFont, 8);
					var panel = help.get().createPane(gameController, font);
					scene2D.helpRoot().getChildren().setAll(panel);
				}
			} else {
				scene2D.helpRoot().getChildren().clear();
			}
		}
	}

	protected void createMainScene(Settings settings) {
		mainScene = new Scene(mainSceneRoot, TILES_X * TS * settings.zoom, TILES_Y * TS * settings.zoom);
		mainScene.heightProperty().addListener((py, ov, nv) -> currentGameScene.onParentSceneResize(mainScene));
		mainScene.widthProperty().addListener((py, ov, nv) -> updateContextSensitiveHelp());
		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2 && currentGameScene != null) {
				// resize 2D scene to fit
				if (!currentGameScene.is3D() && !stage.isFullScreen()) {
					stage.setWidth(currentGameScene.fxSubScene().getWidth() + 16); // don't ask me why
				}
			}
		});
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

	protected void updateStage() {
		mainSceneRoot.setBackground(Game2d.resources.wallpaper2D);
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = pausedPy.get() ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(ResourceManager.fmtMessage(Game2d.resources.messages, messageKey, ""));
			stage.getIcons().setAll(Game2d.resources.graphicsMsPacMan.icon);
		}
		case PACMAN -> {
			var messageKey = pausedPy.get() ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(ResourceManager.fmtMessage(Game2d.resources.messages, messageKey, ""));
			stage.getIcons().setAll(Game2d.resources.graphicsPacMan.icon);
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	/**
	 * @param settings application settings
	 */
	protected void initProperties(Settings settings) {
		// empty
	}

	/**
	 * @param dimension scene dimension (2 or 3)
	 * @return (optional) game scene matching current game state and specified dimension
	 */
	protected Optional<GameScene> findGameScene(int dimension) {
		if (dimension != 2 && dimension != 3) {
			throw new IllegalArgumentException("Dimension must be 2 or 3, but is %d".formatted(dimension));
		}
		var choice = sceneChoiceMatchingCurrentGameState();
		return Optional.of(choice.scene2D());
	}

	protected GameSceneChoice sceneChoiceMatchingCurrentGameState() {
		var game = gameController.game();
		var state = gameController.state();
		int index = switch (state) {
		case BOOT -> INDEX_BOOT_SCENE;
		case CREDIT -> INDEX_CREDIT_SCENE;
		case INTRO -> INDEX_INTRO_SCENE;
		case GAME_OVER, GHOST_DYING, HUNTING, LEVEL_COMPLETE, LEVEL_TEST, CHANGING_TO_NEXT_LEVEL, PACMAN_DYING, READY -> INDEX_PLAY_SCENE;
		case INTERMISSION -> INDEX_PLAY_SCENE + game.level().orElseThrow(IllegalStateException::new).intermissionNumber;
		case INTERMISSION_TEST -> INDEX_PLAY_SCENE + game.intermissionTestNumber;
		default -> throw new IllegalArgumentException("Unknown game state: %s".formatted(state));
		};
		return sceneChoicesMap.get(game.variant()).get(index);
	}

	public void updateGameScene(boolean reload) {
		var nextGameScene = chooseGameScene(sceneChoiceMatchingCurrentGameState());
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateContextSensitiveHelp();
		updateStage();
	}

	protected GameScene chooseGameScene(GameSceneChoice choice) {
		return choice.scene2D();
	}

	protected final void changeGameScene(GameScene newGameScene) {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		Logger.trace("Changing game scene from {} to {}", currentGameScene, newGameScene);
		currentGameScene = newGameScene;
		var renderer = renderers.get(gameController.game().variant());
		Logger.trace("Using renderer {}", renderer);
		currentGameScene.context().setRendering2D(renderer);
		currentGameScene.init();
		currentGameScene.onEmbedIntoParentScene(stage.getScene());
		mainSceneRoot.getChildren().set(0, currentGameScene.fxSubScene());
		Logger.trace("Game scene changed to {}", currentGameScene);
	}

	private void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		currentGameScene.handleKeyboardInput();
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		if (Keyboard.pressed(Game2d.Keys.TOGGLE_HELP)) {
			Game2d.actions.toggleHelp();
		} else if (Keyboard.pressed(Game2d.Keys.AUTOPILOT)) {
			Game2d.actions.toggleAutopilot();
		} else if (Keyboard.pressed(Game2d.Keys.BOOT)) {
			Game2d.actions.reboot();
		} else if (Keyboard.pressed(Game2d.Keys.DEBUG_INFO)) {
			Ufx.toggle(Game2d.showDebugInfoPy);
		} else if (Keyboard.pressed(Game2d.Keys.IMMUNITIY)) {
			Game2d.actions.toggleImmunity();
		} else if (Keyboard.pressed(Game2d.Keys.PAUSE)) {
			Game2d.actions.togglePaused();
		} else if (Keyboard.pressed(Game2d.Keys.PAUSE_STEP) || Keyboard.pressed(Game2d.Keys.SINGLE_STEP)) {
			Game2d.actions.oneSimulationStep();
		} else if (Keyboard.pressed(Game2d.Keys.TEN_STEPS)) {
			Game2d.actions.tenSimulationSteps();
		} else if (Keyboard.pressed(Game2d.Keys.SIMULATION_FASTER)) {
			Game2d.actions.changeSimulationSpeed(5);
		} else if (Keyboard.pressed(Game2d.Keys.SIMULATION_SLOWER)) {
			Game2d.actions.changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(Game2d.Keys.SIMULATION_NORMAL)) {
			Game2d.actions.resetSimulationSpeed();
		} else if (Keyboard.pressed(Game2d.Keys.QUIT)) {
			Game2d.actions.restartIntro();
		} else if (Keyboard.pressed(Game2d.Keys.TEST_LEVELS)) {
			Game2d.actions.startLevelTestMode();
		} else if (Keyboard.pressed(Game2d.Keys.FULLSCREEN)) {
			stage.setFullScreen(true);
		}
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

	public void playVoiceMessage(AudioClip voiceMessage) {
		if (currentVoiceMessage != null && currentVoiceMessage.isPlaying()) {
			return; // don't interrupt voice message still playing, maybe enqueue?
		}
		currentVoiceMessage = voiceMessage;
		currentVoiceMessage.play();
	}

	public void stopVoiceMessage() {
		if (currentVoiceMessage != null) {
			currentVoiceMessage.stop();
		}
	}

	public void playHelpVoiceMessageAfterSeconds(int seconds) {
		Ufx.afterSeconds(seconds, () -> playVoiceMessage(Game2d.resources.voiceExplainKeys)).play();
	}
}