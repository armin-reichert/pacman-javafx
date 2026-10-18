/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.basics.Named;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.GameVariantManager;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games "engine".
 */
public final class GameEngine implements EngineLifecycle, EngineUIContext {

    private final GameVariantManager gameVariantManager;

    private final GameViewManager gameViewManager;

    private final GameSceneManager gameSceneManager;

    private final RenderManager renderManager;

    private final SoundManager soundManager;

    private final SpriteAnimationSystem spriteAnimationSystem;

    private final TranslationManager translationManager;

    private GameUI ui;

    private GameContext currentGame;

    private StateChangeEventMapper stateChangeEventMapper;

    public GameEngine(
        GameVariantManager gameVariantManager,
        GameSceneManager gameSceneManager,
        GameViewManager gameViewManager,
        RenderManager renderManager,
        SoundManager soundManager,
        SpriteAnimationSystem spriteAnimationSystem,
        TranslationManager translationManager)
    {
        this.gameVariantManager = requireNonNull(gameVariantManager);
        this.gameSceneManager = requireNonNull(gameSceneManager);
        this.gameViewManager = requireNonNull(gameViewManager);
        this.renderManager = requireNonNull(renderManager);
        this.soundManager = requireNonNull(soundManager);
        this.spriteAnimationSystem = requireNonNull(spriteAnimationSystem);
        this.translationManager = requireNonNull(translationManager);

        this.gameViewManager.currentViewIDProperty().addListener(
            (_, oldViewID, newViewID) -> onViewChanged(oldViewID, newViewID));

        this.gameVariantManager.selectedVariantNameProperty().addListener(
            (_, oldVariantName, newVariantName) -> onGameVariantNameChanged(oldVariantName, newVariantName));

        gameVariantManager.init(this);

        clock().setPausableAction(this::simulate);
        clock().setPermanentAction(this::render);
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.setEngine(this);
        //TODO Analyse why exactly view manager needs UI to initialized
        gameViewManager.init(this);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        gameVariantManager.selectVariant(variantID.name());

        gameViewManager.currentViewIDProperty().set(GameViewID.START_PAGES);
        gameViewManager.assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);

        gameViewManager.assertView(GameViewID.GAMEPLAY, GamePlayView.class).dashboard().init(this);

        if (ui != null) {
            ui.updateStageIcon(gameVariantManager.currentRuntime());
            ui.window().show();
        }

