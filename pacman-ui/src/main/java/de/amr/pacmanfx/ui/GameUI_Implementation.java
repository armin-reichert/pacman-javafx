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
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import de.amr.pacmanfx.ui.sound.VoicePlayer;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import de.amr.pacmanfx.uilib.model3D.PacManModel3DRepository;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.tinylog.Logger;

import java.util.Map;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    // Oh no, my program!
    private static final String SOMEONE_CALL_AN_AMBULANCE = "KA-TA-STROOO-PHE!\nSOMEONE CALL AN AMBULANCE!";

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

    private final GameUI_ViewManager viewManager;

    private FontIcon pausedIcon;
    private StatusIconBox statusIconBox;

    private StringBinding titleBinding;

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

        customDirWatchdog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);

        clock = new GameClock();
        clock.setPausableAction(this::simulateAndUpdateGameScene);
        clock.setPermanentAction(this::render);

        createScene(sceneWidth, sceneHeight);

        this.viewManager = new GameUI_ViewManager(this, scene, layoutPane, this::createEditorView, flashMessageView);

        setupBindings();

        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);

        // Trigger loading of 3D models used by all game variants
        PacManModel3DRepository.instance();
    }

    private void setupBindings() {
        statusIconBox.visibleProperty()
            .bind(views().currentViewProperty()
                .map(view -> view == views().playView() || view == views().startPagesView()));

        // Show paused icon only in play view
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> views().currentView() == views().playView() && clock.isPaused(),
            views().currentViewProperty(), clock.pausedProperty())
        );

        titleBinding = createStringBinding(
            () -> context().currentGame() == null ? computeStageTitle(null) : computeStageTitle(currentConfig().assets()),
            // depends on:
            views().currentViewProperty(),
            views().playView().currentGameSceneProperty(),
            scene.heightProperty(),
            gameContext.gameVariantNameProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            clock().pausedProperty()
        );
        stage.titleProperty().bind(titleBinding);

        layoutPane.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> currentGameSceneHasID(GameScene_Config.CommonSceneID.PLAY_SCENE_3D)
                ? Background.fill(Gradients.Samples.random())
                : GameUI.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            views().currentViewProperty(),
            views().playView().currentGameSceneProperty()
        ));
    }

    private EditorView createEditorView() {
        final var editorView = new EditorView(stage, this);
        editorView.editor().setOnQuit(_ -> {
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
            () -> views().currentView().onKeyboardInput(this)
        ));
        scene.setOnScroll(e -> views().playView().optGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));

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

    private String computeStageTitle(AssetMap assets) {
        final GameUI_View view = views().currentView();
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
        final String shortTitle   = assets == null ? "" : assets.translated(appTitle, viewModeText);

        final GameScene gameScene = views().playView().optGameScene().orElse(null);
        if (gameScene == null || !debug) {
            return shortTitle;
        }
        final String sceneClassName = gameScene.getClass().getSimpleName();
        return "%s [%s]".formatted(shortTitle, sceneClassName);
    }

    private void simulateAndUpdateGameScene() {
        final Game game = gameContext.currentGame();
        final SimulationStep step = game.simulationStep();
        step.init(clock.tickCount());
        try {
            game.control().update();
            step.printLog();
            views().playView().optGameScene().ifPresent(gameScene -> gameScene.update(game));
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    private void render() {
        try {
            views().currentView().render();
            flashMessageView.update();
        } catch (Throwable x) {
            ka_tas_tro_phe(x);
        }
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        stopGame();
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessage(Duration.seconds(10), SOMEONE_CALL_AN_AMBULANCE);
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
    public void stopGame() {
        views().playView().optGameScene().ifPresent(gameScene -> gameScene.end(context().currentGame()));
        currentConfig().soundManager().stopAll();
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = gameContext.currentGame();
        //TODO this is game-specific and should not be here
        views().playView().optGameScene().ifPresent(gameScene -> {
            boolean shouldConsumeCoin = game.control().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlaying();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });

        stopGame();
        game.control().restart(GameControl.StateName.BOOT.name());
        showStartView();
    }

    @Override
    public void restart() {
        stopGame();
        gameContext.currentGame().control().restart(GameControl.StateName.BOOT.name());
        Platform.runLater(clock::start);
    }

    @Override
    public void show() {
        views().playView().dashboard().init(this);
        views().selectStartView();
        stage.centerOnScreen();
        stage.show();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        customDirWatchdog.dispose();
    }

    @Override
    public VoicePlayer voicePlayer() {
        return voicePlayer;
    }

    @Override
    public GameUI_ViewManager views() {
        return viewManager;
    }

    @Override
    public void showEditorView() {
        if (!gameContext.currentGame().isPlaying() || clock.isPaused()) {
            stopGame();
            views().selectEditorView();
            return;
        }
        Logger.info("Editor cannot be opened while game is playing");
    }

    @Override
    public void showPlayView() {
        views().selectPlayView();
        final Game game = gameContext.currentGame();
        statusIconBox.iconAutopilot().visibleProperty().bind(game.usingAutopilotProperty());
        statusIconBox.iconCheated()  .visibleProperty().bind(game.cheatUsedProperty());
        statusIconBox.iconImmune()   .visibleProperty().bind(game.immuneProperty());
    }

    @Override
    public void showStartView() {
        stopGame();
        views().selectStartView();
    }

    @Override
    public boolean currentGameSceneHasID(GameScene_Config.SceneID sceneID) {
        final GameScene currentGameScene = views().playView().optGameScene().orElse(null);
        return currentGameScene != null && currentGameSceneConfig().gameSceneHasID(currentGameScene, sceneID);
    }

    @Override
    public GameUI_ConfigFactory configFactory() {
        return configFactory;
    }

    @Override
    public GameUI_Config config(String gameVariantName) {
        return configFactory.getOrCreate(gameVariantName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameUI_Config> T currentConfig() {
        final String gameVariantName = gameContext.gameVariantName();
        if (gameVariantName == null) {
            throw new IllegalStateException("Cannot access UI configuration: no game variant is selected");
        }
        return (T) config(gameContext.gameVariantName());
    }

    @Override
    public GameScene_Config currentGameSceneConfig() {
        return currentConfig();
    }
}