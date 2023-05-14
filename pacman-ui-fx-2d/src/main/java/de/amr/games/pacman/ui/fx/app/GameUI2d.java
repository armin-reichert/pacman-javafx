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
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class GameUI2d implements GameEventListener {

	protected final GameClock clock;
	protected final GameController gameController;
	protected final Map<GameVariant, GameSceneConfiguration> gameSceneConfig = new EnumMap<>(GameVariant.class);
	protected final Stage stage;
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final ContextSensitiveHelp csHelp;

	protected Pane mainSceneRoot;
	protected KeyboardSteering keyboardSteering;
	protected GameScene currentGameScene;
	private AudioClip currentVoice;

	public GameUI2d(GameVariant gameVariant, Stage stage, double width, double height) {
		checkNotNull(gameVariant);
		checkNotNull(stage);
		this.gameController = new GameController(gameVariant);
		this.stage = stage;
		stage.setScene(new Scene(new Pane(), width, height, Color.BLACK));
		csHelp = new ContextSensitiveHelp(gameController, Game2d.assets.messages);
		clock = new GameClock() {
			@Override
			public void doUpdate() {
				GameUI2d.this.doUpdate();
			}

			@Override
			public void doRender() {
				GameUI2d.this.doRender();
			}
		};
		clock.pausedPy.addListener((py, ov, nv) -> updateStage());
	}

	protected void doUpdate() {
		gameController.update();
		currentGameScene.update();
	}

	protected void doRender() {
		flashMessageView.update();
		currentGameScene.render();
	}

	public void init(Settings settings) {
		configureGameScenes();
		configureMainScene(stage.getScene(), settings);
		configurePacSteering(settings.keyMap);
		configureBindings(settings);
		GameEvents.addListener(this);
	}

	protected void configureGameScenes() {
		gameSceneConfig.put(GameVariant.PACMAN, new GameSceneConfiguration(new PacManGameRenderer(),
		//@formatter:off
			new GameSceneChoice(new BootScene(gameController)),
			new GameSceneChoice(new PacManIntroScene(gameController)),
			new GameSceneChoice(new PacManCreditScene(gameController)),
			new GameSceneChoice(new PlayScene2D(gameController)),
			new GameSceneChoice(new PacManCutscene1(gameController)), 
			new GameSceneChoice(new PacManCutscene2(gameController)),
			new GameSceneChoice(new PacManCutscene3(gameController))
		//@formatter:on
		));

		gameSceneConfig.put(GameVariant.MS_PACMAN, new GameSceneConfiguration(new MsPacManGameRenderer(),
		//@formatter:off
			new GameSceneChoice(new BootScene(gameController)),
			new GameSceneChoice(new MsPacManIntroScene(gameController)), 
			new GameSceneChoice(new MsPacManCreditScene(gameController)),
			new GameSceneChoice(new PlayScene2D(gameController)),
			new GameSceneChoice(new MsPacManIntermissionScene1(gameController)), 
			new GameSceneChoice(new MsPacManIntermissionScene2(gameController)),
			new GameSceneChoice(new MsPacManIntermissionScene3(gameController))
		//@formatter:on
		));
	}

	protected void configureMainScene(Scene mainScene, Settings settings) {
		mainSceneRoot = new StackPane();
		mainSceneRoot.getChildren().add(new Text("(Game scene)"));
		mainSceneRoot.getChildren().add(flashMessageView);

		mainScene.setRoot(mainSceneRoot);
		mainScene.heightProperty().addListener((py, ov, nv) -> {
			if (currentGameScene != null) {
				currentGameScene.onParentSceneResize(mainScene);
			}
		});
		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToFitCurrentGameScene();
			}
		});
	}

	protected void resizeStageToFitCurrentGameScene() {
		if (currentGameScene != null && !currentGameScene.is3D() && !stage.isFullScreen()) {
			stage.setWidth(currentGameScene.fxSubScene().getWidth() + 16); // don't ask me why
		}
	}

	protected void configurePacSteering(Map<Direction, KeyCode> map) {
		keyboardSteering = new KeyboardSteering(//
				map.get(Direction.UP), //
				map.get(Direction.DOWN), //
				map.get(Direction.LEFT), //
				map.get(Direction.RIGHT));
		gameController.setManualPacSteering(keyboardSteering);
		// TODO: maybe only play scene should handle steering keys?
		stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
	}

	public void startClockAndShowStage() {
		clock.start();
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
	}

	protected void updateStage() {
		mainSceneRoot.setBackground(Game2d.assets.wallpaper2D);
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = clock.pausedPy.get() ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(ResourceManager.fmtMessage(Game2d.assets.messages, messageKey, ""));
			stage.getIcons().setAll(Game2d.assets.iconMsPacManGame);
		}
		case PACMAN -> {
			var messageKey = clock.pausedPy.get() ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(ResourceManager.fmtMessage(Game2d.assets.messages, messageKey, ""));
			stage.getIcons().setAll(Game2d.assets.iconPacManGame);
		}
		default -> throw new IllegalGameVariantException(gameController.game().variant());
		}
	}

	/**
	 * @param settings application settings
	 */
	protected void configureBindings(Settings settings) {
		// snooze...
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
		var state = gameController.state();
		var config = gameSceneConfig.get(game().variant());
		return switch (state) {
		case BOOT -> config.bootSceneChoice();
		case CREDIT -> config.creditSceneChoice();
		case INTRO -> config.introSceneChoice();
		case INTERMISSION -> config.cutSceneChoice(gameLevel().intermissionNumber);
		case INTERMISSION_TEST -> config.cutSceneChoice(game().intermissionTestNumber);
		default -> config.playSceneChoice();
		};
	}

	private GameLevel gameLevel() {
		return game().level().orElseThrow(IllegalStateException::new);
	}

	public void updateGameScene(boolean reload) {
		var nextGameScene = chooseGameScene(sceneChoiceMatchingCurrentGameState());
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state %s.".formatted(gameController.state()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
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
		var variant = gameController.game().variant();
		var renderer = gameSceneConfig.get(variant).renderer();
		Logger.trace("Using renderer {}", renderer);
		currentGameScene.context().setRendering2D(renderer);
		currentGameScene.init();
		currentGameScene.onEmbedIntoParentScene(stage.getScene());
		mainSceneRoot.getChildren().set(0, currentGameScene.fxSubScene());
		Logger.trace("Game scene changed to {}", currentGameScene);
	}

	protected void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		currentGameScene.handleKeyboardInput();
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		if (Keyboard.pressed(Game2d.Keys.SHOW_HELP)) {
			showHelp();
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
		SoundHandler.onSoundEvent(event);
	}

	public void showHelp() {
		if (currentGameScene instanceof GameScene2D gameScene2d) {
			updateHelpContent();
			csHelp.show(gameScene2d.helpRoot(), Duration.seconds(2));
		}
	}

	public void updateHelpContent() {
		if (currentGameScene instanceof GameScene2D gameScene2d) {
			var help = csHelp.current();
			if (help.isEmpty()) {
				gameScene2d.helpRoot().getChildren().clear();
			} else {
				gameScene2d.helpRoot().getChildren().setAll(help.get());
			}
		}
	}

	public void playVoice(AudioClip clip) {
		playVoice(clip, 0);
	}

	public void playVoice(AudioClip clip, float delay) {
		if (currentVoice != null && currentVoice.isPlaying()) {
			return; // don't interrupt voice
		}
		currentVoice = clip;
		if (delay > 0) {
			Ufx.actionAfterSeconds(delay, currentVoice::play).play();
		} else {
			currentVoice.play();
		}
	}

	public void stopVoice() {
		if (currentVoice != null) {
			currentVoice.stop();
		}
	}

	public GameClock clock() {
		return clock;
	}

	public GameController gameController() {
		return gameController;
	}

	public GameModel game() {
		return gameController.game();
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}
}