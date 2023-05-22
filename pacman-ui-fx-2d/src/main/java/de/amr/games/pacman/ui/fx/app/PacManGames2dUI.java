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

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
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
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.BootScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.HelpMenus;
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
public class PacManGames2dUI implements PacManGamesUserInterface, GameEventListener {

	protected final GameClock clock;
	protected final GameController gameController;
	protected final Map<GameVariant, GameSceneConfiguration> gameSceneConfig = new EnumMap<>(GameVariant.class);
	protected final Stage stage;
	protected final FlashMessageView flashMessageView = new FlashMessageView();
	protected final HelpMenus helpMenus;

	protected Pane mainSceneRoot;
	protected KeyboardSteering keyboardSteering;
	protected GameScene currentGameScene;
	private AudioClip currentVoice;

	public PacManGames2dUI(GameVariant gameVariant, Stage stage, double width, double height) {
		checkNotNull(gameVariant);
		checkNotNull(stage);
		this.gameController = new GameController(gameVariant);
		this.stage = stage;
		stage.setScene(new Scene(new Pane(), width, height, Color.BLACK));
		helpMenus = new HelpMenus(gameController, PacManGames2d.assets.messages);
		clock = new GameClock() {
			@Override
			public void doUpdate() {
				PacManGames2dUI.this.doUpdate();
			}

			@Override
			public void doRender() {
				PacManGames2dUI.this.doRender();
			}
		};
		clock.pausedPy.addListener((py, ov, nv) -> updateStage());
	}

	protected void doUpdate() {
		gameController.update();
		if (currentGameScene != null) {
			currentGameScene.update();
		}
	}

	protected void doRender() {
		flashMessageView.update();
		if (currentGameScene != null) {
			currentGameScene.render();
		}
	}

	@Override
	public void init(Settings settings) {
		configureGameScenes();
		configureMainScene(stage.getScene(), settings);
		configurePacSteering();
		configureBindings(settings);
		GameEvents.addListener(this);
	}

	protected void configureGameScenes() {
		gameSceneConfig.put(GameVariant.MS_PACMAN, new GameSceneConfiguration(
		//@formatter:off
			new GameSceneChoice(new BootScene()),
			new GameSceneChoice(new MsPacManIntroScene()), 
			new GameSceneChoice(new MsPacManCreditScene()),
			new GameSceneChoice(new PlayScene2D()),
			new GameSceneChoice(new MsPacManIntermissionScene1()), 
			new GameSceneChoice(new MsPacManIntermissionScene2()),
			new GameSceneChoice(new MsPacManIntermissionScene3())
	  //@formatter:on
		));

		gameSceneConfig.put(GameVariant.PACMAN, new GameSceneConfiguration(
		//@formatter:off
			new GameSceneChoice(new BootScene()),
			new GameSceneChoice(new PacManIntroScene()),
			new GameSceneChoice(new PacManCreditScene()),
			new GameSceneChoice(new PlayScene2D()),
			new GameSceneChoice(new PacManCutscene1()), 
			new GameSceneChoice(new PacManCutscene2()),
			new GameSceneChoice(new PacManCutscene3())
	  //@formatter:on
		));
	}

