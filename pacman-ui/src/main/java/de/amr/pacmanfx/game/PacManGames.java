/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateChangeListener;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.GameStateChangeEvent;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.score.PropertyFileScore;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games collection.
 */
public final class PacManGames implements GameAppContext, GameLifecycle {

    /**
     * High score file for game variant "YYZ" is stored as "highscore-yyz.xml" inside user home directory.
     *
     * @param variantName name of the game variant e.g. "MS_PACMAN"
     * @return high score file name for this game variant
     */
    public static File highScoreFile(String variantName) {
        requireNonNull(variantName);
        final String fileName = "highscore-%s.xml".formatted(variantName.toLowerCase());
        return new File(GameConstants.USER_HOME_DIR, fileName);
    }

    private final GameBox machine;

    private final GameVariantManagerImpl variantManager;

    private final CommonGameActions actions;

    private GameUI ui;

    private GameContext gameContext;

    public PacManGames() {
        machine = GameBox.instance();
        variantManager = new GameVariantManagerImpl();
        actions = new CommonGameActions(this);
        configureClock();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.setAppContext(this);
    }

    public void selectGameVariantAndShow(GameVariantID variantID) {
        requireNonNull(variantID);
        variantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.views().selectStartPagesView();
        ui.views().startPagesView().rootPane().setSelectedIndex(0);
        ui.views().gamePlayView().dashboard().setAppContext(this);

        ui.window().show(this);

        Platform.runLater(this::startBackgroundServices);
    }

    private record StateChangeToGameEventMapper(GameContext gameContext) implements StateChangeListener<State<GameContext>> {
        @Override
            public void onStateChange(State<GameContext> oldState, State<GameContext> newState) {
                gameContext.eventManager().publishGameEvent(new GameStateChangeEvent(gameContext, oldState, newState));
        }
    }

    private StateChangeToGameEventMapper eventMapper;

    public void enterGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);

        //TODO rethink this
        final GameVariantConfig config = gameVariant.config();
        config.init(this);
        ui.viewModel().maze3D.init(config.worldSettings().maze());

        gameContext = new GameContextImpl(gameVariant, machine.coinMechanism());
        gameContext.eventManager().addGameEventSubscriber(ui);

        eventMapper = new StateChangeToGameEventMapper(gameContext);
        gameContext.flow().addStateChangeListener(eventMapper);
    }

    public void exitGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);
        gameVariant.config().dispose();
        ui.sounds().dispose();
        gameContext.eventManager().removeGameEventSubscriber(ui);
        gameContext.flow().removeStateChangeListener(eventMapper);
        gameContext = null;
    }

    public void setGameContext(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    // GameAppContext

    @Override
    public GameLifecycle lifecycle() {
        return this;
    }

    @Override
    public GameVariantManager variants() {
        return variantManager;
    }

    @Override
    public GameContext currentGameContext() {
        return gameContext;
    }

    @Override
    public CommonGameActions commonActions() {
        return actions;
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
    public Input input() {
        return machine.input();
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return machine.watchdog();
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    // GameLifecycle

    @Override
    public void startPlaying() {
        final GameFlowController gameFlow = gameContext.flow();
        gameFlow.setGameContext(gameContext);
        gameFlow.restartState(GameStateID.BOOT);
        clock().start();
        ui.views().selectGamePlayView();
    }

    @Override
    public void suspendPlaying() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.sounds().stopAll();
        clock().stop();
        clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
    }

    @Override
    public void terminate() {
        suspendPlaying();
        ui.terminate();
        machine.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");
        ui.window().mainScene().flashMessageManager().startAnimationTimer();
        ui.sprites().startAnimationTimer();

        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    private void configureClock() {
        clock().setUpdateAction(this::simulateAndUpdateCurrentGameScene);
        clock().setPermanentAction(this::renderCurrentView);
        clock().setErrorHandler(this::handleFatalError);
    }

    private void simulateAndUpdateCurrentGameScene() {
        final GameFlowController gameFlow = gameContext.flow(); //TODO store elsewhere?
        gameFlow.setGameContext(gameContext);
        gameContext.newFrame(clock().currentTick());
        gameFlow.update(gameContext);
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(gameContext.thisFrame()));
    }

    private void renderCurrentView() {
        Platform.runLater(() -> ui.views().assertCurrentView().render());
    }

    private void handleFatalError(Throwable reason) {
        Platform.runLater(() -> {
            suspendPlaying();
            final String errorMessage = ui.translations().translate("error.oh_no_my_program");
            ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
        });
    }

    private class GameVariantManagerImpl implements GameVariantManager, ChangeListener<String> {

        private final Map<String, GameVariant> variantsByName = new HashMap<>();

        private final StringProperty variantName = new SimpleStringProperty();

        public GameVariantManagerImpl() {
            variantName.addListener(this);
        }

        public StringProperty variantNameProperty() {
            return variantName;
        }

        @Override
        public void addVariantNameListener(ChangeListener<String> listener) {
            requireNonNull(listener);
            variantName.addListener(listener);
        }

        @Override
        public String currentVariantName() {
            return variantName.get();
        }

        @Override
        public GameVariant currentVariant() {
            return gameVariantByName(currentVariantName());
        }

        @Override
        public GameVariant gameVariantByName(String gameVariantName) {
            requireNonNull(gameVariantName);
            final boolean testStatesIncluded = ui().viewModel().testStatesIncludedProperty.get();
            return variantsByName.computeIfAbsent(gameVariantName, name -> createGameVariant(name, testStatesIncluded));
        }

        @Override
        public boolean isVariantRegistered(String variantName) {
            requireNonNull(variantName);
            return variantsByName.containsKey(variantName);
        }

        @Override
        public void selectVariant(String gameVariantName) {
            requireNonNull(gameVariantName);
            if (machine.containsCartridgeWithName(gameVariantName)) {
                this.variantName.set(gameVariantName);
            } else throw new IllegalArgumentException("Game with name '" + gameVariantName + "' not found");
        }

        private GameVariant createGameVariant(String variantName, boolean testStatesIncluded) {
            final Cartridge cartridge = machine.cartridgeByName(variantName);
            final var gameVariant = new GameVariant(cartridge);
            if (testStatesIncluded) {
                gameVariant.gameFlow().addTestStates();
            }
            gameVariant.gameModel().setHighScore(new PropertyFileScore(PacManGames.highScoreFile(variantName)));
            return gameVariant;
        }

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldVariantName, String newVariantName) {
            Logger.info("Game variant name: {} -> {}", oldVariantName, newVariantName);

            if (oldVariantName != null) {
                Logger.info("<<< Exit Game variant '{}'", oldVariantName);
                exitGameVariant(gameVariantByName(oldVariantName));
            }
            if (newVariantName != null) {
                Logger.info(">>> Enter game variant '{}'", newVariantName);
                enterGameVariant(gameVariantByName(newVariantName));
            }
        }
    }
}