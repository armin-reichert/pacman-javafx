/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.GhostAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;
import static de.amr.games.pacman.ui.fx.util.Ufx.toggle;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No 3D play scene, no dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, ActionHandler, GameSceneContext {

	protected final GameClock clock;
	protected final Map<GameVariant, Map<String, GameScene>> gameScenes = new EnumMap<>(GameVariant.class);
	protected final Theme theme;
	protected final SoundHandler soundHandler;
	protected final Stage stage;
	protected final Scene mainScene;
	protected final StartPage startPage;
	protected final GamePage gamePage;
	protected Page currentPage;
	public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");

	public PacManGames2dUI(Stage stage, Settings settings, Theme theme) {
		checkNotNull(stage);
		checkNotNull(settings);
		checkNotNull(theme);

		this.stage = stage;
		this.theme = theme;
		this.soundHandler = new SoundHandler(theme);
		this.mainScene = createMainScene();
		this.startPage = createStartPage(theme);
		this.gamePage = createGamePage(theme);
		this.clock = createClock();

		addGameScenes();
		configurePacSteering();
		configureStage(settings);
	}

	protected void addGameScenes() {
		gameScenes.put(GameVariant.MS_PACMAN, new HashMap<>(Map.of(
			"boot",   new BootScene(),
			"intro",  new MsPacManIntroScene(),
			"credit", new MsPacManCreditScene(),
			"play",   new PlayScene2D(),
			"cut1",   new MsPacManCutscene1(),
			"cut2",   new MsPacManCutscene2(),
			"cut3",   new MsPacManCutscene3()
		)));
		gameScenes.put(GameVariant.PACMAN, new HashMap<>(Map.of(
			"boot",   new BootScene(),
			"intro",  new PacManIntroScene(),
			"credit", new PacManCreditScene(),
			"play",   new PlayScene2D(),
			"cut1",   new PacManCutscene1(),
			"cut2",   new PacManCutscene2(),
			"cut3",   new PacManCutscene3()
		)));
	}

	protected GameClock createClock() {
		var clock = new GameClock();
		clock.setOnTick(() -> {
			GameController.it().update();
			currentGameScene().ifPresent(GameScene::update);
		});
		clock.setOnRender(gamePage::render);
		clock.pausedPy.addListener((py, ov, nv) -> updateStage());
		clock.targetFrameratePy.set(GameModel.FPS);
		return clock;
	}

	protected Scene createMainScene() {
		var screenHeight = Screen.getPrimary().getBounds().getHeight();
		double height = Math.min(screenHeight * 0.8, 800);
		double width = height * 4.0 / 3.0;
		var scene = new Scene(new Region(), width, height, Color.BLACK);
		scene.widthProperty().addListener((py, ov, nv) -> currentPage.setSize(scene.getWidth(), scene.getHeight()));
		scene.heightProperty().addListener((py, ov, nv) -> currentPage.setSize(scene.getWidth(), scene.getHeight()));
		return scene;
	}

	protected void configureStage(Settings settings) {
		stage.setScene(mainScene);
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth (GameModel.TILES_X * Globals.TS);
		stage.setMinHeight(GameModel.TILES_Y * Globals.TS);
		stage.centerOnScreen();
	}

	protected StartPage createStartPage(Theme theme) {
		var startPage = new StartPage(theme);
		startPage.setPlayButtonAction(this::showGamePage);
		startPage.setOnKeyPressed(e -> {
			switch (e.getCode()) {
				case ENTER, SPACE -> showGamePage();
				case V            -> switchGameVariant();
				case F11          -> stage.setFullScreen(true);
				default           -> {}
			}
		});
		return startPage;
	}

	protected GamePage createGamePage(Theme theme) {
		var gamePage = new GamePage(this, theme);
		gamePage.setSize(mainScene.getWidth(), mainScene.getHeight());
		return gamePage;
	}

	public void showStartPage() {
		currentPage = startPage;
		if (clock.isRunning()) {
			clock.stop();
			Logger.info("Clock stopped.");
		}
		mainScene.setRoot(startPage.root());
		updateStage();
		startPage.setGameVariant(game().variant());
		startPage.root().requestFocus();
		stage.show();
	}

	public void showGamePage() {
		currentPage = gamePage;
		// call reboot() first such that current game scene is set
		reboot();
		mainScene.setRoot(gamePage.root());
		gamePage.root().requestFocus();
		gamePage.setSize(mainScene.getWidth(), mainScene.getHeight());
		updateStage();
		stage.show();
		clock.start();
		Logger.info("Clock started, speed={} Hz", clock.targetFrameratePy.get());
	}

	protected void configurePacSteering() {
		GameController.it().setManualPacSteering(new KeyboardSteering());
	}

	protected void updateStage() {
		var variant = GameController.it().game().variant();
		var variantKey = variant == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey;
		if (clock.isPaused()) {
			titleKey += ".paused";
		}
		stage.setTitle(message(PacManGames2dApp.TEXTS, titleKey));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
	}

	protected GameScene sceneMatchingCurrentGameState() {
		var config = sceneConfig();
		return switch (GameController.it().state()) {
			case BOOT              -> config.get("boot");
			case CREDIT            -> config.get("credit");
			case INTRO             -> config.get("intro");
			case INTERMISSION      -> config.get("cut" + game().level().get().intermissionNumber);
			case INTERMISSION_TEST -> config.get("cut" + GameController.it().intermissionTestNumber);
			default                -> config.get("play");
		};
	}

	protected void updateOrReloadGameScene(boolean reload) {
		var nextGameScene = sceneMatchingCurrentGameState();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state " + GameController.it().state());
		}
		if (reload || nextGameScene != gameScenePy.get()) {
			setGameScene(nextGameScene);
		}
		updateStage();
	}

	protected void setGameScene(GameScene newGameScene) {
		currentGameScene().ifPresent(prevGameScene -> {
			prevGameScene.end();
			if (prevGameScene != sceneConfig().get("boot")) {
				soundHandler.stopVoice();
			}
		});
		newGameScene.setContext(this);
		newGameScene.init();
		gameScenePy.setValue(newGameScene);
		Logger.trace("Game scene changed to {}", gameScenePy.get());
	}

	// GameSceneContext

	@Override
	public ActionHandler actionHandler() {
		return this;
	}

	@Override
	public Theme theme() {
		return theme;
	}

	@Override
	public Spritesheet spritesheet() {
		return game().variant() == GameVariant.MS_PACMAN ? theme.get("mspacman.spritesheet") : theme.get("pacman.spritesheet");
	}

	@Override
	public SoundHandler soundHandler() {
		return soundHandler;
	}

	// Accessors

	public Scene mainScene() {
		return mainScene;
	}

	public GameClock clock() {
		return clock;
	}

	public Optional<GameScene> currentGameScene() {
		return Optional.ofNullable(gameScenePy.get());
	}

	public Map<String, GameScene> sceneConfig() {
		return gameScenes.get(game().variant());
	}

	// GameEventListener implementation part

	@Override
	public void onGameEvent(GameEvent e) {
		Logger.trace("Event received: {}", e);
		// call event specific handler
		GameEventListener.super.onGameEvent(e);
		currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(e));
		soundHandler.onGameEvent(e);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		updateOrReloadGameScene(false);
	}

	@Override
	public void onUnspecifiedChange(GameEvent e) {
		updateOrReloadGameScene(true);
	}

	@Override
	public void onLevelCreated(GameEvent e) {
		// Found no better point in time to create and assign the sprite animations to the guys
		e.game.level().ifPresent(level -> {
			switch (e.game.variant()) {
			case MS_PACMAN -> {
				var ss = (SpritesheetMsPacManGame) spritesheet();
				level.pac().setAnimations(new PacAnimationsMsPacManGame(level.pac(), ss));
				level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsMsPacManGame(ghost, ss)));
				Logger.info("Created Ms. Pac-Man game creature animations for level #{}", level.number());
			}
			case PACMAN -> {
				var ss = (SpritesheetPacManGame) spritesheet();
				level.pac().setAnimations(new PacAnimationsPacManGame(level.pac(), ss));
				level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsPacManGame(ghost, ss)));
				Logger.info("Created Pac-Man game creature animations for level #{}", level.number());
			}
			default -> throw new IllegalGameVariantException(e.game.variant());
			}
		});
		updateOrReloadGameScene(true);
	}

	// ActionHandler implementation part

	@Override
	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	@Override
	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		gamePage.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	@Override
	public void startGame() {
		if (GameController.it().hasCredit()) {
			soundHandler.stopVoice();
			GameController.it().startPlaying();
		}
	}

	@Override
	public void startCutscenesTest() {
		GameController.it().startCutscenesTest(1);
		showFlashMessage("Cut scenes");
	}

	@Override
	public void restartIntro() {
		soundHandler.stopAllSounds();
		currentGameScene().ifPresent(GameScene::end);
		if (game().isPlaying()) {
			GameController.it().changeCredit(-1);
		}
		GameController.it().restart(INTRO);
	}

	@Override
	public void reboot() {
		currentGameScene().ifPresent(GameScene::end);
		soundHandler.playVoice("voice.explain");
		GameController.it().restart(GameState.BOOT);
	}

	@Override
	public void addCredit() {
		GameController.it().addCredit();
	}

	@Override
	public void togglePaused() {
		toggle(clock.pausedPy);
		if (clock.isPaused()) {
			theme.audioClips().forEach(AudioClip::stop);
		}
	}

	@Override
	public void oneSimulationStep() {
		if (clock.isPaused()) {
			clock.executeSingleStep(true);
		}
	}

	@Override
	public void tenSimulationSteps() {
		if (clock.isPaused()) {
			clock.executeSteps(10, true);
		}
	}

	@Override
	public void changeSimulationSpeed(int delta) {
		int newRate = clock.targetFrameratePy.get() + delta;
		if (newRate > 0) {
			clock.targetFrameratePy.set(newRate);
			showFlashMessageSeconds(0.75, newRate + "Hz");
		}
	}

	@Override
	public void resetSimulationSpeed() {
		clock.targetFrameratePy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, clock.targetFrameratePy.get() + "Hz");
	}

	@Override
	public void switchGameVariant() {
		var variant = game().variant().next();
		GameController.it().startNewGame(variant);
		showStartPage();
	}

	@Override
	public void toggleAutopilot() {
		GameController.it().toggleAutoControlled();
		var auto = GameController.it().isAutoControlled();
		String message = message(PacManGames2dApp.TEXTS, auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		soundHandler.playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off");
	}

	@Override
	public void toggleImmunity() {
		GameController.it().setImmune(!GameController.it().isImmune());
		var immune = GameController.it().isImmune();
		String message = message(PacManGames2dApp.TEXTS, immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		soundHandler.playVoice(immune ? "voice.immunity.on" : "voice.immunity.off");
	}

	@Override
	public void enterLevel(int newLevelNumber) {
		if (GameController.it().state() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					game().nextLevel();
				}
				GameController.it().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
	}

	@Override
	public void startLevelTestMode() {
		if (GameController.it().state() == GameState.INTRO) {
			GameController.it().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	@Override
	public void cheatAddLives() {
		game().addLives((short) 3);
		showFlashMessage(message(PacManGames2dApp.TEXTS, "cheat_add_lives", game().lives()));
	}

	@Override
	public void cheatEatAllPellets() {
		GameController.it().cheatEatAllPellets();
	}

	@Override
	public void cheatEnterNextLevel() {
		GameController.it().cheatEnterNextLevel();
	}

	@Override
	public void cheatKillAllEatableGhosts() {
		GameController.it().cheatKillAllEatableGhosts();
	}
}