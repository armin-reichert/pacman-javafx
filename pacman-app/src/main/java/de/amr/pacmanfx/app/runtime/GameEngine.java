/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.rendering.RenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.gamestate.GameSimulation;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.GameVariantManager;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.gamescene.GameSceneManager;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.sound.SoundManager;
import de.amr.pacmanfx.ui.EngineUIContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameVariantUIConfig;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewManager;
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
public final class GameEngine implements EngineLifecycle, EngineUIContext, TranslationManager {

    private static final ResourceBundle TEXT_BUNDLE = ResourceBundle.getBundle("de.amr.pacmanfx.app.localized_texts");

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

    private final GameSimulation simulation;

    private final GameUI ui;

    private GameContext currentGame;

    public GameEngine(
        GameClock clock,
        GameSimulation simulation,
        CoinMechanism coinMechanism,
        GameUI ui,
        GameVariantManager gameVariantManager,
        GameSceneManager gameSceneManager,
        GameViewManager gameViewManager,
        RenderManager renderManager,
        SoundManager soundManager,
        SpriteAnimationSystem spriteAnimationSystem)
    {
        this.clock = requireNonNull(clock);
        this.simulation = requireNonNull(simulation);
        this.coinMechanism = requireNonNull(coinMechanism);
        this.ui = requireNonNull(ui);
        this.gameVariantManager = requireNonNull(gameVariantManager);
        this.gameSceneManager = requireNonNull(gameSceneManager);
        this.gameViewManager = requireNonNull(gameViewManager);
        this.renderManager = requireNonNull(renderManager);
        this.soundManager = requireNonNull(soundManager);
        this.spriteAnimationSystem = requireNonNull(spriteAnimationSystem);

        input = new Input();

        if (!validateUserDirs()) {
            throw new IllegalStateException("User directory validation failed");
        }
        watchdog = new DirectoryWatchdog(GameConstants.CUSTOM_MAP_DIR);

        clock.setPausableAction(this::simulate);
        clock.setPermanentAction(this::render);

        gameViewManager.currentViewProperty().addListener((_, oldView, newView) -> onViewChanged(oldView, newView));

        gameVariantManager.currentVariantIDProperty().addListener(
            (_, oldVariantName, newVariantName) -> onGameVariantNameChanged(oldVariantName, newVariantName));

        ui.setEngine(this);
        gameViewManager.init(this);
    }

    public void show(GameVariantID variantID) {
        requireNonNull(variantID);

        gameVariantManager.selectVariant(this, variantID.name());
        gameViewManager.onVariantSelected(variantID);

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
            final String message = MessageFormat.format(textBundle().getString(key), args);
            return message.replace("\\n", "\n");
        }
        Logger.error("Missing localized text for key {}", key);
        return "[" + key + "]";
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
    public void createNewSession() {
        final HUD hud = new HUD(gameVariantManager.currentVariantID());
        final int numLives = gameVariantManager.currentRuntime().gameRules().initialLifeCount();
        final var session = new GameSession(hud, numLives);
        currentGame.setSession(session);
    }

    @Override
    public void startSession() {
        ui.onSessionStart(currentGame.session());
        currentGame.runtime().gamePlay().startSession(currentGame);
        gameViewManager.selectGamePlayView();
        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
        clock.start();
    }

    @Override
    public void endSession() {
        ui.onSessionEnd();
        soundManager.stopAll();
        gameSceneManager.optCurrentGameScene().ifPresent(GameScene::deactivate);
        gameSceneManager.removeCurrentGameScene();
        clock.stop();
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
            throw new IllegalArgumentException("Illegal game variant runtime type: " + runtime.getClass().getSimpleName());
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

    private void handleGameEvent(GameEvent gameEvent) {
        gameViewManager.currentView().handleGameEvent(gameEvent);

        //TODO check this
        final GameVariantRuntime runtime = gameVariantManager.currentRuntime();
        final boolean reload = gameEvent instanceof GenericChangeEvent;
        if (gameSceneManager instanceof DefaultGameSceneManager defaultGameSceneManager) {
            defaultGameSceneManager.updateGameSceneWithReload(this, uiConfig(runtime), reload);
        }
        gameSceneManager.currentGameScene().optGameEventHandler().ifPresent(handler -> handler.onGameEvent(gameEvent));
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

    private void enterGameVariant(GameVariantRuntime runtime) {
        final GameVariantUIConfig uiConfig = uiConfig(runtime);

        currentGame = new GameContext(runtime, coinMechanism);
        currentGame.runtime().eventManager().addSubscriber(this::handleGameEvent);

        gameSceneManager.setGameSceneConfig(uiConfig.gameSceneConfig());

        ui.initForGameVariant(this, uiConfig);

        createNewSession();
        spriteAnimationSystem.start();
    }

    private void exitGameVariant(GameVariantRuntime runtime) {
        spriteAnimationSystem.stop();
        soundManager.dispose();
        currentGame.runtime().eventManager().removeAllSubscribers();
        currentGame = null;
        uiConfig(runtime).unload(this);
    }

    private void onViewChanged(GameView oldView, GameView newView) {
        if (oldView != null) {
            oldView.onExit();
        }
        newView.onEnter();

        ui.clearShortMessage();
        ui.mainScene().replaceGameView(newView);
        ui.stageTitleManager().update(this, newView);
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
        if (currentGame == null) {
            return; // should never happen
        }
        currentGame.session().initFrame(clock.currentTick());
        try {
            simulation.makeStep(currentGame);
            // Now the current game scene is up-to-date!
            gameSceneManager.optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(currentGame));
        }
        catch (Exception x) {
            fatalError(x);
        }
    }

    private void render() {
        try {
            gameViewManager.currentView().render();
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