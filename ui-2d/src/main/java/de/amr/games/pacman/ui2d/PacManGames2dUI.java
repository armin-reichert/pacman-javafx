/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.ui2d.rendering.*;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import static de.amr.games.pacman.controller.GameState.*;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No 3D scenes.
 *
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameContext, ActionHandler, SoundHandler {

    public static final BooleanProperty PY_AUTOPILOT           = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_CANVAS_DECORATED    = new SimpleBooleanProperty(true);
    public static final BooleanProperty PY_DEBUG_INFO          = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_IMMUNITY            = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            GameController.it().setPacImmune(get());
        }
    };
    public static final IntegerProperty PY_PIP_HEIGHT          = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_Y);
    public static final BooleanProperty PY_PIP_ON              = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_OPACITY_PERCENT = new SimpleIntegerProperty(100);
    public static final IntegerProperty PY_SIMULATION_STEPS    = new SimpleIntegerProperty(1);

    public final ObjectProperty<GameVariant> gameVariantPy     = new SimpleObjectProperty<>(this, "gameVariant");
    public final ObjectProperty<GameScene>   gameScenePy       = new SimpleObjectProperty<>(this, "gameScene");
    public final BooleanProperty             scoreVisiblePy    = new SimpleBooleanProperty(this, "scoreVisible");
    public final BooleanProperty             mutePy            = new SimpleBooleanProperty(this, "mute", false);

    protected final Theme theme = new Theme();
    protected final GameSceneManager gameSceneManager = new GameSceneManager();
    protected final List<ResourceBundle> bundles = new ArrayList<>();

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane rootPane = new StackPane();
    protected final FlashMessageView messageView = new FlashMessageView();
    protected GameClockFX clock;
    protected TileMapEditor editor;

    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;

    protected MediaPlayer voice;
    protected MediaPlayer startGameSound;
    protected final MediaPlayer[] sirens = new MediaPlayer[4];
    protected MediaPlayer munchingSound;
    protected MediaPlayer powerSound;
    protected MediaPlayer intermissionSound;

    public void loadAssets() {
        bundles.add(ResourceBundle.getBundle("de.amr.games.pacman.ui2d.texts.messages", PacManGames2dUI.class.getModule()));
        ResourceManager rm = () -> PacManGames2dUI.class;

        // Dashboard

        theme.set("image.armin1970",                 rm.loadImage("graphics/armin.jpg"));
        theme.set("icon.mute",                       rm.loadImage("graphics/icons/mute.png"));
        theme.set("icon.play",                       rm.loadImage("graphics/icons/play.png"));
        theme.set("icon.stop",                       rm.loadImage("graphics/icons/stop.png"));
        theme.set("icon.step",                       rm.loadImage("graphics/icons/step.png"));

        theme.set("infobox.min_col_width",           200);
        theme.set("infobox.min_label_width",         140);
        theme.set("infobox.text_color",              Color.WHITE);
        theme.set("infobox.label_font",              Font.font("Sans", 12));
        theme.set("infobox.text_font",               rm.loadFont("fonts/SplineSansMono-Regular.ttf", 12));

        //
        // Common to all game variants
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

        theme.set("canvas.background",                Color.rgb(0,0,0));

        theme.set("startpage.button.bgColor",         Color.rgb(0, 155, 252, 0.8));
        theme.set("startpage.button.color",           Color.WHITE);
        theme.set("startpage.button.font",            rm.loadFont("fonts/emulogic.ttf", 30));

        theme.set("wallpaper.background",             Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        theme.set("wallpaper.color",                  Color.rgb(72, 78, 135));

        theme.set("font.arcade",                      rm.loadFont("fonts/emulogic.ttf", 8));
        theme.set("font.handwriting",                 rm.loadFont("fonts/Molle-Italic.ttf", 9));
        theme.set("font.monospaced",                  rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        theme.set("voice.explain",                    rm.url("sound/voice/press-key.mp3"));
        theme.set("voice.autopilot.off",              rm.url("sound/voice/autopilot-off.mp3"));
        theme.set("voice.autopilot.on",               rm.url("sound/voice/autopilot-on.mp3"));
        theme.set("voice.immunity.off",               rm.url("sound/voice/immunity-off.mp3"));
        theme.set("voice.immunity.on",                rm.url("sound/voice/immunity-on.mp3"));

        //
        // Ms. Pac-Man game
        //

        theme.set("ms_pacman.spritesheet",             new MsPacManGameSpriteSheet());
        theme.set("ms_pacman.startpage.image",         rm.loadImage("graphics/mspacman/mspacman_flyer.png"));
        theme.set("ms_pacman.helpButton.icon",         rm.loadImage("graphics/icons/help-red-64.png"));
        theme.set("ms_pacman.icon",                    rm.loadImage("graphics/icons/mspacman.png"));
        theme.set("ms_pacman.logo.midway",             rm.loadImage("graphics/mspacman/midway_logo.png"));

        theme.set("ms_pacman.audio.bonus_eaten",       rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        theme.set("ms_pacman.audio.credit",            rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        theme.set("ms_pacman.audio.extra_life",        rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        theme.set("ms_pacman.audio.game_over",         rm.loadAudioClip("sound/common/game-over.mp3"));
        theme.set("ms_pacman.audio.ghost_eaten",       rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        theme.set("ms_pacman.audio.ghost_returning",   rm.loadAudioClip("sound/mspacman/GhostEyes.mp3"));
        theme.set("ms_pacman.audio.level_complete",    rm.loadAudioClip("sound/common/level-complete.mp3"));
        theme.set("ms_pacman.audio.pacman_death",      rm.loadAudioClip("sound/mspacman/Died.mp3"));
        theme.set("ms_pacman.audio.sweep",             rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        theme.set("ms_pacman.audio.game_ready",        rm.url("sound/mspacman/Start.mp3"));
        theme.set("ms_pacman.audio.intermission.1",    rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        theme.set("ms_pacman.audio.intermission.2",    rm.url("sound/mspacman/Act2TheChase.mp3"));
        theme.set("ms_pacman.audio.intermission.3",    rm.url("sound/mspacman/Act3Junior.mp3"));
        theme.set("ms_pacman.audio.pacman_munch",      rm.url("sound/mspacman/Pill.wav"));
        theme.set("ms_pacman.audio.pacman_power",      rm.url("sound/mspacman/ScaredGhost.mp3"));
        theme.set("ms_pacman.audio.siren.1",           rm.url("sound/mspacman/GhostNoise1.wav"));
        theme.set("ms_pacman.audio.siren.2",           rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("ms_pacman.audio.siren.3",           rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("ms_pacman.audio.siren.4",           rm.url("sound/mspacman/GhostNoise1.wav"));// TODO

        //
        // Pac-Man game
        //

        theme.set("pacman.spritesheet",               new PacManGameSpriteSheet());
        theme.set("pacman.startpage.image",           rm.loadImage("graphics/pacman/pacman_flyer.png"));
        theme.set("pacman.helpButton.icon",           rm.loadImage("graphics/icons/help-blue-64.png"));
        theme.set("pacman.icon",                      rm.loadImage("graphics/icons/pacman.png"));


        theme.set("pacman.audio.bonus_eaten",         rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        theme.set("pacman.audio.credit",              rm.loadAudioClip("sound/pacman/credit.wav"));
        theme.set("pacman.audio.extra_life",          rm.loadAudioClip("sound/pacman/extend.mp3"));
        theme.set("pacman.audio.game_over",           rm.loadAudioClip("sound/common/game-over.mp3"));
        theme.set("pacman.audio.ghost_eaten",         rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        theme.set("pacman.audio.ghost_returning",     rm.loadAudioClip("sound/pacman/retreating.mp3"));
        theme.set("pacman.audio.level_complete",      rm.loadAudioClip("sound/common/level-complete.mp3"));
        theme.set("pacman.audio.pacman_death",        rm.loadAudioClip("sound/pacman/pacman_death.wav"));
        theme.set("pacman.audio.sweep",               rm.loadAudioClip("sound/common/sweep.mp3"));

        theme.set("pacman.audio.game_ready",          rm.url("sound/pacman/game_start.mp3"));
        theme.set("pacman.audio.intermission",        rm.url("sound/pacman/intermission.mp3"));
        theme.set("pacman.audio.pacman_munch",        rm.url("sound/pacman/doublemunch.wav")); //TODO improve
        theme.set("pacman.audio.pacman_power",        rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        theme.set("pacman.audio.siren.1",             rm.url("sound/pacman/siren_1.mp3"));
        theme.set("pacman.audio.siren.2",             rm.url("sound/pacman/siren_2.mp3"));
        theme.set("pacman.audio.siren.3",             rm.url("sound/pacman/siren_3.mp3"));
        theme.set("pacman.audio.siren.4",             rm.url("sound/pacman/siren_4.mp3"));

        //
        // Pac-Man XXL
        //
        theme.set("pacman_xxl.icon",                 rm.loadImage("graphics/icons/pacman.png"));
        theme.set("pacman_xxl.helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        theme.set("pacman_xxl.startpage.image",      rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));
    }

    protected void logAssets() {
        Logger.info("Assets loaded: {}", theme.summary(List.of(
            new Pair<>(Image.class, "images"),
            new Pair<>(Font.class, "fonts"),
            new Pair<>(Color.class, "colors"),
            new Pair<>(AudioClip.class, "audio clips")
        )));
    }

    public void createUI(Stage stage, Rectangle2D screenSize) {
        this.stage = checkNotNull(stage);

        logAssets();

        Image muteImage = theme.get("icon.mute");
        ImageView muteIcon = new ImageView(muteImage);
        muteIcon.setFitWidth(48);
        muteIcon.setPreserveRatio(true);
        muteIcon.visibleProperty().bind(mutePy);
        StackPane.setAlignment(muteIcon, Pos.BOTTOM_RIGHT);

        // first child will be replaced by page
        rootPane.getChildren().addAll(new Pane(), messageView, muteIcon);

        gameVariantPy.set(game().variant());
        for (var variant : gameController().supportedVariants()) {
            gameController().game(variant).addGameEventListener(this);
        }

        // Touch all game keys such that they get registered with keyboard
        for (var gameKey : GameKeys.values()) {
            Logger.debug("Game key '{}' registered", gameKey);
        }

        gameVariantPy.addListener((py,ov,nv) -> clearSounds());

        createMainScene(computeMainSceneSize(screenSize));
        createStartPage();
        createGamePage();
        createGameScenes();
        createGameClock();

        stage.titleProperty().bind(stageTitleBinding());
        stage.getIcons().setAll(theme.image(game().variant().resourceKey() + ".icon"));
        stage.setScene(mainScene);
        selectStartPage();
    }

    public void show() {
        //TODO this does not work yet correctly
        Dimension2D minSize = DecoratedCanvas.computeSize(
            GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y, 1);
        stage.setMinWidth(minSize.getWidth());
        stage.setMinHeight(minSize.getHeight());
        stage.centerOnScreen();
        stage.show();
    }

    protected Dimension2D computeMainSceneSize(Rectangle2D screenSize) {
        double height = 0.9 * screenSize.getHeight(), width = 0.9 * height;
        return new Dimension2D(width, height);
    }

    protected void createMainScene(Dimension2D size) {
        mainScene = new Scene(rootPane, size.getWidth(), size.getHeight());
        mainScene.setOnMouseClicked(e -> currentPage.onMouseClicked(e));
        mainScene.setOnContextMenuRequested(e -> currentPage.onContextMenuRequested(e));
        mainScene.setOnKeyPressed(this::handleKeyPressed);
        Keyboard.filterKeyEventsFor(mainScene);
        ChangeListener<Number> resizeCurrentPage = (py, ov, nv) -> {
            if (currentPage != null) {
                currentPage.setSize(mainScene.getWidth(), mainScene.getHeight());
            }
        };
        mainScene.widthProperty().addListener(resizeCurrentPage);
        mainScene.heightProperty().addListener(resizeCurrentPage);
    }

    protected void handleKeyPressed(KeyEvent e) {
        if (GameKeys.MUTE.pressed()) {
            mute(!isMuted());
        }
        currentPage.handleKeyboardInput();
    }

    protected void createGameClock() {
        clock = new GameClockFX();
        clock.setPauseableCallback(() -> {
            try {
                gameController().update();
                currentGameScene().ifPresent(GameScene::update);
            } catch (Exception x) {
                Logger.error("Error during game update");
                Logger.error(x);
                clock.stop();
            }
        });
        clock.setContinousCallback(() -> {
            try {
                if (currentPage == gamePage) {
                    messageView.update();
                    gamePage.render();
                }
            } catch (Exception x) {
                Logger.error("Error during game rendering");
                Logger.error(x);
                clock.stop();
            }
        });
        gameController().setClock(clock);
    }

    protected void createGameScenes() {
        for (var variant : gameController().supportedVariants()) {
            gameSceneManager.createGameScenes(variant);
            gameSceneManager.gameScenes2D(variant).forEach(gameScene2D -> {
                gameScene2D.setContext(this);
                gameScene2D.infoVisiblePy.bind(PY_DEBUG_INFO);
            });
        }
    }

    protected StringBinding stageTitleBinding() {
        return Bindings.createStringBinding(
            () -> tt("app.title." + gameVariantPy.get().resourceKey() + (clock.pausedPy.get() ? ".paused" : ""), "2D"),
            clock.pausedPy, gameVariantPy);
    }

    protected void createStartPage() {
        startPage = new StartPage(this);
        startPage.setOnPlayButtonPressed(this::selectGamePage);
        startPage.gameVariantPy.bind(gameVariantPy);
    }

    protected void createGamePage() {
        gamePage = new GamePage(this, mainScene);
        gamePage.canvasPane().decoratedCanvas().decoratedPy.bind(PY_CANVAS_DECORATED);
        gameScenePy.addListener((py, ov, newGameScene) -> {
            if (newGameScene instanceof GameScene2D scene2D) {
                gamePage.embedGameScene2D(scene2D);
            }
        });
    }

    public void sign(String signature) {
        gamePage.configureSignature(theme.font("font.monospaced", 9), signature);
    }

    private void createEditorPage() {
        editor = new TileMapEditor(GameModel.CUSTOM_MAP_DIR);
        editor.createUI(stage);

        var miQuitEditor = new MenuItem(tt("back_to_game"));
        miQuitEditor.setOnAction(e -> quitMapEditor());
        editor.menuFile().getItems().add(miQuitEditor);

        // load maps from core module
        editor.addLoadMapMenuEntry("Pac-Man", loadMap("pacman.world"));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 6; ++mapNumber) {
            WorldMap map = loadMap("mspacman/mspacman_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuEntry("Ms. Pac-Man " + mapNumber, map);
            }
        }
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        for (int mapNumber = 1; mapNumber <= 8; ++mapNumber) {
            WorldMap map = loadMap("masonic/masonic_%d.world".formatted(mapNumber));
            if (map != null) {
                editor.addLoadMapMenuEntry("Pac-Man XXL " + mapNumber, map);
            }
        }
        editorPage = new EditorPage(editor, this);
    }

    private WorldMap loadMap(String relativeMapPath) {
        ResourceManager core = () -> GameModel.class;
        URL url = core.url("/de/amr/games/pacman/maps/" + relativeMapPath);
        if (url != null) {
            WorldMap map = new WorldMap(url);
            Logger.info("Map loaded from URL {}", url);
            return map;
        }
        Logger.error("Could not find map at path {}", relativeMapPath);
        return null;
    }

    private void selectPage(Page page) {
        if (page != currentPage) {
            currentPage = page;
            currentPage.setSize(mainScene.getWidth(), mainScene.getHeight());
            rootPane.getChildren().set(0, currentPage.rootPane());
            currentPage.rootPane().requestFocus();
            currentPage.onSelected();
        }
    }

    protected void updateGameScene(boolean reloadCurrentScene) {
        if (isPageSelected(startPage)) {
            return; // no game scene on start page
        }
        GameScene sceneToDisplay = gameSceneForCurrentGameState();
        GameScene currentScene = gameScenePy.get();
        if (reloadCurrentScene || sceneToDisplay != currentScene) {
            Logger.info("updateGameScene: {}/{} reload={}", currentPage, sceneToDisplay.getClass().getSimpleName(), reloadCurrentScene);
            if (currentScene != null) {
                currentScene.end();
            }
            sceneToDisplay.init();
            gameScenePy.set(sceneToDisplay);
            if (sceneToDisplay == currentScene) {
                Logger.info("Game scene has been reloaded {}", gameScenePy.get());
            } else {
                Logger.info("Game scene changed to {}/{}", currentPage, gameScenePy.get());
            }
        }
    }

    protected GameScene gameSceneForCurrentGameState() {
        GameVariant variant = game().variant();
        return switch (gameState()) {
            case BOOT -> gameSceneManager.gameScene(variant, GameSceneID.BOOT_SCENE);
            case CREDIT -> gameSceneManager.gameScene(variant, GameSceneID.CREDIT_SCENE);
            case INTRO -> gameSceneManager.gameScene(variant, GameSceneID.INTRO_SCENE);
            case INTERMISSION -> gameSceneManager.gameScene(variant, GameSceneID.valueOf(
                "CUT_SCENE_" + game().intermissionNumber(game().levelNumber())));
            case INTERMISSION_TEST -> gameSceneManager.gameScene(variant, GameSceneID.valueOf(
                "CUT_SCENE_" + gameState().<Integer>getProperty("intermissionTestNumber")));
            default -> gameSceneManager.gameScene(variant, GameSceneID.PLAY_SCENE);
        };
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameSceneContext interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public String tt(String key, Object... args) {
        checkNotNull(key);
        for (var bundle : bundles) {
            if (bundle.containsKey(key)) {
                return MessageFormat.format(bundle.getString(key), args);
            }
        }
        Logger.error("Missing localized text for key {}", key);
        return null;
    }

    @Override
    public GameClockFX gameClock() {
        return clock;
    }

    @Override
    public ActionHandler actionHandler() {
        return this;
    }

    @Override
    public SoundHandler soundHandler() {
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
    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
    }

    @Override
    public Theme theme() {
        return theme;
    }

    @Override
    public GameSpriteSheet getSpriteSheet(GameVariant variant) {
        var rk = switch (variant) {
            case MS_PACMAN -> variant.resourceKey();
            case PACMAN, PACMAN_XXL -> GameVariant.PACMAN.resourceKey();
        };
        return theme.get(rk + ".spritesheet");
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisiblePy.get();
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisiblePy.set(visible);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("Received: {}", event);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(event);
        if (gameState() != null) {
            //TODO check this. On game start, event can be published before game state has been initialized!
            updateGameScene(false);
            currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
        }
    }

    @Override
    public void onGameVariantChanged(GameEvent event) {
        var newVariant = game().variant();
        Logger.info("onGameVariantChanged: {}", event);
        gameVariantPy.set(newVariant);
        stage.getIcons().setAll(theme.image(newVariant.resourceKey() + ".icon"));
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        if (!game().isDemoLevel()) {
            playAudioClip("audio.bonus_eaten");
        }
    }

    @Override
    public void onCreditAdded(GameEvent event) {
        playAudioClip("audio.credit");
    }

    @Override
    public void onExtraLifeWon(GameEvent event) {
        if (!game().isDemoLevel()) {
            playAudioClip("audio.extra_life");
        }
    }

    @Override
    public void onGhostEaten(GameEvent event) {
        if (!game().isDemoLevel()) {
            playAudioClip("audio.ghost_eaten");
        }
    }

    @Override
    public void onHuntingPhaseStarted(GameEvent event) {
        if (!game().isDemoLevel()) {
            game().scatterPhase().ifPresent(this::ensureSirenPlaying);
        }
    }

    @Override
    public void onIntermissionStarted(GameEvent event) {
        if (gameState() == GameState.INTERMISSION_TEST) {
            playIntermissionSound(GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber")); //TODO ugly
        } else {
            playIntermissionSound(game().intermissionNumber(game().levelNumber()));
        }
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        // Found no better point in time to create and assign the sprite animations to the guys
        GameModel game = event.game;
        switch (game.variant()) {
            case MS_PACMAN -> {
                var ss = (MsPacManGameSpriteSheet) getSpriteSheet(game.variant());
                game.pac().setAnimations(new MsPacManGamePacAnimations(game.pac(), ss));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, ss)));
                Logger.info("Created Ms. Pac-Man game creature animations for level #{}", game.levelNumber());
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) getSpriteSheet(game.variant());
                game.pac().setAnimations(new PacManGamePacAnimations(game.pac(), ss));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, ss)));
                Logger.info("Created Pac-Man game creature animations for level #{}", game.levelNumber());
            }
        }
        if (!game.isDemoLevel()) {
            game.pac().setManualSteering(new KeyboardPacSteering());
        }
        //TODO better place than here?
        gamePage.adaptCanvasSizeToCurrentWorld();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        if (gameState() == LEVEL_TEST || game().isDemoLevel() || game().levelNumber() > 1) {
            return;
        }
        soundHandler().playStartGameSound();
    }

    @Override
    public void onPacDied(GameEvent event) {
        if (!game().isDemoLevel()) {
            playAudioClip("audio.pacman_death");
            stop(munchingSound);
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (!game().isDemoLevel()) {
            playMunchingSound();
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        if (!game().isDemoLevel()) {
            stopSirens();
            soundHandler().playPowerSound();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        if (!game().isDemoLevel()) {
            soundHandler().stop(powerSound);
            ensureSirenPlaying(game().huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        stopAllSounds();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ActionHandler interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void showSignature() {
        gamePage.signature().show(2, 3);
    }

    @Override
    public void hideSignature() {
        gamePage.signature().hide();
    }

    @Override
    public void selectStartPage() {
        selectPage(startPage);
    }

    @Override
    public void selectGamePage() {
        selectPage(gamePage);
    }

    @Override
    public void selectEditorPage() {
        selectPage(editorPage);
    }

    @Override
    public boolean isPageSelected(Page page) {
        return page == currentPage;
    }

    @Override
    public void showFlashMessage(String message, Object... args) {
        showFlashMessageSeconds(1, message, args);
    }

    @Override
    public void showFlashMessageSeconds(double seconds, String message, Object... args) {
        messageView.showMessage(String.format(message, args), seconds);
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
        PY_IMMUNITY.set(gameController().isPacImmune());
    }

    @Override
    public void openMapEditor() {
        if (game().variant() != GameVariant.PACMAN_XXL) {
            showFlashMessageSeconds(3, "Map editor is not available in this game variant");
            return;
        }
        if (editorPage == null) {
            createEditorPage();
        }
        stopAllSounds();
        currentGameScene().ifPresent(GameScene::end);
        clock.stop();
        stage.titleProperty().bind(editor.titlePy);
        if (game().world() != null) {
            editor.setMap(game().world().map());
        }
        editor.start();
        selectPage(editorPage);
        reboot();
    }

    @Override
    public void quitMapEditor() {
        editor.showConfirmation(editor::saveMapFileAs,
            () -> stage.titleProperty().bind(stageTitleBinding()));
        editor.stop();
        gameController().loadCustomMaps();
        selectPage(startPage);
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
        if (gameState() == LEVEL_TEST) {
            gameState().onExit(game()); //TODO exit other states too?
        }
        clock.setTargetFrameRate(GameModel.FPS);
        gameController().restart(INTRO);
    }

    @Override
    public void reboot() {
        stopAllSounds();
        currentGameScene().ifPresent(GameScene::end);
        game().removeWorld();
        clock.setTargetFrameRate(GameModel.FPS);
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
        Ufx.toggle(clock.pausedPy);
        if (clock.isPaused()) {
            theme().audioClips().forEach(AudioClip::stop);
            stopSirens();
        }
        Logger.info("Game variant ({}) {}", game(), clock.isPaused() ? "paused" : "resumed");
    }


    @Override
    public void toggleDashboard() {
        gamePage.dashboard().toggleVisibility();
    }

    @Override
    public void toggleDrawMode() {
        // not supported in 2D UI
    }

    @Override
    public void togglePipVisible() {
        toggle(PY_PIP_ON);
        showFlashMessage(tt(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
    }

    @Override
    public void doSimulationSteps(int numSteps) {
        if (clock.isPaused()) {
            if (numSteps == 1) {
                clock.makeStep(true);
            } else {
                clock.makeSteps(numSteps, true);
            }
        }
    }

    @Override
    public void changeSimulationSpeed(int delta) {
        double newRate = clock.getTargetFrameRate() + delta;
        if (newRate > 0) {
            clock.setTargetFrameRate(newRate);
            showFlashMessageSeconds(0.75, newRate + "Hz");
        }
    }

    @Override
    public void resetSimulationSpeed() {
        clock.setTargetFrameRate(GameModel.FPS);
        showFlashMessageSeconds(0.75, clock.getTargetFrameRate() + "Hz");
    }

    @Override
    public void selectNextGameVariant() {
        var all = gameController().supportedVariants();
        var current = all.indexOf(game().variant());
        var next = current < all.size() - 1 ? all.get(current + 1) : all.get(0);
        selectGameVariant(next);
    }

    @Override
    public void selectPrevGameVariant() {
        var gameVariants = gameController().supportedVariants();
        var current = gameVariants.indexOf(game().variant());
        var prev = current > 0 ? gameVariants.get(current - 1) : gameVariants.get(gameVariants.size() - 1);
        selectGameVariant(prev);
    }

    private void selectGameVariant(GameVariant variant) {
        gameController().selectGameVariant(variant);
        gameController().restart(GameState.BOOT);
    }

    @Override
    public void selectNextPerspective() {
    }

    @Override
    public void selectPrevPerspective() {
    }

    @Override
    public void toggleAutopilot() {
        Ufx.toggle(PY_AUTOPILOT);
        boolean auto = PY_AUTOPILOT.get();
        showFlashMessage(tt(auto ? "autopilot_on" : "autopilot_off"));
        playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
    }

    @Override
    public void toggle2D3D() {
        // not supported
    }

    @Override
    public void toggleImmunity() {
        Ufx.toggle(PY_IMMUNITY);
        showFlashMessage(tt(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
    }

    @Override
    public void startLevelTestMode() {
        if (gameState() == GameState.INTRO) {
            gameController().restart(GameState.LEVEL_TEST);
            showFlashMessageSeconds(3, "Level TEST MODE");
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
            World world = game().world();
            world.tiles().filter(not(world::isEnergizerTile)).forEach(world::eatFoodAt);
            game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
        }
    }

    @Override
    public void cheatKillAllEatableGhosts() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            game().victims().clear();
            game().ghosts(FRIGHTENED, HUNTING_PAC).forEach(game()::killGhost);
            gameController().changeState(GameState.GHOST_DYING);
        }
    }

    @Override
    public void cheatEnterNextLevel() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            World world = game().world();
            stopAllSounds();
            world.tiles().forEach(world::eatFoodAt);
            gameController().changeState(GameState.LEVEL_COMPLETE);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // SoundHandler interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public boolean isMuted() {
        return mutePy.get();
    }

    @Override
    public void mute(boolean muted) {
        mutePy.set(muted);
        Logger.info(muted? "Muted" : "Unmuted");
    }

    @Override
    public AudioClip audioClip(String key) {
        checkNotNull(key);
        String rk = game().variant() == GameVariant.PACMAN_XXL ? GameVariant.PACMAN.resourceKey() : game().variant().resourceKey();
        return theme.audioClip(rk + "." + key);
    }

    @Override
    public void playAudioClip(String key) {
        AudioClip clip = audioClip(key);
        if (clip != null && !isMuted()) {
            clip.setVolume(0.5);
            clip.play();
        }
    }

    @Override
    public void stopAudioClip(String key) {
        AudioClip clip = audioClip(key);
        if (clip != null) {
            clip.stop();
        }
    }

    @Override
    public void ensureAudioClipRepeats(String key, int repetitions) {
        var clip = audioClip(key);
        if (clip != null && !isMuted()) {
            if (!clip.isPlaying()) {
                clip.setCycleCount(repetitions);
                clip.setVolume(0.5);
                clip.play();
            }
        }
    }

    @Override
    public void ensureAudioClipLoops(String key) {
        ensureAudioClipRepeats(key, AudioClip.INDEFINITE);
    }

    @Override
    public void stopAllSounds() {
        stop(startGameSound);
        stop(munchingSound);
        stop(powerSound);
        stop(intermissionSound);
        stopSirens();
        stopVoice();
        theme.audioClips().forEach(AudioClip::stop);
        Logger.info("All sounds stopped");
    }

    /**
     * Clear media players, they get recreated for the current game variant on demand.
     */
    private void clearSounds() {
        startGameSound = null;
        munchingSound = null;
        powerSound = null;
        intermissionSound = null;
        Arrays.fill(sirens, null);
    }

    /**
     * @param sirenIndex index of siren (0..3)
     */
    @Override
    public void ensureSirenPlaying(int sirenIndex) {
        MediaPlayer siren = sirens[sirenIndex];
        if  (siren == null) {
            URL url = theme.get(rk() + ".audio.siren." + (sirenIndex + 1));
            siren = new MediaPlayer(new Media(url.toExternalForm()));
            siren.muteProperty().bind(mutePy);
            siren.setCycleCount(MediaPlayer.INDEFINITE);
            siren.statusProperty().addListener((py,ov,nv) -> logSound());
            sirens[sirenIndex] = siren;
        }
        if (siren.getStatus() != MediaPlayer.Status.PLAYING && siren.getStatus() != MediaPlayer.Status.READY) {
            stopSirens();
            siren.setVolume(0.33);
            siren.play();
        }
    }

    @Override
    public void stopSirens() {
        for (var siren : sirens) {
            if (siren != null) {
                siren.stop();
            }
        }
    }

    @Override
    public void playStartGameSound() {
        if (startGameSound == null) {
            URL url = theme.get(rk() + ".audio.game_ready");
            startGameSound = new MediaPlayer(new Media(url.toExternalForm()));
            startGameSound.setVolume(0.5);
            startGameSound.muteProperty().bind(mutePy);
        }
        startGameSound.play();
    }

    @Override
    public void playMunchingSound() {
        if (munchingSound == null) {
            URL url = theme.get(rk() + ".audio.pacman_munch");
            munchingSound = new MediaPlayer(new Media(url.toExternalForm()));
            munchingSound.muteProperty().bind(mutePy);
            munchingSound.setVolume(0.5);
            munchingSound.setCycleCount(MediaPlayer.INDEFINITE);
            munchingSound.statusProperty().addListener((py, ov, nv) -> logSound());
        }
        munchingSound.play();
    }

    @Override
    public void stopMunchingSound() {
        stop(munchingSound);
    }

    @Override
    public void playPowerSound() {
        if (powerSound == null) {
            URL url = theme.get(rk() + ".audio.pacman_power");
            powerSound = new MediaPlayer(new Media(url.toExternalForm()));
            powerSound.muteProperty().bind(mutePy);
            powerSound.setVolume(0.5);
            powerSound.setCycleCount(MediaPlayer.INDEFINITE);
            powerSound.statusProperty().addListener((py, ov, nv) -> logSound());
        }
        powerSound.play();
    }

    @Override
    public void playIntermissionSound(int number) {
        switch (game().variant()) {
            case MS_PACMAN -> {
                URL url = theme.get(rk() + ".audio.intermission." + number);
                intermissionSound = new MediaPlayer(new Media(url.toExternalForm()));
                intermissionSound.muteProperty().bind(mutePy);
                intermissionSound.setVolume(0.5);
                intermissionSound.play();
            }
            case PACMAN, PACMAN_XXL -> {
                URL url = theme.get(rk() + ".audio.intermission");
                intermissionSound = new MediaPlayer(new Media(url.toExternalForm()));
                intermissionSound.muteProperty().bind(mutePy);
                intermissionSound.setVolume(0.5);
                intermissionSound.setCycleCount(number == 1 || number == 3 ? 2 : 1);
                intermissionSound.play();
            }
        }
    }

    @Override
    public void playVoice(String voiceKey, double delaySeconds) {
        if (voice != null && voice.getStatus() == MediaPlayer.Status.PLAYING) {
            Logger.info("Cannot play voice {}, another voice is already playing", voiceKey);
            return;
        }
        URL url = theme.get(voiceKey);
        voice = new MediaPlayer(new Media(url.toExternalForm()));
        voice.muteProperty().bind(mutePy);
        voice.setStartTime(Duration.seconds(delaySeconds));
        voice.play();
    }

    @Override
    public void stopVoice() {
        if (voice != null) {
            voice.stop();
        }
    }

    /**
     * @return prefix for sound URL for current game variant. PACMAN_XXL uses sounds from PACMAN.
     */
    private String rk() {
        return game().variant() == GameVariant.PACMAN_XXL ? GameVariant.PACMAN.resourceKey() : game().variant().resourceKey();
    }

    private void logSound() {
        for (int number = 1; number <= 4; ++number) {
            if (sirens[number-1] != null) {
                Logger.info("Siren {}: status {} volume {}",
                        number, sirens[number-1].getStatus(), sirens[number-1].getVolume());
            } else {
                Logger.info("Siren {}: not yet created", number);
            }
        }
        if (munchingSound != null) {
            Logger.info("Munching: status {} volume {}", munchingSound.getStatus(), munchingSound.getVolume());
        }
    }
}