/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.widgets.FlashMessageView;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import static de.amr.pacmanfx.core.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    // Game model access
    private final GameBox gameBox;

    // Observes changes in custom map directory
    private final DirectoryWatchdog customDirWatchdog;

    private final UIServices services;

    // Sprite animation support
    private final SpriteAnimationTimer spriteAnimationTimer = new SpriteAnimationTimer(new SpriteAnimationSet());

    // UI components
    private final Stage stage;
    private final GameUI_MainScene scene;
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final StatusIconBox statusIconBox;

    private StringBinding stageTitleBinding;

    public GameUI_Implementation(GameBox gameBox, Stage stage, int mainSceneWidth, int mainSceneHeight) {
        this.gameBox = requireNonNull(gameBox);
        this.stage = requireNonNull(stage);
        this.scene = new GameUI_MainScene(requireNonNegative(mainSceneWidth), requireNonNegative(mainSceneHeight));
        this.customDirWatchdog = new DirectoryWatchdog(gameBox.customMapDir());

        services = new UIServices(
            new UIConfigManager(),
            new GameSceneManager(),
            new PreferencesManager(GameUI_Implementation.class),
            new SoundManager(),
            () -> GameUIConstants.LOCALIZED_TEXTS,
            new ViewManager()
        );

        createViews();
        statusIconBox = new StatusIconBox(services.translations());
    }

    // GameUI interface


    @Override
    public UIServices services() {
        return services;
    }

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends UIConfig> T currentConfig() {
        final String gameVariantName = gameContext().gameVariantName();
        if (gameVariantName == null) {
            throw new IllegalStateException("Cannot access UI configuration: no game variant is selected");
        }
        return (T) services.configurations().getOrCreateUIConfig(gameVariantName);
    }

    @Override
    public GameContext gameContext() {
        return gameBox;
    }

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        services.views().createEditorIfNotExisting(gameBox.customMapDir());
        services.views().optEditorView().map(EditorView::editor).ifPresent(editor -> {
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                services.views().selectEditorView(this);
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
    public void restart() {
        stopGame();
        gameContext().game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(gameContext().clock()::start);
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public void show() {
        initGameVariantAndRegisterChangeHandler();
        load3DAssets();
        initMainScene();
        initProperties();
        initGameClock();
        initViewManager(services.views(), scene.rootPane(), flashMessageView, gameBox);
        displayStage();
        startServices();
    }

    @Override
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessageView.showMessage(String.format(message, args), duration.toSeconds());
    }

    @Override
    public SpriteAnimationSet spriteAnimationSet() {
        return spriteAnimationTimer.spriteAnimationSet();
    }

    @Override
    public Stage stage() {
        return stage;
    }

    @Override
    public StatusIconBox statusIconBox() {
        return statusIconBox;
    }

    @Override
    public void stopGame() {
        gameContext().game().prepareNewGame();

        gameContext().clock().stop();
        gameContext().clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        services.sounds().stopAll();

        services.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.soundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene.deactivate();
            services.gameScenes().removeFromPlayView(services.views().playView(), gameScene);
            services.gameScenes().gameSceneProperty().set(null);
        });

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        spriteAnimationTimer.stop();
        spriteAnimationTimer.spriteAnimationSet().clear();
        flashMessageView.stopTimer();
        customDirWatchdog.dispose();
    }

    // private stuff

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private void createViews() {
        final ViewManager viewManager = services.views();

        final StartPagesCarousel startView = new StartPagesCarousel(this);
        viewManager.setStartView(startView);

        final PlayView playView = createPlayView();
        viewManager.setPlayView(playView);

        viewManager.setEditorViewFactory(this::createEditorView);
    }

    private void initViewManager(ViewManager viewManager, Pane rootPane, FlashMessageView flashMessageView, GameContext gameContext) {
        viewManager.init(rootPane, flashMessageView);

        viewManager.playView().configurePropertyBindings(this);
        viewManager.playView().dashboard().init(this);

        viewManager.setEditorCanOpen(() -> {
            if (viewManager.isStartViewSelected()) return true;
            if (viewManager.isEditorViewSelected()) return false;
            if (viewManager.isPlayViewSelected()) {
                return !gameContext.game().isPlayingLevel();
            }
            return false;
        });
    }

    private PlayView createPlayView() {
        final var playView = new PlayView(this, GameUIConstants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> playViewResizer = (_,_,_) -> playView.resizeToFit(scene);
        scene.widthProperty().addListener(playViewResizer);
        scene.heightProperty().addListener(playViewResizer);
        return playView;
    }

    private EditorView createEditorView() {
        final var editorView = new EditorView(stage, this);
        editorView.editor().setOnQuit(_ -> {
            // restore title (editor changed it)
            stage.titleProperty().unbind();
            stage.titleProperty().bind(stageTitleBinding);
            services.views().selectStartView();
        });
        return editorView;
    }

    private void initGameClock() {
        final GameClock clock = gameContext().clock();
        clock.setUpdateAction(() -> {
            final SimulationStep step = gameContext().game().doSimulationStep();
            step.clearInfo(clock.tickCount());
            gameContext().game().flow().update();
            step.printLog();
            services.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> services.views().currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(Input.instance().keyboard);

        scene.rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            statusIconBox.rootPane(),
            flashMessageView.rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        scene.init(this);

        statusIconBox.bind(gameContext().game());
    }

    private void initProperties() {

        // These need the current UI config to be initialized
        GameUIConstants.PROPERTY_3D_WALL_HEIGHT .set(currentConfig().worldConfig().maze().obstacleBaseHeight());
        GameUIConstants.PROPERTY_3D_WALL_OPACITY.set(currentConfig().worldConfig().maze().obstacleOpacity());

        services.sounds().muteProperty().bind(GameUIConstants.PROPERTY_MUTED);

        statusIconBox.rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> services.views().isPlayViewSelected() || services.views().isStartViewSelected(),
            services.views().currentViewProperty()));

        stageTitleBinding = createStringBinding(
            this::computeStageTitle,
            gameContext().clock().updatesDisabledProperty(),
            gameContext().gameVariantNameProperty(),
            services.views().currentViewProperty(),
            services.gameScenes().gameSceneProperty(),
            GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUIConstants.PROPERTY_3D_ENABLED
        );

        scene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> services.gameScenes().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? GameUIConstants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUIConstants.WALLPAPERS.length)]
                : GameUIConstants.BACKGROUND_PAC_MAN_WALLPAPER
            , services.views().currentViewProperty(), services.gameScenes().gameSceneProperty()
        ));
    }

    private void startServices() {
        Platform.runLater(() -> {
            customDirWatchdog.startWatching();
            flashMessageView.startTimer();
            spriteAnimationTimer.start();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(gameContext().gameVariantName());
    }

    private void displayStage() {
        stage.setScene(scene);
        stage.setMinWidth(GameUIConstants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUIConstants.MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(stageTitleBinding);
        final Image icon = currentConfig().assets().image("app_icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        stage.centerOnScreen();
        stage.show();
        services.views().selectStartView();
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = services.translations().translate("error.oh_no_my_program");
            showFlashMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stopGame();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }

    private String computeStageTitle() {
        final View view = services.views().currentView();
        return view == null
            ? services.translations().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene());
    }

    private String titleForCurrentGameScene() {
        final GameScene gameScene = services.gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = GameUIConstants.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = GameUIConstants.PROPERTY_3D_ENABLED.get();
        final boolean paused = gameContext().clock().getUpdatesDisabled();

        final String normalTitle = appTitle(paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String appTitle(boolean paused, boolean is3D) {
        final String gameVariantName = gameContext().gameVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = services.translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final TranslationManager appSpecificTranslator = currentConfig().assets();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.bundle() != null
            && appSpecificTranslator.bundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}