/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.layout.*;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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

    private final GameUI_Services services;

    // UI components
    private final Stage stage;
    private final GameUI_MainScene scene;
    private final StatusIconBox statusIconBox;

    private StringBinding stageTitleBinding;

    public GameUI_Implementation(GameBox gameBox, Stage stage, int width, int height) {
        this.gameBox = requireNonNull(gameBox);
        this.stage = requireNonNull(stage);
        this.scene = new GameUI_MainScene(requireNonNegative(width), requireNonNegative(height));
        this.customDirWatchdog = new DirectoryWatchdog(gameBox.customMapDir());
        this.services = new GameUI_Services(
            gameBox,
            new ConfigurationsManager(),
            new FlashMessageManager(),
            new GameSceneManager(),
            new PreferencesManager(GameUI_Implementation.class),
            new SoundManager(),
            new SpriteAnimationManager(),
            () -> GameUI_Constants.LOCALIZED_TEXTS,
            new ViewManager()
        );

        createViews();
        statusIconBox = new StatusIconBox(services.translations());
    }

    // GameUI interface

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
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
                services().showFlashMessage("Cannot open world map file");
            }
            catch (WorldMapParseException x) {
                Logger.error(x, "Error reading map file data from {}", worldMapFile);
                services().showFlashMessage("Cannot read world map file data");
            }
        });
    }

    @Override
    public void restart() {
        stopGame();
        services().gameContext().game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(services().gameContext().clock()::start);
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public GameUI_Services services() {
        return services;
    }

    @Override
    public void show() {
        initGameVariantAndRegisterChangeHandler();
        load3DAssets();
        initMainScene();
        initProperties();
        initGameClock();
        initViewManager();
        displayStage();
        startServices();
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
        services().gameContext().game().prepareNewGame();

        services().gameContext().clock().stop();
        services().gameContext().clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        services.sounds().stopAll();

        services.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            services.currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
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
        services.sprites().stopAnimationTimer();
        services.sprites().animationSet().clear();
        services.flashMessages().stopTimer();
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

    private void initViewManager() {
        final ViewManager views = services.views();

        views.init(scene.rootPane(), services.flashMessages());

        views.playView().configurePropertyBindings(this);
        views.playView().dashboard().sections().forEach(section -> section.init(this));

        views.setEditorCanOpen(() -> {
            if (views.isStartViewSelected()) return true;
            if (views.isEditorViewSelected()) return false;
            if (views.isPlayViewSelected()) {
                return !services().gameContext().game().isPlayingLevel();
            }
            return false;
        });
    }

    private PlayView createPlayView() {
        final var playView = new PlayView(this, GameUI_Constants.DEFAULT_DASHBOARD_CONFIG);
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
        final GameClock clock = services().gameContext().clock();
        clock.setUpdateAction(() -> {
            final SimulationStep step = services().gameContext().game().doSimulationStep();
            step.clearInfo(clock.tickCount());
            services().gameContext().game().flow().update();
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
            services.flashMessages().messageView().rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        scene.init(this);

        statusIconBox.bind(services().gameContext().game());
    }

    private void initProperties() {
        final String currentVariantName = services().gameContext().gameVariantName();
        final UIConfig currentConfig = services.configurations().getOrCreateUIConfig(currentVariantName);

        final MazeConfig3D mazeConfig3D = currentConfig.worldConfig().maze();
        GameUI_Constants.PROPERTY_3D_WALL_HEIGHT .set(mazeConfig3D.obstacleBaseHeight());
        GameUI_Constants.PROPERTY_3D_WALL_OPACITY.set(mazeConfig3D.obstacleOpacity());

        services.sounds().muteProperty().bind(GameUI_Constants.PROPERTY_MUTED);

        statusIconBox.rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> services.views().isPlayViewSelected() || services.views().isStartViewSelected(),
            services.views().currentViewProperty()));

        stageTitleBinding = createStringBinding(
            this::computeStageTitle,
            services().gameContext().clock().updatesDisabledProperty(),
            services().gameContext().gameVariantNameProperty(),
            services.views().currentViewProperty(),
            services.gameScenes().gameSceneProperty(),
            GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUI_Constants.PROPERTY_3D_ENABLED
        );

        scene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> services.gameScenes().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
                : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER,
            services.views().currentViewProperty(),
            services.gameScenes().gameSceneProperty()
        ));
    }

    private void startServices() {
        Platform.runLater(() -> {
            customDirWatchdog.startWatching();
            services.flashMessages().startTimer();
            services.sprites().startAnimationTimer();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        services().gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(services().gameContext().gameVariantName());
    }

    private void displayStage() {
        final UIConfig currentConfig = services.configurations().getOrCreateUIConfig(services().gameContext().gameVariantName());
        final Image icon = currentConfig.assets().image("app_icon");
        stage.setScene(scene);
        stage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
        stage.titleProperty().bind(stageTitleBinding);
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
            services().showFlashMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
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

        final boolean debug = GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = GameUI_Constants.PROPERTY_3D_ENABLED.get();
        final boolean paused = services().gameContext().clock().getUpdatesDisabled();

        final String normalTitle = appTitle(paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String appTitle(boolean paused, boolean is3D) {
        final String gameVariantName = services().gameContext().gameVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = services.translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final UIConfig currentConfig = services.currentUIConfig();
        final TranslationManager appSpecificTranslator = currentConfig.assets();
        final String appTitleKey = paused ? "app.title.paused" : "app.title";
        if (appSpecificTranslator.bundle() != null
            && appSpecificTranslator.bundle().containsKey(appTitleKey)) {
            return appSpecificTranslator.translate(appTitleKey, viewMode);
        } else {
            return "Unspecified Game";
        }
    }
}