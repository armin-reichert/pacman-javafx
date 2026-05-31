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

    private final GameUI_ServiceFacade facade;

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
        this.facade = new GameUI_ServiceFacade(
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
        statusIconBox = new StatusIconBox(facade.translations());
    }

    // GameUI interface

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return customDirWatchdog;
    }

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        facade.views().createEditorIfNotExisting(gameBox.customMapDir());
        facade.views().optEditorView().map(EditorView::editor).ifPresent(editor -> {
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                facade.views().selectEditorView(this);
            } catch (IOException x) {
                Logger.error(x, "Could not open map file {}", worldMapFile);
                facade().showFlashMessage("Cannot open world map file");
            }
            catch (WorldMapParseException x) {
                Logger.error(x, "Error reading map file data from {}", worldMapFile);
                facade().showFlashMessage("Cannot read world map file data");
            }
        });
    }

    @Override
    public void restart() {
        stopGame();
        facade().gameContext().game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(facade().gameContext().clock()::start);
    }

    @Override
    public Scene scene() {
        return scene;
    }

    @Override
    public GameUI_ServiceFacade facade() {
        return facade;
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
        facade().gameContext().game().prepareNewGame();

        facade().gameContext().clock().stop();
        facade().gameContext().clock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        facade.sounds().stopAll();

        facade.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            facade.currentSoundEffects().ifPresent(GameSoundEffects::stopAll);
            gameScene.deactivate();
            facade.gameScenes().removeFromPlayView(facade.views().playView(), gameScene);
            facade.gameScenes().gameSceneProperty().set(null);
        });

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        facade.sprites().stopAnimationTimer();
        facade.sprites().animationSet().clear();
        facade.flashMessages().stopTimer();
        customDirWatchdog.dispose();
    }

    // private stuff

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private void createViews() {
        final ViewManager viewManager = facade.views();

        final StartPagesCarousel startView = new StartPagesCarousel(this);
        viewManager.setStartView(startView);

        final PlayView playView = createPlayView();
        viewManager.setPlayView(playView);

        viewManager.setEditorViewFactory(this::createEditorView);
    }

    private void initViewManager() {
        final ViewManager views = facade.views();

        views.init(scene.rootPane(), facade.flashMessages());

        views.playView().configurePropertyBindings(this);
        views.playView().dashboard().sections().forEach(section -> section.init(this));

        views.setEditorCanOpen(() -> {
            if (views.isStartViewSelected()) return true;
            if (views.isEditorViewSelected()) return false;
            if (views.isPlayViewSelected()) {
                return !facade().gameContext().game().isPlayingLevel();
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
            facade.views().selectStartView();
        });
        return editorView;
    }

    private void initGameClock() {
        final GameClock clock = facade().gameContext().clock();
        clock.setUpdateAction(() -> {
            final SimulationStep step = facade().gameContext().game().doSimulationStep();
            step.clearInfo(clock.tickCount());
            facade().gameContext().game().flow().update();
            step.printLog();
            facade.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> facade.views().currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(Input.instance().keyboard);

        scene.rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            statusIconBox.rootPane(),
            facade.flashMessages().messageView().rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(statusIconBox.rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        scene.init(this);

        statusIconBox.bind(facade().gameContext().game());
    }

    private void initProperties() {
        final String currentVariantName = facade().gameContext().gameVariantName();
        final UIConfig currentConfig = facade.configurations().getOrCreateUIConfig(currentVariantName);

        final MazeConfig3D mazeConfig3D = currentConfig.worldConfig().maze();
        GameUI_Constants.PROPERTY_3D_WALL_HEIGHT .set(mazeConfig3D.obstacleBaseHeight());
        GameUI_Constants.PROPERTY_3D_WALL_OPACITY.set(mazeConfig3D.obstacleOpacity());

        facade.sounds().muteProperty().bind(GameUI_Constants.PROPERTY_MUTED);

        statusIconBox.rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> facade.views().isPlayViewSelected() || facade.views().isStartViewSelected(),
            facade.views().currentViewProperty()));

        stageTitleBinding = createStringBinding(
            this::computeStageTitle,
            facade().gameContext().clock().updatesDisabledProperty(),
            facade().gameContext().gameVariantNameProperty(),
            facade.views().currentViewProperty(),
            facade.gameScenes().gameSceneProperty(),
            GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE,
            GameUI_Constants.PROPERTY_3D_ENABLED
        );

        scene.rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> facade.gameScenes().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
                : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER,
            facade.views().currentViewProperty(),
            facade.gameScenes().gameSceneProperty()
        ));
    }

    private void startServices() {
        Platform.runLater(() -> {
            customDirWatchdog.startWatching();
            facade.flashMessages().startTimer();
            facade.sprites().startAnimationTimer();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        facade().gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(facade().gameContext().gameVariantName());
    }

    private void displayStage() {
        final UIConfig currentConfig = facade.configurations().getOrCreateUIConfig(facade().gameContext().gameVariantName());
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
        facade.views().selectStartView();
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = facade.translations().translate("error.oh_no_my_program");
            facade().showFlashMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stopGame();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }

    private String computeStageTitle() {
        final View view = facade.views().currentView();
        return view == null
            ? facade.translations().translate("view.missing") // Should never happen
            : view.optTitleSupplier().map(Supplier::get).orElse(titleForCurrentGameScene());
    }

    private String titleForCurrentGameScene() {
        final GameScene gameScene = facade.gameScenes().optCurrentGameScene().orElse(null);

        final boolean debug = GameUI_Constants.PROPERTY_DEBUG_INFO_VISIBLE.get();
        final boolean is3D = GameUI_Constants.PROPERTY_3D_ENABLED.get();
        final boolean paused = facade().gameContext().clock().getUpdatesDisabled();

        final String normalTitle = appTitle(paused, is3D);
        return (gameScene == null || !debug)
            ? normalTitle
            : "%s [%s]".formatted(normalTitle, gameScene.getClass().getSimpleName());
    }

    private String appTitle(boolean paused, boolean is3D) {
        final String gameVariantName = facade().gameContext().gameVariantName();
        if (gameVariantName == null) {
            return "";
        }

        final String viewMode = facade.translations().translate(is3D ? "threeD" : "twoD");

        // In game-variant specific resource bundles, there should be two entries with placeholder
        // app.title = Game Variant Name {0}
        // app.title = Game Variant Name {0} (paused)

        final UIConfig currentConfig = facade.currentUIConfig();
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