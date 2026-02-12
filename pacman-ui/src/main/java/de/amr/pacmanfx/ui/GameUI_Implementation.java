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
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.SimpleActionBindingsManager;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.uilib.GameClockImpl;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.uilib.assets.AssetMap;
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

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.ui.ViewManager.ViewID.*;
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
    private final GameClock clock = new GameClockImpl();
    private final DirectoryWatchdog customDirWatchdog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);
    private final GlobalPreferencesManager preferencesManager = new GlobalPreferencesManager();
    private final UIConfigManager uiConfigManager = new UIConfigManager();
    private final SimpleActionBindingsManager globalActionBindings = new SimpleActionBindingsManager();
    private final ViewManager viewManager;
    private final SoundManager soundManager = new SoundManager();

    private final Stage stage;
    private final StackPane sceneLayout = new StackPane();
    private final Scene scene = new Scene(sceneLayout);

    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final VoiceManager voiceManager = new VoiceManager();

    private final FontIcon pausedIcon = FontIcon.of(FontAwesomeSolid.PAUSE, PAUSE_ICON_SIZE, ArcadePalette.ARCADE_WHITE);
    private final StatusIconBox statusIconBox = new StatusIconBox();

    private StringBinding titleBinding;

    public GameUI_Implementation(GameContext context, Stage stage, double mainSceneWidth, double mainSceneHeight) {
        requireNonNull(context);
        requireNonNull(stage);
        requireNonNegative(mainSceneWidth);
        requireNonNegative(mainSceneHeight);

        this.context = context;
        this.stage = stage;

        clock.setPausableAction(() -> {
            final Game game = context.currentGame();
            simulate(game);
            optGameScene().ifPresent(gameScene -> gameScene.update(game));
        });
        clock.setPermanentAction(() -> views().currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);

        sceneLayout.setPrefSize(mainSceneWidth, mainSceneHeight);
        viewManager = new ViewManager(this, scene, this::createEditorView, flashMessageView);

        soundManager.muteProperty().bind(GameUI.PROPERTY_MUTED);

        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);

        composeLayout();
        initGlobalActionBindings();
        initPropertyBindings();
        initScene();

        PROPERTY_3D_WALL_HEIGHT.set(prefs().getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(prefs().getFloat("3d.obstacle.opacity"));

        // Load 3D models
        final var ignored = PacManModel3DRepository.instance();
    }

    private void composeLayout() {
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
        StackPane.setAlignment(pausedIcon, Pos.CENTER);
        // First child is placeholder for current view (start view, play view, ...)
        sceneLayout.getChildren().setAll(new Region(), pausedIcon, statusIconBox, flashMessageView);
    }

    private void initPropertyBindings() {
        statusIconBox.visibleProperty().bind(
            views().selectedIDProperty().map(viewID -> viewID == PLAY_VIEW || viewID == START_VIEW));

        // Show paused icon only in play view
        pausedIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> views().isSelected(PLAY_VIEW) && clock.isPaused(),
            views().selectedIDProperty(), clock.pausedProperty())
        );

        titleBinding = createStringBinding(
            () -> {
                final boolean debug  = PROPERTY_DEBUG_INFO_VISIBLE.get();
                final boolean is3D   = PROPERTY_3D_ENABLED.get();
                final boolean paused = clock.isPaused();
                final GameScene gameScene = optGameScene().orElse(null);
                return computeStageTitle(views().currentView(), gameScene, debug, is3D, paused);
            },
            context.gameVariantNameProperty(),
            views().currentViewProperty(),
            views().getPlayView().currentGameSceneProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            clock().pausedProperty()
        );
        stage.titleProperty().bind(titleBinding);

        sceneLayout.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_3D)
                ? Background.fill(Gradients.Samples.random())
                : GameUI_Resources.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            views().currentViewProperty(),
            views().getPlayView().currentGameSceneProperty()
        ));

        gameContext().gameVariantNameProperty().addListener((_, _, _) -> {
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

    private void initGlobalActionBindings() {
        globalActionBindings.registerAnyFrom(CommonGameActions.ACTION_ENTER_FULLSCREEN, GameUI.COMMON_BINDINGS);
        globalActionBindings.registerAnyFrom(CommonGameActions.ACTION_OPEN_EDITOR,      GameUI.COMMON_BINDINGS);
        globalActionBindings.registerAnyFrom(CommonGameActions.ACTION_TOGGLE_MUTED,     GameUI.COMMON_BINDINGS);
        globalActionBindings.addAll(GameUI.KEYBOARD);
    }

    private void initScene() {
        scene.getStylesheets().add(GameUI_Resources.STYLE_SHEET_PATH);

        scene.addEventFilter(KeyEvent.KEY_PRESSED,  KEYBOARD::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, KEYBOARD::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> globalActionBindings.findMatchingAction(KEYBOARD).ifPresentOrElse(action ->
            {
                boolean executed = action.executeIfEnabled(this);
                if (executed) e.consume();
            },
            () -> views().currentView().onKeyboardInput(this)
        ));
        scene.setOnScroll(e -> optGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));
    }

    private void simulate(Game game) {
        final SimulationStep simulationStep = game.simulationStep();
        simulationStep.init(clock.tickCount());
        game.control().stateMachine().update();
        simulationStep.printLog();
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
    public GameClock clock() {
        return clock;
    }

    @Override
    public UIConfig config(String gameVariantName) {
        return uiConfigManager.getOrCreateUIConfig(gameVariantName);
    }

    @Override
    public GameContext gameContext() {
        return context;
    }

    @Override
    public boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID) {
        final GameScene currentGameScene = optGameScene().orElse(null);
        return currentGameScene != null && currentGameSceneConfig().gameSceneHasID(currentGameScene, sceneID);
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

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
    }

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        requireNonNull(worldMapFile);
        views().selectView(EDITOR_VIEW); // this ensures the editor view is created!
        views().optEditorView().map(EditorView::editor).ifPresent(editor -> {
            try {
                editor.editFile(worldMapFile);
                showEditorView();
            } catch (IOException x) {
                Logger.error(x, "Could not open map file {}", worldMapFile);
                showFlashMessage("Cannot open world map file");
            }
            catch (WorldMapParseException x) {
                Logger.error(x, "Error reading map file data from {}", worldMapFile);
                showFlashMessage("Cannot read world map file data");
            }
        });
    }


    @Override
    public ResourceBundle localizedTexts() {
        return GameUI_Resources.LOCALIZED_TEXTS;
    }

    @Override
    public GlobalPreferencesManager prefs() {
        return preferencesManager;
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = context.currentGame();
        //TODO this is game-specific and should not be here
        optGameScene().ifPresent(gameScene -> {
            boolean shouldConsumeCoin = game.control().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlaying();
            if (shouldConsumeCoin && !context.coinMechanism().isEmpty()) {
                context.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });

        stopGame();
        game.control().restartStateNamed(GameControl.StateName.BOOT.name());
        showStartView();
    }

    @Override
    public void restart() {
        stopGame();
        context.currentGame().control().restartStateNamed(GameControl.StateName.BOOT.name());
        Platform.runLater(clock::start);
    }

    @Override
    public void show() {
        views().getPlayView().dashboard().init(this);
        views().selectView(START_VIEW);
        stage.centerOnScreen();
        stage.show();
        flashMessageView.start();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    @Override
    public void showEditorView() {
        if (!context.currentGame().isPlaying() || clock.isPaused()) {
            stopGame();
            views().selectView(EDITOR_VIEW);
            return;
        }
        Logger.info("Editor cannot be opened while game is playing");
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    @Override
    public void showPlayView() {
        views().selectView(PLAY_VIEW);
    }

    @Override
    public void showStartView() {
        stopGame();
        views().selectView(START_VIEW);
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public void stopGame() {
        optGameScene().ifPresent(gameScene -> gameScene.end(gameContext().currentGame()));
        soundManager.stopAll();
        clock.stop();
        clock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
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
    public ViewManager views() {
        return viewManager;
    }

    @Override
    public VoiceManager voicePlayer() {
        return voiceManager;
    }

    // private stuff

    private String computeStageTitle(
        View view, GameScene gameScene,
        boolean debug, boolean is3D, boolean paused)
    {
        if (view == null) {
            return translate("view.missing"); // Should never happen
        }
        if (view.titleSupplier().isPresent()) {
            return view.titleSupplier().get().get();
        }
        final String appTitle = paused ? "app.title.paused" : "app.title";
        final String viewMode = translate(is3D ? "threeD" : "twoD");
        final AssetMap assets = context.gameVariantName() != null ? currentConfig().assets() : null;
        final String normalTitle = assets == null ? "" : assets.translate(appTitle, viewMode);
        return gameScene == null || !debug
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }
}