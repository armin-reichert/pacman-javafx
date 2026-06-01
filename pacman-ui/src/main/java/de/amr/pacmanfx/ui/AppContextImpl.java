/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.model.CanonicalGameState;
import de.amr.pacmanfx.model.SimulationStep;
import de.amr.pacmanfx.model.world.WorldMapParseException;
import de.amr.pacmanfx.ui.config.UIConfigurationsManager;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImpl;
import de.amr.pacmanfx.uilib.GameClockFX;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

public final class AppContextImpl implements AppContext {

    // All games in a box (only 1,99 €!)
    private final GameBox gameBox;

    private final PreferencesManager preferences;

    private final DirectoryWatchdog directoryWatchdog;

    private final GameClock gameClock = new GameClockFX();

    private final GameUI ui;

    private final GameViewImpl view;

    public AppContextImpl(GameBox gameBox, GameViewImpl view) {
        this.gameBox = requireNonNull(gameBox);
        this.view = requireNonNull(view);

        preferences = new PreferencesManager(getClass());
        directoryWatchdog = new DirectoryWatchdog(gameBox.customMapDir());

        this.ui = new GameUI(
            new UIConfigurationsManager(),
            new FlashMessageManager(),
            new GameSceneManager(this),
            new SoundManager(),
            new SpriteAnimationManager(),
            () -> AppConstants.LOCALIZED_TEXTS,
            view,
            new SubViewManager()
        );

        createSubViews();
    }

    @Override
    public DirectoryWatchdog customDirWatchdog() {
        return directoryWatchdog;
    }

    @Override
    public GameContext gameContext() {
        return gameBox;
    }

    @Override
    public GameClock gameClock() {
        return gameClock;
    }

    @Override
    public PreferencesManager preferences() {
        return preferences;
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    // GameUI_Life interface

    @Override
    public void editMap(File worldMapFile) {
        final SubViewManager subViewManager = ui.subViews();
        subViewManager.ensureEditorViewCreated();
        subViewManager.optEditorView().map(EditorView::editor).ifPresent(editor -> {
            editor.init(gameBox.customMapDir());
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                if (subViewManager.trySelectEditorView()) {
                    stopGame();
                    editor.start();
                }
            } catch (IOException x) {
                Logger.error(x, "Could not open map file {}", worldMapFile);
                shortMessage("Cannot open world map file");
            }
            catch (WorldMapParseException x) {
                Logger.error(x, "Error reading map file data from {}", worldMapFile);
                shortMessage("Cannot read world map file data");
            }
        });
    }

    @Override
    public void restart() {
        stopGame();
        currentGameFlow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(gameClock::start);
    }

    @Override
    public void displayOnScreen() {
        initGameVariantAndRegisterChangeHandler();
        load3DAssets();
        initMainScene();
        view.setAppContext(this);
        initProperties();
        initGameClock();
        initSubViews();
        initView();
        view.show();
        ui.subViews().selectStartView();
        startServices();
    }

    @Override
    public void stopGame() {
        currentGame().prepareNewGame();

        gameClock.stop();
        gameClock.setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        ui.sounds().stopAll();

        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            ui.gameScenes().removeFromPlayView(ui, gameScene);
            ui.gameScenes().gameSceneProperty().set(null);
        });

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        ui.sprites().stopAnimationTimer();
        ui.sprites().animationSet().clear();
        ui.flashMessages().stopTimer();
        directoryWatchdog.dispose();
    }

    // private stuff

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private void createSubViews() {
        final SubViewManager subViewManager = ui.subViews();

        final StartPagesView startView = new StartPagesView(this);
        subViewManager.setStartView(startView);

        final GamePlayView playView = createGamePlaySubView();
        subViewManager.setGamePlayView(playView);

        subViewManager.setEditorViewFactory(() -> createEditorSubView(view.stage()));
    }

    private void initSubViews() {
        ui.subViews().connect(this);
        ui.subViews().gamePlayView().connect(this);
        ui.subViews().gamePlayView().dashboard().connect(this);
    }

    private void initView() {
        view.setIcon(currentUIConfig().assets().image("app_icon"));
    }

    private GamePlayView createGamePlaySubView() {
        final var playView = new GamePlayView(this, AppConstants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> playViewResizer = (_,_,_) -> playView.resizeToFit(view.mainScene());
        view.mainScene().widthProperty().addListener(playViewResizer);
        view.mainScene().heightProperty().addListener(playViewResizer);
        return playView;
    }

    private EditorView createEditorSubView(Stage stage) {
        final var editorView = new EditorView(stage, this);
        editorView.editor().setOnQuit(_ -> {
            // restore title (editor changed it)
            stage.titleProperty().unbind();
            stage.titleProperty().bind(view.stageTitleBindingProperty());
            ui.subViews().selectStartView();
        });
        return editorView;
    }

    private void initGameClock() {
        gameClock.setUpdateAction(() -> {
            final SimulationStep step = currentGame().doSimulationStep();
            step.clearInfo(gameClock.tickCount());
            currentGameFlow().update();
            step.printLog();
            ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(gameClock));
        });
        gameClock.setPermanentAction(() -> ui.subViews().currentView().render());
        gameClock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(Input.instance().keyboard);

        view.mainScene().rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            view.statusIconBox().rootPane(),
            ui.flashMessages().messageView().rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(view.statusIconBox().rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        view.mainScene().init(this);

        view.statusIconBox().bind(currentGame());
    }

    private void initProperties() {
        final UIConfig currentConfig = currentUIConfig();

        final MazeConfig3D mazeConfig3D = currentConfig.worldConfig().maze();
        AppConstants.PROPERTY_3D_WALL_HEIGHT .set(mazeConfig3D.obstacleBaseHeight());
        AppConstants.PROPERTY_3D_WALL_OPACITY.set(mazeConfig3D.obstacleOpacity());

        ui.sounds().muteProperty().bind(AppConstants.PROPERTY_MUTED);

        view.statusIconBox().rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ui.subViews().isSelected(ui.subViews().gamePlayView())
                || ui.subViews().isSelected(ui.subViews().startView()),
            ui.subViews().selectedSubViewProperty()));

        view.mainScene().rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> ui.gameScenes().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? AppConstants.WALLPAPERS[RandomNumberSupport.randomInt(0, AppConstants.WALLPAPERS.length)]
                : AppConstants.BACKGROUND_PAC_MAN_WALLPAPER,
            ui.subViews().selectedSubViewProperty(),
            ui.gameScenes().gameSceneProperty()
        ));
    }

    private void startServices() {
        Platform.runLater(() -> {
            directoryWatchdog.startWatching();
            ui.flashMessages().startTimer();
            ui.sprites().startAnimationTimer();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(gameContext().gameVariantName());
    }

    /**
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = ui.translations().translate("error.oh_no_my_program");
            shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stopGame();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }
}