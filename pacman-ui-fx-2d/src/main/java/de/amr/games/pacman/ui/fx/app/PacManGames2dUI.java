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
import java.util.stream.Stream;

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
import de.amr.games.pacman.ui.fx.rendering2d.Theme;
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

	//@formatter:off
	
	public final AudioClip voiceExplainKeys        = PacManGames2d.MGR.audioClip("sound/voice/press-key.mp3");
	public final AudioClip voiceAutopilotOff       = PacManGames2d.MGR.audioClip("sound/voice/autopilot-off.mp3");
	public final AudioClip voiceAutopilotOn        = PacManGames2d.MGR.audioClip("sound/voice/autopilot-on.mp3");
	public final AudioClip voiceImmunityOff        = PacManGames2d.MGR.audioClip("sound/voice/immunity-off.mp3");
	public final AudioClip voiceImmunityOn         = PacManGames2d.MGR.audioClip("sound/voice/immunity-on.mp3");
	//@formatter:on

	protected final Map<GameVariant, GameSceneConfiguration> gameSceneConfig = new EnumMap<>(GameVariant.class);
	protected final GameClock clock = new GameClock(GameModel.FPS);
	protected Theme theme;
	protected Stage stage;
	protected FlashMessageView flashMessageView = new FlashMessageView();
	protected HelpMenus helpMenus;
	protected GameController gameController;
	protected Pane mainSceneRoot;
	protected KeyboardSteering keyboardSteering;
	protected GameScene currentGameScene;
	private AudioClip currentVoice;

	@Override
	public void init(Stage stage, Settings settings, Theme theme) {
		checkNotNull(stage);
		checkNotNull(settings);
		checkNotNull(theme);

		this.stage = stage;
		stage.setFullScreen(settings.fullScreen);
		this.theme = theme;
		this.gameController = new GameController(settings.variant);
		configureGameScenes();
		createMainScene(stage, settings);
		configureHelpMenus();
		configurePacSteering();
		configureBindings(settings);
		GameEvents.addListener(this);
		clock.pausedPy.addListener((py, ov, nv) -> updateStage());
		clock.setOnTick(this::onTick);
		clock.setOnRender(this::onRender);
	}

	protected void onTick() {
		gameController.update();
		if (currentGameScene != null) {
			currentGameScene.update();
		}
	}

	protected void onRender() {
		flashMessageView.update();
		if (currentGameScene != null) {
			currentGameScene.render();
		}
	}

	protected void configureGameScenes() {
		//@formatter:off
		gameSceneConfig.put(GameVariant.MS_PACMAN, new GameSceneConfiguration(
			new GameSceneChoice(new BootScene()),
			new GameSceneChoice(new MsPacManIntroScene()), 
			new GameSceneChoice(new MsPacManCreditScene()),
			new GameSceneChoice(new PlayScene2D()),
			new GameSceneChoice(new MsPacManIntermissionScene1()), 
			new GameSceneChoice(new MsPacManIntermissionScene2()),
			new GameSceneChoice(new MsPacManIntermissionScene3())
		));

		gameSceneConfig.put(GameVariant.PACMAN, new GameSceneConfiguration(
			new GameSceneChoice(new BootScene()),
			new GameSceneChoice(new PacManIntroScene()),
			new GameSceneChoice(new PacManCreditScene()),
			new GameSceneChoice(new PlayScene2D()),
			new GameSceneChoice(new PacManCutscene1()), 
			new GameSceneChoice(new PacManCutscene2()),
			new GameSceneChoice(new PacManCutscene3())
		));
	  //@formatter:on
	}

	protected void createMainScene(Stage stage, Settings settings) {
		mainSceneRoot = new StackPane();
		// Without this, there appears an ugly vertical line right of the embedded subscene
		mainSceneRoot.setBackground(ResourceManager.coloredBackground(theme.color("wallpaper.color")));
		mainSceneRoot.getChildren().add(new Text("(Game scene)"));
		mainSceneRoot.getChildren().add(flashMessageView);

		var mainScene = new Scene(mainSceneRoot, settings.zoom * 28 * 8, settings.zoom * 36 * 8, Color.BLACK);

		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToFitCurrentGameScene();
			}
		});

		stage.setScene(mainScene);
	}

	protected void configureHelpMenus() {
		helpMenus = new HelpMenus(PacManGames2d.TEXTS);
		helpMenus.setFont(theme.font("font.monospaced", 12));
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

	@Override
	public void show() {
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
		clock.start();
	}

	@Override
	public Theme theme() {
		return theme;
	}

	protected void updateStage() {
		mainSceneRoot.setBackground(theme.background("wallpaper.background"));
		switch (gameVariant()) {
		case MS_PACMAN -> {
			var messageKey = clock.pausedPy.get() ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(ResourceManager.fmtMessage(PacManGames2d.TEXTS, messageKey, ""));
			stage.getIcons().setAll(theme.image("mspacman.icon"));
		}
		case PACMAN -> {
			var messageKey = clock.pausedPy.get() ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(ResourceManager.fmtMessage(PacManGames2d.TEXTS, messageKey, ""));
			stage.getIcons().setAll(theme.image("pacman.icon"));
		}
		default -> throw new IllegalGameVariantException(gameVariant());
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
			throw new IllegalArgumentException(String.format("Dimension must be 2 or 3, but is %d", dimension));
		}
		var choice = sceneChoiceMatchingCurrentGameState();
		return Optional.of(choice.scene2D());
	}

	protected GameSceneChoice sceneChoiceMatchingCurrentGameState() {
		var config = gameSceneConfig.get(gameVariant());
		return switch (gameState()) {
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

	protected void updateGameScene(boolean reload) {
		var nextGameScene = chooseGameScene(sceneChoiceMatchingCurrentGameState());
		if (nextGameScene == null) {
			throw new IllegalStateException(String.format("No game scene found for game state %s.", gameState()));
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
		// TODO check this
		if (currentGameScene instanceof GameScene2D) {
			var scene2D = (GameScene2D) currentGameScene;
			// This avoids a vertical line on the left side of the embedded 2D game scene
			var wallpaperColor = theme().color("wallpaper.color");
			scene2D.setWallpaperColor(wallpaperColor);
			scene2D.root().setBackground(ResourceManager.coloredBackground(wallpaperColor));
		}
		currentGameScene.setContext(
				new GameSceneContext(gameController, this, new MsPacManGameRenderer(theme), new PacManGameRenderer(theme)));
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
		} else if (Keyboard.pressed(PacManGames2d.KEY_CANVAS_SCALED)) {
			if (currentGameScene instanceof GameScene2D) {
				var scene2D = (GameScene2D) currentGameScene;
				Ufx.toggle(scene2D.canvasScaledPy);
				showFlashMessage(scene2D.canvasScaledPy.get() ? "Canvas scaled" : "Canvas unscaled");
			}
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
			var renderer = switch (level.game().variant()) {
			case MS_PACMAN -> new MsPacManGameRenderer(theme);
			case PACMAN -> new PacManGameRenderer(theme);
			default -> throw new IllegalGameVariantException(level.game().variant());
			};
			level.pac().setAnimations(renderer.createPacAnimations(level.pac()));
			level.ghosts().forEach(ghost -> ghost.setAnimations(renderer.createGhostAnimations(ghost)));
			level.world().setAnimations(renderer.createWorldAnimations(level.world()));
			Logger.trace("Created creature and world animations for level #{}", level.number());
		});
		updateGameScene(true);
	}

	private String soundPrefix() {
		return gameVariant() == GameVariant.MS_PACMAN ? "mspacman." : "pacman.";
	}

	@Override
	public void onSoundEvent(SoundEvent event) {
		var p = soundPrefix();
		switch (event.id) {
		case GameModel.SE_BONUS_EATEN -> theme.audioClip(p + "audio.bonus_eaten").play();
		case GameModel.SE_CREDIT_ADDED -> theme.audioClip(p + "audio.credit").play();
		case GameModel.SE_EXTRA_LIFE -> theme.audioClip(p + "audio.extra_life").play();
		case GameModel.SE_GHOST_EATEN -> theme.audioClip(p + "audio.ghost_eaten").play();
		case GameModel.SE_HUNTING_PHASE_STARTED_0 -> ensureSirenStarted(0);
		case GameModel.SE_HUNTING_PHASE_STARTED_2 -> ensureSirenStarted(1);
		case GameModel.SE_HUNTING_PHASE_STARTED_4 -> ensureSirenStarted(2);
		case GameModel.SE_HUNTING_PHASE_STARTED_6 -> ensureSirenStarted(3);
		case GameModel.SE_READY_TO_PLAY -> theme.audioClip(p + "audio.game_ready").play();
		case GameModel.SE_PACMAN_DEATH -> theme.audioClip(p + "audio.pacman_death").play();
		// TODO this does not sound as in the original game
		case GameModel.SE_PACMAN_FOUND_FOOD -> ensureLoop(theme.audioClip(p + "audio.pacman_munch"), AudioClip.INDEFINITE);
		case GameModel.SE_PACMAN_POWER_ENDS -> {
			theme.audioClip(p + "audio.pacman_power").stop();
			event.game.level().ifPresent(level -> ensureSirenStarted(level.huntingPhase() / 2));
		}
		case GameModel.SE_PACMAN_POWER_STARTS -> {
			stopSirens();
			theme.audioClip(p + "audio.pacman_power").stop();
			theme.audioClip(p + "audio.pacman_power").setCycleCount(AudioClip.INDEFINITE);
			theme.audioClip(p + "audio.pacman_power").play();
		}
		case GameModel.SE_START_INTERMISSION_1 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> theme.audioClip(p + "audio.intermission.1").play();
			case PACMAN -> {
				theme.audioClip(p + "audio.intermission").setCycleCount(2);
				theme.audioClip(p + "audio.intermission").play();
			}
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_START_INTERMISSION_2 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> theme.audioClip(p + "audio.intermission.2").play();
			case PACMAN -> theme.audioClip(p + "audio.intermission").play();
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_START_INTERMISSION_3 -> {
			switch (event.game.variant()) {
			case MS_PACMAN -> theme.audioClip(p + "audio.intermission.3").play();
			case PACMAN -> {
				theme.audioClip(p + "audio.intermission").setCycleCount(2);
				theme.audioClip(p + "audio.intermission").play();
			}
			default -> throw new IllegalGameVariantException(event.game.variant());
			}
		}
		case GameModel.SE_STOP_ALL_SOUNDS -> stopAllSounds();
		default -> {
			// ignore
		}
		}
	}

	@Override
	public void stopAllSounds() {
		theme.audioClips().forEach(AudioClip::stop);
	}

	@Override
	public void stopMunchingSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.pacman_munch").stop();
	}

	@Override
	public void loopGhostReturningSound() {
		var p = soundPrefix();
		ensureLoop(theme.audioClip(p + "audio.ghost_returning"), AudioClip.INDEFINITE);
	}

	@Override
	public void playGameOverSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.game_over").play();
	}

	@Override
	public void playLevelCompleteSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.level_complete").play();
	}

	@Override
	public void stopGhostReturningSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.ghost_returning").stop();
	}

	public void ensureLoop(AudioClip clip, int repetitions) {
		if (!clip.isPlaying()) {
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	private void startSiren(int sirenIndex) {
		var p = soundPrefix();
		stopSirens();
		var clip = theme.audioClip(p + "audio.siren." + String.valueOf(sirenIndex + 1));
		clip.setCycleCount(AudioClip.INDEFINITE);
		clip.play();
	}

	private Stream<AudioClip> sirens(GameVariant variant) {
		var p = soundPrefix();
		return Stream.of(p + "audio.siren.1", p + "audio.siren.2", p + "audio.siren.3", p + "audio.siren.4")
				.map(key -> theme.audioClip(key));
	}

	/**
	 * @param sirenIndex index of siren (0..3)
	 */
	@Override
	public void ensureSirenStarted(int sirenIndex) {
		if (sirens(gameVariant()).noneMatch(AudioClip::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens(gameVariant()).forEach(AudioClip::stop);
	}

	public void showHelp() {
		if (currentGameScene instanceof GameScene2D) {
			var scene2D = (GameScene2D) currentGameScene;
			scene2D.showHelpMenu(helpMenus, Duration.seconds(2));
		}
	}

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), seconds);
	}

	@Override
	public void playVoice(AudioClip clip, float delaySeconds) {
		if (currentVoice != null && currentVoice.isPlaying()) {
			return; // don't interrupt voice
		}
		currentVoice = clip;
		if (delaySeconds > 0) {
			Ufx.actionAfterSeconds(delaySeconds, currentVoice::play).play();
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
		playVoice(voiceExplainKeys, 4);
		gameController.restart(GameState.BOOT);
	}

	@Override
	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		gameController.addCredit();
	}

	@Override
	public void enterLevel(int newLevelNumber) {
		if (gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
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
			theme.audioClips().forEach(AudioClip::stop);
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
		if (newFramerate > 0) {
			clock.targetFrameratePy.set(newFramerate);
			showFlashMessageSeconds(0.75, String.format("%dHz", newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		clock.targetFrameratePy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, String.format("%dHz", clock.targetFrameratePy.get()));
	}

	@Override
	public void selectNextGameVariant() {
		gameController.selectGameVariant(gameVariant().next());
		playVoice(voiceExplainKeys, 4);
	}

	@Override
	public void toggleAutopilot() {
		gameController.toggleAutoControlled();
		var auto = gameController.isAutoControlled();
		String message = fmtMessage(PacManGames2d.TEXTS, auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoice(auto ? voiceAutopilotOn : voiceAutopilotOff);
	}

	@Override
	public void toggleImmunity() {
		game().setImmune(!game().isImmune());
		var immune = game().isImmune();
		String message = fmtMessage(PacManGames2d.TEXTS, immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoice(immune ? voiceImmunityOn : voiceImmunityOff);
	}

	public void startLevelTestMode() {
		if (gameState() == GameState.INTRO) {
			gameController.restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	@Override
	public void cheatAddLives() {
		int newLivesCount = game().lives() + 3;
		game().setLives(newLivesCount);
		showFlashMessage(fmtMessage(PacManGames2d.TEXTS, "cheat_add_lives", newLivesCount));
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