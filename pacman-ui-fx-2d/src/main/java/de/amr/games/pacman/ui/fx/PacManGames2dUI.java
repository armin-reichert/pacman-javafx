/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.MsPacManGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.MsPacManPacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.MsPacManSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManPacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacManSpriteSheet;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.*;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.input.Keyboard.*;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.message;
import static de.amr.games.pacman.ui.fx.util.Ufx.toggle;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No 3D play scene, no dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameSceneContext, ActionHandler {

	public static final ResourceBundle MSG_BUNDLE = ResourceBundle.getBundle(
		"de.amr.games.pacman.ui.fx.texts.messages", PacManGames2dUI.class.getModule());

	public static final KeyCodeCombination KEY_CHEAT_EAT_ALL     = alt(KeyCode.E);
	public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES   = alt(KeyCode.L);
	public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL  = alt(KeyCode.N);
	public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS = alt(KeyCode.X);
	public static final KeyCodeCombination KEY_AUTOPILOT         = alt(KeyCode.A);
	public static final KeyCodeCombination KEY_DEBUG_INFO        = alt(KeyCode.D);
	public static final KeyCodeCombination KEY_IMMUNITY          = alt(KeyCode.I);
	public static final KeyCodeCombination KEY_PAUSE             = just(KeyCode.P);
	public static final KeyCodeCombination[] KEYS_SINGLE_STEP    = { just(KeyCode.SPACE), shift(KeyCode.P) };
	public static final KeyCodeCombination KEY_TEN_STEPS         = shift(KeyCode.SPACE);
	public static final KeyCodeCombination KEY_SIMULATION_FASTER = alt(KeyCode.PLUS);
	public static final KeyCodeCombination KEY_SIMULATION_SLOWER = alt(KeyCode.MINUS);
	public static final KeyCodeCombination KEY_SIMULATION_NORMAL = alt(KeyCode.DIGIT0);
	public static final KeyCodeCombination[] KEYS_START_GAME     = { just(KeyCode.DIGIT1), just(KeyCode.NUMPAD1) };
	public static final KeyCodeCombination[] KEYS_ADD_CREDIT     = { just(KeyCode.DIGIT5), just(KeyCode.NUMPAD5) };
	public static final KeyCodeCombination KEY_QUIT              = just(KeyCode.Q);
	public static final KeyCodeCombination KEY_TEST_LEVELS       = alt(KeyCode.T);
	public static final KeyCodeCombination KEY_SELECT_VARIANT    = just(KeyCode.V);
	public static final KeyCodeCombination KEY_PLAY_CUTSCENES    = alt(KeyCode.C);
	public static final KeyCodeCombination KEY_SHOW_HELP         = just(KeyCode.H);
	public static final KeyCodeCombination KEY_BOOT              = just(KeyCode.F3);
	public static final KeyCodeCombination KEY_FULLSCREEN        = just(KeyCode.F11);

	public static final int CANVAS_WIDTH_UNSCALED  = GameModel.TILES_X * Globals.TS; // 28*8 = 224
	public static final int CANVAS_HEIGHT_UNSCALED = GameModel.TILES_Y * Globals.TS; // 36*8 = 288

	public static final BooleanProperty PY_SHOW_DEBUG_INFO = new SimpleBooleanProperty(false);

	protected final GameClock clock;
	protected final Map<GameVariant, Map<String, GameScene>> gameScenes = new EnumMap<>(GameVariant.class);
	protected final Theme theme = new Theme();
	protected final SoundHandler soundHandler;
	protected final Stage stage;
	protected final Scene mainScene;
	protected final StartPage startPage;
	protected final GamePage gamePage;
	protected Page currentPage;
	protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");

	public PacManGames2dUI(Stage stage, Settings settings) {
		checkNotNull(stage);
		checkNotNull(settings);

		populateTheme();

		this.stage = stage;
		this.soundHandler = new SoundHandler(theme);
		this.mainScene = createMainScene();
		this.startPage = createStartPage(theme);
		this.gamePage = createGamePage();
		this.clock = createClock();

		addGameScenes();
		configurePacSteering();
		configureStage(settings);
		stage.setScene(mainScene);
	}

	protected void populateTheme() {
		final ResourceManager rm = () -> PacManGames2dUI.class;
		//
		// Common to both games
		//
		theme.set("canvas.background",               ArcadePalette.BLACK);

		theme.set("ghost.0.color",                   ArcadePalette.RED);
		theme.set("ghost.1.color",                   ArcadePalette.PINK);
		theme.set("ghost.2.color",                   ArcadePalette.CYAN);
		theme.set("ghost.3.color",                   ArcadePalette.ORANGE);

		theme.set("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.8));
		theme.set("startpage.button.color",          Color.WHITE);
		theme.set("startpage.button.font",           rm.font("fonts/emulogic.ttf", 30));

		theme.set("wallpaper.background",            rm.imageBackground("graphics/pacman_wallpaper.png"));
		theme.set("wallpaper.color",                 Color.rgb(72, 78, 135));

		theme.set("font.arcade",                     rm.font("fonts/emulogic.ttf", 8));
		theme.set("font.handwriting",                rm.font("fonts/Molle-Italic.ttf", 9));
		theme.set("font.monospaced",                 rm.font("fonts/Inconsolata_Condensed-Bold.ttf", 12));

		theme.set("voice.explain",                   rm.audioClip("sound/voice/press-key.mp3"));
		theme.set("voice.autopilot.off",             rm.audioClip("sound/voice/autopilot-off.mp3"));
		theme.set("voice.autopilot.on",              rm.audioClip("sound/voice/autopilot-on.mp3"));
		theme.set("voice.immunity.off",              rm.audioClip("sound/voice/immunity-off.mp3"));
		theme.set("voice.immunity.on",               rm.audioClip("sound/voice/immunity-on.mp3"));

		//
		// Ms. Pac-Man game
		//
		theme.set("mspacman.startpage.image",        rm.image("graphics/mspacman/wallpaper-midway.png"));
		theme.set("mspacman.helpButton.icon",        rm.image("graphics/icons/help-red-64.png"));

		theme.set("mspacman.spritesheet",            new MsPacManSpriteSheet(rm.image("graphics/mspacman/sprites.png")));
		theme.set("mspacman.icon",                   rm.image("graphics/icons/mspacman.png"));
		theme.set("mspacman.logo.midway",            rm.image("graphics/mspacman/midway.png"));
		theme.set("mspacman.flashingMazes",          rm.image("graphics/mspacman/mazes-flashing.png"));

		theme.set("mspacman.audio.bonus_eaten",      rm.audioClip("sound/mspacman/Fruit.mp3"));
		theme.set("mspacman.audio.credit",           rm.audioClip("sound/mspacman/Credit.mp3"));
		theme.set("mspacman.audio.extra_life",       rm.audioClip("sound/mspacman/ExtraLife.mp3"));
		theme.set("mspacman.audio.game_ready",       rm.audioClip("sound/mspacman/Start.mp3"));
		theme.set("mspacman.audio.game_over",        rm.audioClip("sound/common/game-over.mp3"));
		theme.set("mspacman.audio.ghost_eaten",      rm.audioClip("sound/mspacman/Ghost.mp3"));
		theme.set("mspacman.audio.ghost_returning",  rm.audioClip("sound/mspacman/GhostEyes.mp3"));
		theme.set("mspacman.audio.intermission.1",   rm.audioClip("sound/mspacman/Act1TheyMeet.mp3"));
		theme.set("mspacman.audio.intermission.2",   rm.audioClip("sound/mspacman/Act2TheChase.mp3"));
		theme.set("mspacman.audio.intermission.3",   rm.audioClip("sound/mspacman/Act3Junior.mp3"));
		theme.set("mspacman.audio.level_complete",   rm.audioClip("sound/common/level-complete.mp3"));
		theme.set("mspacman.audio.pacman_death",     rm.audioClip("sound/mspacman/Died.mp3"));
		theme.set("mspacman.audio.pacman_munch",     rm.audioClip("sound/mspacman/Pill.wav"));
		theme.set("mspacman.audio.pacman_power",     rm.audioClip("sound/mspacman/ScaredGhost.mp3"));
		theme.set("mspacman.audio.siren.1",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));
		theme.set("mspacman.audio.siren.2",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		theme.set("mspacman.audio.siren.3",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		theme.set("mspacman.audio.siren.4",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		theme.set("mspacman.audio.sweep",            rm.audioClip("sound/common/sweep.mp3"));

		//
		// Pac-Man game
		//
		theme.set("pacman.startpage.image",          rm.image("graphics/pacman/1980-Flyer-USA-Midway-front.jpg"));
		theme.set("pacman.helpButton.icon",          rm.image("graphics/icons/help-blue-64.png"));

		theme.set("pacman.spritesheet",              new PacManSpriteSheet(rm.image("graphics/pacman/sprites.png")));
		theme.set("pacman.icon",                     rm.image("graphics/icons/pacman.png"));
		theme.set("pacman.flashingMaze",             rm.image("graphics/pacman/maze_empty_flashing.png"));
		theme.set("pacman.fullMaze",                 rm.image("graphics/pacman/maze_full.png"));
		theme.set("pacman.emptyMaze",                rm.image("graphics/pacman/maze_empty.png"));
		theme.set("pacman.maze.foodColor",           Color.rgb(254, 189, 180));

		theme.set("pacman.audio.bonus_eaten",        rm.audioClip("sound/pacman/eat_fruit.mp3"));
		theme.set("pacman.audio.credit",             rm.audioClip("sound/pacman/credit.wav"));
		theme.set("pacman.audio.extra_life",         rm.audioClip("sound/pacman/extend.mp3"));
		theme.set("pacman.audio.game_ready",         rm.audioClip("sound/pacman/game_start.mp3"));
		theme.set("pacman.audio.game_over",          rm.audioClip("sound/common/game-over.mp3"));
		theme.set("pacman.audio.ghost_eaten",        rm.audioClip("sound/pacman/eat_ghost.mp3"));
		theme.set("pacman.audio.ghost_returning",    rm.audioClip("sound/pacman/retreating.mp3"));
		theme.set("pacman.audio.intermission",       rm.audioClip("sound/pacman/intermission.mp3"));
		theme.set("pacman.audio.level_complete",     rm.audioClip("sound/common/level-complete.mp3"));
		theme.set("pacman.audio.pacman_death",       rm.audioClip("sound/pacman/pacman_death.wav"));
		theme.set("pacman.audio.pacman_munch",       rm.audioClip("sound/pacman/doublemunch.wav"));
		theme.set("pacman.audio.pacman_power",       rm.audioClip("sound/pacman/ghost-turn-to-blue.mp3"));
		theme.set("pacman.audio.siren.1",            rm.audioClip("sound/pacman/siren_1.mp3"));
		theme.set("pacman.audio.siren.2",            rm.audioClip("sound/pacman/siren_2.mp3"));
		theme.set("pacman.audio.siren.3",            rm.audioClip("sound/pacman/siren_3.mp3"));
		theme.set("pacman.audio.siren.4",            rm.audioClip("sound/pacman/siren_4.mp3"));
		theme.set("pacman.audio.sweep",              rm.audioClip("sound/common/sweep.mp3"));
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
		for (var gameScene : gameScenes.get(GameVariant.MS_PACMAN).values()) {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.infoVisiblePy.bind(PY_SHOW_DEBUG_INFO);
			}
		}

		gameScenes.put(GameVariant.PACMAN, new HashMap<>(Map.of(
			"boot",   new BootScene(),
			"intro",  new PacManIntroScene(),
			"credit", new PacManCreditScene(),
			"play",   new PlayScene2D(),
			"cut1",   new PacManCutscene1(),
			"cut2",   new PacManCutscene2(),
			"cut3",   new PacManCutscene3()
		)));
		for (var gameScene : gameScenes.get(GameVariant.PACMAN).values()) {
			if (gameScene instanceof GameScene2D gameScene2D) {
				gameScene2D.infoVisiblePy.bind(PY_SHOW_DEBUG_INFO);
			}
		}
	}

	protected GameClock createClock() {
		var clock = new GameClock();
		clock.setOnTick(() -> {
			gameController().update();
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

	protected GamePage createGamePage() {
		var page = new GamePage(this, messageBundles());
		page.setSize(mainScene.getWidth(), mainScene.getHeight());
		gameScenePy.addListener((py, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
		return page;
	}

	public void showStartPage() {
		currentPage = startPage;
		if (clock.isRunning()) {
			clock.stop();
			Logger.info("Clock stopped.");
		}
		mainScene.setRoot(startPage.root());
		updateStage();
		startPage.setGameVariant(gameVariant());
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
		gameController().setManualPacSteering(new KeyboardSteering());
	}

	protected void updateStage() {
		var variantKey = gameVariant() == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey;
		if (clock.isPaused()) {
			titleKey += ".paused";
		}
		stage.setTitle(message(messageBundles(), titleKey));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
	}

	protected GameScene sceneMatchingCurrentGameState() {
		var config = sceneConfig();
		return switch (gameState()) {
			case BOOT              -> config.get("boot");
			case CREDIT            -> config.get("credit");
			case INTRO             -> config.get("intro");
			case INTERMISSION      -> config.get("cut" + (gameLevel().isPresent()? gameLevel().get().intermissionNumber : 1));
			case INTERMISSION_TEST -> config.get("cut" + gameController().intermissionTestNumber);
			default                -> config.get("play");
		};
	}

	protected void updateOrReloadGameScene(boolean reload) {
		var nextGameScene = sceneMatchingCurrentGameState();
		if (nextGameScene == null) {
			throw new IllegalStateException("No game scene found for game state " + gameController().state());
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

	// GameSceneContext interface implementation

	@Override
	public List<ResourceBundle> messageBundles() {
		return List.of(MSG_BUNDLE);
	}

	@Override
	public GameClock gameClock() {
		return clock;
	}

	@Override
	public ActionHandler actionHandler() {
		return this;
	}

	@Override
	public Optional<GameScene> currentGameScene() {
		return Optional.ofNullable(gameScenePy.get());
	}

	@Override
	public Map<String, GameScene> sceneConfig() {
		return gameScenes.get(gameVariant());
	}

	@Override
	public Theme theme() {
		return theme;
	}

	@Override
	public <S extends SpriteSheet> S spriteSheet() {
		return switch (gameVariant()) {
			case MS_PACMAN -> theme.get("mspacman.spritesheet");
			case PACMAN    -> theme.get("pacman.spritesheet");
		};
	}

	@Override
	public SoundHandler soundHandler() {
		return soundHandler;
	}

	// GameEventListener interface implementation

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
					var ss = this.<MsPacManSpriteSheet>spriteSheet();
					level.pac().setAnimations(new MsPacManPacAnimations(level.pac(), ss));
					level.ghosts().forEach(ghost -> ghost.setAnimations(new MsPacManGhostAnimations(ghost, ss)));
					Logger.info("Created Ms. Pac-Man game creature animations for level #{}", level.number());
				}
				case PACMAN -> {
					var ss = this.<PacManSpriteSheet>spriteSheet();
					level.pac().setAnimations(new PacManPacAnimations(level.pac(), ss));
					level.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGhostAnimations(ghost, ss)));
					Logger.info("Created Pac-Man game creature animations for level #{}", level.number());
				}
			}
		});
		updateOrReloadGameScene(true);
	}


	// ActionHandler interface implementation

	@Override
	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	@Override
	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		gamePage.flashMessageView().showMessage(String.format(message, args), seconds);
	}

	@Override
	public void setFullScreen(boolean on) {
		stage.setFullScreen(on);
	}

	@Override
	public void startGame() {
		if (gameController().hasCredit()) {
			soundHandler.stopVoice();
			gameController().startPlaying();
		}
	}

	@Override
	public void startCutscenesTest() {
		gameController().startCutscenesTest(1);
		showFlashMessage("Cut scenes");
	}

	@Override
	public void restartIntro() {
		soundHandler.stopAllSounds();
		currentGameScene().ifPresent(GameScene::end);
		if (game().isPlaying()) {
			gameController().changeCredit(-1);
		}
		gameController().restart(INTRO);
	}

	@Override
	public void reboot() {
		soundHandler.stopAllSounds();
		currentGameScene().ifPresent(GameScene::end);
		soundHandler.playVoice("voice.explain");
		gameController().restart(GameState.BOOT);
	}

	@Override
	public void addCredit() {
		gameController().addCredit();
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
		gameController().startNewGame(gameVariant().next());
		showStartPage();
	}

	@Override
	public void toggleAutopilot() {
		gameController().toggleAutoControlled();
		var auto = gameController().isAutoControlled();
		showFlashMessage(message(messageBundles(), auto ? "autopilot_on" : "autopilot_off"));
		soundHandler.playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off");
	}

	@Override
	public void toggleImmunity() {
		gameController().setImmune(!gameController().isImmune());
		var immune = gameController().isImmune();
		showFlashMessage(message(messageBundles(), immune ? "player_immunity_on" : "player_immunity_off"));
		soundHandler.playVoice(immune ? "voice.immunity.on" : "voice.immunity.off");
	}

	@Override
	public void enterLevel(int newLevelNumber) {
		if (gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		gameLevel().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					game().nextLevel();
				}
				gameController().changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			}
		});
	}

	@Override
	public void startLevelTestMode() {
		if (gameState() == GameState.INTRO) {
			gameController().restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	@Override
	public void cheatAddLives() {
		game().addLives((short) 3);
		showFlashMessage(message(messageBundles(), "cheat_add_lives", game().lives()));
	}

	@Override
	public void cheatEatAllPellets() {
		gameController().cheatEatAllPellets();
	}

	@Override
	public void cheatEnterNextLevel() {
		gameController().cheatEnterNextLevel();
	}

	@Override
	public void cheatKillAllEatableGhosts() {
		gameController().cheatKillAllEatableGhosts();
	}
}