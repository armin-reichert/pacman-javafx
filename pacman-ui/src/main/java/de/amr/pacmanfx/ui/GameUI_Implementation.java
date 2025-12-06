/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.SimulationStepResult;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;
import de.amr.pacmanfx.ui.api.*;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
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
import javafx.scene.input.KeyEvent;
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

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.api.GameScene_Config.SCENE_ID_PLAY_SCENE_3D;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private static final int PAUSE_ICON_SIZE = 80;

    private final GlobalGameAssets assets;
    private final GameClock clock;
    private final GameContext gameContext;
    private final Joypad joypad;
    private final Keyboard keyboard;
    private final Stage stage;
    private final UIPreferences prefs;
    private final DirectoryWatchdog customDirWatchdog;

    private final ActionBindingsManager actionBindings = new DefaultActionBindingsManager();
    private final Map<String, Class<?>> configClassesByGameVariant;
    private final Map<String, GameUI_Config> configByGameVariant = new HashMap<>();

    private final ObjectProperty<GameUI_View> currentView = new SimpleObjectProperty<>();

    private Scene scene;
    private final StackPane layoutPane = new StackPane();

    private final FlashMessageView flashMessageView = new FlashMessageView();

    private final StartPagesCarousel startPagesView;
    private final PlayView playView;
    private EditorView editorView;

    private StatusIconBox statusIconBox;

    private final StringBinding titleBinding;

    public GameUI_Implementation(
        Map<String, Class<?>> configClassesByVariantName,
        GameContext gameContext,
        Stage stage,
        double sceneWidth,
        double sceneHeight)
    {
        this.configClassesByGameVariant = requireNonNull(configClassesByVariantName, "UI configuration map is null");
        this.gameContext = requireNonNull(gameContext, "Game context is null");
        this.stage = requireNonNull(stage, "Stage is null");
        requireNonNegative(sceneWidth, "Main scene width must be a positive number");
        requireNonNegative(sceneHeight, "Main scene height must be a positive number");

        assets = new GlobalGameAssets();
        prefs = new GameUI_Preferences();
        PROPERTY_3D_WALL_HEIGHT.set(prefs.getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(prefs.getFloat("3d.obstacle.opacity"));

        keyboard = new Keyboard();
        joypad = new Joypad();
        joypad.setSimulatingKeyboard(keyboard);

        customDirWatchdog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);

        clock = new GameClock();
        clock.setPausableAction(this::doSimulationStepAndUpdateGameScene);
        clock.setPermanentAction(this::drawCurrentView);

        createScene(sceneWidth, sceneHeight);

        playView = new PlayView(scene);
        playView.setUI(this);

        startPagesView = new StartPagesCarousel();
        startPagesView.setUI(this);

        currentView.addListener((py, ov, newView) -> {
            if (newView != null) {
                embedView(newView);
                Logger.info("Embedded view: {}", newView);
            }
        });

        titleBinding = createStringBinding(this::computeStageTitle,
            // depends on:
            currentViewProperty(),
            playView.currentGameSceneProperty(),
            scene.heightProperty(),
            gameContext.gameVariantNameProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            clock().pausedProperty()
        );

        layoutPane.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> isCurrentGameSceneID(SCENE_ID_PLAY_SCENE_3D)
                    ? Background.fill(Gradients.Samples.random())
                    : assets.background_PacManWallpaper,
            // depends on:
            currentViewProperty(),
            playView.currentGameSceneProperty()
        ));

        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(titleBinding);

        actionBindings.useFirst(ACTION_ENTER_FULLSCREEN, GameUI.ACTION_BINDINGS);
        actionBindings.useFirst(ACTION_OPEN_EDITOR,      GameUI.ACTION_BINDINGS);
        actionBindings.useFirst(ACTION_TOGGLE_MUTED,     GameUI.ACTION_BINDINGS);
        actionBindings.attach(keyboard);
    }

    private void createScene(double width, double height) {
        scene = new Scene(layoutPane, width, height);
        scene.getStylesheets().add(GlobalGameAssets.STYLE_SHEET_PATH);

        // Keyboard events are first handled by the global keyboard object
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  keyboard::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> actionBindings.matchingAction(keyboard).ifPresentOrElse(action ->
            {
                boolean executed = action.executeIfEnabled(this);
                if (executed) e.consume();
            },
            () -> currentView().handleKeyboardInput(this)
        ));
        scene.setOnScroll(e -> currentGameScene().ifPresent(gameScene -> gameScene.handleScrollEvent(e)));

        createStatusIconBox();

        // Large "paused" icon which appears at center of scene
        FontIcon pausedIcon = createPausedIcon();
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        // First child is placeholder for view (start pages view, play view, ...)
        layoutPane.getChildren().setAll(new Region(), pausedIcon, statusIconBox, flashMessageView);
    }

    private FontIcon createPausedIcon() {
        var pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, PAUSE_ICON_SIZE, ArcadePalette.ARCADE_WHITE);
        // Show paused icon only in play view
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == playView && clock.isPaused(),
            currentViewProperty(), clock.pausedProperty())
        );
        return pausedIcon;
    }

    // Status icon box appears at bottom-left corner of all views except editor view
    private void createStatusIconBox() {
        statusIconBox = new StatusIconBox();
        // hide icon box if editor view is active, avoid creation of editor view in binding expression!
        statusIconBox.visibleProperty().bind(currentViewProperty().map(currentView -> optEditorView().isEmpty() || currentView != optEditorView().get()));
        statusIconBox.iconMuted()    .visibleProperty().bind(PROPERTY_MUTED);
        statusIconBox.icon3D()       .visibleProperty().bind(PROPERTY_3D_ENABLED);
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
    }

    private void embedView(GameUI_View view) {
        requireNonNull(view);
        layoutPane.getChildren().set(0, view.root());
        view.root().requestFocus();
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
            oldView.onExit();
            oldView.actionBindingsManager().release(keyboard);
            gameContext.currentGame().removeGameEventListener(oldView);
        }
        view.actionBindingsManager().attach(keyboard);
        gameContext.currentGame().addGameEventListener(view);
        currentViewProperty().set(view);
        view.onEnter();
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
        final Game game = gameContext.currentGame();
        try {
            final SimulationStepResult events = game.simulationStepResult();
            events.reset();
            events.setTick(clock.tickCount());
            game.control().update();
            events.printLog();
            currentGameScene().ifPresent(gameScene -> gameScene.update(game));
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    private void drawCurrentView() {
        try {
            if (currentView() == playView) {
                playView.draw();
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
            editorView.editor().init(GameBox.CUSTOM_MAP_DIR);
        }
        return editorView;
    }

    public ObjectProperty<GameUI_View> currentViewProperty() {
        return currentView;
    }

    // GameUI interface

    @Override
    public GlobalGameAssets assets() {
        return assets;
    }

    @Override
    public DirectoryWatchdog directoryWatchdog() {
        return customDirWatchdog;
    }

    @Override
    public GameClock clock() {
        return clock;
    }

    @Override
    public GameContext context() {
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
        final Game game = gameContext.currentGame();

        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        soundManager().stopAll();

        //TODO this is game-specific and should not be handled here
        currentGameScene().ifPresent(gameScene -> {
            gameScene.end(game);
            boolean shouldConsumeCoin = game.control().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlaying();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });

        game.control().restart(GameControl.StateName.BOOT.name());
        showStartView();
    }

    @Override
    public void restart() {
        final Game game = gameContext.currentGame();

        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        currentGameScene().ifPresent(gameScene -> gameScene.end(game));

        game.control().restart(GameControl.StateName.BOOT.name());
        Platform.runLater(clock::start);
    }

    @Override
    public void selectGameVariant(String gameVariantName) {
        if (gameVariantName == null) {
            Logger.error("Cannot select game variant (NULL)");
            return;
        }

        String prevVariantName = gameContext.gameVariantName();
        if (gameVariantName.equals(prevVariantName)) {
            return;
        }

        if (prevVariantName != null) {
            GameUI_Config previousConfig = config(prevVariantName);
            Logger.info("Unloading assets for game variant {}", prevVariantName);
            previousConfig.dispose();
        }

        GameUI_Config newConfig = config(gameVariantName);
        Logger.info("Loading assets for game variant {}", gameVariantName);
        newConfig.loadAssets();
        newConfig.soundManager().mutedProperty().bind(PROPERTY_MUTED);

        Image appIcon = newConfig.assets().image("app_icon");
        if (appIcon != null) {
            stage.getIcons().setAll(appIcon);
        } else {
            Logger.error("Could not find app icon for current game variant {}", gameVariantName);
        }

        // this triggers a game event and the event handlers:
        gameContext.gameVariantNameProperty().set(gameVariantName);
    }

    @Override
    public void showUI() {
        playView.dashboard().init(this);
        if (startPagesView.numItems() > 0) {
            startPagesView().setSelectedIndex(0);
            showStartView();
        }
        else {
            Logger.error("No start page has been set!");
        }
        stage.centerOnScreen();
        stage.show();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        clock.stop();
        customDirWatchdog.dispose();
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
        return playView;
    }

    @Override
    public StartPagesCarousel startPagesView() {
        return startPagesView;
    }

    @Override
    public void showEditorView() {
        if (!gameContext.currentGame().isPlaying() || clock.isPaused()) {
            currentGameScene().ifPresent(gameScene -> gameScene.end(gameContext.currentGame()));
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
        final Game game = gameContext.currentGame();
        statusIconBox.iconAutopilot().visibleProperty().bind(game.usingAutopilotProperty());
        statusIconBox.iconCheated()  .visibleProperty().bind(game.cheatUsedProperty());
        statusIconBox.iconImmune()   .visibleProperty().bind(game.immuneProperty());
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
        playView.updateGameScene(gameContext.currentGame(), forceReloading);
    }

    // GameUI_ConfigManager interface

    @Override
    public GameUI_Config config(String gameVariant) {
        GameUI_Config config = configByGameVariant.get(gameVariant);
        if (config == null) {
            Class<?> configClass = configClassesByGameVariant.get(gameVariant);
            try {
                config = (GameUI_Config) configClass.getDeclaredConstructor(GameUI.class).newInstance(this);
                config.sceneConfig().createGameScenes(this);
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
        return (T) config(gameContext.gameVariantName());
    }
}