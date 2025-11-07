/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.controller.PacManGamesState;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.*;
import de.amr.pacmanfx.ui.api.*;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    private static final String CONTEXT_MENU_CSS_PATH = "/de/amr/pacmanfx/ui/css/menu-style.css";
    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private static final Set<ActionBinding> DEFAULT_ACTION_BINDINGS = Set.of(
        new ActionBinding(CheatActions.ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
        new ActionBinding(CheatActions.ACTION_ADD_LIVES,        alt(KeyCode.L)),
        new ActionBinding(CheatActions.ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
        new ActionBinding(CheatActions.ACTION_KILL_GHOSTS,      alt(KeyCode.X)),

        new ActionBinding(ArcadeActions.ACTION_INSERT_COIN,     bare(KeyCode.DIGIT5), bare(KeyCode.NUMPAD5)),
        new ActionBinding(ArcadeActions.ACTION_START_GAME,      bare(KeyCode.DIGIT1), bare(KeyCode.NUMPAD1)),

        new ActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,           bare(KeyCode.F3)),
        new ActionBinding(ACTION_ENTER_FULLSCREEN,              bare(KeyCode.F11)),
        new ActionBinding(ACTION_OPEN_EDITOR,                   alt_shift(KeyCode.E)),
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,          alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,              alt(KeyCode.RIGHT)),
        new ActionBinding(ACTION_SHOW_HELP,                     bare(KeyCode.H)),
        new ActionBinding(ACTION_STEER_UP,                      bare(KeyCode.UP), control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,                    bare(KeyCode.DOWN), control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,                    bare(KeyCode.LEFT), control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT,                   bare(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        new ActionBinding(ACTION_QUIT_GAME_SCENE,               bare(KeyCode.Q)),
        new ActionBinding(ACTION_SIMULATION_SLOWER,             alt(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_SLOWEST,            alt_shift(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_FASTER,             alt(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_FASTEST,            alt_shift(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_RESET,              alt(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_SIMULATION_ONE_STEP,           shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionBinding(ACTION_SIMULATION_TEN_STEPS,          shift(KeyCode.SPACE)),
        new ActionBinding(TestActions.ACTION_CUT_SCENES_TEST,   alt(KeyCode.C)),
        new ActionBinding(TestActions.ACTION_SHORT_LEVEL_TEST,  alt(KeyCode.T)),
        new ActionBinding(TestActions.ACTION_MEDIUM_LEVEL_TEST, alt_shift(KeyCode.T)),
        new ActionBinding(ACTION_TOGGLE_AUTOPILOT,              alt(KeyCode.A)),
        new ActionBinding(ACTION_TOGGLE_COLLISION_STRATEGY,     alt(KeyCode.S)),
        new ActionBinding(ACTION_TOGGLE_DEBUG_INFO,             alt(KeyCode.D)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                  alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,                 bare(KeyCode.P), bare(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,              bare(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,               alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY,   bare(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,       alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,              alt(KeyCode.W))
    );

    private final GameAssets assets;
    private final DirectoryWatchdog customDirectoryWatchdog;
    private final GameClock clock;
    private final GameContext gameContext;
    private final Joypad joypad;
    private final Keyboard keyboard;
    private final Stage stage;
    private final UIPreferences prefs;

    private final ActionBindingsManager actionBindings = new DefaultActionBindingsManager();
    private final Map<String, Class<?>> configClassesByGameVariant;
    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();

    private final ObjectProperty<GameUI_View> currentView = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            GameUI_View newView = get();
            if (newView != null) {
                embedView(newView);
            }
        }
    };

    private Scene scene;
    private StackPane layoutPane;
    private FlashMessageView flashMessageView;

    // These are lazily created
    private StartPagesView startPagesView;
    private PlayView playView;
    private EditorView editorView;

    private StringBinding titleBinding;

    public GameUI_Implementation(
        Map<String, Class<?>> configClassesByVariantName,
        GameContext gameContext,
        Stage stage,
        double mainSceneWidth,
        double mainSceneHeight)
    {
        this.configClassesByGameVariant = requireNonNull(configClassesByVariantName, "UI configuration map is null");
        this.gameContext = requireNonNull(gameContext, "Game context is null");
        this.stage = requireNonNull(stage, "Stage is null");
        Validations.requireNonNegative(mainSceneWidth, "Main scene width must be a positive number");
        Validations.requireNonNegative(mainSceneHeight, "Main scene height must be a positive number");

        keyboard = new Keyboard();
        joypad = new Joypad(keyboard);

        customDirectoryWatchdog = new DirectoryWatchdog(gameContext.customMapDir());

        clock = new GameClock();
        clock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        clock.setPermanentAction(this::drawCurrentView);

        assets = new GameAssets();
        prefs = new GameUI_Preferences();
        PROPERTY_3D_WALL_HEIGHT.set(prefs.getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(prefs.getFloat("3d.obstacle.opacity"));

        createSceneLayout(mainSceneWidth, mainSceneHeight);

        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(titleBinding);

        actionBindings.bindAction(ACTION_ENTER_FULLSCREEN, DEFAULT_ACTION_BINDINGS);
        actionBindings.bindAction(ACTION_OPEN_EDITOR, DEFAULT_ACTION_BINDINGS);
        actionBindings.bindAction(ACTION_TOGGLE_MUTED, DEFAULT_ACTION_BINDINGS);
        actionBindings.assignBindingsToKeyboard(keyboard);
    }

    private void createSceneLayout(double width, double height) {
        layoutPane = new StackPane();

        scene = new Scene(layoutPane, width, height);
        scene.getStylesheets().add(CONTEXT_MENU_CSS_PATH);

        // Keyboard events are first handled by the global keyboard object
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  keyboard::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> actionBindings.matchingAction(keyboard).ifPresentOrElse(
            action -> {
                boolean executed = action.executeIfEnabled(this);
                if (executed) e.consume();
            },
            () -> currentView().handleKeyboardInput(this)
        ));
        scene.setOnScroll(this::handleScrollEvent);

        flashMessageView = new FlashMessageView();

        // Large "paused" icon which appears at center of scene
        var pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, 80, ArcadePalette.ARCADE_WHITE);
        // Show paused icon only in play view
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> currentView() == playView() && clock.isPaused(),
                currentViewProperty(), clock.pausedProperty())
        );

        // Status icon box appears at bottom-left corner of all views except editor view
        var statusIconBox = new StatusIconBox();

        // hide icon box if editor view is active, avoid creation of editor view in binding expression!
        statusIconBox.visibleProperty().bind(currentViewProperty()
                .map(currentView -> optEditorView().isEmpty() || currentView != optEditorView().get()));

        statusIconBox.iconMuted()    .visibleProperty().bind(PROPERTY_MUTED);
        statusIconBox.icon3D()       .visibleProperty().bind(PROPERTY_3D_ENABLED);
        statusIconBox.iconAutopilot().visibleProperty().bind(gameContext().gameController().usingAutopilotProperty());
        statusIconBox.iconCheated()  .visibleProperty().bind(gameContext().gameController().cheatUsedProperty());
        statusIconBox.iconImmune()   .visibleProperty().bind(gameContext().gameController().immunityProperty());

        StackPane.setAlignment(pausedIcon, Pos.CENTER);
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);

        var viewPlaceholder = new Region();
        layoutPane.getChildren().setAll(viewPlaceholder, pausedIcon, statusIconBox, flashMessageView);

        //TODO check why this crashes when done before scene layout creation
        layoutPane.backgroundProperty().bind(Bindings.createObjectBinding(
                () -> isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)
                        ? Background.fill(Gradients.Samples.random())
                        : assets.background("background.scene"),
                currentViewProperty(), playView().currentGameSceneProperty()
        ));
    }

    private void embedView(GameUI_View view) {
        requireNonNull(view);
        layoutPane.getChildren().set(0, view.root());
        view.root().requestFocus();
    }

    private void handleScrollEvent(ScrollEvent scrollEvent) {
        currentGameScene().ifPresent(gameScene -> gameScene.handleScrollEvent(scrollEvent));
    }

    // Asset key: "app.title" or "app.title.paused"
    private String computeStageTitle() {
        var currentView = currentViewProperty().get();
        if (currentView == null) {
            return "No View?";
        }
        if (currentView.titleSupplier().isPresent()) {
            return currentView.titleSupplier().get().get();
        }
        boolean mode3D = PROPERTY_3D_ENABLED.get();
        boolean modeDebug = PROPERTY_DEBUG_INFO_VISIBLE.get();
        String assetKey       = clock().isPaused() ? "app.title.paused" : "app.title";
        String translatedMode = assets.translated(mode3D ? "threeD" : "twoD");
        String shortTitle     = currentConfig().assets().translated(assetKey, translatedMode);

        var currentGameScene = currentGameScene().orElse(null);
        if (currentGameScene == null || !modeDebug) {
            return shortTitle;
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        return shortTitle + " [%s]".formatted(sceneClassName);
    }

    private void selectView(GameUI_View view) {
        requireNonNull(view);
        final GameUI_View oldView = currentView();
        if (oldView == view) {
            return;
        }
        if (oldView != null) {
            oldView.actionBindingsManager().removeBindingsFromKeyboard(keyboard);
            gameContext.eventManager().removeEventListener(oldView);
        }
        view.actionBindingsManager().assignBindingsToKeyboard(keyboard);
        gameContext.eventManager().addEventListener(view);
        currentViewProperty().set(view);
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessage(Duration.seconds(10), "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            gameContext.game().simulationStepResults().reset(clock.tickCount());
            gameContext.gameController().updateGameState();
            gameContext.game().simulationStepResults().printLog();
            currentGameScene().ifPresent(GameScene::update);
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    private void drawCurrentView() {
        try {
            if (currentView() == playView()) {
                playView().draw();
            }
            flashMessageView.update();
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    private EditorView getOrCreateEditView() {
        if (editorView == null) {
            editorView = new EditorView(stage, assets);
            editorView.setQuitEditorAction(editor -> {
                stage.titleProperty().bind(titleBinding);
                showStartView();
            });
            editorView.editor().init(gameContext.customMapDir());
        }
        return editorView;
    }

    public ObjectProperty<GameUI_View> currentViewProperty() {
        return currentView;
    }

    // GameUI interface

    @Override
    public Set<ActionBinding> actionBindings() {
        return DEFAULT_ACTION_BINDINGS;
    }

    @Override
    public GameAssets assets() {
        return assets;
    }

    @Override
    public DirectoryWatchdog directoryWatchdog() {
        return customDirectoryWatchdog;
    }

    @Override
    public GameClock clock() {
        return clock;
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public Joypad joypad() {
        return joypad;
    }

    @Override
    public Keyboard keyboard() {
        return keyboard;
    }

    @Override
    public SoundManager soundManager() { return
        currentConfig().soundManager();
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public StackPane layoutPane() {
        return layoutPane;
    }

    @Override
    public UIPreferences preferences() {
        return prefs;
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    // GameUI_Lifecycle interface

    @Override
    public void quitCurrentGameScene() {
        soundManager().stopAll();
        currentGameScene().ifPresent(gameScene -> {
            gameScene.end();
            boolean shouldConsumeCoin = gameContext.gameState() == PacManGamesState.STARTING_GAME_OR_LEVEL
                    || gameContext.game().isPlaying();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        gameContext.gameController().restart(PacManGamesState.BOOT);
        showStartView();
    }

    @Override
    public void restart() {
        soundManager().stopAll();
        currentGameScene().ifPresent(GameScene::end);
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        gameContext.gameController().restart(PacManGamesState.BOOT);
        Platform.runLater(clock::start);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }

        String previousVariant = gameContext.gameController().gameVariant();
        if (gameVariant.equals(previousVariant)) {
            return;
        }

        if (previousVariant != null) {
            GameUI_Config previousConfig = config(previousVariant);
            Logger.info("Unloading assets for game variant {}", previousVariant);
            previousConfig.dispose();
        }

        GameUI_Config newConfig = config(gameVariant);
        Logger.info("Loading assets for game variant {}", gameVariant);
        newConfig.loadAssets();
        newConfig.soundManager().mutedProperty().bind(PROPERTY_MUTED);

        Image appIcon = newConfig.assets().image("app_icon");
        if (appIcon != null) {
            stage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariant);
        }

        // this triggers a game event and the event handlers:
        gameContext.gameController().setGameVariant(gameVariant);
    }

    @Override
    public void showUI() {
        playView().dashboard().init(this);
        startPagesView().setSelectedIndex(0);
        showStartView();
        stage.centerOnScreen();
        stage.show();
        Platform.runLater(customDirectoryWatchdog::startWatching);
        gameContext.gameController().setEventsEnabled(true);
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        clock.stop();
        customDirectoryWatchdog.dispose();
    }

    @Override
    public void updateTitle() {
        titleBinding.invalidate();
    }

    // GameUI_ViewAccess interface

    @Override
    public GameUI_View currentView() {
        return currentView.get();
    }

    @Override
    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    @Override
    public PlayView playView() {
        if (playView == null) {
            playView = new PlayView(scene);
            playView.setUI(this);
            titleBinding = createStringBinding(this::computeStageTitle,
                // depends on:
                currentViewProperty(),
                playView.currentGameSceneProperty(),
                scene.heightProperty(),
                gameContext.gameController().gameVariantProperty(),
                PROPERTY_DEBUG_INFO_VISIBLE,
                PROPERTY_3D_ENABLED,
                clock().pausedProperty()
            );
        }
        return playView;
    }

    @Override
    public StartPagesView startPagesView() {
        if (startPagesView == null) {
            startPagesView = new StartPagesView();
            startPagesView.setUI(this);
        }
        return startPagesView;
    }

    @Override
    public void showEditorView() {
        if (!gameContext.game().isPlaying() || clock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            soundManager().stopAll();
            clock.stop();
            getOrCreateEditView().editor().start();
            selectView(editorView);
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showPlayView() {
        selectView(playView());
    }

    @Override
    public void showStartView() {
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        soundManager().stopAll();
        selectView(startPagesView());
        startPagesView().currentStartPage().ifPresent(startPage -> Platform.runLater(() -> {
            startPage.onEnter(this); // sets game variant!
            startPage.layoutRoot().requestFocus();
        }));
    }

    // GameUI_SceneAccess interface

    @Override
    public Optional<GameScene> currentGameScene() {
        return playView.currentGameScene();
    }

    @Override
    public boolean isCurrentGameSceneID(String id) {
        GameScene currentGameScene = playView.currentGameScene().orElse(null);
        return currentGameScene != null && currentConfig().sceneConfig().gameSceneHasID(currentGameScene, id);
    }

    @Override
    public void updateGameScene(boolean forceReloading) {
        playView().updateGameScene(forceReloading);
    }

    // GameUI_ConfigManager interface

    @Override
    public GameUI_Config config(String gameVariant) {
        GameUI_Config config = configByGameVariant.get(gameVariant);
        if (config == null) {
            Class<?> configClass = configClassesByGameVariant.get(gameVariant);
            try {
                config = (GameUI_Config) configClass.getDeclaredConstructor(GameUI.class).newInstance(this);
                config.sceneConfig().createGameScenes();
                config.sceneConfig().gameScenes().forEach(scene -> {
                    if (scene instanceof GameScene2D gameScene2D) {
                        gameScene2D.debugInfoVisibleProperty().bind(PROPERTY_DEBUG_INFO_VISIBLE);
                    }
                });
                configByGameVariant.put(gameVariant, config);
            }
            catch (Exception x) {
                Logger.error("Could not create UI configuration for game variant {} and configuration class {}",
                    gameVariant, configClass);
                throw new IllegalStateException(x);
            }
        }
        return config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameUI_Config> T currentConfig() {
        return (T) config(gameContext.gameController().gameVariant());
    }
}