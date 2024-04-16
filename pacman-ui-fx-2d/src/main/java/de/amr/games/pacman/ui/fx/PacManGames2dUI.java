/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariants;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.page.GamePage;
import de.amr.games.pacman.ui.fx.page.Page;
import de.amr.games.pacman.ui.fx.page.StartPage;
import de.amr.games.pacman.ui.fx.rendering2d.*;
import de.amr.games.pacman.ui.fx.scene2d.*;
import de.amr.games.pacman.ui.fx.util.*;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui.fx.util.Keyboard.*;
import static de.amr.games.pacman.ui.fx.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No 3D play scene, no dashboard, no picture-in-picture view.
 *
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameSceneContext, ActionHandler {

    public static final ResourceBundle MSG_BUNDLE = ResourceBundle.getBundle(
        "de.amr.games.pacman.ui.fx.texts.messages", PacManGames2dUI.class.getModule());

    public static final KeyCodeCombination KEY_SHOW_HELP            = just(KeyCode.H);
    public static final KeyCodeCombination KEY_PAUSE                = just(KeyCode.P);
    public static final KeyCodeCombination KEY_QUIT                 = just(KeyCode.Q);
    public static final KeyCodeCombination KEY_SELECT_VARIANT       = just(KeyCode.V);

    public static final KeyCodeCombination KEY_AUTOPILOT            = alt(KeyCode.A);
    public static final KeyCodeCombination KEY_PLAY_CUTSCENES       = alt(KeyCode.C);
    public static final KeyCodeCombination KEY_DEBUG_INFO           = alt(KeyCode.D);
    public static final KeyCodeCombination KEY_CHEAT_EAT_ALL        = alt(KeyCode.E);
    public static final KeyCodeCombination KEY_IMMUNITY             = alt(KeyCode.I);
    public static final KeyCodeCombination KEY_CHEAT_ADD_LIVES      = alt(KeyCode.L);
    public static final KeyCodeCombination KEY_CHEAT_NEXT_LEVEL     = alt(KeyCode.N);
    public static final KeyCodeCombination KEY_TEST_LEVELS          = alt(KeyCode.T);
    public static final KeyCodeCombination KEY_CHEAT_KILL_GHOSTS    = alt(KeyCode.X);

    public static final KeyCodeCombination[] KEYS_SHOW_GAME_PAGE    = {just(KeyCode.SPACE), just(KeyCode.ENTER)};
    public static final KeyCodeCombination[] KEYS_SINGLE_STEP       = {just(KeyCode.SPACE), shift(KeyCode.P)};
    public static final KeyCodeCombination KEY_TEN_STEPS            = shift(KeyCode.SPACE);
    public static final KeyCodeCombination KEY_SIMULATION_FASTER    = alt(KeyCode.PLUS);
    public static final KeyCodeCombination KEY_SIMULATION_SLOWER    = alt(KeyCode.MINUS);
    public static final KeyCodeCombination KEY_SIMULATION_NORMAL    = alt(KeyCode.DIGIT0);
    public static final KeyCodeCombination[] KEYS_START_GAME        = {just(KeyCode.DIGIT1), just(KeyCode.NUMPAD1)};
    public static final KeyCodeCombination[] KEYS_ADD_CREDIT        = {just(KeyCode.DIGIT5), just(KeyCode.NUMPAD5)};
    public static final KeyCodeCombination KEY_BOOT                 = just(KeyCode.F3);
    public static final KeyCodeCombination KEY_FULLSCREEN           = just(KeyCode.F11);

    public static final int CANVAS_WIDTH_UNSCALED = ArcadeWorld.TILES_X * TS; // 28*8 = 224
    public static final int CANVAS_HEIGHT_UNSCALED = ArcadeWorld.TILES_Y * TS; // 36*8 = 288

    public static final BooleanProperty PY_USE_AUTOPILOT   = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_SHOW_DEBUG_INFO = new SimpleBooleanProperty(false);

    private static void loadAssets2D(Theme theme) {
        ResourceManager rm = () -> PacManGames2dUI.class;

        //
        // Common to both games
        //

        theme.set("palette.black",                    Color.rgb(0, 0, 0));
        theme.set("palette.red",                      Color.rgb(255, 0, 0));
        theme.set("palette.yellow",                   Color.rgb(255, 255, 0));
        theme.set("palette.pink",                     Color.rgb(252, 181, 255));
        theme.set("palette.cyan",                     Color.rgb(0, 255, 255));
        theme.set("palette.orange",                   Color.rgb(251, 190, 88));
        theme.set("palette.blue",                     Color.rgb(33, 33, 255));
        theme.set("palette.pale",                     Color.rgb(222, 222, 255));
        theme.set("palette.rose",                     Color.rgb(252, 187, 179));

        theme.set("canvas.background",                theme.color("palette.black"));

        theme.set("ghost.0.color",                    theme.color("palette.red"));
        theme.set("ghost.1.color",                    theme.color("palette.pink"));
        theme.set("ghost.2.color",                    theme.color("palette.cyan"));
        theme.set("ghost.3.color",                    theme.color("palette.orange"));

        theme.set("startpage.button.bgColor",         Color.rgb(0, 155, 252, 0.8));
        theme.set("startpage.button.color",           Color.WHITE);
        theme.set("startpage.button.font",            rm.loadFont("fonts/emulogic.ttf", 30));

        theme.set("wallpaper.background",             rm.imageBackground("graphics/pacman_wallpaper.png"));
        theme.set("wallpaper.color",                  Color.rgb(72, 78, 135));

        theme.set("font.arcade",                      rm.loadFont("fonts/emulogic.ttf", 8));
        theme.set("font.handwriting",                 rm.loadFont("fonts/Molle-Italic.ttf", 9));
        theme.set("font.monospaced",                  rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        theme.set("voice.explain",                    rm.loadAudioClip("sound/voice/press-key.mp3"));
        theme.set("voice.autopilot.off",              rm.loadAudioClip("sound/voice/autopilot-off.mp3"));
        theme.set("voice.autopilot.on",               rm.loadAudioClip("sound/voice/autopilot-on.mp3"));
        theme.set("voice.immunity.off",               rm.loadAudioClip("sound/voice/immunity-off.mp3"));
        theme.set("voice.immunity.on",                rm.loadAudioClip("sound/voice/immunity-on.mp3"));

        //
        // Ms. Pac-Man game
        //
        theme.set("mspacman.startpage.image",         rm.loadImage("graphics/mspacman/mspacman_flyer.png"));
        theme.set("mspacman.helpButton.icon",         rm.loadImage("graphics/icons/help-red-64.png"));

        theme.set("mspacman.spritesheet", new MsPacManGameSpriteSheet(
            rm.loadImage("graphics/mspacman/mspacman_spritesheet.png"),
                rm.loadImage("graphics/mspacman/mazes_flashing.png")
                    ));

        theme.set("mspacman.icon",                    rm.loadImage("graphics/icons/mspacman.png"));
        theme.set("mspacman.logo.midway",             rm.loadImage("graphics/mspacman/midway_logo.png"));

        theme.set("mspacman.audio.bonus_eaten",       rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        theme.set("mspacman.audio.credit",            rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        theme.set("mspacman.audio.extra_life",        rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        theme.set("mspacman.audio.game_ready",        rm.loadAudioClip("sound/mspacman/Start.mp3"));
        theme.set("mspacman.audio.game_over",         rm.loadAudioClip("sound/common/game-over.mp3"));
        theme.set("mspacman.audio.ghost_eaten",       rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        theme.set("mspacman.audio.ghost_returning",   rm.loadAudioClip("sound/mspacman/GhostEyes.mp3"));
        theme.set("mspacman.audio.intermission.1",    rm.loadAudioClip("sound/mspacman/Act1TheyMeet.mp3"));
        theme.set("mspacman.audio.intermission.2",    rm.loadAudioClip("sound/mspacman/Act2TheChase.mp3"));
        theme.set("mspacman.audio.intermission.3",    rm.loadAudioClip("sound/mspacman/Act3Junior.mp3"));
        theme.set("mspacman.audio.level_complete",    rm.loadAudioClip("sound/common/level-complete.mp3"));
        theme.set("mspacman.audio.pacman_death",      rm.loadAudioClip("sound/mspacman/Died.mp3"));
        theme.set("mspacman.audio.pacman_munch",      rm.loadAudioClip("sound/mspacman/Pill.wav"));
        theme.set("mspacman.audio.pacman_power",      rm.loadAudioClip("sound/mspacman/ScaredGhost.mp3"));
        theme.set("mspacman.audio.siren.1",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));
        theme.set("mspacman.audio.siren.2",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("mspacman.audio.siren.3",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("mspacman.audio.siren.4",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("mspacman.audio.sweep",             rm.loadAudioClip("sound/common/sweep.mp3"));

        //
        // Pac-Man game
        //
        theme.set("pacman.startpage.image",           rm.loadImage("graphics/pacman/pacman_flyer.png"));
        theme.set("pacman.helpButton.icon",           rm.loadImage("graphics/icons/help-blue-64.png"));

        theme.set("pacman.spritesheet", new PacManGameSpriteSheet(
            rm.loadImage("graphics/pacman/pacman_spritesheet.png"),
                rm.loadImage("graphics/pacman/maze_flashing.png"))
                    );

        theme.set("pacman.icon",                      rm.loadImage("graphics/icons/pacman.png"));
        theme.set("pacman.maze.foodColor",            Color.rgb(254, 189, 180));

        theme.set("pacman.audio.bonus_eaten",         rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        theme.set("pacman.audio.credit",              rm.loadAudioClip("sound/pacman/credit.wav"));
        theme.set("pacman.audio.extra_life",          rm.loadAudioClip("sound/pacman/extend.mp3"));
        theme.set("pacman.audio.game_ready",          rm.loadAudioClip("sound/pacman/game_start.mp3"));
        theme.set("pacman.audio.game_over",           rm.loadAudioClip("sound/common/game-over.mp3"));
        theme.set("pacman.audio.ghost_eaten",         rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        theme.set("pacman.audio.ghost_returning",     rm.loadAudioClip("sound/pacman/retreating.mp3"));
        theme.set("pacman.audio.intermission",        rm.loadAudioClip("sound/pacman/intermission.mp3"));
        theme.set("pacman.audio.level_complete",      rm.loadAudioClip("sound/common/level-complete.mp3"));
        theme.set("pacman.audio.pacman_death",        rm.loadAudioClip("sound/pacman/pacman_death.wav"));
        theme.set("pacman.audio.pacman_munch",        rm.loadAudioClip("sound/pacman/doublemunch.wav"));
        theme.set("pacman.audio.pacman_power",        rm.loadAudioClip("sound/pacman/ghost-turn-to-blue.mp3"));
        theme.set("pacman.audio.siren.1",             rm.loadAudioClip("sound/pacman/siren_1.mp3"));
        theme.set("pacman.audio.siren.2",             rm.loadAudioClip("sound/pacman/siren_2.mp3"));
        theme.set("pacman.audio.siren.3",             rm.loadAudioClip("sound/pacman/siren_3.mp3"));
        theme.set("pacman.audio.siren.4",             rm.loadAudioClip("sound/pacman/siren_4.mp3"));
        theme.set("pacman.audio.sweep",               rm.loadAudioClip("sound/common/sweep.mp3"));
    }

    protected static final Theme THEME = new Theme();
    static {
        loadAssets2D(THEME);
        Logger.info("2D theme loaded");
    }

    protected final GameClock clock;
    protected final Map<GameVariants, Map<String, GameScene>> gameScenesByVariant = new EnumMap<>(GameVariants.class);
    protected final Stage stage;
    protected final Scene mainScene;
    protected final StartPage startPage;
    protected final GamePage gamePage;
    protected Page currentPage;
    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");
    private AudioClip voiceClip;
    private final Animation voiceClipExecution = new PauseTransition();

    public PacManGames2dUI(Stage stage, Settings settings) {
        checkNotNull(stage);
        checkNotNull(settings);

        this.stage = stage;
        mainScene = createMainScene();
        startPage = createStartPage();
        gamePage  = createGamePage(mainScene);

        Keyboard.handleKeyEventsFor(mainScene);

        clock = new GameClock();
        clock.pausedPy.addListener((py, ov, nv) -> updateStage());
        clock.setOnTick(() -> {
            gameController().update();
            currentGameScene().ifPresent(GameScene::update);
        });
        clock.setOnRender(gamePage::render);

        gameScenesByVariant.put(GameVariants.MS_PACMAN, new HashMap<>(Map.of(
            "boot",   new BootScene(),
            "intro",  new MsPacManIntroScene(),
            "credit", new MsPacManCreditScene(),
            "play",   new PlayScene2D(),
            "cut1",   new MsPacManCutscene1(),
            "cut2",   new MsPacManCutscene2(),
            "cut3",   new MsPacManCutscene3()
        )));
        gameScenesByVariant.put(GameVariants.PACMAN, new HashMap<>(Map.of(
            "boot",   new BootScene(),
            "intro",  new PacManIntroScene(),
            "credit", new PacManCreditScene(),
            "play",   new PlayScene2D(),
            "cut1",   new PacManCutscene1(),
            "cut2",   new PacManCutscene2(),
            "cut3",   new PacManCutscene3()
        )));
        for (Map<String, GameScene> gameSceneMap : gameScenesByVariant.values()) {
            for (var gameScene : gameSceneMap.values()) {
                if (gameScene instanceof GameScene2D gameScene2D) {
                    gameScene2D.infoVisiblePy.bind(PY_SHOW_DEBUG_INFO);
                }
            }
        }

        stage.setFullScreen(settings.fullScreen);
        stage.setMinWidth(CANVAS_WIDTH_UNSCALED);
        stage.setMinHeight(CANVAS_HEIGHT_UNSCALED);
        stage.centerOnScreen();
        stage.setScene(mainScene);
    }

    protected Scene createMainScene() {
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        double height = Math.min(screenHeight * 0.8, 800);
        double width = height * 4.0 / 3.0;
        var scene = new Scene(new Region(), width, height, Color.BLACK);
        scene.widthProperty().addListener((py, ov, nv) -> currentPage.setSize(scene.getWidth(), scene.getHeight()));
        scene.heightProperty().addListener((py, ov, nv) -> currentPage.setSize(scene.getWidth(), scene.getHeight()));
        return scene;
    }

    protected StartPage createStartPage() {
        var startPage = new StartPage(theme());
        startPage.setPlayButtonAction(this::showGamePage);
        startPage.setOnKeyPressed(e -> {
            if (Arrays.stream(KEYS_SHOW_GAME_PAGE).anyMatch(combination -> combination.match(e))) {
                showGamePage();
            } else if (KEY_SELECT_VARIANT.match(e)) {
                switchGameVariant();
            } else if (KEY_FULLSCREEN.match(e)) {
                stage.setFullScreen(true);
            }
        });
        return startPage;
    }

    protected GamePage createGamePage(Scene parentScene) {
        checkNotNull(parentScene);
        var page = new GamePage(this, parentScene.getWidth(), parentScene.getHeight());
        page.setUnscaledCanvasWidth(CANVAS_WIDTH_UNSCALED);
        page.setUnscaledCanvasHeight(CANVAS_HEIGHT_UNSCALED);
        page.setMinScaling(0.7);
        page.setDiscreteScaling(false);
        page.setCanvasBorderEnabled(true);
        page.setCanvasBorderColor(theme().color("palette.pale"));
        page.getCanvasLayer().setBackground(theme().background("wallpaper.background"));
        page.getCanvasContainer().setBackground(ResourceManager.coloredBackground(theme().color("canvas.background")));

        gameScenePy.addListener((py, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
        return page;
    }

    private void setPage(Page page) {
        currentPage = page;
        mainScene.setRoot(page.rootPane());
        page.setSize(mainScene.getWidth(), mainScene.getHeight());
        page.rootPane().requestFocus();
        updateStage();
        stage.show();
    }

    public void showStartPage() {
        if (clock.isRunning()) {
            clock.stop();
            Logger.info("Clock stopped.");
        }
        startPage.setGameVariant(game());
        setPage(startPage);
    }

    public void showGamePage() {
        // call reboot() first such that current game scene is set
        reboot();
        setPage(gamePage);
        clock.start();
        Logger.info("Clock started, speed={} Hz", clock.targetFrameRatePy.get());
    }

    protected void updateStage() {
        String variantKey = game() == GameVariants.MS_PACMAN ? "mspacman" : "pacman";
        String titleKey = "app.title." + variantKey;
        if (clock.isPaused()) {
            titleKey += ".paused";
        }
        stage.setTitle(tt(titleKey));
        stage.getIcons().setAll(THEME.image(variantKey + ".icon"));
    }

    protected GameScene sceneMatchingCurrentGameState() {
        var config = sceneConfig();
        return switch (gameState()) {
            case BOOT -> config.get("boot");
            case CREDIT -> config.get("credit");
            case INTRO -> config.get("intro");
            case INTERMISSION -> config.get("cut" + gameLevel().map(level -> level.intermissionNumber()).orElse((byte) 1));
            case INTERMISSION_TEST -> config.get("cut" + gameState().<Integer>getProperty("intermissionTestNumber"));
            default -> config.get("play");
        };
    }

    protected void updateOrReloadGameScene(boolean reload) {
        var nextGameScene = sceneMatchingCurrentGameState();
        if (nextGameScene == null) {
            throw new IllegalStateException("No game scene found for game state " + gameState());
        }
        Logger.trace("updateOrReloadGameScene({}), scene: {}", reload, nextGameScene.getClass().getSimpleName());
        GameScene prevGameScene = gameScenePy.get();
        if (nextGameScene != prevGameScene || reload) {
            if (prevGameScene != null) {
                prevGameScene.end();
                if (prevGameScene != sceneConfig().get("boot")) {
                    stopVoice();
                }
            }
            nextGameScene.setContext(this);
            nextGameScene.init();
            gameScenePy.set(nextGameScene);
            Logger.trace("Game scene changed to {}", gameScenePy.get());
        }
        updateStage();
    }

    // GameSceneContext interface implementation

    @Override
    public String tt(String key, Object... args) {
        return GameSceneContext.message(List.of(MSG_BUNDLE), key, args);
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
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public Map<String, GameScene> sceneConfig() {
        return gameScenesByVariant.get((GameVariants) game());
    }

    @Override
    public Theme theme() {
        return THEME;
    }

    @Override
    public <S extends SpriteSheet> S spriteSheet() {
        return switch (game()) {
            case GameVariants.MS_PACMAN -> THEME.get("mspacman.spritesheet");
            case GameVariants.PACMAN -> THEME.get("pacman.spritesheet");
            default -> throw new IllegalGameVariantException(game());
        };
    }

    // GameEventListener interface implementation

    @Override
    public void onGameEvent(GameEvent e) {
        Logger.trace("Received: {}", e);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(e);
        updateOrReloadGameScene(false);
        currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(e));
    }

    @Override
    public void onUnspecifiedChange(GameEvent e) {
        updateOrReloadGameScene(true);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            playAudioClip("audio.bonus_eaten");
        }
    }

    @Override
    public void onCreditAdded(GameEvent event) {
        playAudioClip("audio.credit");
    }

    @Override
    public void onExtraLifeWon(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            playAudioClip("audio.extra_life");
        }
    }

    @Override
    public void onGhostEaten(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            playAudioClip("audio.ghost_eaten");
        }
    }

    @Override
    public void onHuntingPhaseStarted(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            level.scatterPhase().ifPresent(this::ensureSirenStarted);
        }
    }

    @Override
    public void onIntermissionStarted(GameEvent event) {
        int intermissionNumber = 0; // 0=undefined
        if (GameController.it().state() == GameState.INTERMISSION_TEST) {
            intermissionNumber = GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber");
        } else {
            GameLevel level = event.game.level().orElse(null);
            if (level != null) {
                intermissionNumber = level.intermissionNumber();
            }
        }
        if (intermissionNumber != 0) {
            switch (game()) {
                case GameVariants.MS_PACMAN -> playAudioClip("audio.intermission." + intermissionNumber);
                case GameVariants.PACMAN -> {
                    var clip = audioClip("audio.intermission");
                    clip.setCycleCount(intermissionNumber == 1 || intermissionNumber == 3 ? 2 : 1);
                    clip.play();
                }
                default -> throw new IllegalGameVariantException(game());
            }
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        // Found no better point in time to create and assign the sprite animations to the guys
        e.game.level().ifPresent(level -> {
            switch (e.game) {
                case GameVariants.MS_PACMAN -> {
                    level.pac().setAnimations(new MsPacManGamePacAnimations(level.pac(), spriteSheet()));
                    level.ghosts().forEach(ghost -> ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, spriteSheet())));
                    Logger.info("Created Ms. Pac-Man game creature animations for level #{}", level.levelNumber);
                }
                case GameVariants.PACMAN -> {
                    level.pac().setAnimations(new PacManGamePacAnimations(level.pac(), spriteSheet()));
                    level.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, spriteSheet())));
                    Logger.info("Created Pac-Man game creature animations for level #{}", level.levelNumber);
                }
                default -> throw new IllegalGameVariantException(e.game);
            }
            if (!level.isDemoLevel()) {
                level.pac().setManualSteering(new KeyboardPacSteering());
            }
        });
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel() && level.levelNumber == 1) {
            playAudioClip("audio.game_ready");
        }
    }

    @Override
    public void onPacDied(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            playAudioClip("audio.pacman_death");
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            //TODO (fixme) this does not sound 100% as in the original game
            ensureAudioLoop("audio.pacman_munch", AudioClip.INDEFINITE);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            stopSirens();
            var clip = audioClip("audio.pacman_power");
            clip.stop();
            clip.setCycleCount(AudioClip.INDEFINITE);
            clip.play();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        var level = event.game.level().orElse(null);
        if (level != null && !level.isDemoLevel()) {
            stopAudioClip("audio.pacman_power");
            ensureSirenStarted(level.huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onStopAllSounds(GameEvent e) {
        stopAllSounds();
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
            stopVoice();
            if (gameState() == GameState.INTRO || gameState() == GameState.CREDIT) {
                gameController().changeState(GameState.READY);
            } else {
                Logger.error("Cannot start playing when in game state {}", gameState());
            }
        }
    }

    @Override
    public void startCutscenesTest() {
        if (gameState() == GameState.INTRO) {
            gameController().changeState(GameState.INTERMISSION_TEST);
        } else {
            Logger.error("Intermission test can only be started from intro screen");
        }
        showFlashMessage("Cut scenes");
    }

    @Override
    public void restartIntro() {
        stopAllSounds();
        currentGameScene().ifPresent(GameScene::end);
        if (game().isPlaying()) {
            gameController().changeCredit(-1);
        }
        gameController().restart(INTRO);
    }

    @Override
    public void reboot() {
        stopAllSounds();
        currentGameScene().ifPresent(GameScene::end);
        playVoice("voice.explain", 0);
        gameController().restart(GameState.BOOT);
    }

    /**
     * Adds credit (simulates insertion of a coin) and switches to the credit scene.
     */
    @Override
    public void addCredit() {
        if (!game().isPlaying()) {
            boolean added = gameController().changeCredit(1);
            if (added) {
                game().publishGameEvent(GameEventType.CREDIT_ADDED);
            }
            if (gameState() != GameState.CREDIT) {
                gameController().changeState(GameState.CREDIT);
            }
        }
    }

    @Override
    public void togglePaused() {
        toggle(clock.pausedPy);
        if (clock.isPaused()) {
            theme().audioClips().forEach(AudioClip::stop);
        }
    }

    @Override
    public void doSimulationSteps(int numSteps) {
        if (clock.isPaused()) {
            if (numSteps == 1) {
                clock.executeSingleStep(true);
            } else {
                clock.executeSteps(numSteps, true);
            }
        }
    }

    @Override
    public void changeSimulationSpeed(int delta) {
        int newRate = clock.targetFrameRatePy.get() + delta;
        if (newRate > 0) {
            clock.targetFrameRatePy.set(newRate);
            showFlashMessageSeconds(0.75, newRate + "Hz");
        }
    }

    @Override
    public void resetSimulationSpeed() {
        clock.targetFrameRatePy.set(GameModel.FPS);
        showFlashMessageSeconds(0.75, clock.targetFrameRatePy.get() + "Hz");
    }

    @Override
    public void switchGameVariant() {
        gameController().selectGame(game() == GameVariants.PACMAN ? GameVariants.MS_PACMAN : GameVariants.PACMAN);
        gameController().restart(GameState.BOOT);
        showStartPage();
    }

    @Override
    public void toggleAutopilot() {
        toggle(PY_USE_AUTOPILOT);
        boolean auto = PY_USE_AUTOPILOT.get();
        showFlashMessage(tt(auto ? "autopilot_on" : "autopilot_off"));
        playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
    }

    @Override
    public void toggleImmunity() {
        gameController().setPacImmune(!gameController().isPacImmune());
        boolean immune = gameController().isPacImmune();
        showFlashMessage(tt(immune ? "player_immunity_on" : "player_immunity_off"));
        playVoice(immune ? "voice.immunity.on" : "voice.immunity.off", 0);
    }

    @Override
    public void enterLevel(int newLevelNumber) {
        if (gameState() == GameState.LEVEL_TRANSITION) {
            return;
        }
        gameLevel().ifPresent(level -> {
            if (newLevelNumber > level.levelNumber) {
                stopAllSounds();
                for (int n = level.levelNumber; n < newLevelNumber - 1; ++n) {
                    game().createAndStartLevel(level.levelNumber + 1);
                }
                gameController().changeState(GameState.LEVEL_TRANSITION);
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
        game().addLives(3);
        showFlashMessage(tt("cheat_add_lives", game().lives()));
    }

    @Override
    public void cheatEatAllPellets() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            gameLevel().ifPresent(level -> {
                level.world().tiles().filter(not(level.world()::isEnergizerTile)).forEach(level.world()::eatFoodAt);
                game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
            });
        }
    }

    @Override
    public void cheatKillAllEatableGhosts() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            gameLevel().ifPresent(level -> {
                level.pac().victims().clear();
                level.killGhosts(level.ghosts(FRIGHTENED, HUNTING_PAC).toList());
                gameController().changeState(GameState.GHOST_DYING);
            });
        }
    }

    @Override
    public void cheatEnterNextLevel() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            gameLevel().ifPresent(level -> {
                stopAllSounds();
                level.world().tiles().forEach(level.world()::eatFoodAt);
                gameController().changeState(GameState.LEVEL_COMPLETE);
            });
        }
    }

    // Sound

    @Override
    public AudioClip audioClip(String key) {
        checkNotNull(key);
        return switch (game()) {
            case GameVariants.MS_PACMAN -> theme().audioClip("mspacman." + key);
            case GameVariants.PACMAN    -> theme().audioClip("pacman." + key);
            default -> throw new IllegalGameVariantException(game());
        };
    }

    @Override
    public void stopAllSounds() {
        theme().audioClips().filter(clip -> clip != voiceClip).forEach(AudioClip::stop);
        Logger.trace("All sounds stopped");
    }

    private void startSiren(int sirenIndex) {
        stopSirens();
        var siren = audioClip("audio.siren." + (sirenIndex + 1));
        siren.setCycleCount(AudioClip.INDEFINITE);
        siren.play();
    }

    private Stream<AudioClip> sirens() {
        return IntStream.rangeClosed(1, 4).mapToObj(i -> audioClip("audio.siren." + i));
    }

    /**
     * @param sirenIndex index of siren (0..3)
     */
    @Override
    public void ensureSirenStarted(int sirenIndex) {
        if (sirens().noneMatch(AudioClip::isPlaying)) {
            startSiren(sirenIndex);
        }
    }

    @Override
    public void stopSirens() {
        sirens().forEach(AudioClip::stop);
    }

    @Override
    public void ensureAudioLoop(String key, int repetitions) {
        var clip = audioClip(key);
        if (!clip.isPlaying()) {
            clip.setCycleCount(repetitions);
            clip.play();
        }
    }

    @Override
    public void ensureAudioLoop(String key) {
        ensureAudioLoop(key, AudioClip.INDEFINITE);
    }

    public void playVoice(String name, double delaySeconds) {
        if (voiceClip != null && voiceClip.isPlaying()) {
            return; // don't interrupt
        }
        voiceClip = theme().audioClip(name);
        voiceClipExecution.setDelay(Duration.seconds(delaySeconds));
        voiceClipExecution.setOnFinished(e -> voiceClip.play());
        voiceClipExecution.play();
    }

    public void stopVoice() {
        if (voiceClip != null && voiceClip.isPlaying()) {
            voiceClip.stop();
        }
        if (voiceClipExecution.getStatus() == Animation.Status.RUNNING) {
            voiceClipExecution.stop();
        }
    }
}