/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.basics.Named;
import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.event.base.DefaultGameEventManager;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gameplay.PacEatingEventHandler;
import de.amr.pacmanfx.core.gameplay.PacPowerEventHandler;
import de.amr.pacmanfx.engine.DefaultGameClock;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.GameVariantManager;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.EngineContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.view3d.PacManMeshes3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games "engine".
 */
public final class GameEngine implements EngineLifecycle, EngineContext, TranslationManager {

    private static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle(
        "de.amr.pacmanfx.ui.localized_texts", GameEngine.class.getModule());

    private final GameClock clock;

    private final CoinMechanism coinMechanism;

    private final Input input;

    private final DirectoryWatchdog watchdog;

    private final GameVariantManager gameVariantManager;

    private final GameViewManager gameViewManager;

    private final GameSceneManager gameSceneManager;

    private final RenderManager renderManager;

    private final SoundManager soundManager;

    private final SpriteAnimationSystem spriteAnimationSystem;

    private final GameUI ui;

    private GameContext currentGame;

    private StateChangeEventMapper stateChangeEventMapper;

    public GameEngine(
        GameUI ui,
        GameVariantManager gameVariantManager,
        GameSceneManager gameSceneManager,
        GameViewManager gameViewManager,
        RenderManager renderManager,
        SoundManager soundManager,
        SpriteAnimationSystem spriteAnimationSystem)
    {
        this.ui = requireNonNull(ui);
        this.gameVariantManager = requireNonNull(gameVariantManager);
        this.gameSceneManager = requireNonNull(gameSceneManager);
        this.gameViewManager = requireNonNull(gameViewManager);
        this.renderManager = requireNonNull(renderManager);
        this.soundManager = requireNonNull(soundManager);
        this.spriteAnimationSystem = requireNonNull(spriteAnimationSystem);

        coinMechanism = new CoinMechanism(99);

        clock = new DefaultGameClock();
        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
        clock.setPausableAction(this::simulate);
        clock.setPermanentAction(this::render);

        input = new Input();

        watchdog = new DirectoryWatchdog(GameConstants.CUSTOM_MAP_DIR);

        gameViewManager.currentViewIDProperty().addListener(
            (_, oldViewID, newViewID) -> onViewChanged(oldViewID, newViewID));

        gameVariantManager.currentVariantIDProperty().addListener(
            (_, oldVariantName, newVariantName) -> onGameVariantNameChanged(oldVariantName, newVariantName));

        if (!validateUserDirs()) {
            throw new IllegalStateException("User directory validation failed");
        }

        ui.setEngine(this);
        gameViewManager.init(this);
    }

    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        gameVariantManager.selectVariant(this, variantID.name());

        gameViewManager.currentViewIDProperty().set(GameViewID.START_PAGES);
        gameViewManager.assertView(GameViewID.START_PAGES, StartPagesView.class).rootPane().setSelectedIndex(0);

        gameViewManager.assertView(GameViewID.GAMEPLAY, GamePlayView.class).dashboard().init(this);

        ui.show(uiConfig(currentGame.runtime()));

