/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.rendering.RenderManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import javafx.util.Duration;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public final class GameLoop {

    private final GameApp app;
    private final GameClock clock;
    private final RenderManager renderManager;

    public GameLoop(GameApp app, GameClock clock, RenderManager renderManager) {
        this.app = requireNonNull(app);
        this.clock = requireNonNull(clock);
        this.renderManager = requireNonNull(renderManager);
    }

    public void start() {
        clock.setUpdateAction(this::simulate);
        clock.setPermanentAction(this::render);
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
        game.playConfig().systems().updateSystem().updateEntities(game);
        game.playConfig().gameFlow().update(game);
        app.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(game));
    }

    private void render() {
        final GameViewManager views = app.ui().viewManager();
        try {
            if (views.isSelected(GameViewID.GAMEPLAY)) {
                views.gamePlayView().render(renderManager, clock.currentTick());
                views.gamePlayView().updateDashboard();
                views.gamePlayView().updateMiniView();
            }
        } catch (Exception x) {
            Logger.error(x, "Rendering triggered exception");
        }
    }

    private void handleFatalError(Throwable reason) {
        app.suspendGame();
        final String errorMessage = app.ui().translationManager().translate("error.oh_no_my_program");
        app.ui().shortMessage(Duration.seconds(60), errorMessage + "\n" + reason.getMessage());
        Logger.error(reason, "*** KA-TAS-TROOPHE! SOMETHING VERY BAD HAPPENED!");
    }
}
