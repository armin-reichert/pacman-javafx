/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.RenderManager;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.views.GameViewID;
import javafx.util.Duration;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public final class GameLoop {

    private final GameAppContext app;
    private final GameClock clock;
    private final RenderManager renderManager = new RenderManager();

    public GameLoop(GameAppContext app, GameClock clock) {
        this.app = requireNonNull(app);
        this.clock = requireNonNull(clock);
    }

    public void start() {
        clock.setUpdateAction(this::simulate);
        clock.setPermanentAction(() -> render(renderManager, clock.currentTick()));
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
        game.variantConfig().systems().updateSystem().updateEntities(game);
        game.variantConfig().gameFlow().update(game);
        app.ui().gameScenes().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(game));
    }

    private void render(RenderManager renderManager, long tick) {
        if (app.ui().views().isSelected(GameViewID.GAMEPLAY)) {
            app.ui().views().gamePlayView().render(renderManager, tick);
        }
    }

    private void handleFatalError(Throwable reason) {
        app.suspendGame();
        final String errorMessage = app.ui().translations().translate("error.oh_no_my_program");
        app.ui().shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}