        Platform.runLater(this::startBackgroundServices);
    }

    // Interface TranslationManager

    @Override
    public ResourceBundle textBundle() {
        return TEXT_BUNDLE;
    }

    @Override
    public String translate(String key, Object... args) {
        requireNonNull(key);
        if (textBundle().containsKey(key)) {
            return replaceEscapeSequences(MessageFormat.format(textBundle().getString(key), args));
        }
        Logger.error("Missing localized text for key {}", key);
        return "[" + key + "]";
    }

    private static String replaceEscapeSequences(String s) {
        return s.replace("\\n", "\n");
    }

    // Interface GameEngineContext

    @Override
    public GameClock clock() {
        return clock;
    }

    @Override
    public Optional<GameContext> optCurrentGame() {
        return Optional.ofNullable(currentGame);
    }

    @Override
    public EngineLifecycle lifeCycle() {
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
        return input;
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
        return this;
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
        return watchdog;
    }

    // EngineLifecycle

    @Override
    public void startSession() {
        ui.mainScene().setGameSession(currentGame.session());
        currentGame.runtime().gamePlay().startSession(currentGame);
        gameViewManager.currentViewIDProperty().set(GameViewID.GAMEPLAY);

        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
        clock.start();
    }

    @Override
    public void endSession() {
        soundManager.stopAll();
        gameSceneManager.optCurrentGameScene().ifPresent(GameScene::deactivate);
        gameSceneManager.removeCurrentGameScene();
        clock.stop();
    }

    @Override
    public void createNewSession() {
        final var session = new GameSession(
            gameVariantManager.currentVariantID(),
            gameVariantManager.currentRuntime().gameRules().initialLifeCount()
        );
        currentGame.setSession(session);
    }

    @Override
    public void terminate() {
        endSession();
        ui.stopShortMessageSystem();
        spriteAnimationSystem.stop();
        watchdog.dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    @Override
    public GameVariantUIConfig uiConfig(GameVariantRuntime runtime) {
        if (!(runtime instanceof DefaultGameVariantRuntime runtimeImpl)) {
            throw new IllegalArgumentException("Illegal game variant runtime type" + runtime.getClass().getSimpleName());
        }
        return runtimeImpl.uiConfig();
    }

    // Private area, no trespassing!

    private boolean validateUserDirs() {
        return dirExistsAndIsWritable(GameConstants.USER_HOME_DIR, "Game root directory")
            && dirExistsAndIsWritable(GameConstants.CUSTOM_MAP_DIR, "Custom maps directory");
    }

    private static boolean dirExistsAndIsWritable(File dir, String description) {
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.error("{} could not be created", description);
            return false;
        }
        if (!dir.canWrite()) {
            Logger.error("{} is not writable: {}", description, dir);
            return false;
        }
        Logger.info("{} exists and is writable: {}", description, dir);
        return true;
    }

    //TODO This code belongs elsewhere and needs review
    private void handleGameEvent(GameEvent gameEvent) {
        boolean forceGameSceneReload = false;
        switch (gameEvent) {
            case LevelCreatedEvent levelCreatedEvent -> {
                final GameScene currentGameScene = gameSceneManager.currentGameScene();
                final GamePlayView playView = gameViewManager.assertView(GameViewID.GAMEPLAY, GamePlayView.class);
                playView.acceptLevel(currentGameScene, levelCreatedEvent.level());
            }

            case GenericChangeEvent _ -> forceGameSceneReload = true;

            case HighScoreAccessErrorEvent failure -> {
                ui.showMessage(Duration.seconds(5), "Accessing high score failed!\n%s", failure.reason().getMessage());
                return;
            }

            default -> {}
        }

        if (gameSceneManager instanceof DefaultGameSceneManager defaultGameSceneManager) {
            defaultGameSceneManager.updateGameSceneAndForceReload(this, forceGameSceneReload);
        }
        gameSceneManager.optCurrentGameScene()
            .flatMap(GameScene::optGameEventHandler)
            .ifPresent(handler -> handler.onGameEvent(gameEvent));
    }

    private void onGameVariantNameChanged(String oldVariantName, String newVariantName) {
        if (oldVariantName != null) {
            Logger.info("<<< Exit Game variant '{}'", oldVariantName);
            final GameVariantRuntime oldRuntime = gameVariantManager.variantRuntime(oldVariantName);
            exitGameVariant(oldRuntime);
        }
        if (newVariantName != null) {
            Logger.info(">>> Enter game variant '{}'", newVariantName);
            final GameVariantRuntime newRuntime = gameVariantManager.variantRuntime(newVariantName);
            enterGameVariant(newRuntime);
        }
    }

    //TODO This method is messy and needs a cleanup!
    private void enterGameVariant(GameVariantRuntime runtime) {
        final GameEventManager gameEventManager = new DefaultGameEventManager();

        currentGame = new GameContext(runtime, coinMechanism, gameEventManager);

        stateChangeEventMapper = new StateChangeEventMapper(gameEventManager);
        runtime.gameFlow().addStateChangeListener(stateChangeEventMapper);
        spriteAnimationSystem.start();

        gameEventManager.addSubscriber(this::handleGameEvent);
        gameEventManager.addSubscriber(new PacEatingEventHandler(currentGame));
        gameEventManager.addSubscriber(new PacPowerEventHandler(currentGame));

        final GameVariantUIConfig uiConfig = uiConfig(runtime);
        gameSceneManager.setGameSceneConfig(uiConfig.gameSceneConfig());
        ui.initForGameVariant(this, uiConfig);

        createNewSession();
    }

    private void exitGameVariant(GameVariantRuntime runtime) {
        runtime.gameFlow().removeStateChangeListener(stateChangeEventMapper);
        spriteAnimationSystem.stop();
        soundManager.dispose();
        currentGame.eventManager().removeAllSubscribers();
        currentGame = null;
        uiConfig(runtime).unload(this);
    }

    private void onViewChanged(Named oldViewID, Named newViewID) {
        if (oldViewID != null) {
            gameViewManager.assertView(oldViewID).onExit();
        }
        final GameView newView = gameViewManager.assertView(newViewID);
        newView.onEnter();

        ui.clearShortMessage();
        ui.mainScene().replaceGameView(newView);
        ui.stageTitleManager().update(this, newViewID);
    }

    private void startBackgroundServices() {
        watchdog.startWatching();
        spriteAnimationSystem.start();
        ui.startShortMessageSystem();
        // Load 3D meshes by accessing the singleton instance
        //noinspection ResultOfMethodCallIgnored
        PacManMeshes3D.instance();
    }

    // --- Game loop

    private void simulate() {
        final long tick = clock.currentTick();
        try {
            currentGame.session().newFrameState(tick);
            currentGame.runtime().systems().entityUpdateSystem().updateEntities(currentGame);
            currentGame.runtime().gameFlow().update(currentGame);
            // Now the current game scene is up-to-date!
            gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(currentGame));
        }
        catch (Exception x) {
            fatalError(x);
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
        Logger.error(reason, "*** KA-TAS-TRO-PHE! SOMETHING VERY BAD HAPPENED!");
        endSession();
        ui.showMessage(Duration.seconds(60), translate("error.oh_no_my_program") + "\n" + reason.getMessage());
    }
}