/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.action.ActionBindingsManager;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameActionBindingsManager;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.layout.playview.GameSceneManager;
import de.amr.pacmanfx.ui.layout.playview.MiniGameView;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.ui.layout.playview.GameSceneEmbedder;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.Translationmanager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonActions.*;
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

    private static final String OH_NO_MY_PROGRAM = "Oh no my program!\nSomeone call an ambulance!";

    private static final int MIN_STAGE_WIDTH  = 280;
    private static final int MIN_STAGE_HEIGHT = 360;

    private final DirectoryWatchdog customDirWatchdog;

    private final SpriteAnimationTimer spriteAnimationTimer = new SpriteAnimationTimer();
    private final SpriteAnimationSet spriteAnimationSet = new SpriteAnimationSet();

    private final GameContext gameContext;

    // So many managers? I think I should fire some!
    private final UIConfigManager uiConfigManager = new UIConfigManager();
    private final ViewManager viewManager;
    private final GameSceneManager gameSceneManager = new GameSceneManager();
    private final ActionBindingsManager actionBindingsManager = new GameActionBindingsManager(Input.instance().keyboard);
    private final SoundManager soundManager = new SoundManager();
    private final VoiceManager voiceManager = new VoiceManager();
    private final Translationmanager translator;

    private final GameSceneEmbedder gameSceneEmbedder = new GameSceneEmbedder();

    // UI components
    private final Stage stage;
    private final StackPane rootPane = new StackPane();
    private final Scene scene = new Scene(rootPane);
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
        viewManager.setEditorViewFactory(this::createEditorView);

        gameSceneManager.setEmbedder(this, gameSceneEmbedder);

        //TODO refactor and untangle
        final PlayView playView = createPlayView();
        viewManager.setPlayView(playView);

        translator = () -> GameUIConstants.LOCALIZED_TEXTS;
        spriteAnimationTimer.setSpriteAnimationSet(spriteAnimationSet);

        BaseRenderer.setArcadeFont(GameUIConstants.FONT_ARCADE_8);

        initLayout(mainSceneWidth,mainSceneHeight);
        initGlobalActionBindings();
        initPropertyBindings();
        initScene();
        initStage();
        initGameClock();

        // preload to make 3D scene creation faster
        load3DModels();
    }

    private PlayView createPlayView() {
        final var playView = new PlayView(this, DEFAULT_DASHBOARD_CONFIG);

        playView.configurePropertyBindings();

        scene.widthProperty().addListener((_,_,_) -> playView.resizeToFit(scene));
        scene.heightProperty().addListener((_,_,_) -> playView.resizeToFit(scene));

        final ActionBindingsManager actionBindings = playView.actionBindings();

        actionBindings.addAny(ACTION_BOOT_SHOW_PLAY_VIEW, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_ENTER_FULLSCREEN, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_QUIT_GAME_SCENE, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SHOW_HELP, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_SLOWER, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_SLOWEST, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_FASTER, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_FASTEST, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_RESET, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_ONE_STEP, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_SIMULATION_TEN_STEPS, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_AUTOPILOT, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_DEBUG_INFO, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_MUTED, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_PAUSED, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_COLLISION_STRATEGY, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_DASHBOARD, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_IMMUNITY, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_MINI_VIEW_VISIBILITY, GameUIConstants.COMMON_BINDINGS);
        actionBindings.addAny(ACTION_TOGGLE_PLAY_SCENE_2D_3D, GameUIConstants.COMMON_BINDINGS);

        return playView;
    }

    private void initGameClock() {
        final GameClock clock = requireNonNull(gameContext.clock(), "Game clock has not been set in game context?");
        clock.setUpdateAction(() -> {
            simulate(gameContext.game());
            optGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> viewManager.currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initLayout(int mainSceneWidth, int mainSceneHeight) {
        rootPane.setPrefSize(mainSceneWidth, mainSceneHeight);
        // First child is placeholder for current view (start view, play view, ...)
        rootPane.getChildren().setAll(new Region(), statusIconBox, flashMessageView, createKeyboardMonitor());
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
    }

    private Node createKeyboardMonitor() {
        final VBox box = new VBox();
        box.setAlignment(Pos.TOP_CENTER);
        box.setBackground(Background.fill(Color.TRANSPARENT));
        box.setPrefSize(300, 50);
        box.setMaxSize(300, 200);
        box.setSpacing(3);
        box.setStyle("""
    -fx-border-color: #ccc;
    -fx-border-width: 2;
    -fx-border-radius: 12;
    -fx-background-radius: 12;
""");
        StackPane.setMargin(box, new Insets(10));
        StackPane.setAlignment(box, Pos.TOP_RIGHT);
        box.visibleProperty().bind(GameUIConstants.PROPERTY_KEYBOARD_MONITOR_VISIBLE);

        Input.instance().keyboard.addListener(keyboard -> {
            box.getChildren().clear();

            final Font titleFont = Font.font("Sans", FontWeight.BLACK, 16);
            final Text title = new Text("Keyboard State");
            title.setFill(Color.WHITE);
            title.setFont(titleFont);
            VBox.setMargin(title, new Insets(4));
            box.getChildren().add(title);

            final Font labelFont = Font.font("Monospace", FontWeight.BOLD, 16);
            keyboard.pressedKeys().stream().sorted().forEach(keyCode -> {
                final Text stateLabel = new Text();
                stateLabel.setFill(Color.LIGHTGRAY);
                stateLabel.setFont(labelFont);
                String modText = "";
                if (keyboard.altDown()) modText += "Alt ";
                if (keyboard.shiftDown()) modText += "Shift ";
                if (keyboard.controlDown()) modText += "Control ";
                if (keyboard.metaDown()) modText += "Meta ";
                stateLabel.setText(modText + keyCode.getName());
                box.getChildren().add(stateLabel);
            });

            final Text footer = new Text("Press Alt+K to close");
            footer.setFill(Color.WHITE);
            footer.setFont(titleFont);
            VBox.setMargin(footer, new Insets(4));
            box.getChildren().add(footer);

        });

        return box;
    }

    private void initPropertyBindings() {
        soundManager.muteProperty().bind(GameUIConstants.PROPERTY_MUTED);

        statusIconBox.visibleProperty().bind(
            viewManager.selectedIDProperty().map(viewID -> viewID == PLAY_VIEW || viewID == START_VIEW));

        titleBinding = createStringBinding(
            () -> {
                final boolean debug  = GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.get();
                final boolean is3D   = GameUIConstants.PROPERTY_3D_ENABLED.get();
                final boolean paused = gameContext.clock().getUpdatesDisabled();
                final GameScene gameScene = optGameScene().orElse(null);
                return computeStageTitle(viewManager.currentView(), gameScene, debug, is3D, paused);
            },
            gameContext.gameVariantNameProperty(),
            viewManager.currentViewProperty(),
            gameSceneManager().gameSceneProperty(),
            GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUIConstants.PROPERTY_3D_ENABLED,
            gameContext.clock().updatesDisabledProperty()
        );
        stage.titleProperty().bind(titleBinding);

        rootPane.backgroundProperty().bind(Bindings.createObjectBinding(
            () -> currentGameSceneHasID(GameSceneConfig.CommonSceneID.PLAY_SCENE_3D)
                ? Background.fill(Gradients.Samples.random())
                : GameUIConstants.BACKGROUND_PAC_MAN_WALLPAPER,
            // depends on:
            viewManager.currentViewProperty(),
            gameSceneManager().gameSceneProperty()
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

    private void initGlobalActionBindings() {
        actionBindingsManager.addAny(CommonActions.ACTION_ENTER_FULLSCREEN,        GameUIConstants.COMMON_BINDINGS);
        actionBindingsManager.addAny(CommonActions.ACTION_OPEN_EDITOR,             GameUIConstants.COMMON_BINDINGS);
        actionBindingsManager.addAny(CommonActions.ACTION_TOGGLE_KEYBOARD_MONITOR, GameUIConstants.COMMON_BINDINGS);
        actionBindingsManager.addAny(CommonActions.ACTION_TOGGLE_MUTED,            GameUIConstants.COMMON_BINDINGS);
        actionBindingsManager.assignToKeyboard();
    }

    private void initScene() {
        scene.getStylesheets().add(GameUIConstants.STYLE_SHEET_PATH);

        scene.addEventFilter(KeyEvent.KEY_PRESSED,  Input.instance().keyboard::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, Input.instance().keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> actionBindingsManager.matchingAction()
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
        showFlashMessage(Duration.seconds(60), "%s\n%s".formatted(OH_NO_MY_PROGRAM, reason.getMessage()));
        stopGame();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void load3DModels() {
        Logger.info("Preloading 3D models...");
        PacManWorld3D.instance();
        Logger.info("Pac-Man scene 3D model loaded");
    }

    // PreferencesManager interface

    @Override
    protected void storeDefaultPrefValues() {
        // store user preference default values here
    }

    // GameUI interface


    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public UIConfig config(String gameVariantName) {
        return uiConfigManager.getOrCreateUIConfig(gameVariantName);
    }

    @Override
    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
    }

    @Override
    public void embedGameSceneIntoPlayView(GameScene gameScene) {
        gameSceneEmbedder.embedGameSceneIntoPlayView(playView(), currentGameSceneConfig(), gameScene);
        playView().contextMenu().hide();
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
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public Translationmanager translator() {
        return translator;
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
        return gameSceneManager().optCurrentGameScene();
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
        game.flow().restartStateWithName(CanonicalGameState.BOOT.name());
        showStartView();
    }

    @Override
    public void restart() {
        stopGame();
        gameContext.game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(gameContext.clock()::start);
    }

    @Override
    public void show() {
        // These need the current UI config to be initialized
        GameUIConstants.PROPERTY_3D_WALL_HEIGHT .set(currentConfig().worldConfig().maze().obstacleBaseHeight());
        GameUIConstants.PROPERTY_3D_WALL_OPACITY.set(currentConfig().worldConfig().maze().obstacleOpacity());

        logPreferences();
        dashboard().init(this);
        viewManager.selectView(START_VIEW);
        stage.centerOnScreen();
        stage.show();
        flashMessageView.start();
        spriteAnimationTimer.start();
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
    public SpriteAnimationSet spriteAnimationSet() {
        return spriteAnimationSet;
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public void stopGame() {
        soundManager.stopAll();
        optGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
        });
        gameContext.clock().stop();
        gameContext.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        spriteAnimationTimer.stop();
        spriteAnimationSet.clear();
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
            return translator.translate("view.missing"); // Should never happen
        }
        if (view.titleSupplier().isPresent()) {
            return view.titleSupplier().get().get();
        }
        final String appTitle = paused ? "app.title.paused" : "app.title";
        final String viewMode = translator.translate(is3D ? "threeD" : "twoD");
        final AssetMap assets = gameContext.gameVariantName() != null ? currentConfig().assets() : null;
        final String normalTitle = assets == null ? "" : assets.translate(appTitle, viewMode);
        return gameScene == null || !debug
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }
}