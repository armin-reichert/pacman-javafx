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
import de.amr.pacmanfx.ui.action.ActionBindingsSet;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.GameActionBindingsSet;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.ui.layout.playview.PlayViewContextMenuHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static de.amr.pacmanfx.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation extends PreferencesManager implements GameUI {

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
    private final ActionBindingsSet actionBindings = new GameActionBindingsSet();
    private final SoundManager soundManager = new SoundManager();
    private final VoiceManager voiceManager = new VoiceManager();
    private final TranslationManager translator;

    private final GameSceneEmbedder gameSceneEmbedder = new GameSceneEmbedder();

    // UI components
    private final Stage stage;
    private final StackPane rootPane = new StackPane();
    private final Scene scene = new Scene(rootPane);
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final StatusIconBox statusIconBox = new StatusIconBox();

    private StringBinding titleBinding;

    private final File initialEditorDir;

    public GameUI_Implementation(GameBox gameBox, Stage stage, int mainSceneWidth, int mainSceneHeight) {
        super(GameUI_Implementation.class);

        requireNonNegative(mainSceneWidth);
        requireNonNegative(mainSceneHeight);

        this.gameContext = requireNonNull(gameBox);
        this.customDirWatchdog = new DirectoryWatchdog(gameBox.customMapDir());
        this.initialEditorDir = gameBox.customMapDir();

        this.stage = requireNonNull(stage);

        gameContext.gameVariantNameProperty().addListener(new GameVariantChangeHandler(this));

        gameSceneManager.setEmbedder(this, gameSceneEmbedder);

        viewManager = createViewManager();
        viewManager.setStartView(new StartPagesCarousel(this));
        viewManager.setEditorViewFactory(this::createEditorView);
        viewManager.setPlayView(createPlayView());

        translator = () -> GameUIConstants.LOCALIZED_TEXTS;
        spriteAnimationTimer.setSpriteAnimationSet(spriteAnimationSet);

        BaseRenderer.setArcadeFont(GameUIConstants.FONT_ARCADE_8);

        initRootPane(mainSceneWidth,mainSceneHeight);
        initGlobalActionBindings();
        initPropertyBindings();
        initScene();
        initStage();
        initGameClock();
    }

    private ViewManager createViewManager() {
        final var viewManager = new ViewManager(rootPane, flashMessageView);

        viewManager.setEditorCanOpen(() -> {
            if (viewManager.isStartViewSelected()) return true;
            if (viewManager.isEditorViewSelected()) return false;
            if (viewManager.isPlayViewSelected()) {
                return !gameContext.game().isPlayingLevel();
            }
            return false;
        });
        return viewManager;
    }

    private PlayView createPlayView() {
        final var playView = new PlayView(this, GameUIConstants.DEFAULT_DASHBOARD_CONFIG);

        playView.rootPane().setOnContextMenuRequested(new PlayViewContextMenuHandler(this, playView));

        playView.configurePropertyBindings();

        scene.widthProperty().addListener((_,_,_) -> playView.resizeToFit(scene));
        scene.heightProperty().addListener((_,_,_) -> playView.resizeToFit(scene));

        final ActionBindingsSet actionBindings = playView.actionBindings();
        actionBindings.registerAllBindingsFromSet(GameUIConstants.COMMON_BINDINGS);

        return playView;
    }

    private EditorView createEditorView() {
        final var editorView = new EditorView(stage, this);
        editorView.editor().setOnQuit(_ -> {
            stage.titleProperty().bind(titleBinding);
            viewManager.selectStartView(this);
        });
        return editorView;
    }

    private void initGameClock() {
        final GameClock clock = requireNonNull(gameContext.clock(), "Game clock has not been set in game context?");
        clock.setUpdateAction(() -> {
            simulate(gameContext.game());
            gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> viewManager.currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    // First child is placeholder for the current view (start view, play view, editor view)
    private void initRootPane(int width, int height) {
        rootPane.setPrefSize(width, height);
        StackPane.setAlignment(statusIconBox, Pos.BOTTOM_LEFT);
        final Region viewPlaceholder = new Region();
        final KeyboardInfo keyboardInfo = new KeyboardInfo();
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);
        rootPane.getChildren().addAll(
            viewPlaceholder,
            statusIconBox,
            flashMessageView,
            keyboardInfo.rootPane());
    }

    private void initPropertyBindings() {
        soundManager.muteProperty().bind(GameUIConstants.PROPERTY_MUTED);

        statusIconBox.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewManager.isPlayViewSelected() || viewManager.isStartViewSelected(),
            viewManager.currentViewProperty()));

        titleBinding = createStringBinding(
            () -> {
                final boolean debug  = GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.get();
                final boolean is3D   = GameUIConstants.PROPERTY_3D_ENABLED.get();
                final boolean paused = gameContext.clock().getUpdatesDisabled();
                final GameScene gameScene = gameSceneManager.optCurrentGameScene().orElse(null);
                return computeStageTitle(viewManager.currentView(), gameScene, debug, is3D, paused);
            },
            gameContext.clock().updatesDisabledProperty(),
            gameContext.gameVariantNameProperty(),
            viewManager.currentViewProperty(),
            gameSceneManager.gameSceneProperty(),
            GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUIConstants.PROPERTY_3D_ENABLED
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

    private void initGlobalActionBindings() {
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_ENTER_FULLSCREEN,        GameUIConstants.COMMON_BINDINGS);
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_OPEN_EDITOR,             GameUIConstants.COMMON_BINDINGS);
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_TOGGLE_KEYBOARD_MONITOR, GameUIConstants.COMMON_BINDINGS);
        actionBindings.registerAnyBindingFromSet(CommonActions.ACTION_TOGGLE_MUTED,            GameUIConstants.COMMON_BINDINGS);
        actionBindings.activate();
    }

    private void initScene() {
        scene.getStylesheets().add(GameUIConstants.STYLE_SHEET_PATH);

        final Keyboard keyboard = Input.instance().keyboard;
        scene.addEventFilter(KeyEvent.KEY_PRESSED,  keyboard::onKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);

        // If a global action is bound to the key press, execute it; otherwise let the current view handle it.
        scene.setOnKeyPressed(e -> actionBindings.matchingAction(keyboard)
            .ifPresentOrElse(action -> {
                if (action.executeIfEnabled(this)) e.consume();
            }, () -> viewManager.currentView().onInput(this)
        ));
        scene.setOnScroll(e -> gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> gameScene.onScroll(e)));
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
        gameSceneEmbedder.embedGameSceneIntoPlayView(scene, viewManager.playView(), currentGameSceneConfig(), gameScene);
        viewManager.playView().contextMenu().hide();
    }

    @Override
    public boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID) {
        final GameScene currentGameScene = gameSceneManager.optCurrentGameScene().orElse(null);
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
    public GameContext gameContext() {
        return gameContext;
    }

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        viewManager.createEditorIfNotExisting(initialEditorDir);
        viewManager.optEditorView().map(EditorView::editor).ifPresent(editor -> {
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                viewManager.selectEditorView(this);
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
    public PreferencesManager prefs() {
        return this;
    }

    @Override
    public void quitCurrentGameScene() {
        final Game game = gameContext.game();
        //TODO this is game-specific and should not be here
        gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> {
            boolean shouldConsumeCoin = game.flow().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlayingLevel();
            if (shouldConsumeCoin && !gameContext.coinMechanism().isEmpty()) {
                gameContext.coinMechanism().consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());
        });

        stopGame();
        game.flow().restartStateWithName(CanonicalGameState.BOOT.name());
        viewManager.selectStartView(this);
    }

    @Override
    public void restart() {
        stopGame();
        gameContext.game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(gameContext.clock()::start);
    }

    @Override
    public void show() {
        logPreferences();

        // preload to make 3D scene creation faster
        load3DModels();

        // These need the current UI config to be initialized
        GameUIConstants.PROPERTY_3D_WALL_HEIGHT .set(currentConfig().worldConfig().maze().obstacleBaseHeight());
        GameUIConstants.PROPERTY_3D_WALL_OPACITY.set(currentConfig().worldConfig().maze().obstacleOpacity());

        viewManager.playView().dashboard().init(this);
        viewManager.selectStartView(this);

        stage.centerOnScreen();
        stage.show();

        Platform.runLater(() -> {
            customDirWatchdog.startWatching();
            flashMessageView.start();
            spriteAnimationTimer.start();
        });
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
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
        gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
        });
        gameContext.clock().stop();
        gameContext.clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);
        Logger.info("Game STOPPED!");
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
    public TranslationManager translator() {
        return translator;
    }

    @Override
    public UIConfigManager uiConfigManager() {
        return uiConfigManager;
    }

    @Override
    public ViewManager viewManager() {
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