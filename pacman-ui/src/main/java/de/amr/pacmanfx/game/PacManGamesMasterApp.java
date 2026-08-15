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
import de.amr.pacmanfx.core.event.gameplay.GameStateChangeEvent;
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
public final class PacManGamesMasterApp implements GameAppContext, GameLifecycle {

    private final GameBox gameBox;

    private final StateChangeEventConverter changeEventConverter;

    private final CommonGameActions actions;

    private GameUI ui;

    private GameContext game;

    private DefaultGameVariantManager gameVariantManager;

    public PacManGamesMasterApp(GameBox gameBox) {
        this.gameBox = requireNonNull(gameBox);
        changeEventConverter = new StateChangeEventConverter();
        actions = new CommonGameActions();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        createVariantManager(ui);

        ui.setApp(this);
    }

    private void createVariantManager(GameUI ui) {
        gameVariantManager = new DefaultGameVariantManager(
            gameBox.cartridgeRepository(),
            ui.viewModel()
        );
        gameVariantManager.variantNameProperty().addListener((_, oldVariantName, newVariantName) -> {
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

    public void enterGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);

        //TODO rethink this
        final GameVariantUIConfig uiConfig = gameVariant.uiConfig();
        uiConfig.initApp(this);
        uiConfig.loadSounds(ui.sounds());

        ui.viewModel().maze3D.init(gameVariant.uiConfig().worldSettings().maze());

        game = createGameContext(gameVariant);
        createSession(gameVariant);
        game.eventManager().addGameEventSubscriber(ui);
        gameVariant.config().gameFlow().addStateChangeListener(changeEventConverter);
    }

    private void createSession(GameVariant gameVariant) {
        final String variantName = gameVariantManager.currentVariantName();
        final var session = new GameSession(variantName, gameVariant.config().gameFlow(), new GameCheats());
        session.hud().creditProperty().bind(gameBox.coinMechanism().numCoinsProperty());
        game.setSession(session);
    }

    private GameContext createGameContext(GameVariant gameVariant) {
        return new GameContext(
            gameBox.coinMechanism(),
            gameVariant.config(),
            new DefaultGameEventManager()
        );
    }

    public void exitGameVariant(GameVariant gameVariant) {
        requireNonNull(gameVariant);

        gameVariant.config().gameFlow().removeStateChangeListener(changeEventConverter);
        gameVariant.uiConfig().unloadSounds(ui.sounds());
        gameVariant.uiConfig().dispose();

        ui.sounds().dispose();

        game.eventManager().removeGameEventSubscriber(ui);
        game = null;
    }

    public void setGame(GameContext game) {
        this.game = game;
    }

    // GameAppContext

    @Override
    public GameLifecycle lifecycle() {
        return this;
    }

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

    @Override
    public void startPlaying() {
        createSession(gameVariantManager.currentGameVariant());
        ui.window().mainScene().connect(game.session());
        ui.views().selectGamePlayView();
        game.variantConfig().gamePlay().onSessionStart(game);
        GameSimulation.start(this);
    }

    @Override
    public void suspendPlaying() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.sounds().stopAll();
        GameSimulation.stop(this);
    }

    @Override
    public void terminate() {
        suspendPlaying();
        ui.terminate();
        gameBox.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");
        ui.window().mainScene().flashMessageManager().startAnimationTimer();
        ui.sprites().startAnimationTimer();

        //noinspection ResultOfMethodCallIgnored
        PacMan3DModel.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    /**
     * A state change event from the current game flow state machine is converted
     * into a game event and published such that UI components (views, game scenes) can handle them.
     */
    private class StateChangeEventConverter implements StateChangeListener<GameContext> {

        @Override
        public void onStateChange(State<GameContext> oldState, State<GameContext> newState) {
            game.eventManager().publishGameEvent(new GameStateChangeEvent(oldState, newState));
        }
    }

}