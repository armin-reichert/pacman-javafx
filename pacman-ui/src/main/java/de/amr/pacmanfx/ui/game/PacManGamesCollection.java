/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * The Pac-Man games collection.
 */
public final class PacManGamesCollection implements Game {

    private final GameVariantManager variantManager;

    private final CommonActions commonActions;

    private GameUI ui;

    private GameVariantContext gameVariantContext;

    public PacManGamesCollection() {
        this.variantManager = new VariantManager(this);
        this.commonActions = new CommonActions(this);
        configureClock();
        //noinspection ResultOfMethodCallIgnored
        PacManWorld3D.instance(); // loads 3D assets as side effect of accessing the singleton
    }

    public void setGameVariantContext(GameVariantContext gameVariantContext) {
        this.gameVariantContext = gameVariantContext;
    }

    // Game interface

    @Override
    public void setUI(GameUI ui) {
        this.ui = requireNonNull(ui);
        ui.connect(this);
    }

    @Override
    public PacManGamesMachine machine() {
        return PacManGamesMachine.instance();
    }

    @Override
    public GameVariantManager variants() {
        return variantManager;
    }

    @Override
    public GameContext context() {
        return gameVariantContext;
    }

    @Override
    public CommonActions actions() {
        return commonActions;
    }

    @Override
    public GameUI ui() {
        return ui;
    }

    // Lifecycle

    @Override
    public void showGameVariant(GameVariantID variantID) {
        requireNonNull(variantID);
        variantManager.selectVariant(variantID.name());

        //TODO rethink this
        ui.views().selectStartPagesView();
        ui.views().startPagesView().rootPane().setSelectedIndex(0);
        ui.views().gamePlayView().dashboard().connect(this);

        ui.window().show(this);

        startBackgroundServices();
    }

    @Override
    public void startGamePlay() {
        gameVariantContext.flow().restartState(GameStateID.BOOT);
        ui.views().selectGamePlayView();
        Platform.runLater(machine().clock()::start);
    }

    @Override
    public void suspendGamePlay() {
        ui.gameScenes().optCurrentGameScene().ifPresent(gameScene -> {
            ui.views().gamePlayView().disembedGameScene(gameScene);
            ui.gameScenes().currentGameSceneProperty().set(null);
        });
        ui.sounds().stopAll();
        machine().clock().stop();
        machine().clock().setTargetFrameRate(GameClock.DEFAULT_TICKS_PER_SECOND);
    }

    @Override
    public void terminate() {
        suspendGamePlay();
        ui.terminate();
        machine().dispose();
        Logger.info("Application terminated. There is no way back!");
    }

    // Private area, no trespassing!

    private void startBackgroundServices() {
        Platform.runLater(() -> {
            if (
                variantManager.isVariantRegistered(GameVariantID.ARCADE_PACMAN_XXL.name()) ||
                variantManager.isVariantRegistered(GameVariantID.ARCADE_MS_PACMAN_XXL.name())
            ) {
                machine().watchdog().startWatching();
                Logger.info("Custom map directory is getting watched!");
            }
            ui.window().mainScene().flashMessageManager().startAnimationTimer();
            ui.sprites().startAnimationTimer();
        });
    }

    private void configureClock() {
        machine().clock().setUpdateAction(this::simulateAndUpdateCurrentGameScene);
        machine().clock().setPermanentAction(this::renderCurrentView);
        machine().clock().setErrorHandler(this::handleFatalError);
    }

    private void simulateAndUpdateCurrentGameScene() {
        gameVariantContext.flow().makeStep();
        Platform.runLater(() -> ui.gameScenes().optCurrentGameScene()
            .ifPresent(gameScene -> gameScene.onTick(machine().clock().currentTick())));
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