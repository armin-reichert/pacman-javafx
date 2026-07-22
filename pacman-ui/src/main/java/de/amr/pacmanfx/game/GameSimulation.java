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

public class GameSimulation {

    private final GameAppContext game;

    public GameSimulation(GameAppContext game) {
        this.game = game;
    }

    public void start() {
        clock().setUpdateAction(this::simulate);
        clock().setPermanentAction(this::renderCurrentView);
        clock().setErrorHandler(this::handleFatalError);
        clock().start();
    }

    public void stop() {
        clock().stop();
        clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
    }

    // private

    private GameClock clock() {
        return game.clock();
    }

    private void simulate() {
        final GameContext gameContext = game.currentGameContext();
        gameContext.newFrame(clock().currentTick());
        gameContext.flow().update(gameContext);

        game.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(gameContext.thisFrame()));
    }

    private void renderCurrentView() {
        Platform.runLater(() -> game.ui().views().assertCurrentView().render());
    }

    private void handleFatalError(Throwable reason) {
        game.lifecycle().suspendPlaying();
        final String errorMessage = game.ui().translations().translate("error.oh_no_my_program");
        game.ui().shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}