        Platform.runLater(this::startBackgroundServices);
    }

    // Interface GameEngineContext

    @Override
    public GameClock clock() {
        return PlayStation.instance().clock();
    }

    @Override
    public Optional<GameContext> optCurrentGame() {
        return Optional.ofNullable(currentGame);
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
    public SpriteAnimationSystem spriteAnimationSystem() {
        return spriteAnimationSystem;
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
    public GameViewManager viewManager() {
        return gameViewManager;
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return PlayStation.instance().watchdog();
    }

    // EngineLifecycle

    @Override
    public void startGame() {
        if (currentGame != null) {
            newGameSession(currentGame);
            currentGame.playConfig().gamePlay().startSession(currentGame);

            if (ui != null) {
                ui.mainScene().connect(currentGame.session());
            } else {
                Logger.error("No UI has been attached to engine!");
            }

            gameViewManager.currentViewIDProperty().set(GameViewID.GAMEPLAY);

            clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
            clock().start();
        } else {
            fatalError(new IllegalStateException("Game cannot be started, no game is currently selected"));
        }
    }

    @Override
    public void suspendGame() {
        if (currentGame != null) {
            soundManager.stopAll();
            gameSceneManager.optCurrentGameScene().ifPresent(GameScene::deactivate);
            gameSceneManager.removeCurrentGameScene();
            clock().stop();
        } else {
            fatalError(new IllegalStateException("Game cannot be suspended, no game is currently selected"));
        }
    }

    @Override
    public void newGameSession(GameContext game) {
        requireNonNull(game);
        final GameSession session = new GameSession(
            gameVariantManager.currentVariantName(),
            new GameCheats(),
            3 //TODO make initial life selection work again
        );
        game.setSession(session);
    }

    @Override
    public void terminate() {
        if (ui != null) {
            ui.stopShortMessageSystem();
        }
        suspendGame();
        spriteAnimationSystem.stop();
        PlayStation.instance().dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    @Override
    public void enterGameVariant(String variantName) {
        enterGameVariant(gameVariantManager.runtimeByName(variantName));
    }

    @Override
    public void exitGameVariant(String variantName) {
        exitGameVariant(gameVariantManager.runtimeByName(variantName));
    }

    // Private area, no trespassing!

    private void onGameVariantNameChanged(String oldVariantName, String newVariantName) {
        if (oldVariantName != null) {
            Logger.info("<<< Exit Game variant '{}'", oldVariantName);
            final GameVariantRuntime oldRuntime = gameVariantManager.runtimeByName(oldVariantName);
            exitGameVariant(oldRuntime);
        }
        if (newVariantName != null) {
            Logger.info(">>> Enter game variant '{}'", newVariantName);
            final GameVariantRuntime newRuntime = gameVariantManager.runtimeByName(newVariantName);
            enterGameVariant(newRuntime);
        }
    }

    //TODO This method is messy and needs a cleanup!
    private void enterGameVariant(GameVariantRuntime runtime) {

        final GameEventManager gameEventManager = new DefaultGameEventManager();

        currentGame = new GameContext(runtime.playConfig(), runtime.coinMechanism(), gameEventManager);
        newGameSession(currentGame);
        stateChangeEventMapper = new StateChangeEventMapper(gameEventManager);
        runtime.playConfig().gameFlow().addStateChangeListener(stateChangeEventMapper);
        spriteAnimationSystem.start();

        gameEventManager.addSubscriber(ui);
        gameEventManager.addSubscriber(new PacEatingEventHandler(currentGame));
        gameEventManager.addSubscriber(new PacPowerEventHandler(currentGame));

        if (ui != null) {
            ui.onEnterGameVariant(runtime);
        }
    }

    private void exitGameVariant(GameVariantRuntime runtime) {
        runtime.playConfig().gameFlow().removeStateChangeListener(stateChangeEventMapper);
        spriteAnimationSystem.stop();
        soundManager.dispose();
        currentGame.eventManager().removeAllSubscribers();
        currentGame = null;

        if (ui != null) {
            ui.onExitGameVariant(runtime);
        }
    }

    private void onViewChanged(Named oldViewID, Named newViewID) {
        if (oldViewID != null) {
            gameViewManager.assertView(oldViewID).onExit();
        }
        final GameView newView = gameViewManager.assertView(newViewID);
        newView.onEnter();

        if (ui != null) {
            ui.clearShortMessage();
            ui.mainScene().replaceGameView(newView);
            ui.window().updateStageTitleBinding(gameViewManager, newViewID);
        }
    }

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");

        spriteAnimationSystem.start();
        if (ui != null) {
            ui.startShortMessageSystem();
        }

        //noinspection ResultOfMethodCallIgnored
        PacManMeshes3D.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    // --- Game loop

    private void simulate() {
        if (currentGame != null) {
            try {
                currentGame.session().newFrameState(clock().currentTick());
                currentGame.playConfig().systems().updateSystem().updateEntities(currentGame);

                // This can change the current game state!
                currentGame.playConfig().gameFlow().update(currentGame);

                // IMPORTANT: The current game scene is up-to-date only at this point!
                gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(currentGame));
            }
            catch (Exception x) {
                fatalError(x);
            }
        }
    }

    private void render() {
        try {
            if (gameViewManager.isSelected(GameViewID.GAMEPLAY)) {
                gameViewManager.assertView(GameViewID.GAMEPLAY, GamePlayView.class).render();
            }
        } catch (Exception x) {
            fatalError(x);
        }
    }

    private void fatalError(Throwable reason) {
        suspendGame();
        final String errorMessage = translationManager.translate("error.oh_no_my_program");
        ui.showShortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}