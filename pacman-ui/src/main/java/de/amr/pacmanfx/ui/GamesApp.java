/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.Globals;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import de.amr.pacmanfx.ui.app.GameSpecification;
import de.amr.pacmanfx.ui.app.GamesContainer;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.config.UIConfigManager;
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
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;

import static java.util.Objects.requireNonNull;

public final class GamesApp implements AppContext {

    class GameContextImpl implements GameContext {

        private final GameFlow gameFlow;
        private HuntingStepResult huntingStepResult;

        public GameContextImpl(GameFlow gameFlow) {
            this.gameFlow = requireNonNull(gameFlow);
        }

        @Override
        public GameModel gameModel() {
            return gameForVariant(currentGameVariantName()).gameModel();
        }

        @Override
        public GameRules gameRules() {
            return gameForVariant(currentGameVariantName()).gameRules();
        }

        @Override
        public GameFlow gameFlow() {
            return gameFlow;
        }

        @Override
        public CollisionStrategy collisionStrategy() {
            return collisionStrategy;
        }

        @Override
        public void setCollisionStrategy(CollisionStrategy strategy) {
            collisionStrategy = requireNonNull(strategy);
        }

        @Override
        public Boolean isCollisionDoubleChecked() {
            return collisionDoubleChecked.get();
        }

        @Override
        public void setCollisionDoubleChecked(boolean doubleChecked) {
            collisionDoubleChecked.set(doubleChecked);
        }

        @Override
        public void startNewHuntingStep() {
            huntingStepResult = new HuntingStepResult();
        }

        @Override
        public HuntingStepResult huntingResult() {
            return huntingStepResult;
        }
    }

    // All games in a box (only 1,99 €!)
    private final GamesContainer gamesContainer;

    private final StringProperty gameVariantName = new SimpleStringProperty();

    private final File customMapDir;

    private final PreferencesManager prefs;

    private final DirectoryWatchdog watchdog;

    private final GameUI ui;

    private final GameViewImpl view;

    private final GameClock gameClock;

    private final CoinMechanism coinMechanism;

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private CollisionStrategy collisionStrategy = CollisionStrategy.SAME_TILE;

    private GameContext currentGameContext;

    public GamesApp(GamesContainer gamesContainer, GameViewImpl view, GameClock gameClock, CoinMechanism coinMechanism) {
        this.gamesContainer = requireNonNull(gamesContainer);
        this.view = requireNonNull(view);
        this.gameClock = requireNonNull(gameClock);
        this.coinMechanism = requireNonNull(coinMechanism);
        this.customMapDir = gamesContainer.customMapDir();//TODO
        prefs = new PreferencesManager(getClass());
        watchdog = new DirectoryWatchdog(customMapDir);

        ui = new GameUI(
            new UIConfigManager(),
            new FlashMessageManager(),
            new GameSceneManager(this),
            new SoundManager(),
            new SpriteAnimationManager(),
            () -> AppConstants.LOCALIZED_TEXTS,
            view,
            new SubViewManager()
        );

        createSubViews();

        gameVariantName.addListener((_, _, newVariantName) -> {
            final GameFlow gameFlow = gameForVariant(newVariantName).gameFlowFactory().get();
            currentGameContext = new GameContextImpl(gameFlow);
            //TODO change this
            gameFlow.setContext(currentGameContext);
        });
    }

    @Override
    public File customMapDir() {
        return customMapDir;
    }

    @Override
    public StringProperty gameVariantNameProperty() {
        return gameVariantName;
    }

    @Override
    public void selectGameVariant(String variantName) {
        requireNonNull(variantName);
        if (gamesContainer.hasGameForVariantName(variantName)) {
            gameVariantName.set(variantName);
        }
        else {
            throw new IllegalArgumentException("Game with name '" + variantName + "' not found");
        }
    }

    @Override
    public String currentGameVariantName() {
        return gameVariantName.get();
    }

    @Override
    public GameSpecification gameForVariant(String variantName) {
        return gamesContainer.gameForVariant(variantName);
    }

    @Override
    public GameContext currentGameContext() {
        return currentGameContext;
    }

    @Override
    public Input input() {
        return Input.instance();
    }

    @Override
    public GameClock gameClock() {
        return gameClock;
    }

    @Override
    public CoinMechanism coinMechanism() {
        return coinMechanism;
    }

    @Override
    public PreferencesManager prefs() {
        return prefs;
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return watchdog;
    }

    @Override
    public void restartGame() {
        stopGame();
        currentGameContext().gameFlow().restartState(GameStateID.BOOT.name());
        Platform.runLater(gameClock()::start);
    }

    @Override
    public void displayOnScreen() {
        initGameVariantAndRegisterChangeHandler();
        load3DAssets();
        initMainScene();
        view.setAppContext(this);
        initProperties();
        initGameClock();
        ui.subViews().connect(this);
        view.setIcon(currentUIConfig().assets().image("app_icon"));
        view.show();
        ui.subViews().selectStartView();
        startServices();
    }

    @Override
    public void stopGame() {
        currentGameContext.gameModel().prepareNewGame();

        gameClock().stop();
        gameClock().setTargetFrameRate(Globals.NUM_TICKS_PER_SEC);

        ui.sounds().stopAll();

        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            ui.gameScenes().removeFromPlayView(this, gameScene);
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
        watchdog.dispose();
    }

    // private stuff

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private void createSubViews() {
        final SubViewManager subViews = ui.subViews();

        final StartPagesView startView = new StartPagesView(this);
        subViews.setStartView(startView);

        final GamePlayView playView = createGamePlaySubView();
        subViews.setGamePlayView(playView);

        subViews.setEditorViewFactory(() -> createEditorSubView(view.stage()));
    }

    private GamePlayView createGamePlaySubView() {
        final var playView = new GamePlayView(this, AppConstants.DEFAULT_DASHBOARD_CONFIG);
        final ChangeListener<? super Number> resizeHandler = (_,_,_) -> playView.resizeToFit(view.mainScene());
        view.mainScene().widthProperty().addListener(resizeHandler);
        view.mainScene().heightProperty().addListener(resizeHandler);
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
        gameClock().setUpdateAction(() -> {
            currentGameContext.gameFlow().makeStep();
            ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(gameClock().tickCount()));
        });
        gameClock().setPermanentAction(() -> ui.subViews().currentView().render());
        gameClock().setErrorHandler(this::ka_tas_tro_phe);
    }

    private void initMainScene() {
        final KeyboardInfo keyboardInfo = new KeyboardInfo(input().keyboard());

        view.mainScene().rootPane().getChildren().addAll(
            new Region(), // placeholder, will be replaced by current view (start, play, edit)
            view.statusIconBox().rootPane(),
            ui.flashMessages().messageView().rootPane(),
            keyboardInfo.rootPane());

        StackPane.setAlignment(view.statusIconBox().rootPane(), Pos.BOTTOM_LEFT);
        keyboardInfo.rootPane().setAlignment(Pos.TOP_CENTER);

        view.mainScene().init(this);

        view.statusIconBox().bind(currentGameContext().gameModel());
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
            watchdog.startWatching();
            ui.flashMessages().startTimer();
            ui.sprites().startAnimationTimer();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final GameVariantChangeHandler gameVariantChangeHandler = new GameVariantChangeHandler(this);
        gameVariantName.addListener(gameVariantChangeHandler);
        gameVariantChangeHandler.enterGameVariant(currentGameVariantName());
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