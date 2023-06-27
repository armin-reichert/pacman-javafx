/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
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
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.util.*;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, ActionHandler {

	protected GameSceneConfiguration configMsPacMan;
	protected GameSceneConfiguration configPacMan;
	protected GameClock clock;
	protected Theme theme;
	protected Stage stage;
	protected Scene scene;
	protected StartPage startPage;
	protected GamePage gamePage;
	protected SoundHandler soundHandler;
	protected GameScene currentGameScene;
	protected boolean showingStartPage;

	public PacManGames2dUI() {}

	public void init(Stage stage, Settings settings) {
		checkNotNull(stage);
		checkNotNull(settings);

		this.stage = stage;

		createClock();
		createMainScene();
		createGameScenes();

		configurePacSteering();
		configureBindings(settings);
		configureStage(settings);
	}

	public void setTheme(Theme theme) {
		checkNotNull(theme);
		this.theme = theme;
		soundHandler = new SoundHandler(theme);
		createStartPage(theme);
		createGamePage(theme);
	}

	protected void createClock() {
		clock = new GameClock(() -> {
			GameController.it().update();
			gamePage.update();
		}, () -> gamePage.render());
		clock.pausedPy.addListener((py, ov, nv) -> updateStage());
		clock.targetFrameratePy.set(GameModel.FPS);
	}

	protected void createMainScene() {
		var screenSize = Screen.getPrimary().getBounds();
		double height = screenSize.getHeight() * 0.8;
		double width = height * 4.0 / 3.0;
		scene = new Scene(new Region(), width, height, Color.BLACK);
		scene.heightProperty().addListener((py, ov, newSceneHeight) -> {
			if (!showingStartPage) {
				resizeGamePage(newSceneHeight.doubleValue());
			}
		});
	}

	protected void configureStage(Settings settings) {
		stage.setScene(scene);
		stage.setFullScreen(settings.fullScreen);
		stage.setMinWidth(28*8);
		stage.setMinHeight(36*8);
		stage.centerOnScreen();
	}

	protected void createGameScenes() {
		//@formatter:off
		configMsPacMan = new GameSceneConfiguration(
			new BootScene(),
			new MsPacManIntroScene(),
			new MsPacManCreditScene(),
			new PlayScene2D(),
			null,
			new MsPacManCutscene1(),
			new MsPacManCutscene2(),
			new MsPacManCutscene3()
		);
		configPacMan = new GameSceneConfiguration(
			new BootScene(),
			new PacManIntroScene(),
			new PacManCreditScene(),
			new PlayScene2D(),
			null,
			new PacManCutscene1(),
			new PacManCutscene2(),
			new PacManCutscene3()
		);
  	//@formatter:on
	}

	protected void createStartPage(Theme theme) {
		startPage = new StartPage(theme);
		startPage.setPlayButtonAction(this::showGamePage);
		startPage.setOnKeyPressed(e -> {
			switch (e.getCode()) {
				case ENTER:	case SPACE:
					showGamePage();
					break;
				case V:
					switchGameVariant();
					break;
				case F11:
					stage.setFullScreen(true);
					break;
				default:
					break;
			}
		});
	}

	protected void createGamePage(Theme theme) {
		gamePage = new GamePage(this, theme);
		resizeGamePage(scene.getHeight());
	}

	protected void resizeGamePage(double sceneHeight) {
		double ratio = sceneHeight / GamePage.CANVAS_HEIGHT_UNSCALED;
		// let game page use around 90% of available scene height
		gamePage.resize(truncate(ratio * 0.9), false);
	}

	private static double truncate(double value) {
		return Math.floor(value * 10) / 10; // e.g. 1.13 -> 11.3 -> 11.0 -> 1.1
	}

	public void showStartPage() {
		clock.stop();

		startPage.setGameVariant(game().variant());

		scene.setRoot(startPage.root());
		updateStage();
		stage.show();
		startPage.root().requestFocus();
		showingStartPage = true;
	}

	public void showGamePage() {
		// call reboot() first such that current game scene is not null!
		reboot();
		clock.start();

		resizeGamePage(scene.getHeight());

		scene.setRoot(gamePage.root());
		updateStage();
		stage.show();
		gamePage.root().requestFocus();
		showingStartPage = false;
	}

	protected void configurePacSteering() {
		GameController.it().setManualPacSteering(new KeyboardSteering());
	}

	protected void updateStage() {
		var variant = GameController.it().game().variant();
		var variantKey = variant == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey;
		if (clock().isPaused()) {
			titleKey += ".paused";
		}
		stage.setTitle(message(PacManGames2d.TEXTS, titleKey));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
	}

	/**
	 * @param settings application settings
	 */
	protected void configureBindings(Settings settings) {
		// snooze...
	}

	protected GameScene sceneMatchingCurrentGameState() {
		var config = sceneConfig();
		switch (GameController.it().state()) {
		case BOOT:
			return config.bootScene();
		case CREDIT:
			return config.creditScene();
		case INTRO:
			return config.introScene();
		case INTERMISSION:
			return config.cutScene(game().level().get().intermissionNumber);
		case INTERMISSION_TEST:
			return config.cutScene(game().intermissionTestNumber);
		default:
			return config.playScene();
		}
	}

	protected void updateOrReloadGameScene(boolean reload) {
		var nextGameScene = sceneMatchingCurrentGameState();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state " + GameController.it().state());
		}
		if (reload || nextGameScene != currentGameScene) {
			setGameScene(nextGameScene);
		}
		updateStage();
	}

	protected void setGameScene(GameScene newGameScene) {
		var prevGameScene = currentGameScene;
		if (prevGameScene != null) {
			prevGameScene.end();
			soundHandler.stopVoice();
			soundHandler.stopAllSounds();
		}
		currentGameScene = newGameScene;
		currentGameScene.setParentScene(stage.getScene());
		currentGameScene.setActionHandler(this);
		currentGameScene.setTheme(theme);
		currentGameScene.setSpritesheet(spritesheet());
		currentGameScene.setSoundHandler(soundHandler());
		currentGameScene.init();
		gamePage.setGameScene(currentGameScene);
		Logger.trace("Game scene changed from {} to {}", prevGameScene, currentGameScene);
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
			switch (level.game().variant()) {
			case MS_PACMAN: {
				var ss = (SpritesheetMsPacManGame) spritesheet();
				level.pac().setAnimations(new PacAnimationsMsPacManGame(level.pac(), ss));
				level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsMsPacManGame(ghost, ss)));
				Logger.trace("Created Ms. Pac-Man game creature animations for level #{}", level.number());
				break;
			}
			case PACMAN: {
				var ss = (SpritesheetPacManGame) spritesheet();
				level.pac().setAnimations(new PacAnimationsPacManGame(level.pac(), ss));
				level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsPacManGame(ghost, ss)));
				Logger.trace("Created Pac-Man game creature animations for level #{}", level.number());
				break;
			}
			default:
				throw new IllegalGameVariantException(level.game().variant());
			}
		});
		updateOrReloadGameScene(true);
	}

	@Override
	public void onSoundEvent(SoundEvent e) {
		soundHandler.onSoundEvent(e);
	}

	public Theme theme() {
		return theme;
	}

	public SoundHandler soundHandler() {
		return soundHandler;
	}

	public GameClock clock() {
		return clock;
	}

	public GameScene currentGameScene() {
		return currentGameScene;
	}

	public GameModel game() {
		return GameController.it().game();
	}

	public GameSceneConfiguration sceneConfig() {
		return game().variant() == GameVariant.MS_PACMAN ? configMsPacMan : configPacMan;
	}

	public Spritesheet spritesheet() {
		switch (game().variant()) {
		case MS_PACMAN:
			return theme().get("mspacman.spritesheet");
		case PACMAN:
			return theme().get("pacman.spritesheet");
		default:
			throw new IllegalGameVariantException(game().variant());
		}
	}

	// Actions

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		gamePage.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	public void startGame() {
		if (game().hasCredit()) {
			soundHandler.stopVoice();
			GameController.it().startPlaying();
		}
	}

	public void startCutscenesTest() {
		GameController.it().startCutscenesTest(1);
		showFlashMessage("Cut scenes");
	}

	public void restartIntro() {
		if (currentGameScene != null) {
			currentGameScene.end();
			soundHandler.stopAllSounds();
			GameController.setSoundEventsEnabled(true);
			if (game().isPlaying()) {
				game().changeCredit(-1);
			}
			GameController.it().restart(INTRO);
		}
	}

	public void reboot() {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		soundHandler().playVoice("voice.explain");
		GameController.it().restart(GameState.BOOT);
	}

	public void addCredit() {
		GameController.setSoundEventsEnabled(true);
		GameController.it().addCredit();
	}

	public void togglePaused() {
		Ufx.toggle(clock.pausedPy);
		if (clock.isPaused()) {
			theme.audioClips().forEach(AudioClip::stop);
		}
	}

	public void oneSimulationStep() {
		if (clock.isPaused()) {
			clock.executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (clock.isPaused()) {
			clock.executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = clock.targetFrameratePy.get() + delta;
		if (newFramerate > 0) {
			clock.targetFrameratePy.set(newFramerate);
			showFlashMessageSeconds(0.75, newFramerate + "Hz");
		}
	}

	public void resetSimulationSpeed() {
		clock.targetFrameratePy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, clock.targetFrameratePy.get() + "Hz");
	}

	public void switchGameVariant() {
		var variant = game().variant().next();
		GameController.it().selectGameVariant(variant);
		showStartPage();
	}

	public void toggleAutopilot() {
		GameController.it().toggleAutoControlled();
		var auto = GameController.it().isAutoControlled();
		String message = message(PacManGames2d.TEXTS, auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		soundHandler.playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off");
	}

	public void toggleImmunity() {
		game().setImmune(!game().isImmune());
		var immune = game().isImmune();
		String message = message(PacManGames2d.TEXTS, immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		soundHandler.playVoice(immune ? "voice.immunity.on" : "voice.immunity.off");
	}

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

	public void startLevelTestMode() {
		if (GameController.it().state() == GameState.INTRO) {
			GameController.it().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	public void cheatAddLives() {
		int newLivesCount = game().lives() + 3;
		game().setLives(newLivesCount);
		showFlashMessage(message(PacManGames2d.TEXTS, "cheat_add_lives", newLivesCount));
	}

	public void cheatEatAllPellets() {
		GameController.it().cheatEatAllPellets();
	}

	public void cheatEnterNextLevel() {
		GameController.it().cheatEnterNextLevel();
	}

	public void cheatKillAllEatableGhosts() {
		GameController.it().cheatKillAllEatableGhosts();
	}
}