/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.*;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games collection.
 */
public final class PacManGames implements GameAppContext, GameLifecycle {

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

    private final GameVariantManager variantManager;

    private final CommonGameActions actions;

    private GameUI ui;

    private GameContext gameContext;

    public PacManGames() {
        machine = GameBox.instance();
        variantManager = new GameVariantManager(this);
        actions = new CommonGameActions(this);
        configureClock();
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.setAppContext(this);
    }

    public void selectGameVariantAndShow(GameVariantID variantID) {
        requireNonNull(variantID);
        variantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.views().selectStartPagesView();
        ui.views().startPagesView().rootPane().setSelectedIndex(0);
        ui.views().gamePlayView().dashboard().setGameActionContext(this);

        ui.window().show(this);

        Platform.runLater(this::startBackgroundServices);
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
        final GameFlowController gameFlow = gameContext.flow();
        gameFlow.setGameContext(gameContext);
        gameFlow.restartState(GameStateID.BOOT);
        clock().start();
        ui.views().selectGamePlayView();
    }

    @Override
    public void suspendPlaying() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.sounds().stopAll();
        clock().stop();
        clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
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

    private void configureClock() {
        clock().setUpdateAction(this::simulateAndUpdateCurrentGameScene);
        clock().setPermanentAction(this::renderCurrentView);
        clock().setErrorHandler(this::handleFatalError);
    }

    private void simulateAndUpdateCurrentGameScene() {
        final GameFlowController gameFlow = gameContext.flow(); //TODO store elsewhere?
        gameFlow.setGameContext(gameContext);

        // New simulation step ("frame")
        gameContext.newFrame(clock().currentTick());

        // Update game flow
        gameFlow.update(gameContext);

        // Update current game scene
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene ->
            Platform.runLater(() -> gameScene.onTick(gameContext.thisFrame())));
    }

    private void renderCurrentView() {
        Platform.runLater(() -> ui.views().assertCurrentView().render());
    }

    private void handleFatalError(Throwable reason) {
        Platform.runLater(() -> {
            suspendPlaying();
            final String errorMessage = ui.translations().translate("error.oh_no_my_program");
            ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
        });
    }
}