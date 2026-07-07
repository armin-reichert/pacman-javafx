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
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.score.PropertyFileScore;
import de.amr.pacmanfx.ui.GameTranslationManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.model.GameViewModel;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.ui.window.GameWindow;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games collection.
 */
public final class PacManGamesCollection implements Game {

    private final PacManGamesMachine machine;

    private final Map<String, GameVariant> variantsByName = new HashMap<>();

    private final StringProperty variantName = new SimpleStringProperty();

    private final DirectoryWatchdog watchdog;

    private final GameExtensions extensions;

    private final CommonActions commonActions;

    private GameUI ui;

    private GameContextImpl context;

    public PacManGamesCollection(PacManGamesMachine machine) {
        this.machine = requireNonNull(machine);
        this.commonActions = new CommonActions(this);
        this.extensions = new GameExtensions(this);
        this.watchdog = new DirectoryWatchdog(GameConstants.CUSTOM_MAP_DIR);

        new GameClockController(this, machine.clock()).configure();

        variantName.addListener((_, oldName, newName) -> onGameVariantNameChanged(oldName, newName));
    }

    @Override
    public void createUI(GameUISettings settings, DashboardFactory dashboardFactory, Stage stage, int width, int height) {
        final TranslationManager translationManager = new GameTranslationManager();

        final GameViewModel viewModel = new GameViewModel();
        viewModel.init(settings);

        final GamePlayView playView = new GamePlayView();
        playView.populateDashboard(dashboardFactory, settings.dashboard(), translationManager);

        final GameViewManager viewManager = new GameViewManager();
        viewManager.registerView(GameViewID.START_PAGES, new StartPagesView());
        viewManager.registerView(GameViewID.GAMEPLAY, playView);
        viewManager.registerView(GameViewID.EDITOR, new EditorView());

        final SoundManager soundManager = new SoundManager();
        soundManager.muteProperty().bind(viewModel.mutedProperty);

        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing the singleton

        ui = new GameUI(
            new GameWindow(stage, width, height),
            viewManager,
            new GameSceneManager(),
            translationManager,
            soundManager,
            new SpriteAnimationManager(60),
            viewModel
        );
        ui.connect(this);
    }

    // Game interface

    @Override
    public StringProperty variantNameProperty() {
        return variantName;
    }

    @Override
    public void selectVariant(String variantName) {
        requireNonNull(variantName);
        if (machine.containsCartridgeWithName(variantName)) {
            this.variantName.set(variantName);
        }
        else throw new IllegalArgumentException("Game with name '" + variantName + "' not found");
    }

    @Override
    public GameVariant gameVariant() {
        return gameVariant(variantName());
    }

    @Override
    public String variantName() {
        return variantName.get();
    }

    @Override
    public GameVariant gameVariant(String variantName) {
        return variantsByName.computeIfAbsent(variantName, this::createGameVariant);
    }

    @Override
    public GameContext context() {
        return context;
    }

    @Override
    public GameVariantConfig config() {
        return gameVariant().config();
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
        selectVariant(variantID.name());

        ui.viewManager().selectStartPagesView();
        ui.viewManager().assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);

        // TODO: Dashboard expects current game view being already being set when connected
        ui.viewManager().gamePlayView().dashboard().connect(this);

        ui.window().show(this);

        startServicesLater();
    }

    @Override
    public void start() {
        context.flow().setGameContext(context);
        context.flow().restartState(GameStateID.BOOT);
        ui.viewManager().selectGamePlayView();
        Platform.runLater(clock()::start);
    }

    @Override
    public void stop() {
        ui.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            ui.gameSceneManager().removeFromPlayView(gameScene);
            ui.gameSceneManager().currentGameSceneProperty().set(null);
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
    public void ka_tas_tro_phe(Throwable reason) {
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
            exitGameVariant(gameVariant(oldVariantName));
        }
        if (newVariantName != null) {
            enterGameVariant(gameVariant(newVariantName));
        }
    }

    private void enterGameVariant(GameVariant gameVariant) {
        //TODO rethink
        gameVariant.config().init(this);
        ui.viewModel().maze3D.init(gameVariant.config().worldSettings().maze());

        // create new game context for current game variant
        context = new GameContextImpl(this, gameVariant);
        context.eventManager().addGameEventListener(ui);
        context.model().hudState().creditProperty().bind(coinMechanism().numCoinsProperty());
    }

    private void exitGameVariant(GameVariant gameVariant) {
        ui.sounds().dispose();
        gameVariant.config().dispose();

        context.eventManager().removeGameEventListener(ui);
        context = null;
    }

    private GameVariant createGameVariant(String variantName) {
        final Cartridge cartridge = machine.cartridgeByName(variantName);
        final var gameVariant = new GameVariant(cartridge);

        //TODO make configurable again if tests should be available
        final GameFlow flow = gameVariant.gameFlow();
        flow.addState(new LevelShortTestState());
        flow.addState(new LevelMediumTestState());
        flow.addState(new CutScenesTestState());

        gameVariant.gameModel().setHighScore(
            new PropertyFileScore(PacManGamesMachine.highScoreFile(variantName)));

        return gameVariant;
    }


    private void startServicesLater() {
        Platform.runLater(() -> {
            watchdog.startWatching();
            ui.window().mainScene().flashMessageManager().startAnimationTimer();
            ui.sprites().startAnimationTimer();
        });
    }
}