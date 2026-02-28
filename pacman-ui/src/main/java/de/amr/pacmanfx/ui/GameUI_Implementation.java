/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.action.ActionBindingsManagerImpl;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.layout.EditorView;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.Models3D;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.ui.ViewManager.ViewID.*;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation extends PreferencesManager implements GameUI {

    // Oh no, my program!
    private static final String SOMEONE_CALL_AN_AMBULANCE = "KA-TA-STRO-PHE!\nSOMEONE CALL AN AMBULANCE!";

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private final DirectoryWatchdog customDirWatchdog = new DirectoryWatchdog(GameBox.CUSTOM_MAP_DIR);
    private final UIConfigManager uiConfigManager = new UIConfigManager();
    private final ActionBindingsManager actionBindingsManager = new ActionBindingsManagerImpl();
    private final SoundManager soundManager = new SoundManager();
    private final VoiceManager voiceManager = new VoiceManager();
    private final GameContext gameContext;
    private final ViewManager viewManager;

    private final Stage stage;
    private final StackPane sceneLayout = new StackPane();
    private final Scene scene = new Scene(sceneLayout);

    private final FlashMessageView flashMessageView = new FlashMessageView();

    private final StatusIconBox statusIconBox = new StatusIconBox();

    private StringBinding titleBinding;

    public GameUI_Implementation(GameContext gameContext, Stage stage, int mainSceneWidth, int mainSceneHeight) {
        super(GameUI_Implementation.class);

        requireNonNegative(mainSceneWidth);
        requireNonNegative(mainSceneHeight);

        this.gameContext = requireNonNull(gameContext);
        this.stage = requireNonNull(stage);
        this.viewManager = new ViewManager(this, scene, this::createEditorView, flashMessageView);

        initLayout(mainSceneWidth,mainSceneHeight);
        initActionBindings();
        initPropertyBindings();
        initScene();
        initStage();
        initGameClock();
    }

    private void initGameClock() {
        final GameClock clock = requireNonNull(gameContext.clock(), "Game clock has not been set in game context?");
        clock.setPausableAction(() -> {
            final Game game = gameContext.currentGame();
            simulate(game);
            optGameScene().ifPresent(gameScene -> gameScene.update(game));
        });
        clock.setPermanentAction(() -> viewManager.currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initLayout(int mainSceneWidth, int mainSceneHeight) {
        sceneLayout.setPrefSize(mainSceneWidth, mainSceneHeight);
        // First child is placeholder for current view (start view, play view, ...)
        sceneLayout.getChildren().setAll(new Region(), statusIconBox, flashMessageView);
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
    }

    private void initPropertyBindings() {
        PROPERTY_3D_WALL_HEIGHT.set(getFloat("3d.obstacle.base_height"));
        PROPERTY_3D_WALL_OPACITY.set(getFloat("3d.obstacle.opacity"));

        soundManager.muteProperty().bind(GameUI.PROPERTY_MUTED);

        statusIconBox.visibleProperty().bind(
            viewManager.selectedIDProperty().map(viewID -> viewID == PLAY_VIEW || viewID == START_VIEW));

        titleBinding = createStringBinding(
            () -> {
                final boolean debug  = PROPERTY_DEBUG_INFO_VISIBLE.get();
                final boolean is3D   = PROPERTY_3D_ENABLED.get();
                final boolean paused = gameContext.clock().isPaused();
                final GameScene gameScene = optGameScene().orElse(null);
                return computeStageTitle(viewManager.currentView(), gameScene, debug, is3D, paused);
            },
            gameContext.gameVariantNameProperty(),
            viewManager.currentViewProperty(),
            viewManager.getPlayView().currentGameSceneProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            gameContext.clock().pausedProperty()
        );
        stage.titleProperty().bind(titleBinding);

        sceneLayout.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_3D)
                ? Background.fill(Gradients.Samples.random())
                : GameUI_Resources.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            viewManager.currentViewProperty(),
            viewManager.getPlayView().currentGameSceneProperty()
        ));

        gameContext.gameVariantNameProperty().addListener((_, _, _) -> {
            final Game game = gameContext.currentGame();
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

    private void initActionBindings() {
        actionBindingsManager.registerAnyFrom(CommonGameActions.ACTION_ENTER_FULLSCREEN, GameUI.COMMON_BINDINGS);
        actionBindingsManager.registerAnyFrom(CommonGameActions.ACTION_OPEN_EDITOR,      GameUI.COMMON_BINDINGS);
        actionBindingsManager.registerAnyFrom(CommonGameActions.ACTION_TOGGLE_MUTED,     GameUI.COMMON_BINDINGS);
        actionBindingsManager.addAll(GameUI.KEYBOARD);
    }

    private void initScene() {
        scene.getStylesheets().add(GameUI_Resources.STYLE_SHEET_PATH);

        scene.addEventFilter(KeyEvent.KEY_PRESSED,  KEYBOARD::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, KEYBOARD::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> actionBindingsManager.findMatchingAction(KEYBOARD).ifPresentOrElse(action ->
            {
                boolean executed = action.executeIfEnabled(this);
                if (executed) e.consume();
            },
            () -> viewManager.currentView().onKeyboardInput(this)
        ));
        scene.setOnScroll(e -> optGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));
    }

    private void initStage() {
        stage.setScene(scene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    private void simulate(Game game) {
        final SimulationStep simulationStep = game.simulationStep();
        simulationStep.init(gameContext.clock().tickCount());
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

    // PreferencesManager interface

    @Override
		protected void storeDefaultValues() {
        storeDefaultEntry("3d.bonus.symbol.width", 8.0f);
        storeDefaultEntry("3d.bonus.points.width", 1.8f * 8.0f);
        storeDefaultEntry("3d.floor.padding", 5.0f);
        storeDefaultEntry("3d.floor.thickness", 0.5f);
        storeDefaultEntry("3d.ghost.size", 15.5f);
        storeDefaultEntry("3d.house.base_height", 12.0f);
        storeDefaultEntry("3d.house.opacity", 0.4f);
        storeDefaultEntry("3d.house.sensitivity", 1.5f * TS);
        storeDefaultEntry("3d.house.wall_thickness", 2.5f);
        storeDefaultEntry("3d.level_counter.symbol_size", 10.0f);
        storeDefaultEntry("3d.level_counter.elevation", 6f);
        storeDefaultEntry("3d.lives_counter.capacity", 5);
        storeDefaultColor("3d.lives_counter.pillar_color", Color.grayRgb(120));
        storeDefaultColor("3d.lives_counter.plate_color",  Color.grayRgb(180));
        storeDefaultEntry("3d.lives_counter.shape_size", 12.0f);
        storeDefaultEntry("3d.obstacle.base_height", 4.0f);
        storeDefaultEntry("3d.obstacle.corner_radius", 4.0f);
        storeDefaultEntry("3d.obstacle.opacity", 1.0f);
        storeDefaultEntry("3d.obstacle.wall_thickness", 2.25f);
        storeDefaultEntry("3d.pac.size", 16.0f);
        storeDefaultEntry("3d.pellet.radius", 1.0f);

        // "Kornblumenblau, sind die Augen der Frauen beim Weine. Hicks!"
        storeDefaultColor("context_menu.title.fill", Color.CORNFLOWERBLUE);
        storeDefaultFont("context_menu.title.font", Font.font("Dialog", FontWeight.BLACK, 14.0f));

        storeDefaultColor("debug_text.fill", Color.WHITE);
        storeDefaultColor("debug_text.stroke", Color.GRAY);
        storeDefaultFont("debug_text.font", Font.font("Sans", 16.0f));

        storeDefaultEntry("scene2d.max_scaling", 5.0f);
    }

    // GameUI interface

    @Override
    public UIConfig config(String gameVariantName) {
        return uiConfigManager.getOrCreateUIConfig(gameVariantName);
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID) {
        final GameScene currentGameScene = optGameScene().orElse(null);
        return currentGameScene != null && currentGameSceneConfig().gameSceneHasID(currentGameScene, sceneID);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UIConfig> T currentConfig() {
        final String gameVariantName = gameContext.gameVariantName();
        if (gameVariantName == null) {
            throw new IllegalStateException("Cannot access UI configuration: no game variant is selected");
        }
        return (T) config(gameContext.gameVariantName());
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
        viewManager.selectView(EDITOR_VIEW); // this ensures the editor view is created!
        viewManager.optEditorView().map(EditorView::editor).ifPresent(editor -> {
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
    public PreferencesManager prefs() {
        return this;
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = gameContext.currentGame();
        //TODO this is game-specific and should not be here
        optGameScene().ifPresent(gameScene -> {
            boolean shouldConsumeCoin = game.control().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlaying();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
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
        gameContext.currentGame().control().restartStateNamed(GameControl.StateName.BOOT.name());
        Platform.runLater(gameContext.clock()::start);
    }

    @Override
    public void show() {
        logPreferences();
        load3DModels(); // fail fast
        viewManager.getPlayView().dashboard().init(this);
        viewManager.selectView(START_VIEW);
        stage.centerOnScreen();
        stage.show();
        flashMessageView.start();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    @Override
    public void showEditorView() {
        if (!gameContext.currentGame().isPlaying() || gameContext.clock().isPaused()) {
            stopGame();
            viewManager.selectView(EDITOR_VIEW);
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
        viewManager.selectView(PLAY_VIEW);
    }

    @Override
    public void showStartView() {
        stopGame();
        viewManager.selectView(START_VIEW);
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
        optGameScene().ifPresent(gameScene -> gameScene.end(gameContext.currentGame()));
        soundManager.stopAll();
        gameContext.clock().stop();
        gameContext.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
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
        final AssetMap assets = gameContext.gameVariantName() != null ? currentConfig().assets() : null;
        final String normalTitle = assets == null ? "" : assets.translate(appTitle, viewMode);
        return gameScene == null || !debug
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private void load3DModels() {
        Logger.info("Loaded {}", Models3D.PAC_MAN_MODEL);
        Logger.info("Loaded {}", Models3D.GHOST_MODEL);
        Logger.info("Loaded {}", Models3D.PELLET_MODEL);
    }
}