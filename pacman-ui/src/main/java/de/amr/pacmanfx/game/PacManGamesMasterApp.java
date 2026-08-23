/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateChangeListener;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.gameplay.GameStateChangeEvent;
import de.amr.pacmanfx.core.gameplay.FoodEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.uilib.PacMan3DModel;
import javafx.application.Platform;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games master app.
 */
public final class PacManGamesMasterApp implements GameAppContext {

    /**
     * A state change event from the current game flow state machine is converted
     * into a game event and published such that UI components (views, game scenes) can handle them.
     */
    private record StateChangeEventMapper(GameEventManager eventManager) implements StateChangeListener<GameContext> {

        private StateChangeEventMapper(GameEventManager eventManager) {
            this.eventManager = requireNonNull(eventManager);
        }

        @Override
        public void onStateChange(State<GameContext> oldState, State<GameContext> newState) {
            eventManager.publishGameEvent(new GameStateChangeEvent(oldState, newState));
        }
    }

    private final GameBox gameBox;

    private final CommonGameActions actions;

    private final GameSimulation simulation;

    private GameUI ui;

    private GameContext game;

    private StateChangeEventMapper stateChangeEventMapper;

    private DefaultGameVariantManager gameVariantManager;

    public PacManGamesMasterApp(GameBox gameBox) {
        this.gameBox = requireNonNull(gameBox);
        simulation = new GameSimulation(this, gameBox.clock());
        actions = new CommonGameActions();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        createVariantManager(ui);

        ui.setApp(this);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        gameVariantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.views().selectStartPagesView();
        ui.views().startPagesView().rootPane().setSelectedIndex(0);
        ui.views().gamePlayView().dashboard().setAppContext(this);

        ui.window().show(this);

        Platform.runLater(this::startBackgroundServices);
    }

    // GameAppContext

    @Override
    public GameVariantManager gameVariants() {
        return gameVariantManager;
    }

    @Override
    public GameContext game() {
        return game;
    }

    @Override
    public CommonGameActions commonActions() {
        return actions;
    }

    @Override
    public GameClock clock() {
        return gameBox.clock();
    }

    @Override
    public Input input() {
        return gameBox.input();
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return gameBox.watchdog();
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public boolean runAction(GameAction gameAction) {
        boolean success = false;
        if (gameAction.isEnabled(this)) {
            try {
                gameAction.execute(this);
                success = true;
                Logger.info("Action '{}' executed successfully", gameAction.id());
            }
            catch (Exception x) {
                Logger.error(x, "An error occurred executing action '{}'", gameAction.id());
            }
        } else {
            Logger.warn("Action {}' not executed (disabled)", gameAction.id());
        }

        //TODO This is dubious!
        // Clear the input that triggered this action
        input().keyboard().clearState();

        return success;
    }

    // GameLifecycle

    public void startGame() {
        createSession();
        ui.window().mainScene().connect(game.session());
        ui.views().selectGamePlayView();
        game.variant().gamePlay().startSession(game);
        simulation.start();
    }

    public void suspendGame() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.soundManager().stopAll();
        simulation.stop();
    }

    public void terminate() {
        suspendGame();
        ui.terminate();
        gameBox.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private void createVariantManager(GameUI ui) {
        gameVariantManager = new DefaultGameVariantManager(
            gameBox.cartridgeRepository(),
            ui.viewModel()
        );
        gameVariantManager.selectedVariantNameProperty().addListener((_, oldVariantName, newVariantName) -> {
            Logger.info("Game variant name: {} -> {}", oldVariantName, newVariantName);

            if (oldVariantName != null) {
                Logger.info("<<< Exit Game variant '{}'", oldVariantName);
                exitGameVariant(gameVariantManager.gameVariantByName(oldVariantName));
            }
            if (newVariantName != null) {
                Logger.info(">>> Enter game variant '{}'", newVariantName);
                enterGameVariant(gameVariantManager.gameVariantByName(newVariantName));
            }
        });
    }

    private void createSession() {
        game.setSession(new GameSession(gameVariantManager.currentVariantName(), new GameCheats()));
    }

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");
        ui.window().mainScene().flashMessageManager().startAnimationTimer();
        ui.spriteAnimTimer().start();

        //noinspection ResultOfMethodCallIgnored
        PacMan3DModel.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    private void enterGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);

        //TODO rethink this
        final GameVariantUIConfig uiConfig = gameVariant.uiConfig();
        uiConfig.init();
        uiConfig.loadSounds(ui.soundManager());
        uiConfig.connectApp(this);

        ui.viewModel().maze3D.init(gameVariant.uiConfig().worldSettings().maze());

        ui.spriteAnimTimer().attachAnimContainer(gameVariant.spriteAnimContainer());
        //TODO do not start here
        ui.spriteAnimTimer().start();

        game = new GameContext(
            gameBox.coinMechanism(),
            gameVariant.config(),
            new DefaultGameEventManager()
        );
        createSession();

        stateChangeEventMapper = new StateChangeEventMapper(game.eventManager());

        // Just to be sure:
        game.eventManager().clear();
        game.eventManager().addGameEventSubscriber(ui);
        game.eventManager().addGameEventSubscriber(new FoodEventHandler(game));
        game.eventManager().addGameEventSubscriber(new PacPowerEventHandler(game));

        gameVariant.config().gameFlow().addStateChangeListener(stateChangeEventMapper);
    }

    private void exitGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);

        gameVariant.config().gameFlow().removeStateChangeListener(stateChangeEventMapper);
        gameVariant.uiConfig().unloadSounds(ui.soundManager());
        gameVariant.uiConfig().dispose();

        gameVariant.spriteAnimContainer().clear();
        ui.spriteAnimTimer().detachAnimationContainer();
        ui.soundManager().dispose();

        game.eventManager().clear();
        game = null;
    }
}