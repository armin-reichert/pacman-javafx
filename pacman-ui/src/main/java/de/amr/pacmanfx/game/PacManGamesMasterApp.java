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
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.RenderManager;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
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
            eventManager.publishEvent(new GameStateChangeEvent(oldState, newState));
        }
    }

    private final GameBox gameBox;

    private final CommonGameActions actions;

    private final GameLoop gameLoop;

    private final RenderManager renderManager;

    private final GameSceneManager gameSceneManager;

    private GameUI ui;

    private GameContext game;

    private StateChangeEventMapper stateChangeEventMapper;

    private DefaultGameVariantManager gameVariantManager;

    public PacManGamesMasterApp(GameBox gameBox) {
        this.gameBox = requireNonNull(gameBox);
        renderManager = new RenderManager();
        gameSceneManager = new GameSceneManager();
        gameLoop = new GameLoop(this, gameBox.clock(), renderManager);
        actions = new CommonGameActions();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        createVariantManager(ui);

        ui.connectWithApp(this);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        gameVariantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.viewManager().onGameVariantChanged();

        ui.window().show(this);

        Platform.runLater(this::startBackgroundServices);
    }

    // GameAppContext

    @Override
    public RenderManager renderManager() {
        return renderManager;
    }

    @Override
    public GameVariantManager variantManager() {
        return gameVariantManager;
    }

    @Override
    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
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
                Logger.trace("Action '{}' executed successfully", gameAction.id());
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
    public void startGame() {
        final GameSession session = new GameSession(
            gameVariantManager.currentVariantName(),
            new GameCheats(),
            game.variantPlayConfig().initialLifeCount()
        );
        game.setSession(session);
        game.variantPlayConfig().gamePlay().startSession(game);

        ui.window().mainScene().connect(game.session());
        ui.viewManager().selectGamePlayView();

        gameLoop.start();
    }

    @Override
    public void suspendGame() {
        gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> {
            ui.viewManager().gamePlayView().disembedGameScene(gameScene);
            gameSceneManager.currentGameSceneProperty().set(null);
        });
        ui.soundManager().stopAll();
        gameLoop.stop();
    }

    public void terminate() {
        suspendGame();
        ui.terminate();
        gameBox.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private void createVariantManager(GameUI ui) {
        gameVariantManager = new DefaultGameVariantManager(gameBox, ui.viewModel());
        gameVariantManager.selectedVariantNameProperty().addListener((_, oldVariantName, newVariantName) -> {
            Logger.info("Game variant name: {} -> {}", oldVariantName, newVariantName);

            if (oldVariantName != null) {
                Logger.info("<<< Exit Game variant '{}'", oldVariantName);
                exitGameVariant(gameVariantManager.variantConfigByName(oldVariantName));
            }
            if (newVariantName != null) {
                Logger.info(">>> Enter game variant '{}'", newVariantName);
                enterGameVariant(gameVariantManager.variantConfigByName(newVariantName));
            }
        });
    }

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");
        ui.window().mainScene().flashMessageManager().startAnimationTimer();
        ui.spriteAnimTimer().start();

        //noinspection ResultOfMethodCallIgnored
        PacMan3DModel.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    //TODO This method is messy and needs a cleanup!
    private void enterGameVariant(GameVariantConfig gameVariantConfig) {
        requireNonNull(gameVariantConfig);

        final GameVariantUIConfig uiConfig = gameVariantConfig.uiConfig();
        uiConfig.load(this);

        // Update game scene manager
        gameSceneManager.setGameSceneConfig(uiConfig.gameSceneConfig());

        ui.viewModel().maze3DSettings().init(gameVariantConfig.uiConfig().worldSettings().maze());

        ui.spriteAnimTimer().attachAnimContainer(gameVariantConfig.spriteAnimContainer());

        //TODO do not start animation timer here
        ui.spriteAnimTimer().start();

        game = new GameContext(
            gameVariantConfig.playConfig(),
            new DefaultGameEventManager()
        );
        game.setSession(new GameSession(gameVariantManager.currentVariantName(), new GameCheats(), gameVariantConfig.playConfig().initialLifeCount()));

        stateChangeEventMapper = new StateChangeEventMapper(game.eventManager());

        // Just to be sure:
        game.eventManager().removeAllSubscribers();

        game.eventManager().addSubscriber(ui);
        game.eventManager().addSubscriber(new PacEatingEventHandler(game));
        game.eventManager().addSubscriber(new PacPowerEventHandler(game));

        gameVariantConfig.playConfig().gameFlow().addStateChangeListener(stateChangeEventMapper);
    }

    private void exitGameVariant(GameVariantConfig gameVariantConfig) {
        gameVariantConfig.playConfig().gameFlow().removeStateChangeListener(stateChangeEventMapper);

        gameVariantConfig.uiConfig().unload(this);

        gameVariantConfig.spriteAnimContainer().clear();
        ui.spriteAnimTimer().detachAnimationContainer();
        ui.soundManager().dispose();

        game.eventManager().removeAllSubscribers();
        game = null;
    }
}