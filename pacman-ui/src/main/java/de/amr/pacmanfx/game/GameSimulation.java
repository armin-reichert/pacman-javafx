/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tinylog.Logger;

public final class GameSimulation {

    private GameSimulation() {}

    public static void start(GameAppContext appContext) {
        final GameClock clock = appContext.clock();
        clock.setUpdateAction(() -> simulate(appContext));
        clock.setPermanentAction(() -> renderCurrentView(appContext));
        clock.setErrorHandler(x -> handleFatalError(appContext, x));
        clock.start();
    }

    public static void stop(GameAppContext appContext) {
        appContext.clock().stop();
        appContext.clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
    }

    // private

    private static void simulate(GameAppContext appContext) {
        final GameContext gameContext = appContext.currentGameContext();
        gameContext.newFrameContext(appContext.clock().currentTick());
        gameContext.flow().update(gameContext);

        appContext.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(gameContext.thisFrame()));
    }

    private static void renderCurrentView(GameAppContext appContext) {
        Platform.runLater(() -> appContext.ui().views().assertCurrentView().render());
    }

    private static void handleFatalError(GameAppContext appContext, Throwable reason) {
        appContext.lifecycle().suspendPlaying();
        final String errorMessage = appContext.ui().translations().translate("error.oh_no_my_program");
        appContext.ui().shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}
