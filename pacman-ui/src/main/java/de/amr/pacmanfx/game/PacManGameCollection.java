/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateChangeListener;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.GameStateChangeEvent;
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
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games collection.
 */
public final class PacManGameCollection implements GameAppContext, GameLifecycle {

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

    private final GameSimulation simulation = new GameSimulation(this);

    private final GameVariantManagerImpl variantManager;

    private final StateChangeEventConverter changeEventConverter;

    private final CommonGameActions actions;

    private GameUI ui;

    private GameContext gameContext;

    public PacManGameCollection() {
        machine = GameBox.instance();
        variantManager = new GameVariantManagerImpl();
        changeEventConverter = new StateChangeEventConverter();
        actions = new CommonGameActions(this);
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.setAppContext(this);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        variantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.views().selectStartPagesView();
        ui.views().startPagesView().rootPane().setSelectedIndex(0);
        ui.views().gamePlayView().dashboard().setAppContext(this);

        ui.window().show(this);

        Platform.runLater(this::startBackgroundServices);
    }

    public void enterGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);

        //TODO rethink this
        final GameVariantConfig config = gameVariant.config();
        config.init(this);
        ui.viewModel().maze3D.init(config.worldSettings().maze());

        gameContext = new GameContextImpl(gameVariant, machine.coinMechanism());
        gameContext.eventManager().addGameEventSubscriber(ui);

        gameContext.flow().addStateChangeListener(changeEventConverter);
    }

    public void exitGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);
        gameVariant.config().dispose();
        ui.sounds().dispose();
        gameContext.eventManager().removeGameEventSubscriber(ui);
        gameContext.flow().removeStateChangeListener(changeEventConverter);
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
        gameContext.flow().restartState(gameContext, GameStateID.BOOT);
        ui.views().selectGamePlayView();
        simulation.start();
    }

    @Override
    public void suspendPlaying() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.sounds().stopAll();
        simulation.stop();
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

    /**
     * A state change event from the current game flow state machine is converted
     * into a game event and published such that UI components (views, game scenes) can handle them.
     */
    private class StateChangeEventConverter implements StateChangeListener<GameContext> {

        @Override
        public void onStateChange(State<GameContext> oldState, State<GameContext> newState) {
            gameContext.eventManager().publishGameEvent(new GameStateChangeEvent(oldState, newState));
        }
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

        private GameVariant createGameVariant(String variantName, boolean testStatesIncluded) {
            final Cartridge cartridge = machine.cartridgeByName(variantName);
            final var gameVariant = new GameVariant(cartridge);
            if (testStatesIncluded) {
                gameVariant.gameFlow().addTestStates();
            }
            gameVariant.gameModel().setHighScore(new PropertyFileScore(PacManGameCollection.highScoreFile(variantName)));
            return gameVariant;
        }

    }
}