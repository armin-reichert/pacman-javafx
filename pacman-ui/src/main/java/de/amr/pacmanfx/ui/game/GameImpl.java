/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.config.SettingsLoader;
import de.amr.pacmanfx.ui.config.ui.UISettings;
import de.amr.pacmanfx.ui.config.world.Maze3DSettings;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.viewmodel.UISettingsVM;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.ui.window.GameTranslationManager;
import de.amr.pacmanfx.ui.window.GameWindowImpl;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class GameImpl implements Game {

    public static final String UI_SETTINGS_JSON = "/de/amr/pacmanfx/ui/ui.json";

    private final PacManGamesMachine machine;

    private final Map<String, GameVariant> gameVariantsMap = new HashMap<>();

    private final StringProperty gameVariantName = new SimpleStringProperty();

    private final PreferencesManager prefs;

    private final DirectoryWatchdog watchdog;

    private final GameExtensions extensions;

    private final CommonActions commonActions;

    private GameUI ui;

    private final BooleanProperty collisionDoubleChecked = new SimpleBooleanProperty(true);

    private CollisionStrategy collisionStrategy = CollisionStrategy.SAME_TILE;

    private GameContextImpl currentGameContext;

    private final GlobalGameEventHandler globalGameEventHandler = new GlobalGameEventHandler(this);

    public GameImpl(PacManGamesMachine machine) {
        this.machine = requireNonNull(machine);

        this.commonActions = new CommonActions(this);
        this.extensions = new GameExtensions();
        this.prefs = new PreferencesManager(getClass());
        this.watchdog = new DirectoryWatchdog(GameConstants.CUSTOM_MAP_DIR);

        gameVariantName.addListener((_, oldName, newName) -> onGameVariantNameChanged(oldName, newName));
    }

    @Override
    public void createUI(Stage stage, int width, int height) {

        UISettings initialSettings = new SettingsLoader().loadJSON(UI_SETTINGS_JSON, UISettings.class);
        final UISettingsVM uiSettingsViewModel = new UISettingsVM(initialSettings);

        final GameViewManager views = new GameViewManager();
        views.registerView(GameViewID.START_PAGES, new StartPagesView());
        views.registerView(GameViewID.GAMEPLAY, new GamePlayView());
        views.registerView(GameViewID.EDITOR, new EditorView());

        final SoundManager sounds = new SoundManager();
        sounds.muteProperty().bind(uiSettingsViewModel.mutedProperty);

        ui = new GameUI(
            new GameWindowImpl(stage, width, height),
            views,
            new GameSceneManager(),
            new GameTranslationManager(),
            sounds,
            new SpriteAnimationManager(60),
            uiSettingsViewModel
        );

        ui.connect(this);

        load3DAssets();
        configureGameClock();
    }

    public CollisionStrategy collisionStrategy() {
        return collisionStrategy;
    }

    public boolean isCollisionDoubleChecked() {
        return collisionDoubleChecked.get();
    }

    // Game interface

    @Override
    public StringProperty gameVariantNameProperty() {
        return gameVariantName;
    }

    @Override
    public void selectGameVariant(String variantName) {
        requireNonNull(variantName);
        if (machine.containsCartridgeWithName(variantName)) {
            gameVariantName.set(variantName);
        }
        else throw new IllegalArgumentException("Game with name '" + variantName + "' not found");
    }

    @Override
    public GameVariant currentGameVariant() {
        return gameVariant(currentGameVariantName());
    }

    @Override
    public String currentGameVariantName() {
        return gameVariantName.get();
    }

    @Override
    public GameVariant gameVariant(String variantName) {
        return gameVariantsMap.computeIfAbsent(variantName, this::createGameVariant);
    }

    @Override
    public GameContext currentGameContext() {
        return currentGameContext;
    }

    @Override
    public GameVariantConfig currentUIConfig() {
        return currentGameVariant().uiConfig();
    }

    @Override
    public Input input() {
        return Input.instance();
    }

    @Override
    public GameClock clock() {
        return machine.clock();
    }

    @Override
    public CoinMechanism coinMechanism() {
        return machine.coinMechanism();
    }

    @Override
    public CommonActions actions() {
        return commonActions;
    }

    @Override
    public GameExtensions extensions() {
        return extensions;
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

    // GameLifecycle interface

    @Override
    public void showUI(GameVariantID variantID) {
        selectGameVariant(variantID.name());

        ui.views().selectStartPagesView();
        ui.views().assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);

        // TODO: Dashboard expects current game view being already being set when connected
        ui.views().gamePlayView().dashboard().connect(this);

        ui.window().show(this);

        startServicesLater();
    }

    @Override
    public void start() {
        currentGameContext.flow().setGameContext(currentGameContext);
        currentGameContext.flow().restartState(GameStateID.BOOT);
        ui.views().selectGamePlayView();
        Platform.runLater(clock()::start);
    }

    @Override
    public void stop() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            ui.gameScenes().removeFromPlayView(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });

        ui.sounds().stopAll();

        clock().stop();
        clock().setTargetFrameRate(GameClock.DEFAULT_TICKS_PER_SECOND);

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        stop();
        ui.terminate();
        watchdog.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    /**
     * @see <a href="https://de.wikipedia.org/wiki/Steel_Buddies_%E2%80%93_Stahlharte_Gesch%C3%A4fte">Katastrophe!</a>
     */
    private void ka_tas_tro_phe(Throwable reason) {
        Platform.runLater(() -> {
            final String errorMessage = ui.translations().translate("error.oh_no_my_program");
            ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            stop();
            Logger.error("*** SOMETHING VERY BAD HAPPENED:");
            Logger.error(reason);
        });
    }

    private void onGameVariantNameChanged(String oldVariantName, String newVariantName) {
        if (oldVariantName != null) {
            exitGameVariant(oldVariantName);
        }
        if (newVariantName != null) {
            enterGameVariant(newVariantName);
        }
    }

    private void exitGameVariant(String variantName) {
        ui.sounds().dispose();
        gameVariant(variantName).uiConfig().dispose();
        currentGameContext.flow().removeGameEventListener(globalGameEventHandler);
    }

    private void enterGameVariant(String variantName) {
        final GameVariant gameVariant = gameVariant(variantName);
        gameVariant.uiConfig().init(this);
        updateSettings3D(gameVariant.uiConfig());

        // create new game context for current game variant
        currentGameContext = new GameContextImpl(this, gameVariant);
        currentGameContext.model().hud().creditProperty().bind(coinMechanism().numCoinsProperty());
        currentGameContext.flow().addGameEventListener(globalGameEventHandler);
    }

    private GameVariant createGameVariant(String variantName) {
        final Cartridge cartridge = machine.cartridgeByName(variantName);
        final var gameVariant = new GameVariant(cartridge);

        //TODO make configurable again if tests should be available
        final GameFlow flow = gameVariant.gameFlow();
        flow.addState(new LevelShortTestState());
        flow.addState(new LevelMediumTestState());
        flow.addState(new CutScenesTestState());

        gameVariant.gameModel().setHighScoreFile(PacManGamesMachine.highScoreFile(variantName));

        return gameVariant;
    }

    private static void load3DAssets() {
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing singleton
    }

    private void configureGameClock() {
        clock().setUpdateAction(() -> {
            currentGameContext.flow().makeStep();
            ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(clock().currentTick()));
        });
        clock().setPermanentAction(() -> ui.views().assertCurrentView().render());
        clock().setErrorHandler(this::ka_tas_tro_phe);
    }

    private void updateSettings3D(GameVariantConfig uiConfig) {
        final Maze3DSettings mazeConfig3D = uiConfig.worldConfig().maze();
        ui.settings().d3.mazeWallHeightProperty.set(mazeConfig3D.obstacleBaseHeight());
        ui.settings().d3.mazeWallOpacityProperty.set(mazeConfig3D.obstacleOpacity());
        Logger.info("Update maze 3D settings for UI config {}", uiConfig);
        Logger.info("Maze 3D settings: {}", mazeConfig3D);
    }

    private void startServicesLater() {
        Platform.runLater(() -> {
            watchdog.startWatching();
            ui.window().mainScene().flashMessageManager().startAnimationTimer();
            ui.sprites().startAnimationTimer();
        });
    }
}