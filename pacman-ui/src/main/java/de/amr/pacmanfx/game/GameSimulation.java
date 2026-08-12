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

    private static void simulate(GameAppContext app) {
        final GameContext game = app.currentGame();
        game.session().newFrameState(app.clock().currentTick());
        game.session().gameFlow().update(game);

        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(game));
    }

    private static void renderCurrentView(GameAppContext appContext) {
        Platform.runLater(() -> {
            try {
                appContext.ui().views().assertCurrentView().render();
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    private static void handleFatalError(GameAppContext appContext, Throwable reason) {
        appContext.lifecycle().suspendPlaying();
        final String errorMessage = appContext.ui().translations().translate("error.oh_no_my_program");
        appContext.ui().shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}
