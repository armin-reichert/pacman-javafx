/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.GameActionBindingsManager;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationDriver;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.actor.GhostModel3D;
import de.amr.pacmanfx.uilib.model3D.actor.PacManModel3D;
import de.amr.pacmanfx.uilib.model3D.world.PelletModel3D;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.ui.layout.ViewManager.ViewID.*;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation extends PreferencesManager implements GameUI {

    private static final DashboardConfig DEFAULT_DASHBOARD_CONFIG = new DashboardConfig(
        110, // label width
        320, // width
        Color.rgb(0, 0, 50, 1.0), // background
        Color.WHITE, // text
        Font.font("Sans", 12), // label font
        Font.font("Sans", 12) // content font
    );

    // Oh no, my program!
    private static final String SOMEONE_CALL_AN_AMBULANCE = "KA-TA-STRO-PHE!\nSOMEONE CALL AN AMBULANCE!";

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private final DirectoryWatchdog customDirWatchdog;
    private final UIConfigManager uiConfigManager = new UIConfigManager();
    private final ActionBindingsManager actionBindings = new GameActionBindingsManager(Input.instance().keyboard);
    private final SpriteAnimationDriver spriteAnimationDriver = new SpriteAnimationDriver();
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

    public GameUI_Implementation(GameBox gameBox, Stage stage, int mainSceneWidth, int mainSceneHeight) {
        super(GameUI_Implementation.class);

        requireNonNegative(mainSceneWidth);
        requireNonNegative(mainSceneHeight);

        this.gameContext = requireNonNull(gameBox);
        this.customDirWatchdog = new DirectoryWatchdog(gameBox.customMapDir());

        this.stage = requireNonNull(stage);
        this.viewManager = new ViewManager(this, scene, gameBox.customMapDir(), flashMessageView);

        viewManager.setStartView(new StartPagesCarousel(this));
        viewManager.setPlayView(new PlayView(this, scene, DEFAULT_DASHBOARD_CONFIG));
        viewManager.setEditorViewFactory(this::createEditorView);

        BaseRenderer.setArcadeFont(GameUI_Resources.FONT_ARCADE_8);

        initLayout(mainSceneWidth,mainSceneHeight);
        initActionBindings();
        initPropertyBindings();
        initScene();
        initStage();
        initGameClock();

        // preload to make 3D scene creation faster
        load3DModels();
    }

    private void initGameClock() {
        final GameClock clock = requireNonNull(gameContext.clock(), "Game clock has not been set in game context?");
        clock.setUpdateAction(() -> {
            simulate(gameContext.game());
            optGameScene().ifPresent(GameScene::update);
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
        soundManager.muteProperty().bind(GameUI.PROPERTY_MUTED);

        statusIconBox.visibleProperty().bind(
            viewManager.selectedIDProperty().map(viewID -> viewID == PLAY_VIEW || viewID == START_VIEW));

        titleBinding = createStringBinding(
            () -> {
                final boolean debug  = PROPERTY_DEBUG_INFO_VISIBLE.get();
                final boolean is3D   = PROPERTY_3D_ENABLED.get();
                final boolean paused = gameContext.clock().getUpdatesDisabled();
                final GameScene gameScene = optGameScene().orElse(null);
                return computeStageTitle(viewManager.currentView(), gameScene, debug, is3D, paused);
            },
            gameContext.gameVariantNameProperty(),
            viewManager.currentViewProperty(),
            playView().gameSceneProperty(),
            PROPERTY_DEBUG_INFO_VISIBLE,
            PROPERTY_3D_ENABLED,
            gameContext.clock().updatesDisabledProperty()
        );
        stage.titleProperty().bind(titleBinding);

        sceneLayout.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_3D)
                ? Background.fill(Gradients.Samples.random())
                : GameUI_Resources.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            viewManager.currentViewProperty(),
            playView().gameSceneProperty()
        ));

        gameContext.gameVariantNameProperty().addListener((_, _, _) -> {
            final Game game = gameContext.game();
            statusIconBox.iconAutopilot().visibleProperty().bind(game.cheats().usingAutopilotProperty());
            statusIconBox.iconCheated()  .visibleProperty().bind(game.cheats().cheatUsedProperty());
            statusIconBox.iconImmune()   .visibleProperty().bind(game.cheats().immuneProperty());
        });
    }

    private PlayView playView() {
        return views().getView(ViewManager.ViewID.PLAY_VIEW, PlayView.class);
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
        actionBindings.addAny(CommonGameActions.ACTION_ENTER_FULLSCREEN, GameUI.COMMON_BINDINGS);
        actionBindings.addAny(CommonGameActions.ACTION_OPEN_EDITOR,      GameUI.COMMON_BINDINGS);
        actionBindings.addAny(CommonGameActions.ACTION_TOGGLE_MUTED,     GameUI.COMMON_BINDINGS);
        actionBindings.assignToKeyboard();
    }

    private void initScene() {
        scene.getStylesheets().add(GameUI_Resources.STYLE_SHEET_PATH);

        scene.addEventFilter(KeyEvent.KEY_PRESSED,  Input.instance().keyboard::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, Input.instance().keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> actionBindings.matchingAction()
            .ifPresentOrElse(action -> {
                if (action.executeIfEnabled(this)) e.consume();
            }, () -> viewManager.currentView().onKeyboardInput(this)
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
        game.flow().update();
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

    private void load3DModels() {
        Logger.info("Preloading 3D models...");
        PacManModel3D.instance();
        GhostModel3D.instance();
        PelletModel3D.instance();
    }

    // PreferencesManager interface

    @Override
    protected void storeDefaultPrefValues() {
        // store user preference default values here
    }

    // GameUI interface

    @Override
    public UIConfig config(String gameVariantName) {
        return uiConfigManager.getOrCreateUIConfig(gameVariantName);
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
        return currentConfig().gameSceneConfig();
    }

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
    }

    @Override
    public Dashboard dashboard() {
        return playView().dashboard();
    }

    @Override
    public void forceGameSceneUpdate() {
        playView().forceGameSceneUpdate();
    }

    @Override
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public ResourceBundle localizedTexts() {
        return GameUI_Resources.LOCALIZED_TEXTS;
    }

    @Override
    public MiniGameView miniView() {
        return playView().miniView();
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
    public Optional<GameScene> optGameScene() {
        return playView().optCurrentGameScene();
    }

    @Override
    public PreferencesManager prefs() {
        return this;
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = gameContext.game();
        //TODO this is game-specific and should not be here
        optGameScene().ifPresent(gameScene -> {
            boolean shouldConsumeCoin = game.flow().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlayingLevel();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });

        stopGame();
        game.flow().restartStateWithName(GameFlow.CanonicalGameState.BOOT.name());
        showStartView();
    }

    @Override
    public void restart() {
        stopGame();
        gameContext.game().flow().restartStateWithName(GameFlow.CanonicalGameState.BOOT.name());
        Platform.runLater(gameContext.clock()::start);
    }

    @Override
    public void show() {
        // These need the current UI config to be initialized
        PROPERTY_3D_WALL_HEIGHT .set(currentConfig().entityConfig().maze().obstacleBaseHeight());
        PROPERTY_3D_WALL_OPACITY.set(currentConfig().entityConfig().maze().obstacleOpacity());

        logPreferences();
        dashboard().init(this);
        viewManager.selectView(START_VIEW);
        stage.centerOnScreen();
        stage.show();
        flashMessageView.start();
        spriteAnimationDriver.startAnimation();
        Platform.runLater(customDirWatchdog::startWatching);
    }

    @Override
    public void showEditorView() {
        if (!gameContext.game().isPlayingLevel() || gameContext.clock().getUpdatesDisabled()) {
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
    public SpriteAnimationDriver spriteAnimationDriver() {
        return spriteAnimationDriver;
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public void stopGame() {
        soundManager.stopAll();
        optGameScene().ifPresent(gameScene -> {
            gameScene.end();
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
        });
        gameContext.clock().stop();
        gameContext.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        spriteAnimationDriver.stopAnimation();
        spriteAnimationDriver.clear();
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
}