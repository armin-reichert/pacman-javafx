/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.math.RandomNumberSupport;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GlobalsUI;
import de.amr.pacmanfx.ui.config.MazeConfig3D;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.d3.Globals3D;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.KeyboardInfo;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.ui.subviews.editor.EditorView;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.FlashMessageManager;
import de.amr.pacmanfx.ui.view.GameViewImplementation;
import de.amr.pacmanfx.uilib.GameClockFX;
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
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class GameImplementation implements Game {

    private static File highScoreFile(String gameVariantName) {
        requireNonNull(gameVariantName);
        final String fileName = "highscore-%s.xml".formatted(gameVariantName).toLowerCase();
        return new File(Globals.USER_HOME_DIR, fileName);
    }

    private final PacManGamesMachine machine;

    private final Map<String, GameVariant> gameVariantsMap = new HashMap<>();

    private final StringProperty gameVariantName = new SimpleStringProperty();

    private final PreferencesManager prefs;

    private final DirectoryWatchdog watchdog;

    private final GameUI ui;

    private final GameViewImplementation view;

    private final GameClock gameClock;

    private final CoinMechanism coinMechanism;

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private CollisionStrategy collisionStrategy = CollisionStrategy.SAME_TILE;

    private GameContextImpl currentGameContext;

    public GameImplementation(PacManGamesMachine machine, GameViewImplementation view) {
        this.machine = requireNonNull(machine);
        this.view = requireNonNull(view);
        this.gameClock = new GameClockFX();
        this.coinMechanism = new CoinMechanism();
        prefs = new PreferencesManager(getClass());
        watchdog = new DirectoryWatchdog(Globals.CUSTOM_MAP_DIR);

        ui = new GameUI(
            new FlashMessageManager(),
            new GameSceneManager(this),
            new SoundManager(),
            new SpriteAnimationManager(),
            () -> GlobalsUI.LOCALIZED_TEXTS,
            view,
            new SubViewManager()
        );

        createSubViews();

        view.setGame(this);

        gameVariantName.addListener((_, _, newVariantName) -> {
            GameVariant runtime = gameVariant(newVariantName);
            currentGameContext = new GameContextImpl(this, runtime);
            currentGameContext.model().hud().creditProperty().bind(coinMechanism.numCoinsProperty());
        });
    }

    private GameVariant createGameVariantImplementation(String variantName) {
        final Cartridge cartridge = machine.cartridgeForVariant(variantName);
        final var runtime = new GameVariant(
            cartridge.gameFlowFactory().get(),
            cartridge.gameModelFactory().get(),
            cartridge.gameRulesFactory().get(),
            cartridge.uiConfigFactory().get()
        );

        //TODO make configurable again if tests should be available
        final GameFlow flow = runtime.gameFlow();
        flow.addState(new LevelShortTestState());
        flow.addState(new LevelMediumTestState());
        flow.addState(new CutScenesTestState());

        runtime.gameModel().createHighScore(highScoreFile(variantName));

        return runtime;
    }

    private GameVariant currentVariantImpl() {
        return gameVariant(currentGameVariantName());
    }

    public CollisionStrategy collisionStrategy() {
        return collisionStrategy;
    }

    public boolean isCollisionDoubleChecked() {
        return collisionDoubleChecked.get();
    }

    @Override
    public StringProperty variantNameProperty() {
        return gameVariantName;
    }

    @Override
    public void selectGameVariant(String variantName) {
        requireNonNull(variantName);
        if (machine.isCartridgeForVariantRegistered(variantName)) {
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
    public GameVariant gameVariant(String variantName) {
        return gameVariantsMap.computeIfAbsent(variantName, this::createGameVariantImplementation);
    }

    @Override
    public GameContext currentGameContext() {
        return currentGameContext;
    }

    @Override
    public UIConfig currentUIConfig() {
        return currentVariantImpl().uiConfig();
    }

    @Override
    public Input input() {
        return Input.instance();
    }

    @Override
    public GameClock clock() {
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
    public void setCollisionDoubleChecked(boolean value) {
        collisionDoubleChecked.set(value);
    }

    @Override
    public void setCollisionStrategy(CollisionStrategy collisionStrategy) {
        this.collisionStrategy = collisionStrategy;
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return watchdog;
    }

    // Lifecycle

    @Override
    public void show(Stage stage) {
        view.stageProperty().set(stage);

        load3DAssets();
        initMainScene();
        initGameClock();
        initGameVariantAndRegisterChangeHandler();
        initProperties();

        ui.subViews().connect(this);
        ui.subViews().selectStartView();
        ui.subViews().startView().setSelectedIndex(0);

        view.statusIconBox().bind(currentGameContext().model());
        view.show();

        startServicesLater();
    }

    @Override
    public void startGame() {
        stopGame();
        currentGameContext().flow().setGameContext(currentGameContext);
        currentGameContext().flow().restartState(GameStateID.BOOT);
        Platform.runLater(clock()::start);
    }

    @Override
    public void stopGame() {
        currentGameContext.model().prepareNewGame();

        clock().stop();
        clock().setTargetFrameRate(de.amr.pacmanfx.core.Globals.NUM_TICKS_PER_SEC);

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
        ui.flashMessages().stopAnimationTimer();
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
        final var playView = new GamePlayView(this, GlobalsUI.DEFAULT_DASHBOARD_CONFIG);
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
        gameClock.setUpdateAction(() -> {
            currentGameContext.flow().makeStep();
            ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(gameClock.tickCount()));
        });
        gameClock.setPermanentAction(() -> ui.subViews().currentView().render());
        gameClock.setErrorHandler(this::ka_tas_tro_phe);
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
    }

    private void initProperties() {
        final UIConfig currentConfig = currentUIConfig();

        final MazeConfig3D mazeConfig3D = currentConfig.worldConfig().maze();
        Globals3D.PROPERTY_3D_WALL_HEIGHT .set(mazeConfig3D.obstacleBaseHeight());
        Globals3D.PROPERTY_3D_WALL_OPACITY.set(mazeConfig3D.obstacleOpacity());

        ui.sounds().muteProperty().bind(GlobalsUI.PROPERTY_MUTED);

        view.statusIconBox().rootPane().visibleProperty().bind(Bindings.createBooleanBinding(
            () -> ui.subViews().isSelected(ui.subViews().gamePlayView())
                || ui.subViews().isSelected(ui.subViews().startView()),
            ui.subViews().selectedSubViewProperty()));

        view.mainScene().rootPane().backgroundProperty().bind(Bindings.createObjectBinding(
            () -> ui.gameScenes().currentGameSceneHasID(this, CommonSceneID.PLAY_SCENE_3D)
                ? GlobalsUI.WALLPAPERS[RandomNumberSupport.randomInt(0, GlobalsUI.WALLPAPERS.length)]
                : GlobalsUI.BACKGROUND_PAC_MAN_WALLPAPER,
            ui.subViews().selectedSubViewProperty(),
            ui.gameScenes().gameSceneProperty()
        ));
    }

    private void startServicesLater() {
        Platform.runLater(() -> {
            watchdog.startWatching();
            ui.flashMessages().startAnimationTimer();
            ui.sprites().startAnimationTimer();
        });
    }

    private void initGameVariantAndRegisterChangeHandler() {
        final var changeHandler = new GameVariantChangeHandler(this);
        gameVariantName.addListener(changeHandler);
        changeHandler.enterGameVariant(currentGameVariantName());
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