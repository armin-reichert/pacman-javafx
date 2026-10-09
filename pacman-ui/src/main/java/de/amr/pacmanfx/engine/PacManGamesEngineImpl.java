/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.assets.CommonTranslationManager;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationTimer;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.rendering.RenderManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games "engine".
 */
public final class PacManGamesEngineImpl implements PacManGamesEngine {

    private final GameBox gameBox;

    private final GameLoop gameLoop;

    private final RenderManager renderManager;

    private final SoundManager soundManager;

    private final TranslationManager translationManager;

    private final GameSceneManager gameSceneManager;

    private final SpriteAnimationTimer spriteAnimationTimer;

    private GameUI ui;

    private GameContext currentGame;

    private StateChangeEventMapper stateChangeEventMapper;

    private DefaultGameVariantManager gameVariantManager;

    public PacManGamesEngineImpl() {
        gameBox = new GameBox();
        renderManager = new RenderManager();
        gameSceneManager = new GameSceneManager();
        soundManager = new SoundManager();
        spriteAnimationTimer = new SpriteAnimationTimer();
        translationManager = new CommonTranslationManager();
        gameLoop = new GameLoop(gameBox.clock(), this);
        gameLoop.setErrorHandler(this::handleFatalError);
    }

    private void handleFatalError(Throwable reason) {
        suspendGame();
        final String errorMessage = translationManager.translate("error.oh_no_my_program");
        ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }

    public void setUI(GameUI ui, DashboardFactory dashboardFactory) {
        this.ui = requireNonNull(ui);
        createGameVariantManager(ui);
        ui.connectEngine(this, dashboardFactory);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        gameVariantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.viewManager().onGameVariantChanged();

        ui.window().show(this);

        Platform.runLater(this::startBackgroundServices);
    }

    // PacManGamesEngine interface

    @Override
    public GameBox gameBox() {
        return gameBox;
    }

    @Override
    public void newGameSession() {
        final GameSession session = new GameSession(
            gameVariantManager.currentVariantName(), new GameCheats(), currentGame.playConfig().initialLifeCount());
        currentGame.setSession(session);
    }

    @Override
    public RenderManager renderManager() {
        return renderManager;
    }

    @Override
    public SoundManager soundManager() {
        return soundManager;
    }

    @Override
    public SpriteAnimationTimer spriteAnimationTimer() {
        return spriteAnimationTimer;
    }

    @Override
    public TranslationManager translationManager() {
        return translationManager;
    }

    @Override
    public GameVariantManager gameVariantManager() {
        return gameVariantManager;
    }

    @Override
    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
    }

    @Override
    public GameContext currentGame() {
        return currentGame;
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

    //TODO This method is messy and needs a cleanup!
    @Override
    public void enterGameVariant(GameVariantRuntime runtime) {
        requireNonNull(runtime);

        // Create new game context
        currentGame = new GameContext(runtime.playConfig(), runtime.coinMechanism(), new DefaultGameEventManager());

        newGameSession();

        stateChangeEventMapper = new StateChangeEventMapper(currentGame.eventManager());

        // Update game scene manager
        gameSceneManager.setGameSceneConfig(runtime.uiConfig().gameSceneConfig());

        // Just to be sure:
        currentGame.eventManager().removeAllSubscribers();
        currentGame.eventManager().addSubscriber(ui);
        currentGame.eventManager().addSubscriber(new PacEatingEventHandler(currentGame));
        currentGame.eventManager().addSubscriber(new PacPowerEventHandler(currentGame));

        runtime.playConfig().gameFlow().addStateChangeListener(stateChangeEventMapper);

        // Init UI for new runtime (game variant)
        runtime.uiConfig().load(this);

        spriteAnimationTimer.attachAnimContainer(runtime.spriteAnimContainer());
        spriteAnimationTimer.start();

        ui.viewModel().maze3DSettings().init(runtime.uiConfig().worldSettings().maze());
    }

    @Override
    public void exitGameVariant(GameVariantRuntime variantRuntime) {
        requireNonNull(variantRuntime);

        variantRuntime.playConfig().gameFlow().removeStateChangeListener(stateChangeEventMapper);
        variantRuntime.uiConfig().unload(this);
        variantRuntime.spriteAnimContainer().clear();

        spriteAnimationTimer.detachAnimationContainer();
        soundManager.dispose();

        currentGame.eventManager().removeAllSubscribers();
        currentGame = null;
    }

    // GameLifecycle

    @Override
    public void startGame() {
        newGameSession();

        currentGame.playConfig().gamePlay().startSession(currentGame);

        ui.window().mainScene().connect(currentGame.session());
        ui.viewManager().selectGamePlayView();

        gameLoop.start();
    }

    @Override
    public void suspendGame() {
        soundManager.stopAll();
        gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> ui.viewManager().onGameSuspended(gameScene));
        gameSceneManager.removeCurrentGameScene();
        gameLoop.stop();
    }

    public void terminate() {
        suspendGame();
        spriteAnimationTimer.stop();
        ui.window().mainScene().flashMessageManager().stopAnimationTimer();
        gameBox.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private void createGameVariantManager(GameUI ui) {
        gameVariantManager = new DefaultGameVariantManager(gameBox, this, ui.viewModel());
        gameVariantManager.selectedVariantNameProperty().addListener((_, oldVariantName, newVariantName) -> {
            Logger.info("Game variant name: {} -> {}", oldVariantName, newVariantName);

            if (oldVariantName != null) {
                Logger.info("<<< Exit Game variant '{}'", oldVariantName);
                exitGameVariant(gameVariantManager.variantRuntimeByName(oldVariantName));
            }
            if (newVariantName != null) {
                Logger.info(">>> Enter game variant '{}'", newVariantName);
                enterGameVariant(gameVariantManager.variantRuntimeByName(newVariantName));
            }
        });
    }

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");
        ui.window().mainScene().flashMessageManager().startAnimationTimer();
        spriteAnimationTimer.start();

        //noinspection ResultOfMethodCallIgnored
        PacManMeshes3D.instance(); // loads 3D assets as side effect of accessing the singleton
    }

}