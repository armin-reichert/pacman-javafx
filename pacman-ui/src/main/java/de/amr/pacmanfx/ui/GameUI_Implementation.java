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
import de.amr.pacmanfx.ui.config.ConfigurationsManager;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.Editor_SubView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlay_SubView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPages_SubView;
import de.amr.pacmanfx.ui.view.GameUI_View;
import de.amr.pacmanfx.ui.view.GameUI_View_Implementation;
import de.amr.pacmanfx.uilib.GameClockFX;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * User interface for the Pac-Man game suite. Shows a carousel with a start page for each game variant.
 */
public final class GameUI_Implementation implements GameUI {

    // All games in a box (only 1,99 €!)
    private final GameBox gameBox;

    private final GameUI_ServicesAccess access;

    private final GameUI_View_Implementation view;

    public GameUI_Implementation(GameBox gameBox, GameUI_View_Implementation view) {
        this.gameBox = requireNonNull(gameBox);
        this.view = requireNonNull(view);

        this.access = new GameUI_ServicesAccess(
            gameBox,
            new GameClockFX(),
            new DirectoryWatchdog(gameBox.customMapDir()),
            new ConfigurationsManager(),
            new FlashMessageManager(),
            new GameSceneManager(view),
            new PreferencesManager(GameUI_Implementation.class),
            new SoundManager(),
            new SpriteAnimationManager(),
            () -> GameUI_Constants.LOCALIZED_TEXTS,
            new SubViewManager()
        );

        createSubViews();
    }

    // GameUI interface

    @Override
    public GameUI_View view() {
        return view;
    }

    @Override
    public GameUI_ServicesAccess access() {
        return access;
    }

    // GameUI_Life interface

    @Override
    public void openWorldMapFileInEditor(File worldMapFile) {
        final SubViewManager subViewManager = access().subViews();
        subViewManager.ensureEditorViewCreated();
        subViewManager.optEditorView().map(Editor_SubView::editor).ifPresent(editor -> {
            editor.init(gameBox.customMapDir());
            try {
                if (worldMapFile != null) {
                    editor.editFile(worldMapFile);
                }
                if (subViewManager.trySelectEditorView()) {
                    stopGame();
                }
            } catch (IOException x) {
                Logger.error(x, "Could not open map file {}", worldMapFile);
                access().flashMessage("Cannot open world map file");
            }
            catch (WorldMapParseException x) {
                Logger.error(x, "Error reading map file data from {}", worldMapFile);
                access().flashMessage("Cannot read world map file data");
            }
        });
    }

    @Override
    public void restart() {
        stopGame();
        access().gameContext().game().flow().restartStateWithName(CanonicalGameState.BOOT.name());
        Platform.runLater(access().gameClock()::start);
    }

    @Override
    public void show() {
        initGameVariantAndRegisterChangeHandler();
        load3DAssets();
        initMainScene();
        view().attachServices(access);
        initProperties();
        initGameClock();
        initSubViews();
        displayStage(view().stage());
        startServices();
    }

