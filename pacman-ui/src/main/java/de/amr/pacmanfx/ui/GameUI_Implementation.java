/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.Validations;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.tilemap.editor.TileMapEditor;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.*;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameUI_Config.SCENE_ID_PLAY_SCENE_3D;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public class GameUI_Implementation implements GameUI {

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private final Set<ActionBinding> defaultActionBindings = Set.of(
        new ActionBinding(ACTION_ARCADE_INSERT_COIN,          nude(KeyCode.DIGIT5), nude(KeyCode.NUMPAD5)),
        new ActionBinding(ACTION_ARCADE_START_GAME,           nude(KeyCode.DIGIT1), nude(KeyCode.NUMPAD1)),
        new ActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,         nude(KeyCode.F3)),
        new ActionBinding(ACTION_CHEAT_EAT_ALL_PELLETS,       alt(KeyCode.E)),
        new ActionBinding(ACTION_CHEAT_ADD_LIVES,             alt(KeyCode.L)),
        new ActionBinding(ACTION_CHEAT_ENTER_NEXT_LEVEL,      alt(KeyCode.N)),
        new ActionBinding(ACTION_CHEAT_KILL_GHOSTS,           alt(KeyCode.X)),
        new ActionBinding(ACTION_ENTER_FULLSCREEN,            nude(KeyCode.F11)),
        new ActionBinding(ACTION_OPEN_EDITOR,                 alt_shift(KeyCode.E)),
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,        alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,            alt(KeyCode.RIGHT)),
        new ActionBinding(ACTION_SHOW_HELP,                   nude(KeyCode.H)),
        new ActionBinding(ACTION_STEER_UP,                    nude(KeyCode.UP), control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,                  nude(KeyCode.DOWN), control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,                  nude(KeyCode.LEFT), control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT,                 nude(KeyCode.RIGHT), control(KeyCode.RIGHT)),
        new ActionBinding(ACTION_QUIT_GAME_SCENE,             nude(KeyCode.Q)),
        new ActionBinding(ACTION_SIMULATION_SLOWER,           alt(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_SLOWEST,          alt_shift(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_FASTER,           alt(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_FASTEST,          alt_shift(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_RESET,            alt(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_SIMULATION_ONE_STEP,         shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionBinding(ACTION_SIMULATION_TEN_STEPS,        shift(KeyCode.SPACE)),
        new ActionBinding(ACTION_TEST_CUT_SCENES,             alt(KeyCode.C)),
        new ActionBinding(ACTION_TEST_LEVELS_SHORT,           alt(KeyCode.T)),
        new ActionBinding(ACTION_TEST_LEVELS_MEDIUM,          alt_shift(KeyCode.T)),
        new ActionBinding(ACTION_TOGGLE_AUTOPILOT,            alt(KeyCode.A)),
        new ActionBinding(ACTION_TOGGLE_DEBUG_INFO,           alt(KeyCode.D)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,               nude(KeyCode.P), nude(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,            nude(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,             alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, nude(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,     alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,            alt(KeyCode.W))
    );

    private final GlobalAssets assets;
    private final DirectoryWatchdog customDirectoryWatchdog;
    private final GameClock clock;
    private final GameContext gameContext;
    private final Joypad joypad;
    private final Keyboard keyboard;
    private final Stage stage;
    private final UIPreferences prefs;

    private final ActionBindingsManager globalActionBindings = new DefaultActionBindingsManager();
    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();
    private final MainScene mainScene;

    // These are lazily created
    private StartPagesView startPagesView;
    private PlayView playView;
    private EditorView editorView;

    public GameUI_Implementation(Map<String, Class<?>> configMap, GameContext gameContext, Stage stage, double mainSceneWidth, double mainSceneHeight) {
        requireNonNull(configMap, "UI configuration map is null");
        requireNonNull(gameContext, "Game context is null");
        requireNonNull(stage, "Stage is null");
        Validations.requireNonNegative(mainSceneWidth, "Main scene width must be a positive number");
        Validations.requireNonNegative(mainSceneHeight, "Main scene height must be a positive number");

        // Input
        keyboard = new Keyboard();
        joypad = new Joypad(keyboard);

        // Game context
        this.gameContext = gameContext;
        customDirectoryWatchdog = new DirectoryWatchdog(gameContext.customMapDir());
        clock = new GameClock();
        clock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        clock.setPermanentAction(this::drawCurrentView);

        // Game UI
        this.stage = stage;
        assets = new GlobalAssets();
        prefs = new GameUI_Preferences();
        PROPERTY_3D_WALL_HEIGHT.set(prefs.getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(prefs.getFloat("3d.obstacle.opacity"));

        mainScene = new MainScene(this, mainSceneWidth, mainSceneHeight);
        configureMainScene();
        configureStage(stage);
        configMap.forEach(this::applyConfiguration);
        defineGlobalActionBindings();
    }

    private void applyConfiguration(String gameVariant, Class<?> configClass) {
        try {
            GameUI_Config config = (GameUI_Config) configClass.getDeclaredConstructor(GameUI.class).newInstance(this);
            config.createGameScenes();
            Logger.info("Game scenes for game variant '{}' created", gameVariant);
            config.gameScenes().forEach(scene -> {
                if (scene instanceof GameScene2D gameScene2D) {
                    gameScene2D.debugInfoVisibleProperty().bind(PROPERTY_DEBUG_INFO_VISIBLE);
                }
            });
            setConfig(gameVariant, config);
        } catch (Exception x) {
            Logger.error("Could not apply UI configuration of class {}", configClass);
            throw new IllegalStateException(x);
        }
    }

    private void configureMainScene() {
        mainScene.currentGameSceneProperty().bindBidirectional(PROPERTY_CURRENT_GAME_SCENE);
        mainScene.currentViewProperty().bindBidirectional(PROPERTY_CURRENT_VIEW);

        // Check if a global action is defined for the key press, otherwise let the current view handle it.
        mainScene.setOnKeyPressed(e -> {
            AbstractGameAction matchingAction = globalActionBindings.matchingAction(keyboard).orElse(null);
            if (matchingAction != null) {
                matchingAction.executeIfEnabled(this);
            } else {
                currentView().handleKeyboardInput(this);
            }
        });

        mainScene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> assets.background(isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)
                ? "background.play_scene3d" : "background.scene"),
            PROPERTY_CURRENT_VIEW, PROPERTY_CURRENT_GAME_SCENE
        ));

        // Show paused icon only in play view
        mainScene.pausedIcon().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == playView() && clock.isPaused(),
            PROPERTY_CURRENT_VIEW, clock.pausedProperty())
        );

        // hide icon box if editor view is active, avoid creation of editor view in binding expression!
        StatusIconBox statusIcons = mainScene.statusIconBox();
        statusIcons.visibleProperty().bind(PROPERTY_CURRENT_VIEW
            .map(currentView -> optEditorView().isEmpty() || currentView != optEditorView().get()));

        statusIcons.iconMuted()    .visibleProperty().bind(PROPERTY_MUTED);
        statusIcons.icon3D()       .visibleProperty().bind(PROPERTY_3D_ENABLED);
        statusIcons.iconAutopilot().visibleProperty().bind(gameContext().gameController().propertyUsingAutopilot());
        statusIcons.iconImmune()   .visibleProperty().bind(gameContext().gameController().propertyImmunity());
    }

    private void configureStage(Stage stage) {
        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        bindStageTitle(stage);
    }

    // This is also called when quitting the editor to undo the editor title binding
    private void bindStageTitle(Stage stage) {
        stage.titleProperty().bind(createStringBinding(
            this::computeStageTitle,
            PROPERTY_CURRENT_VIEW,
            PROPERTY_CURRENT_GAME_SCENE,
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            clock().pausedProperty(),
            mainScene.heightProperty()
        ));
    }

    // Asset key: "app.title" or "app.title.paused"
    private String computeStageTitle() {
        var currentView = PROPERTY_CURRENT_VIEW.get();
        if (currentView == null) {
            return "No View?";
        }
        if (currentView.titleSupplier().isPresent()) {
            return currentView.titleSupplier().get().get();
        }
        boolean mode3D = PROPERTY_3D_ENABLED.get();
        boolean modeDebug = PROPERTY_DEBUG_INFO_VISIBLE.get();
        String assetKey       = clock().isPaused() ? "app.title.paused" : "app.title";
        String translatedMode = assets().translated(mode3D ? "threeD" : "twoD");

        String shortTitle     = currentConfig().assets().translated(assetKey, translatedMode);

        var currentGameScene = currentGameScene().orElse(null);
        if (currentGameScene == null || !modeDebug) {
            return shortTitle;
        }
        String sceneClassName = currentGameScene.getClass().getSimpleName();
        return currentGameScene instanceof GameScene2D gameScene2D
            ? shortTitle + " [%s]".formatted(sceneClassName) + " (%.2fx)".formatted(gameScene2D.scaling())
            : shortTitle + " [%s]".formatted(sceneClassName);
    }

    private void defineGlobalActionBindings() {
        globalActionBindings.assign(ACTION_ENTER_FULLSCREEN, defaultActionBindings);
        globalActionBindings.assign(ACTION_OPEN_EDITOR, defaultActionBindings);
        globalActionBindings.assign(ACTION_TOGGLE_MUTED, defaultActionBindings);
        globalActionBindings.installBindings(keyboard);
    }

    private void selectView(GameUI_View view) {
        requireNonNull(view);
        final GameUI_View oldView = mainScene.currentView();
        if (oldView == view) {
            return;
        }
        if (oldView != null) {
            oldView.actionBindingsManager().uninstallBindings(keyboard);
            gameContext.eventManager().removeEventListener(oldView);
        }
        view.actionBindingsManager().installBindings(keyboard);
        gameContext.eventManager().addEventListener(view);
        PROPERTY_CURRENT_VIEW.set(view);
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_trooo_phe(Throwable reason) {
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessage(Duration.seconds(10), "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void doSimulationStepAndUpdateGameScene() {
        try {
            gameContext.game().simulationStep().start(clock.tickCount());
            gameContext.gameController().updateGameState();
            gameContext.game().simulationStep().logState();
            currentGameScene().ifPresent(GameScene::update);
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private void drawCurrentView() {
        try {
            if (currentView() == playView()) {
                playView().draw();
            }
            mainScene.flashMessageLayer().update();
        } catch (Throwable x) {
            ka_tas_trooo_phe(x);
        }
    }

    private EditorView ensureEditorViewExists() {
        if (editorView == null) {
            var editor = new TileMapEditor(stage, assets().theModel3DRepository());
            var miReturnToGame = new MenuItem(assets().translated("back_to_game"));
            miReturnToGame.setOnAction(e -> {
                editor.stop();
                editor.executeWithCheckForUnsavedChanges(this::showStartView);
                // Undo editor stage title binding change:
                bindStageTitle(stage);
            });
            editor.getMenuBar().getFileMenu().getItems().addAll(new SeparatorMenuItem(), miReturnToGame);
            editor.init(gameContext.customMapDir());
            editorView = new EditorView(editor);
        }
        return editorView;
    }

    // GameUI interface

    @Override
    public Set<ActionBinding> actionBindings() {
        return defaultActionBindings;
    }

    @Override
    public GlobalAssets assets() {
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
    public UIPreferences preferences() {
        return prefs;
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        mainScene.flashMessageLayer().showMessage(String.format(message, args), duration.toSeconds());
    }

    // GameUI_Lifecycle interface

    @Override
    public void quitCurrentGameScene() {
        soundManager().stopAll();
        currentGameScene().ifPresent(gameScene -> {
            gameScene.end();
            boolean shouldConsumeCoin = gameContext.gameState() == GamePlayState.STARTING_GAME_OR_LEVEL
                    || gameContext.game().isPlaying();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        gameContext.gameController().restart(GamePlayState.BOOT);
        showStartView();
    }

    @Override
    public void restart() {
        soundManager().stopAll();
        currentGameScene().ifPresent(GameScene::end);
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        gameContext.gameController().restart(GamePlayState.BOOT);
        Platform.runLater(clock::start);
    }

    @Override
    public void selectGameVariant(String gameVariant) {
        if (gameVariant == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }

        String previousVariant = gameContext.gameController().selectedGameVariant();
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

        playView().canvasFrame().setRoundedBorder(newConfig.hasGameCanvasRoundedBorder());

        // this triggers a game event and the event handlers:
        gameContext.gameController().selectGameVariant(gameVariant);
    }

    @Override
    public void showUI() {
        playView().initDashboard();
        startPagesView().selectStartPage(0);
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

    // GameUI_ViewAccess interface

    @Override
    public GameUI_View currentView() {
        return mainScene.currentView();
    }

    @Override
    public Optional<EditorView> optEditorView() {
        return Optional.ofNullable(editorView);
    }

    @Override
    public PlayView playView() {
        if (playView == null) {
            playView = new PlayView(this, mainScene);
        }
        return playView;
    }

    @Override
    public StartPagesView startPagesView() {
        if (startPagesView == null) {
            startPagesView = new StartPagesView(this);
        }
        return startPagesView;
    }

    @Override
    public void showEditorView() {
        if (!gameContext.game().isPlaying() || clock.isPaused()) {
            currentGameScene().ifPresent(GameScene::end);
            soundManager().stopAll();
            clock.stop();
            ensureEditorViewExists().editor().start(stage);
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
        return mainScene.currentGameScene();
    }

    @Override
    public boolean isCurrentGameSceneID(String id) {
        GameScene currentGameScene = mainScene.currentGameScene().orElse(null);
        return currentGameScene != null && currentConfig().gameSceneHasID(currentGameScene, id);
    }

    @Override
    public void updateGameScene(boolean forceReloading) {
        playView().updateGameScene(forceReloading);
    }

    // GameUI_ConfigManager interface

    @Override
    public void setConfig(String variant, GameUI_Config config) {
        requireNonNull(variant);
        requireNonNull(config);
        configByGameVariant.put(variant, config);
    }

    @Override
    public GameUI_Config config(String gameVariant) {
        return configByGameVariant.get(gameVariant);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameUI_Config> T currentConfig() {
        return (T) config(gameContext.gameController().selectedGameVariant());
    }
}