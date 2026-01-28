/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
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
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoicePlayer;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.Translator;
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

import java.util.ResourceBundle;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    // Oh no, my program!
    private static final String SOMEONE_CALL_AN_AMBULANCE = "KA-TA-STRO-PHE!\nSOMEONE CALL AN AMBULANCE!";

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;
    private static final int PAUSE_ICON_SIZE = 80;

    private final GameContext context;
    private final GameClock clock = new GameClock();
    private final DirectoryWatchdog customDirWatchdog;
    private final ActionBindingsManager globalActionBindings = new GlobalActionBindings();
    private final UIConfigManager uiConfigManager;
    private final ViewManager viewManager;
    private final SoundManager soundManager;
    private final Stage stage;
    private final Scene scene;
    private final StackPane layoutPane = new StackPane();

    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final VoicePlayer voicePlayer = new VoicePlayer();

    private final FontIcon pausedIcon;
    private final StatusIconBox statusIconBox = new StatusIconBox();

    private StringBinding titleBinding;

    public GameUI_Implementation(GameContext context, Stage stage, double sceneWidth, double sceneHeight) {
        requireNonNull(context);
        requireNonNull(stage);
        requireNonNegative(sceneWidth);
        requireNonNegative(sceneHeight);

        this.context = context;
        this.stage = stage;

        clock.setPausableAction(this::simulateAndUpdateGameScene);
        clock.setPermanentAction(this::render);

        uiConfigManager = new UIConfigManager();
        customDirWatchdog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);
        scene = new Scene(layoutPane, sceneWidth, sceneHeight);
        pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, PAUSE_ICON_SIZE, ArcadePalette.ARCADE_WHITE);
        viewManager = new ViewManager(this, scene, layoutPane, this::createEditorView, flashMessageView);

        soundManager = new SoundManager();
        soundManager.muteProperty().bind(GameUI.PROPERTY_MUTED);

        composeLayout();
        setupScene();
        setupBindings();
        setupStage();

        PROPERTY_3D_WALL_HEIGHT.set(GlobalPreferencesManager.instance().getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(GlobalPreferencesManager.instance().getFloat("3d.obstacle.opacity"));

        // Load 3D models
        final var ignored = PacManModel3DRepository.instance();
    }

    private void setupStage() {
        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    private void composeLayout() {
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);
        // First child is placeholder for current view (start view, play view, ...)
        layoutPane.getChildren().setAll(new Region(), pausedIcon, statusIconBox, flashMessageView);
    }

    private void setupBindings() {
        statusIconBox.visibleProperty().bind(views().currentViewProperty()
            .map(view -> view == views().playView() || view == views().startPagesView()));

        // Show paused icon only in play view
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> views().currentView() == views().playView() && clock.isPaused(),
            views().currentViewProperty(), clock.pausedProperty())
        );

        titleBinding = createStringBinding(
            () -> {
                final GameScene gameScene = viewManager.playView().optGameScene().orElse(null);
                final AssetMap assets = context.gameVariantName() != null ? currentConfig().assets() : null;
                final View view = views().currentView();
                final boolean debug  = PROPERTY_DEBUG_INFO_VISIBLE.get();
                final boolean is3D   = PROPERTY_3D_ENABLED.get();
                final boolean paused = clock.isPaused();
                return computeStageTitle(this, view, assets, gameScene, debug, is3D, paused);
            }, // depends on:
            context.gameVariantNameProperty(),
            views().currentViewProperty(),
            views().playView().currentGameSceneProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            clock().pausedProperty()
        );
        stage.titleProperty().bind(titleBinding);

        layoutPane.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_3D)
                ? Background.fill(Gradients.Samples.random())
                : GameUI.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            views().currentViewProperty(),
            views().playView().currentGameSceneProperty()
        ));

        context().gameVariantNameProperty().addListener((_, _, _) -> {
            final Game game = context.currentGame();
            statusIconBox.iconAutopilot().visibleProperty().bind(game.usingAutopilotProperty());
            statusIconBox.iconCheated()  .visibleProperty().bind(game.cheatUsedProperty());
            statusIconBox.iconImmune()   .visibleProperty().bind(game.immuneProperty());
        });
    }

    private EditorView createEditorView() {
        final var editorView = new EditorView(stage, this);
        editorView.editor().setOnQuit(_ -> {
            stage.titleProperty().bind(titleBinding);
            showStartView();
        });
        return editorView;
    }

    private void setupScene() {
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
    }

    private void simulateAndUpdateGameScene() {
        final Game game = context.currentGame();
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
        Logger.error(reason);
        Logger.error("SOMETHING VERY BAD HAPPENED!");
        showFlashMessage(Duration.seconds(60), "%s\n%s".formatted(SOMEONE_CALL_AN_AMBULANCE, reason.getMessage()));
        stopGame();
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
        return context;
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    @Override
    public void stopGame() {
        views().playView().optGameScene().ifPresent(gameScene -> gameScene.end(context().currentGame()));
        soundManager.stopAll();
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = context.currentGame();
        //TODO this is game-specific and should not be here
        views().playView().optGameScene().ifPresent(gameScene -> {
            boolean shouldConsumeCoin = game.control().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlaying();
            if (shouldConsumeCoin && !context.coinMechanism().isEmpty()) {
                context.coinMechanism().consumeCoin();
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
        context.currentGame().control().restart(GameControl.StateName.BOOT.name());
        Platform.runLater(clock::start);
    }

    @Override
    public void show() {
        views().playView().dashboard().init(this);
        views().selectStartView();
        stage.centerOnScreen();
        stage.show();
        flashMessageView.start();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        flashMessageView.stop();
        customDirWatchdog.dispose();
    }

    @Override
    public UIConfigManager uiConfigManager() {
        return uiConfigManager;
    }

    @Override
    public VoicePlayer voicePlayer() {
        return voicePlayer;
    }

    @Override
    public ViewManager views() {
        return viewManager;
    }

    @Override
    public void showEditorView() {
        if (!context.currentGame().isPlaying() || clock.isPaused()) {
            stopGame();
            views().selectEditorView();
            return;
        }
        Logger.info("Editor cannot be opened while game is playing");
    }

    @Override
    public void showPlayView() {
        views().selectPlayView();
    }

    @Override
    public void showStartView() {
        stopGame();
        views().selectStartView();
    }

    @Override
    public boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID) {
        final GameScene currentGameScene = views().playView().optGameScene().orElse(null);
        return currentGameScene != null && currentGameSceneConfig().gameSceneHasID(currentGameScene, sceneID);
    }

    @Override
    public UIConfig config(String gameVariantName) {
        return uiConfigManager.getOrCreate(gameVariantName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UIConfig> T currentConfig() {
        final String gameVariantName = context.gameVariantName();
        if (gameVariantName == null) {
            throw new IllegalStateException("Cannot access UI configuration: no game variant is selected");
        }
        return (T) config(context.gameVariantName());
    }

    @Override
    public GameSceneConfig currentGameSceneConfig() {
        return currentConfig();
    }

    // private stuff

    private static String computeStageTitle(Translator translator, View view, AssetMap assets, GameScene gameScene,
                                            boolean debug, boolean is3D, boolean paused)
    {
        if (view == null) {
            return translator.translate("view.missing"); // Should never happen
        }
        if (view.titleSupplier().isPresent()) {
            return view.titleSupplier().get().get();
        }
        final String appTitle = paused ? "app.title.paused" : "app.title";
        final String viewMode = translator.translate(is3D ? "threeD" : "twoD");
        final String normalTitle = assets == null ? "" : assets.translate(appTitle, viewMode);
        return gameScene == null || !debug
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }
}