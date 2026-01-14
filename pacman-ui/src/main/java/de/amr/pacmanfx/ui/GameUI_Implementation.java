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
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import de.amr.pacmanfx.ui.sound.VoicePlayer;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;
import org.tinylog.Supplier;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private static final int PAUSE_ICON_SIZE = 80;

    private final GameClock clock;
    private final GameContext gameContext;
    private final Stage stage;
    private final UIPreferences prefs;
    private final DirectoryWatchdog customDirWatchdog;

    private final ActionBindingsManager globalActionBindings = new GlobalActionBindings();
    private final GameUI_ConfigFactory configFactory;

    private Scene scene;
    private final StackPane layoutPane = new StackPane();
    private final FlashMessageView flashMessageView = new FlashMessageView();

    private final VoicePlayer voicePlayer = new VoicePlayer();

    private final ViewManager viewManager;

    private FontIcon pausedIcon;
    private StatusIconBox statusIconBox;

    private final StringBinding titleBinding;

    private static class ViewManager {

        private final GameContext gameContext;
        private final ObjectProperty<GameUI_View> currentView = new SimpleObjectProperty<>();

        private final StartPagesCarousel startPagesView;
        private final PlayView playView;
        private EditorView editorView;
        private final Supplier<EditorView> editorViewCreator;

        ViewManager(
            GameContext gameContext,
            Pane layoutPane,
            StartPagesCarousel startPagesView,
            PlayView playView,
            Supplier<EditorView> editorViewCreator,
            FlashMessageView flashMessageView)
        {
            this.gameContext = gameContext;
            this.startPagesView = startPagesView;
            this.playView = playView;
            this.editorViewCreator = editorViewCreator;

            currentView.addListener((_, oldView, newView) -> {
                if (oldView != null) {
                    oldView.onExit();
                }
                if (newView != null) {
                    layoutPane.getChildren().set(0, newView.root());
                    newView.onEnter();
                }
                flashMessageView.clear();
            });
        }

        void selectView(GameUI_View view) {
            requireNonNull(view);
            final GameUI_View oldView = currentView();
            if (oldView == view) {
                return;
            }
            if (oldView != null) {
                oldView.onExit();
                oldView.actionBindingsManager().releaseBindings(KEYBOARD);
                gameContext.currentGame().removeGameEventListener(oldView);
            }
            view.actionBindingsManager().activateBindings(KEYBOARD);
            gameContext.currentGame().addGameEventListener(view);
            currentView.set(view);
        }

        private EditorView getOrCreateEditView() {
            if (editorView == null) {
                editorView = editorViewCreator.get();
                editorView.editor().init(GameBox.CUSTOM_MAP_DIR);
            }
            return editorView;
        }

        ObjectProperty<GameUI_View> currentViewProperty() {
            return currentView;
        }

        GameUI_View currentView() {
            return currentView.get();
        }

        StartPagesCarousel startPagesView() {
            return startPagesView;
        }

        PlayView playView() {
            return playView;
        }

        EditorView editorView() {
            return getOrCreateEditView();
        }
    }

    public GameUI_Implementation(
        Map<String, Class<? extends GameUI_Config>> uiConfigMap,
        GameContext gameContext,
        Stage stage,
        double sceneWidth,
        double sceneHeight)
    {
        requireNonNull(uiConfigMap, "UI configuration map is null");
        this.gameContext = requireNonNull(gameContext, "Game context is null");
        this.stage = requireNonNull(stage, "Stage is null");
        requireNonNegative(sceneWidth, "Main scene width must be a positive number");
        requireNonNegative(sceneHeight, "Main scene height must be a positive number");

        prefs = new GameUI_Preferences();
        PROPERTY_3D_WALL_HEIGHT.set(prefs.getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(prefs.getFloat("3d.obstacle.opacity"));

        configFactory = new GameUI_ConfigFactory(uiConfigMap, prefs);

        // Trigger loading of 3D models used by all game variants
        PacManModel3DRepository.instance();

        customDirWatchdog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);

        clock = new GameClock();
        clock.setPausableAction(() -> simulateAndUpdateGameScene(gameContext.currentGame()));
        clock.setPermanentAction(this::drawCurrentView);

        createScene(sceneWidth, sceneHeight);

        this.viewManager = new ViewManager(
            gameContext,
            layoutPane,
            new StartPagesCarousel(),
            new PlayView(scene),
            this::createEditorView,
            flashMessageView
        );

        viewManager.startPagesView().setUI(this);
        viewManager.playView.setUI(this);

        statusIconBox.visibleProperty().bind(viewManager.currentViewProperty()
            .map(view -> optEditorView().isEmpty() || view != optEditorView().get()));

        // Show paused icon only in play view
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> currentView() == viewManager.playView() && clock.isPaused(),
            viewManager.currentViewProperty(), clock.pausedProperty())
        );

        titleBinding = createStringBinding(this::computeStageTitle,
            // depends on:
            viewManager.currentViewProperty(),
            viewManager.playView().currentGameSceneProperty(),
            scene.heightProperty(),
            gameContext.gameVariantNameProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            clock().pausedProperty()
        );

        layoutPane.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> isCurrentGameSceneID(GameScene_Config.CommonSceneID.PLAY_SCENE_3D)
                    ? Background.fill(Gradients.Samples.random())
                    : GameUI.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            viewManager.currentViewProperty(),
            viewManager.playView().currentGameSceneProperty()
        ));

        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(titleBinding);
    }

    private EditorView createEditorView() {
        final var editorView = new EditorView(stage, this);
        editorView.setQuitEditorAction(_ -> {
            stage.titleProperty().bind(titleBinding);
            showStartView();
        });
        return editorView;
    }

    private void createScene(double width, double height) {
        scene = new Scene(layoutPane, width, height);
        scene.getStylesheets().add(GameUI.STYLE_SHEET_PATH);

        scene.addEventFilter(KeyEvent.KEY_PRESSED,  KEYBOARD::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, KEYBOARD::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> globalActionBindings.matchingAction(KEYBOARD).ifPresentOrElse(action ->
            {
                boolean executed = action.executeIfEnabled(this);
                if (executed) e.consume();
            },
            () -> currentView().onKeyboardInput(this)
        ));
        scene.setOnScroll(e -> currentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

        createStatusIconBox();

        // Large "paused" icon which appears at center of scene
        pausedIcon = createPausedIcon();
        StackPane.setAlignment(pausedIcon, Pos.CENTER);

        // First child is placeholder for view (start pages view, play view, ...)
        layoutPane.getChildren().setAll(new Region(), pausedIcon, statusIconBox, flashMessageView);
    }

    private FontIcon createPausedIcon() {
        return FontIcon.of(FontAwesomeSolid.PAUSE, PAUSE_ICON_SIZE, ArcadePalette.ARCADE_WHITE);
    }

    // Status icon box appears at bottom-left corner of all views except editor view
    private void createStatusIconBox() {
        statusIconBox = new StatusIconBox();
        // hide icon box if editor view is active, avoid creation of editor view in binding expression!
        statusIconBox.iconMuted()    .visibleProperty().bind(PROPERTY_MUTED);
        statusIconBox.icon3D()       .visibleProperty().bind(PROPERTY_3D_ENABLED);
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
    }

    private String computeStageTitle() {
        final GameUI_View view = currentView();
        if (view == null) {
            return "No View?";
        }

        // If view explicitly provides a title, use that
        if (view.titleSupplier().isPresent()) {
            return view.titleSupplier().get().get();
        }

        final boolean debug  = PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D   = PROPERTY_3D_ENABLED.get();
        final boolean paused = clock.isPaused();

        final String appTitle     = paused ? "app.title.paused" : "app.title";
        final String viewModeText = translated(is3D ? "threeD" : "twoD");
        final String shortTitle   = currentConfig().assets().translated(appTitle, viewModeText);

        final GameScene gameScene = currentGameScene().orElse(null);
        if (gameScene == null || !debug) {
            return shortTitle;
        }
        final String sceneClassName = gameScene.getClass().getSimpleName();
        return "%s [%s]".formatted(shortTitle, sceneClassName);
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        clock.stop();
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessage(Duration.seconds(10), "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!");
    }

    private void simulateAndUpdateGameScene(Game game) {
        final SimulationStep step = game.simulationStep();
        step.init(clock.tickCount());
        try {
            game.control().update();
            step.printLog();
            currentGameScene().ifPresent(gameScene -> gameScene.update(game));
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    private void drawCurrentView() {
        try {
            if (currentView() == viewManager.playView()) {
                viewManager.playView().draw();
            }
            flashMessageView.update();
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    // GameUI interface

    @Override
    public ResourceBundle localizedTexts() {
        return GameUI.LOCALIZED_TEXTS;
    }

    @Override
    public DirectoryWatchdog customDirWatchdog() {
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
    public Stage stage() {
        return stage;
    }

    @Override
    public UIPreferences preferences() {
        return prefs;
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = gameContext.currentGame();

        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        currentConfig().soundManager().stopAll();

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
            configFactory.dispose(prevVariantName);
        }

        GameUI_Config newConfig = config(gameVariantName);
        newConfig.init();
        newConfig.soundManager().muteProperty().bind(PROPERTY_MUTED);

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
    public void show() {
        viewManager.playView().dashboard().init(this);
        initStartPage();
        stage.centerOnScreen();
        stage.show();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    private void initStartPage() {
        if (viewManager.startPagesView().numItems() > 0) {
            startPagesView().setSelectedIndex(0);
            startPagesView().currentStartPage().ifPresent(startPage -> startPage.init(this));
            showStartView();
        } else {
            Logger.error("No start page has been added to this UI!");
        }
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        clock.stop();
        customDirWatchdog.dispose();
    }

    @Override
    public VoicePlayer voicePlayer() {
        return voicePlayer;
    }

    @Override
    public GameUI_View currentView() {
        return viewManager.currentView();
    }

    @Override
    public Optional<GameUI_View> optEditorView() {
        return Optional.ofNullable(viewManager.editorView());
    }

    @Override
    public PlayView playView() {
        return viewManager.playView();
    }

    @Override
    public StartPagesCarousel startPagesView() {
        return viewManager.startPagesView();
    }

    @Override
    public Dashboard dashboard() {
        return viewManager.playView().dashboard();
    }

    @Override
    public void showEditorView() {
        if (!gameContext.currentGame().isPlaying() || clock.isPaused()) {
            currentGameScene().ifPresent(gameScene -> gameScene.end(gameContext.currentGame()));
            currentConfig().soundManager().stopAll();
            clock.stop();
            viewManager.editorView().editor().start();
            viewManager.selectView(viewManager.editorView());
        } else {
            Logger.info("Editor view cannot be opened, game is playing");
        }
    }

    @Override
    public void showPlayView() {
        viewManager.selectView(playView());
        final Game game = gameContext.currentGame();
        statusIconBox.iconAutopilot().visibleProperty().bind(game.usingAutopilotProperty());
        statusIconBox.iconCheated()  .visibleProperty().bind(game.cheatUsedProperty());
        statusIconBox.iconImmune()   .visibleProperty().bind(game.immuneProperty());
    }

    @Override
    public void showStartView() {
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        currentConfig().soundManager().stopAll();
        viewManager.selectView(startPagesView());
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return viewManager.playView().currentGameScene();
    }

    @Override
    public boolean isCurrentGameSceneID(GameScene_Config.SceneID sceneID) {
        final GameScene currentGameScene = viewManager.playView().currentGameScene().orElse(null);
        return currentGameScene != null && currentGameSceneConfig().gameSceneHasID(currentGameScene, sceneID);
    }

    @Override
    public void updateGameScene(boolean forceReloading) {
        viewManager.playView().updateGameScene(gameContext.currentGame(), forceReloading);
    }

    @Override
    public GameUI_Config config(String gameVariantName) {
        return configFactory.getOrCreate(gameVariantName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameUI_Config> T currentConfig() {
        return (T) config(gameContext.gameVariantName());
    }

    @Override
    public GameScene_Config currentGameSceneConfig() {
        return currentConfig();
    }
}