    @Override
    public void stopGame() {
        access().currentGame().prepareNewGame();

        access().gameClock().stop();
        access().gameClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        access().sounds().stopAll();

        access().gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            access().gameScenes().removeFromPlayView(access, gameScene);
            access().gameScenes().gameSceneProperty().set(null);
        });

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        Logger.info("Application is terminated now. There is no way back!");
        stopGame();
        access().sprites().stopAnimationTimer();
        access().sprites().animationSet().clear();
        access().flashMessages().stopTimer();
        access().customDirWatchdog().dispose();
    }

    // private stuff

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private void createSubViews() {
        final SubViewManager subViewManager = access().subViews();

        final StartPages_SubView startView = new StartPages_SubView(this);
        subViewManager.setStartView(startView);

        final GamePlay_SubView playView = createGamePlaySubView();
        subViewManager.setGamePlayView(playView);

        subViewManager.setEditorViewFactory(() -> createEditorSubView(view().stage()));
    }

    private void initSubViews() {
        final SubViewManager subViewManager = access().subViews();

        subViewManager.setUI(this);

        access().gamePlayView().configurePropertyBindings(this);
        access().dashboard().sections().forEach(section -> section.init(this));

        subViewManager.setEditorCanOpen(() -> {
            // No editor view exists or editor already selected: cannot open
            if (subViewManager.optEditorView().isEmpty()) return false;
            if (subViewManager.isSelected(subViewManager.optEditorView().get())) return false;

            if (subViewManager.isSelected(subViewManager.startView())) return true;

            if (subViewManager.isSelected(subViewManager.gamePlayView())) {
                return !access().currentGame().isPlayingLevel();
            }
            return false;
        });
    }

    private GamePlay_SubView createGamePlaySubView() {
        final var playView = new GamePlay_SubView(this, GameUI_Constants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> playViewResizer = (_,_,_) -> playView.resizeToFit(view().mainScene());
        view().mainScene().widthProperty().addListener(playViewResizer);
        view().mainScene().heightProperty().addListener(playViewResizer);
        return playView;
    }

    private Editor_SubView createEditorSubView(Stage stage) {
        final var editorView = new Editor_SubView(stage, this);
        editorView.editor().setOnQuit(_ -> {
            // restore title (editor changed it)
            stage.titleProperty().unbind();
            stage.titleProperty().bind(view.stageTitleBindingProperty());
            access().selectStartView();
        });
        return editorView;
    }

    private void initGameClock() {
        final GameClock clock = access().gameClock();
        clock.setUpdateAction(() -> {
            final SimulationStep step = access().gameContext().game().doSimulationStep();
            step.clearInfo(clock.tickCount());
            access().gameContext().game().flow().update();
            step.printLog();
            access().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock));
        });
        clock.setPermanentAction(() -> access().subViews().currentView().render());
        clock.setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(Input.instance().keyboard);

        view().mainScene().rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            view().statusIconBox().rootPane(),
            access().flashMessages().messageView().rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(view().statusIconBox().rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        view().mainScene().init(this);

        view().statusIconBox().bind(access().gameContext().game());
    }

    private void initProperties() {
        final String currentVariantName = access().gameContext().gameVariantName();
        final UIConfig currentConfig = access().configurations().getOrCreateUIConfig(currentVariantName);

        final MazeConfig3D mazeConfig3D = currentConfig.worldConfig().maze();
        GameUI_Constants.PROPERTY_3D_WALL_HEIGHT .set(mazeConfig3D.obstacleBaseHeight());
        GameUI_Constants.PROPERTY_3D_WALL_OPACITY.set(mazeConfig3D.obstacleOpacity());

        access().sounds().muteProperty().bind(GameUI_Constants.PROPERTY_MUTED);

        view().statusIconBox().rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> access().subViews().isSelected(access().subViews().gamePlayView())
                || access().subViews().isSelected(access().subViews().startView()),
            access().subViews().currentSubViewProperty()));

        view().mainScene().rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> access().gameScenes().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? GameUI_Constants.WALLPAPERS[RandomNumberSupport.randomInt(0, GameUI_Constants.WALLPAPERS.length)]
                : GameUI_Constants.BACKGROUND_PAC_MAN_WALLPAPER,
            access().subViews().currentSubViewProperty(),
            access().gameScenes().gameSceneProperty()
        ));
    }

    private void startServices() {
        Platform.runLater(() -> {
            access().customDirWatchdog().startWatching();
            access().flashMessages().startTimer();
            access().sprites().startAnimationTimer();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        access().gameContext().gameVariantNameProperty().addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(access().gameContext().gameVariantName());
    }

    private void displayStage(Stage stage) {
        final UIConfig currentConfig = access().configurations().getOrCreateUIConfig(access().gameContext().gameVariantName());
        final Image icon = currentConfig.assets().image("app_icon");
        stage.setScene(view().mainScene());
        stage.setMinWidth(GameUI_Constants.MIN_STAGE_WIDTH);
        stage.setMinHeight(GameUI_Constants.MIN_STAGE_HEIGHT);
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        stage.centerOnScreen();
        stage.show();
        access().selectStartView();
    }

    /**
     * @param reason what caused this catastrophe
     *
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = access().translations().translate("error.oh_no_my_program");
            access().flashMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stopGame();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }
}