	protected void configureMainScene(Scene mainScene, Settings settings) {
		mainSceneRoot = new StackPane();
		mainSceneRoot.getChildren().add(new Text("(Game scene)"));
		mainSceneRoot.getChildren().add(flashMessageView);

		// Without this, there appears an ugly vertical line right of the embedded subscene
		mainSceneRoot.setBackground(ResourceManager.coloredBackground(PacManGames2d.assets.wallpaperColor));

		mainScene.setRoot(mainSceneRoot);
		mainScene.heightProperty().addListener((py, ov, nv) -> {
			if (currentGameScene != null) {
				currentGameScene.setParentScene(mainScene);
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

	protected void configurePacSteering() {
		keyboardSteering = new KeyboardSteering();
		gameController.setManualPacSteering(keyboardSteering);
		// TODO: maybe only play scene should handle steering keys?
		stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
	}

	public void showStage() {
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
	}

	protected void updateStage() {
		mainSceneRoot.setBackground(PacManGames2d.assets.wallpaper);
		switch (gameController.game().variant()) {
		case MS_PACMAN -> {
			var messageKey = clock.pausedPy.get() ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(ResourceManager.fmtMessage(PacManGames2d.assets.messages, messageKey, ""));
			stage.getIcons().setAll(PacManGames2d.assets.iconMsPacMan);
		}
		case PACMAN -> {
			var messageKey = clock.pausedPy.get() ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(ResourceManager.fmtMessage(PacManGames2d.assets.messages, messageKey, ""));
			stage.getIcons().setAll(PacManGames2d.assets.iconPacMan);
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

	protected void changeGameScene(GameScene newGameScene) {
		var prevGameScene = currentGameScene;
		if (prevGameScene != null) {
			prevGameScene.end();
		}
		currentGameScene = newGameScene;
		currentGameScene.setParentScene(stage.getScene());
		//@formatter:off
		currentGameScene.setContext(
			new GameSceneContext(gameController, this,
				new MsPacManGameRenderer(PacManGames2d.assets.spritesMsPacMan),
				new PacManGameRenderer(PacManGames2d.assets.spritesPacMan),
				PacManGames2d.assets.gameSounds(game().variant())
		));
		//@formatter:on
		currentGameScene.init();
		mainSceneRoot.getChildren().set(0, currentGameScene.fxSubScene());
		Logger.trace("Game scene changed from {} to {}", prevGameScene, currentGameScene);
	}

	public void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		currentGameScene.handleKeyboardInput();
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2d.KEY_SHOW_HELP)) {
			showHelp();
		} else if (Keyboard.pressed(PacManGames2d.KEY_AUTOPILOT)) {
			toggleAutopilot();
		} else if (Keyboard.pressed(PacManGames2d.KEY_BOOT)) {
			reboot();
		} else if (Keyboard.pressed(PacManGames2d.KEY_DEBUG_INFO)) {
			Ufx.toggle(PacManGames2d.PY_SHOW_DEBUG_INFO);
		} else if (Keyboard.pressed(PacManGames2d.KEY_IMMUNITIY)) {
			toggleImmunity();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE)) {
			togglePaused();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE_STEP) || Keyboard.pressed(PacManGames2d.KEY_SINGLE_STEP)) {
			oneSimulationStep();
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEN_STEPS)) {
			tenSimulationSteps();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_FASTER)) {
			changeSimulationSpeed(5);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_SLOWER)) {
			changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_NORMAL)) {
			resetSimulationSpeed();
		} else if (Keyboard.pressed(PacManGames2d.KEY_QUIT)) {
			restartIntro();
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEST_LEVELS)) {
			startLevelTestMode();
		} else if (Keyboard.pressed(PacManGames2d.KEY_FULLSCREEN)) {
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
			var r = currentGameScene.context().renderer();
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
			gameScene2d.showHelpMenu(helpMenus, Duration.seconds(2));
		}
	}

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), seconds);
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

	@Override
	public void stopVoice() {
		if (currentVoice != null) {
			currentVoice.stop();
		}
	}

	@Override
	public GameClock clock() {
		return clock;
	}

	@Override
	public GameController gameController() {
		return gameController;
	}

	@Override
	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public FlashMessageView flashMessageView() {
		return flashMessageView;
	}

	// Actions

	@Override
	public void startGame() {
		if (game().hasCredit()) {
			stopVoice();
			gameController.startPlaying();
		}
	}

	@Override
	public void startCutscenesTest() {
		gameController.startCutscenesTest();
		showFlashMessage("Cut scenes");
	}

	@Override
	public void restartIntro() {
		currentGameScene.end();
		GameEvents.setSoundEventsEnabled(true);
		if (game().isPlaying()) {
			game().changeCredit(-1);
		}
		gameController.restart(INTRO);
	}

	public void reboot() {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		playVoice(PacManGames2d.assets.voiceExplainKeys, 4);
		gameController.restart(GameState.BOOT);
	}

	@Override
	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		gameController.addCredit();
	}

	@Override
	public void enterLevel(int newLevelNumber) {
		if (gameController.state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					game().nextLevel();
				}
				gameController.changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
	}

	@Override
	public void togglePaused() {
		Ufx.toggle(clock.pausedPy);
		// TODO mute and unmute?
		if (clock.pausedPy.get()) {
			PacManGames2d.assets.gameSounds(game().variant()).stopAll();
		}
	}

	public void oneSimulationStep() {
		if (clock.pausedPy.get()) {
			clock.executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (clock.pausedPy.get()) {
			clock.executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = clock.targetFrameratePy.get() + delta;
		if (newFramerate > 0 && newFramerate < 120) {
			clock.targetFrameratePy.set(newFramerate);
			showFlashMessageSeconds(0.75, "%dHz".formatted(newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		clock.targetFrameratePy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, "%dHz".formatted(clock.targetFrameratePy.get()));
	}

	@Override
	public void selectNextGameVariant() {
		var gameVariant = game().variant().next();
		gameController.selectGameVariant(gameVariant);
		playVoice(PacManGames2d.assets.voiceExplainKeys, 4);
	}

	@Override
	public void toggleAutopilot() {
		gameController.toggleAutoControlled();
		var auto = gameController.isAutoControlled();
		String message = fmtMessage(PacManGames2d.assets.messages, auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoice(auto ? PacManGames2d.assets.voiceAutopilotOn : PacManGames2d.assets.voiceAutopilotOff);
	}

	@Override
	public void toggleImmunity() {
		game().setImmune(!game().isImmune());
		var immune = game().isImmune();
		String message = fmtMessage(PacManGames2d.assets.messages, immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoice(immune ? PacManGames2d.assets.voiceImmunityOn : PacManGames2d.assets.voiceImmunityOff);
	}

	public void startLevelTestMode() {
		if (gameController.state() == GameState.INTRO) {
			gameController.restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	@Override
	public void cheatAddLives() {
		int newLivesCount = game().lives() + 3;
		game().setLives(newLivesCount);
		showFlashMessage(fmtMessage(PacManGames2d.assets.messages, "cheat_add_lives", newLivesCount));
	}

	@Override
	public void cheatEatAllPellets() {
		gameController.cheatEatAllPellets();
	}

	@Override
	public void cheatEnterNextLevel() {
		gameController.cheatEnterNextLevel();
	}

	@Override
	public void cheatKillAllEatableGhosts() {
		gameController.cheatKillAllEatableGhosts();
	}
}