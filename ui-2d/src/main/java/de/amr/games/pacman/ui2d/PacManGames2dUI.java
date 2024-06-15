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
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.*;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.LEVEL_TEST;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.rangeClosed;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No 3D scenes.
 *
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameContext, ActionHandler, SoundHandler {

    public static final String SIGNATURE_TEXT = "Remake (2021-2024) by Armin Reichert";

    public static final double MIN_SCALING                        = 0.75;
    public static final int DEFAULT_CANVAS_WIDTH_UNSCALED         = GameModel.ARCADE_MAP_TILES_X * TS; // 28*8 = 224
    public static final int DEFAULT_CANVAS_HEIGHT_UNSCALED        = GameModel.ARCADE_MAP_TILES_Y * TS; // 36*8 = 288

    public static final IntegerProperty PY_SIMULATION_STEPS       = new SimpleIntegerProperty(1);
    public static final BooleanProperty PY_IMMUNITY               = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            GameController.it().setPacImmune(get());
        }
    };
    public static final BooleanProperty PY_AUTOPILOT              = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_DEBUG_INFO             = new SimpleBooleanProperty(false);
    public static final BooleanProperty PY_CANVAS_DECORATED       = new SimpleBooleanProperty(true);
    public static final BooleanProperty PY_PIP_ON                 = new SimpleBooleanProperty(false);
    public static final IntegerProperty PY_PIP_HEIGHT             = new SimpleIntegerProperty(GameModel.ARCADE_MAP_SIZE_PX.y());
    public static final IntegerProperty PY_PIP_OPACITY_PERCENT    = new SimpleIntegerProperty(100);
    public static final BooleanProperty PY_3D_ENABLED             = new SimpleBooleanProperty(false);
    public static final ObjectProperty<DrawMode> PY_3D_DRAW_MODE  = new SimpleObjectProperty<>(DrawMode.FILL);

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant");
    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");
    public final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(this, "scoreVisible", false);

    protected final Theme theme = new Theme();
    protected final GameSceneManager gameSceneManager = new GameSceneManager();
    protected final List<ResourceBundle> bundles = new ArrayList<>();

    protected Stage stage;
    protected Scene mainScene;
    protected GameClockFX clock;
    protected TileMapEditor editor;

    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;

    //TODO reconsider this
    protected final Animation voiceClipExecution = new PauseTransition();
    protected AudioClip voiceClip;

    private void addMapEditor() {
        editor = new TileMapEditor(GameModel.CUSTOM_MAP_DIR);
        editor.createUI(stage);

        var miQuitEditor = new MenuItem("Back to Game");
        miQuitEditor.setOnAction(e -> quitMapEditor());
        editor.menuFile().getItems().add(miQuitEditor);

        // load maps from core project resources
        ResourceManager core = () -> GameModel.class;
        editor.addPredefinedMap("Pac-Man",
                new WorldMap(core.url("/de/amr/games/pacman/maps/pacman.world")));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 6).forEach(mapNumber -> editor.addPredefinedMap("Ms. Pac-Man " + mapNumber,
                new WorldMap(core.url("/de/amr/games/pacman/maps/mspacman/mspacman_" + mapNumber + ".world"))));
        editor.menuLoadMap().getItems().add(new SeparatorMenuItem());
        rangeClosed(1, 8).forEach(mapNumber -> editor.addPredefinedMap("Pac-Man XXL " + mapNumber,
                new WorldMap(core.url("/de/amr/games/pacman/maps/masonic/masonic_" + mapNumber + ".world"))));

        editorPage = new EditorPage(editor, this);
    }

    @Override
    public void enterMapEditor() {
        if (game().variant() != GameVariant.PACMAN_XXL) {
            showFlashMessageSeconds(3, "Map editor is not available in this game variant");
            return;
        }
        stopVoice();
        stopAllSounds();
        clock.stop();
        currentGameScene().ifPresent(GameScene::end);
        //TODO introduce GAME_PAUSED state?
        reboot();
        stage.titleProperty().bind(editor.titlePy);
        if (game().world() != null) {
            editor.setMap(game().world().map());
        }
        editor.start();
        selectPage(editorPage);
    }

    @Override
    public void quitMapEditor() {
        editor.showConfirmation(editor::saveMapFileAs, () -> {
            stage.titleProperty().bind(stageTitleBinding());
            editor.stop();
            selectPage(startPage);
        });
    }

    public void loadAssets() {
        bundles.add(ResourceBundle.getBundle("de.amr.games.pacman.ui2d.texts.messages", PacManGames2dUI.class.getModule()));
        ResourceManager rm = () -> PacManGames2dUI.class;

        // Dashboard

        theme.set("image.armin1970",                 rm.loadImage("graphics/armin.jpg"));
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

        theme.set("voice.explain",                    rm.loadAudioClip("sound/voice/press-key.mp3"));
        theme.set("voice.autopilot.off",              rm.loadAudioClip("sound/voice/autopilot-off.mp3"));
        theme.set("voice.autopilot.on",               rm.loadAudioClip("sound/voice/autopilot-on.mp3"));
        theme.set("voice.immunity.off",               rm.loadAudioClip("sound/voice/immunity-off.mp3"));
        theme.set("voice.immunity.on",                rm.loadAudioClip("sound/voice/immunity-on.mp3"));

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
        theme.set("ms_pacman.audio.game_ready",        rm.loadAudioClip("sound/mspacman/Start.mp3"));
        theme.set("ms_pacman.audio.game_over",         rm.loadAudioClip("sound/common/game-over.mp3"));
        theme.set("ms_pacman.audio.ghost_eaten",       rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        theme.set("ms_pacman.audio.ghost_returning",   rm.loadAudioClip("sound/mspacman/GhostEyes.mp3"));
        theme.set("ms_pacman.audio.intermission.1",    rm.loadAudioClip("sound/mspacman/Act1TheyMeet.mp3"));
        theme.set("ms_pacman.audio.intermission.2",    rm.loadAudioClip("sound/mspacman/Act2TheChase.mp3"));
        theme.set("ms_pacman.audio.intermission.3",    rm.loadAudioClip("sound/mspacman/Act3Junior.mp3"));
        theme.set("ms_pacman.audio.level_complete",    rm.loadAudioClip("sound/common/level-complete.mp3"));
        theme.set("ms_pacman.audio.pacman_death",      rm.loadAudioClip("sound/mspacman/Died.mp3"));
        theme.set("ms_pacman.audio.pacman_munch",      rm.loadAudioClip("sound/mspacman/Pill.wav"));
        theme.set("ms_pacman.audio.pacman_power",      rm.loadAudioClip("sound/mspacman/ScaredGhost.mp3"));
        theme.set("ms_pacman.audio.siren.1",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));
        theme.set("ms_pacman.audio.siren.2",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("ms_pacman.audio.siren.3",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("ms_pacman.audio.siren.4",           rm.loadAudioClip("sound/mspacman/GhostNoise1.wav"));// TODO
        theme.set("ms_pacman.audio.sweep",             rm.loadAudioClip("sound/common/sweep.mp3"));

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

        //
        // Pac-Man XXL
        //
        theme.set("pacman_xxl.icon",                 rm.loadImage("graphics/icons/pacman.png"));
        theme.set("pacman_xxl.helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        theme.set("pacman_xxl.startpage.image",      rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));

        Logger.info("Assets loaded: {}", theme.summary(List.of(
            new Pair<>(Image.class, "images"),
            new Pair<>(Font.class, "fonts"),
            new Pair<>(Color.class, "colors"),
            new Pair<>(AudioClip.class, "audio clips")
        )));
    }

    public void createUI(Stage stage, double width, double height) {
        this.stage = checkNotNull(stage);
        gameVariantPy.set(GameController.it().game().variant());
        for (var variant : GameController.it().supportedVariants()) {
            GameController.it().game(variant).addGameEventListener(this);
        }

        // Touch all game keys such that they get registered with keyboard
        for (var gameKey : GameKeys.values()) {
            Logger.info("Game key '{}' registered", gameKey);
        }

        mainScene = createMainScene(width, height);

        startPage = createStartPage();
        gamePage  = createGamePage();

        createGameScenes(); // must be done *after* creating game page!
        createGameClock();
        addMapEditor();

        stage.titleProperty().bind(stageTitleBinding());
        stage.getIcons().setAll(theme.image(game().variant().resourceKey() + ".icon"));
        stage.setScene(mainScene);
        selectStartPage();
    }

    public void show() {
        //TODO this does not work yet correctly
        Dimension2D minSize = DecoratedCanvas.computeSize(
            DEFAULT_CANVAS_WIDTH_UNSCALED, DEFAULT_CANVAS_HEIGHT_UNSCALED, 1.25 * MIN_SCALING);
        stage.setMinWidth(minSize.getWidth());
        stage.setMinHeight(minSize.getHeight());
        stage.centerOnScreen();
        stage.show();
    }

    protected void onMouseClicked(MouseEvent e) {
        currentPage.onMouseClicked(e);
    }

    protected void onContextMenuRequested(ContextMenuEvent e) {
        currentPage.onContextMenuRequested(e);
    }

    protected Scene createMainScene(double width, double height) {
        var placeholder = new Region();
        placeholder.setBackground(Ufx.coloredBackground(Color.BLACK));
        var scene = new Scene(placeholder, width, height);
        Keyboard.filterKeyEventsFor(scene);
        scene.setOnMouseClicked(this::onMouseClicked);
        scene.setOnContextMenuRequested(this::onContextMenuRequested);
        scene.setOnKeyPressed(e -> currentPage.handleKeyboardInput());
        ChangeListener<Number> resizeCurrentPage = (py, ov, nv) -> {
            if (currentPage != null) {
                currentPage.setSize(scene.getWidth(), scene.getHeight());
            }
        };
        scene.widthProperty().addListener(resizeCurrentPage);
        scene.heightProperty().addListener(resizeCurrentPage);
        return scene;
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
        for (var variant : GameVariant.values()) {
            gameSceneManager.createGameScenes(variant);
            gameSceneManager.gameScenes(variant).forEach(gameScene -> gameScene.setContext(this));
            gameSceneManager.gameScenes2D(variant).forEach(gameScene2D -> {
                gameScene2D.setCanvas(gamePage.layout().decoratedCanvas().canvas());
                gameScene2D.scalingPy.bind(gamePage.layout().scalingPy);
                gameScene2D.infoVisiblePy.bind(PY_DEBUG_INFO);
            });
        }
    }

    protected StringBinding stageTitleBinding() {
        return Bindings.createStringBinding(
            () -> tt("app.title." + gameVariantPy.get().resourceKey() + (clock.pausedPy.get() ? ".paused" : ""), "2D"),
            clock.pausedPy, gameVariantPy);
    }

    protected StartPage createStartPage() {
        var startPage = new StartPage(this);
        startPage.playButton().setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                selectPage(gamePage);
            }
        });
        startPage.gameVariantPy.bind(gameVariantPy);
        return startPage;
    }

    protected GamePage createGamePage() {
        var page = new GamePage(this);
        page.configureSignature(theme.font("font.monospaced", 9), SIGNATURE_TEXT);
        page.layout().decoratedCanvas().decoratedPy.bind(PY_CANVAS_DECORATED);
        page.layout().setMinScaling(MIN_SCALING);
        gameScenePy.addListener((py, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
        return page;
    }

    private void selectPage(Page page) {
        if (page != currentPage) {
            currentPage = page;
            currentPage.setSize(mainScene.getWidth(), mainScene.getHeight());
            mainScene.setRoot(currentPage.rootPane());
            mainScene.getRoot().requestFocus();
            currentPage.onSelected();
        }
    }

    protected void updateGameScene(boolean reloadCurrentScene) {
        if (isPageSelected(startPage)) {
            return; // no game scene on start page
        }
        GameScene sceneToDisplay = sceneMatchingCurrentGameState();
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

    protected GameScene sceneMatchingCurrentGameState() {
        GameVariant variant = game().variant();
        return switch (gameState()) {
            case BOOT -> gameSceneManager.gameScene(variant, GameSceneID.BOOT_SCENE);
            case CREDIT -> gameSceneManager.gameScene(variant, GameSceneID.CREDIT_SCENE);
            case INTRO -> gameSceneManager.gameScene(variant, GameSceneID.INTRO_SCENE);
            case INTERMISSION -> gameSceneManager.gameScene(variant, GameSceneID.valueOf(
                "CUT_SCENE_" + game().intermissionNumberAfterLevel(game().levelNumber())));
            case INTERMISSION_TEST -> gameSceneManager.gameScene(variant, GameSceneID.valueOf(
                "CUT_SCENE_" + gameState().<Integer>getProperty("intermissionTestNumber")));
            default -> gameSceneManager.gameScene(variant, GameSceneID.PLAY_SCENE);
        };
    }

    protected String message(String key, Object... args) {
        checkNotNull(key);
        for (var bundle : bundles) {
            if (bundle.containsKey(key)) {
                return MessageFormat.format(bundle.getString(key), args);
            }
        }
        Logger.error("Missing localized text for key {}", key);
        return null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameSceneContext interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public String tt(String key, Object... args) {
        return message(key, args);
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
    public void onGameEvent(GameEvent e) {
        Logger.trace("Received: {}", e);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(e);
        if (gameState() != null) {
            //TODO check this. On game start, event can be published before game state has been initialized!
            updateGameScene(false);
            currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(e));
        }
    }

    @Override
    public void onGameVariantChanged(GameEvent e) {
        var newVariant = game().variant();
        Logger.info("onGameVariantChanged: {}", e);
        gameVariantPy.set(newVariant);
        stage.getIcons().setAll(theme.image(newVariant.resourceKey() + ".icon"));
    }

    @Override
    public void onUnspecifiedChange(GameEvent e) {
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
            game().scatterPhase().ifPresent(this::ensureSirenStarted);
        }
    }

    @Override
    public void onIntermissionStarted(GameEvent event) {
        int intermissionNumber; // 0=no intermission
        if (gameState() == GameState.INTERMISSION_TEST) {
            intermissionNumber = GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber");
        } else {
            intermissionNumber = event.game.intermissionNumberAfterLevel(event.game.levelNumber());
        }
        if (intermissionNumber != 0) {
            switch (game().variant()) {
                case MS_PACMAN -> playAudioClip("audio.intermission." + intermissionNumber);
                case PACMAN, PACMAN_XXL -> {
                    var clip = audioClip("audio.intermission");
                    clip.setCycleCount(intermissionNumber == 1 || intermissionNumber == 3 ? 2 : 1);
                    clip.play();
                }
            }
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        // Found no better point in time to create and assign the sprite animations to the guys
        GameModel game = e.game;
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
        playAudioClip("audio.game_ready");
    }

    @Override
    public void onPacDied(GameEvent event) {
        if (!game().isDemoLevel()) {
            playAudioClip("audio.pacman_death");
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (!game().isDemoLevel()) {
            //TODO (fixme) this does not sound 100% as in the original game
            ensureAudioLoop("audio.pacman_munch", AudioClip.INDEFINITE);
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        if (!game().isDemoLevel()) {
            stopSirens();
            var clip = audioClip("audio.pacman_power");
            clip.stop();
            clip.setCycleCount(AudioClip.INDEFINITE);
            clip.play();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        if (!game().isDemoLevel()) {
            stopAudioClip("audio.pacman_power");
            ensureSirenStarted(game().huntingPhaseIndex() / 2);
        }
    }

    @Override
    public void onStopAllSounds(GameEvent e) {
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
        PY_IMMUNITY.set(GameController.it().isPacImmune());
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
        }
        Logger.info("Game variant ({}) {}", game(), clock.isPaused() ? "paused" : "resumed");
    }


    @Override
    public void toggleDashboard() {
        gamePage.dashboard().toggleVisibility();
    }

    @Override
    public void toggleDrawMode() {
        PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
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
        var all = GameController.it().supportedVariants();
        var current = all.indexOf(game().variant());
        var next = current < all.size() - 1 ? all.get(current + 1) : all.getFirst();
        selectGameVariant(next);
    }

    @Override
    public void selectPrevGameVariant() {
        var all = GameController.it().supportedVariants();
        var current = all.indexOf(game().variant());
        var prev = current > 0 ? all.get(current - 1) : all.getLast();
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
    public AudioClip audioClip(String key) {
        checkNotNull(key);
        String rk = (game().variant() != GameVariant.PACMAN_XXL)
            ? game().variant().resourceKey()
            : GameVariant.PACMAN.resourceKey();
        return theme().audioClip(rk + "." + key);
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

    @Override
    public void playVoice(String clipID, double delaySeconds) {
        if (voiceClip != null && voiceClip.isPlaying()) {
            return; // don't interrupt
        }
        voiceClip = theme().audioClip(clipID);
        voiceClipExecution.setDelay(Duration.seconds(delaySeconds));
        voiceClipExecution.setOnFinished(e -> voiceClip.play());
        voiceClipExecution.play();
    }

    @Override
    public void stopVoice() {
        if (voiceClip != null && voiceClip.isPlaying()) {
            voiceClip.stop();
        }
        if (voiceClipExecution.getStatus() == Animation.Status.RUNNING) {
            voiceClipExecution.stop();
        }
    }
}