/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationTimer;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.action.GameAction;
import de.amr.pacmanfx.engine.config.DefaultGameVariantManager;
import de.amr.pacmanfx.engine.config.GameVariantManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.assets.CommonTranslationManager;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games "engine".
 */
public final class PacManGamesEngine implements EngineLifecycle, GameEngineContext {

    private final RenderManager renderManager;

    private final SoundManager soundManager;

    private final TranslationManager translationManager;

    private final GameSceneManager gameSceneManager;

    private final SpriteAnimationTimer spriteAnimationTimer;

    private GameUI ui;

    private GameContext currentGame;

    private StateChangeEventMapper stateChangeEventMapper;

    private DefaultGameVariantManager gameVariantManager;

    public PacManGamesEngine() {
        renderManager = new RenderManager();
        gameSceneManager = new GameSceneManager();
        soundManager = new SoundManager();
        spriteAnimationTimer = new SpriteAnimationTimer();
        translationManager = new CommonTranslationManager();

        clock().setUpdateAction(this::simulate);
        clock().setPermanentAction(this::render);
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

    //TODO This method is messy and needs a cleanup!
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

    // Interface GameEngineContext

    @Override
    public GameClock clock() {
        return PlayStation.instance().clock();
    }

    @Override
    public GameContext currentGame() {
        return currentGame;
    }

    @Override
    public EngineLifecycle engineLife() {
        return this;
    }

    @Override
    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
    }

    @Override
    public GameVariantManager gameVariantManager() {
        return gameVariantManager;
    }

    @Override
    public Input input() {
        return PlayStation.instance().input();
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
    public TranslationManager translationManager() {
        return translationManager;
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return PlayStation.instance().watchdog();
    }

    // EngineLifecycle

    @Override
    public void startGame() {
        newGameSession();

        currentGame.playConfig().gamePlay().startSession(currentGame);

        ui.window().mainScene().connect(currentGame.session());
        ui.viewManager().selectGamePlayView();

        clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
        clock().start();
    }

    @Override
    public void suspendGame() {
        soundManager.stopAll();
        gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> ui.viewManager().onGameSuspended(gameScene));
        gameSceneManager.removeCurrentGameScene();
        clock().stop();
    }

    @Override
    public void newGameSession() {
        final GameSession session = new GameSession(
            gameVariantManager.currentVariantName(), new GameCheats(), currentGame.playConfig().initialLifeCount());
        currentGame.setSession(session);
    }

    @Override
    public void terminate() {
        suspendGame();
        spriteAnimationTimer.stop();
        ui.window().mainScene().flashMessageManager().stopAnimationTimer();
        PlayStation.instance().dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    @Override
    public void enterGameVariant(String variantName) {
        enterGameVariant(gameVariantManager.variantRuntimeByName(variantName));
    }

    @Override
    public void exitGameVariant(String variantName) {
        exitGameVariant(gameVariantManager.variantRuntimeByName(variantName));
    }

    // Private area, no trespassing!

    private void createGameVariantManager(GameUI ui) {
        gameVariantManager = new DefaultGameVariantManager(this, ui.viewModel());
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

    // --- Game loop

    private void simulate() {
        try {
            final GameContext game = currentGame();
            game.session().newFrameState(clock().currentTick());
            game.playConfig().systems().updateSystem().updateEntities(game);

            // This can change the current game state!
            game.playConfig().gameFlow().update(game);

            // IMPORTANT: The current game scene is up-to-date only at this point!
            gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(game));
        }
        catch (Exception x) {
            handleFatalError(x);
        }
    }

    private void render() {
        try {
            renderCurrentGameView(ui().viewManager());
        } catch (Exception x) {
            handleFatalError(x);
        }
    }

    private void renderCurrentGameView(GameViewManager viewManager) {
        if (viewManager.isSelected(GameViewID.GAMEPLAY)) {
            viewManager.gamePlayView().render();
            viewManager.gamePlayView().update();
        }
    }

    private void handleFatalError(Throwable reason) {
        suspendGame();
        final String errorMessage = translationManager.translate("error.oh_no_my_program");
        ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}