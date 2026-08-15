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

import static java.util.Objects.requireNonNull;

public final class GameSimulation {

    private final GameAppContext app;
    private final GameClock clock;

    public GameSimulation(GameAppContext app, GameClock clock) {
        this.app = requireNonNull(app);
        this.clock = requireNonNull(clock);
    }

    public void start() {
        clock.setUpdateAction(this::simulate);
        clock.setPermanentAction(this::renderCurrentView);
        clock.setErrorHandler(this::handleFatalError);
        clock.start();
    }

    public void stop() {
        clock.stop();
        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
    }

    // private

    private void simulate() {
        final GameContext game = app.game();
        game.session().newFrameState(clock.currentTick());
        game.variant().gameFlow().update(game);
        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(game));
    }

    private void renderCurrentView() {
        Platform.runLater(() -> {
            try {
                app.ui().views().assertCurrentView().render();
            } catch (Exception x) {
                Logger.error(x);
            }
        });
    }

    private void handleFatalError(Throwable reason) {
        app.suspendGame();
        final String errorMessage = app.ui().translations().translate("error.oh_no_my_program");
        app.ui().shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}
