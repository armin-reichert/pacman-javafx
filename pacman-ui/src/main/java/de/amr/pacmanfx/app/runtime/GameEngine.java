/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.basics.Named;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.core.model.GameCheats;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.DefaultGameViewManager;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games "engine".
 */
public final class GameEngine implements EngineLifecycle, EngineUIContext {

    private final DefaultGameVariantManager gameVariantManager;

    private final GameViewManager viewManager;

    private final GameSceneManager gameSceneManager;

    private final RenderManager renderManager;

    private final SoundManager soundManager;

    private final SpriteAnimationSystem spriteAnimationSystem;

    private final TranslationManager translationManager;

    private GameUI ui;

    private GameContext currentGame;

    private StateChangeEventMapper stateChangeEventMapper;

    public GameEngine(
        RenderManager renderManager,
        GameSceneManager gameSceneManager,
        SoundManager soundManager,
        SpriteAnimationSystem spriteAnimationSystem,
        TranslationManager translationManager)
    {
        this.renderManager = requireNonNull(renderManager);
        this.gameSceneManager = requireNonNull(gameSceneManager);
        this.soundManager = requireNonNull(soundManager);
        this.spriteAnimationSystem = requireNonNull(spriteAnimationSystem);
        this.translationManager = requireNonNull(translationManager);

        viewManager = new DefaultGameViewManager();
        viewManager.currentViewIDProperty().addListener(
            (_, oldViewID, newViewID) -> onViewChanged(oldViewID, newViewID));

        gameVariantManager = new DefaultGameVariantManager();

        gameVariantManager.selectedVariantNameProperty().addListener(
            (_, oldVariantName, newVariantName) -> onGameVariantNameChanged(oldVariantName, newVariantName));

        clock().setUpdateAction(this::simulate);
        clock().setPermanentAction(this::render);
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        this.ui.setEngineContext(this);
        gameVariantManager.init(this);
        viewManager.init(this);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        gameVariantManager.selectVariant(variantID.name());

        viewManager.currentViewIDProperty().set(GameViewID.START_PAGES);
        viewManager.assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);

        viewManager.assertView(GameViewID.GAMEPLAY, GamePlayView.class).dashboard().init(this);

        updateStageIcon();
        ui.window().show();

        Platform.runLater(this::startBackgroundServices);
    }

    //TODO This method is messy and needs a cleanup!
    public void enterGameVariant(GameVariantRuntime runtime, GameVariantUIConfig uiConfig) {
        requireNonNull(runtime);

        final GameEventManager gameEventManager = new DefaultGameEventManager();

        // Create new game context
        currentGame = new GameContext(runtime.playConfig(), runtime.coinMechanism(), gameEventManager);

        newGameSession(currentGame);

        stateChangeEventMapper = new StateChangeEventMapper(gameEventManager);

        // Update game scene manager
        gameSceneManager.setGameSceneConfig(uiConfig.gameSceneConfig());

        // Just to be sure:
        gameEventManager.removeAllSubscribers();
        gameEventManager.addSubscriber(ui);
        gameEventManager.addSubscriber(new PacEatingEventHandler(currentGame));
        gameEventManager.addSubscriber(new PacPowerEventHandler(currentGame));

        runtime.playConfig().gameFlow().addStateChangeListener(stateChangeEventMapper);

        // Init UI for new runtime (game variant)
        uiConfig.load(this);

        spriteAnimationSystem.start();

        ui.viewModel().maze3DSettings().init(uiConfig.worldSettings().maze());
    }

    public void exitGameVariant(GameVariantRuntime variantRuntime, GameVariantUIConfig uiConfig) {
        requireNonNull(variantRuntime);

        variantRuntime.playConfig().gameFlow().removeStateChangeListener(stateChangeEventMapper);

        uiConfig.unload(this);

        spriteAnimationSystem.stop();

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
    public DefaultGameVariantManager gameVariantManager() {
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
        return viewManager;
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

            ui.window().mainScene().connect(currentGame.session());
            viewManager.currentViewIDProperty().set(GameViewID.GAMEPLAY);

            clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
            clock().start();
        }
        else {
            fatalError(new IllegalStateException("Game not be started, no game is currently selected"));
        }
    }

    @Override
    public void suspendGame() {
        if (currentGame != null) {
            soundManager.stopAll();
            gameSceneManager.optCurrentGameScene().ifPresent(GameScene::deactivate);
            gameSceneManager.removeCurrentGameScene();
            clock().stop();
        }
        else {
            fatalError(new IllegalStateException("Game not be suspended, no game is currently selected"));
        }
    }

    @Override
    public void newGameSession(GameContext game) {
        requireNonNull(game);
        final GameSession session = new GameSession(
            gameVariantManager.currentVariantName(),
            new GameCheats(),
            3
        );
        game.setSession(session);
    }

    @Override
    public void terminate() {
        suspendGame();
        spriteAnimationSystem.stop();
        ui.window().mainScene().flashMessageManager().stopAnimationTimer();
        PlayStation.instance().dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    @Override
    public void enterGameVariant(String variantName) {
        enterGameVariant(gameVariantManager.runtimeByName(variantName), gameVariantManager.currentRuntime().uiConfig());
    }

    @Override
    public void exitGameVariant(String variantName) {
        exitGameVariant(gameVariantManager.runtimeByName(variantName), gameVariantManager.currentRuntime().uiConfig());
    }

    // Private area, no trespassing!

    private void onGameVariantNameChanged(String oldVariantName, String newVariantName) {
        final GameVariantUIConfig uiConfig = gameVariantManager.currentRuntime().uiConfig();
        if (oldVariantName != null) {
            Logger.info("<<< Exit Game variant '{}'", oldVariantName);
            exitGameVariant(gameVariantManager.runtimeByName(oldVariantName), uiConfig);
        }
        if (newVariantName != null) {
            Logger.info(">>> Enter game variant '{}'", newVariantName);
            enterGameVariant(gameVariantManager.runtimeByName(newVariantName), uiConfig);
        }
        updateStageIcon();

    }

    private void onViewChanged(Named oldViewID, Named newViewID) {
        ui.clearMessage();

        if (oldViewID != null) {
            viewManager.assertView(oldViewID).onExit();
        }

        final GameView newView = viewManager.assertView(newViewID);
        newView.onEnter();

        ui.window().mainScene().replaceGameView(newView);
        ui.window().updateStageTitleBinding(viewManager, newViewID);
    }

    private void updateStageIcon() {
        final AssetMap assets = gameVariantManager.currentRuntime().uiConfig().assets();
        final Image icon = assets.image("app_icon");
        if (icon != null) {
            ui.window().stage().getIcons().setAll(icon);
        }
    }

    private void startBackgroundServices() {
        watchdog().startWatching();
        Logger.info("Custom map directory is getting watched!");
        ui.window().mainScene().flashMessageManager().startAnimationTimer();
        spriteAnimationSystem.start();

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
            if (viewManager.isSelected(GameViewID.GAMEPLAY)) {
                final GamePlayView playView = viewManager.assertView(GameViewID.GAMEPLAY, GamePlayView.class);
                playView.render();
                playView.update();
            }
        } catch (Exception x) {
            fatalError(x);
        }
    }

    private void fatalError(Throwable reason) {
        suspendGame();
        final String errorMessage = translationManager.translate("error.oh_no_my_program");
        ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}