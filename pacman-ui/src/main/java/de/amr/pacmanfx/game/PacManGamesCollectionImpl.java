/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.action.core.GameLifecycle;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games collection.
 */
public final class PacManGamesCollectionImpl implements GameActionContext, GameLifecycle {

    private final PacManGamesMachine machine = PacManGamesMachine.instance();

    private final GameVariantManager variantManager;

    private final CommonActions commonActions;

    private GameUI ui;

    private GameContext gameContext;

    public PacManGamesCollectionImpl() {
        variantManager = new GameVariantManager(this);
        commonActions = new CommonActions(this);
        configureClock();

        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.setGameActionContext(this);
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

    // GameActionContext

    @Override
    public GameLifecycle lifecycle() {
        return this;
    }

    @Override
    public DirectoryWatchdog watchdog() {
        return machine.watchdog();
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
    public CommonActions commonActions() {
        return commonActions;
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
    public GameUI ui() {
        return ui;
    }

    // GameLifecycle

    @Override
    public void startGamePlay() {
        gameContext.flow().restartState(GameStateID.BOOT);
        ui.views().selectGamePlayView();
        clock().start();
    }

    @Override
    public void suspendGamePlay() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.sounds().stopAll();
        clock().stop();
        clock().setTargetFrameRate(GameClock.DEFAULT_TICKS_PER_SECOND);
    }

    @Override
    public void terminate() {
        suspendGamePlay();
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
    }

    private void configureClock() {
        clock().setUpdateAction(this::simulateAndUpdateCurrentGameScene);
        clock().setPermanentAction(this::renderCurrentView);
        clock().setErrorHandler(this::handleFatalError);
    }

    private void simulateAndUpdateCurrentGameScene() {
        gameContext.flow().update();
        Platform.runLater(() -> ui.gameScenes().optCurrentGameScene()
            .ifPresent(gameScene -> gameScene.onTick(clock().currentTick())));
    }

    private void renderCurrentView() {
        Platform.runLater(() -> ui.views().assertCurrentView().render());
    }

    private void handleFatalError(Throwable reason) {
        Platform.runLater(() -> {
            suspendGamePlay();
            final String errorMessage = ui.translations().translate("error.oh_no_my_program");
            ui.shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
            Logger.error(reason, "*** KA TAS TROOOOOPHE! SOMETHING VERY BAD HAPPENED:");
        });
    }
}