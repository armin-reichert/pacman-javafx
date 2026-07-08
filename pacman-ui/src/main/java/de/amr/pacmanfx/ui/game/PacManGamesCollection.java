/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

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
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    public static class VariantManager implements GameVariantManager, ChangeListener<String> {

        private final PacManGamesCollection game;

        private final Map<String, GameVariant> variantsByName = new HashMap<>();

        private final StringProperty variantName = new SimpleStringProperty();

        public VariantManager(PacManGamesCollection game) {
            this.game = requireNonNull(game);
            variantName.addListener(this);
        }

        @Override
        public StringProperty variantNameProperty() {
            return variantName;
        }

        @Override
        public void addVariantNameListener(ChangeListener<String> listener) {
            requireNonNull(listener);
            variantName.addListener(listener);
        }

        @Override
        public void selectVariant(String gameVariantName) {
            requireNonNull(gameVariantName);
            if (machine().containsCartridgeWithName(gameVariantName)) {
                this.variantName.set(gameVariantName);
            }
            else throw new IllegalArgumentException("Game with name '" + gameVariantName + "' not found");
        }

        @Override
        public GameVariant selectedVariant() {
            return variant(selectedVariantName());
        }

        @Override
        public String selectedVariantName() {
            return variantName.get();
        }

        @Override
        public GameVariant variant(String gameVariantName) {
            return variantsByName.computeIfAbsent(gameVariantName, this::createGameVariant);
        }

        @Override
        public boolean isVariantRegistered(String gameVariantName) {
            requireNonNull(gameVariantName);
            return variantsByName.containsKey(gameVariantName);
        }

        private GameVariant createGameVariant(String variantName) {
            final Cartridge cartridge = machine().cartridgeByName(variantName);
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

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldVariantName, String newVariantName) {
            Logger.info("Game variant name change: {} -> {}", oldVariantName, newVariantName);

            if (oldVariantName != null) {
                exitGameVariant(variant(oldVariantName));
            }
            if (newVariantName != null) {
                enterGameVariant(variant(newVariantName));
            }
        }

        private void enterGameVariant(GameVariant gameVariant) {
            gameVariant.config().init(game);
            //TODO rethink
            game.ui().viewModel().maze3D.init(gameVariant.config().worldSettings().maze());

            final var gameVariantContext = new GameVariantContext(game, gameVariant);
            gameVariantContext.flow().setContext(gameVariantContext);
            gameVariantContext.eventManager().addGameEventSubscriber(game.ui());

            game.setGameVariantContext(gameVariantContext);
        }

        private void exitGameVariant(GameVariant gameVariant) {
            game.ui().sounds().dispose();
            gameVariant.config().dispose();

            game.context().eventManager().removeGameEventSubscriber(game.ui());
            game.setGameVariantContext(null);
        }

        private PacManGamesMachine machine() {
            return PacManGamesMachine.instance();
        }
    }

    private final GameVariantManager variantManager;

    private final GameExtensions extensions;

    private final CommonActions commonActions;

    private final GameViewModel viewModel;

    private final SoundManager soundManager;

    private final TranslationManager translationManager;

    private GameUI ui;

    private GameVariantContext gameVariantContext;

    public PacManGamesCollection() {
        this.variantManager = new VariantManager(this);
        this.commonActions = new CommonActions(this);
        this.extensions = new GameExtensions(this);
        this.viewModel = new GameViewModel();
        this.soundManager = new SoundManager();
        this.translationManager = new GameTranslationManager();

        soundManager.muteProperty().bind(viewModel.mutedProperty);
        configureClock();

        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    public void setGameVariantContext(GameVariantContext gameVariantContext) {
        this.gameVariantContext = gameVariantContext;
    }

    // Game interface

    @Override
    public GameUI createUI(GameUISettings settings, DashboardFactory dashboardFactory, Stage stage, int width, int height) {
        viewModel.init(settings);
        return new GameUI(
            new GameWindow(stage, width, height),
            createGameViewManager(settings, dashboardFactory, translationManager),
            new GameSceneManager(),
            translationManager,
            soundManager,
            new SpriteAnimationManager(60),
            viewModel
        );
    }

    @Override
    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.connect(this);
    }

    @Override
    public PacManGamesMachine machine() {
        return PacManGamesMachine.instance();
    }

    @Override
    public GameVariantManager variantManager() {
        return variantManager;
    }

    @Override
    public GameContext context() {
        return gameVariantContext;
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

    // GameLifecycle interface

    @Override
    public void showUI(GameVariantID variantID) {
        requireNonNull(variantID);

        variantManager.selectVariant(variantID.name());

        ui.viewManager().selectStartPagesView();
        ui.viewManager().assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);
        // TODO: Dashboard expects current game view being already being set when connected
        ui.viewManager().gamePlayView().dashboard().connect(this);
        ui.window().show(this);

        startBackgroundServices();
    }

    @Override
    public void start() {
        gameVariantContext.flow().restartState(GameStateID.BOOT);
        ui.viewManager().selectGamePlayView();
        Platform.runLater(machine().clock()::start);
    }

    @Override
    public void stop() {
        ui.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> {
            gameScene.deactivate();
            ui.gameSceneManager().removeFromPlayView(gameScene);
            ui.gameSceneManager().currentGameSceneProperty().set(null);
        });

        ui.sounds().stopAll();

        machine().clock().stop();
        machine().clock().setTargetFrameRate(GameClock.DEFAULT_TICKS_PER_SECOND);

        Logger.info("Game STOPPED!");
    }

    @Override
    public void terminate() {
        stop();
        ui.terminate();
        machine().dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private GameViewManager createGameViewManager(
        GameUISettings settings,
        DashboardFactory dashboardFactory,
        TranslationManager translationManager)
    {
        final GamePlayView playView = new GamePlayView();
        playView.populateDashboard(dashboardFactory, settings.dashboard(), translationManager);

        final GameViewManager viewManager = new GameViewManager();
        viewManager.registerView(GameViewID.START_PAGES, new StartPagesView());
        viewManager.registerView(GameViewID.GAMEPLAY, playView);
        viewManager.registerView(GameViewID.EDITOR, new EditorView());

        return viewManager;
    }

    private void startBackgroundServices() {
        Platform.runLater(() -> {
            if (
                variantManager.isVariantRegistered(GameVariantID.ARCADE_PACMAN_XXL.name()) ||
                variantManager.isVariantRegistered(GameVariantID.ARCADE_MS_PACMAN_XXL.name())
            ) {
                machine().watchdog().startWatching();
                Logger.info("Custom map directory is getting watched!");
            }
            ui.window().mainScene().flashMessageManager().startAnimationTimer();
            ui.sprites().startAnimationTimer();
        });
    }

    private void configureClock() {
        machine().clock().setUpdateAction(this::simulateAndUpdateCurrentGameScene);
        machine().clock().setPermanentAction(this::renderCurrentView);
        machine().clock().setErrorHandler(this::handleFatalError);
    }

    private void simulateAndUpdateCurrentGameScene() {
        gameVariantContext.flow().makeStep();
        Platform.runLater(() -> ui.gameSceneManager().optCurrentGameScene()
            .ifPresent(gameScene -> gameScene.onTick(machine().clock().currentTick())));
    }

    private void renderCurrentView() {
        Platform.runLater(() -> ui.viewManager().assertCurrentView().render());
    }

    private void handleFatalError(Throwable reason) {
        ka_tas_tro_phe(reason);
    }

    /*
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
}