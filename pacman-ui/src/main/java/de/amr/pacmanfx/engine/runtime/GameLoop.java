/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.runtime;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.rendering.GamePlayViewRenderer;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import org.tinylog.Logger;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class GameLoop {

    private final GameClock clock;
    private Consumer<Throwable> errorHandler;

    public GameLoop(GameClock clock, PacManGamesEngine app) {
        this.clock = requireNonNull(clock);
        requireNonNull(app);

        this.errorHandler = x -> Logger.error(x, "An error occurred in the game loop");

        clock.setUpdateAction(() -> {
            try {
                final GameContext game = app.currentGame();
                game.session().newFrameState(clock.currentTick());
                game.playConfig().systems().updateSystem().updateEntities(game);

                // This can change the current game state!
                game.playConfig().gameFlow().update(game);

                // IMPORTANT: The current game scene is up-to-date only at this point!
                app.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onTick(game));
            }
            catch (Exception x) {
                errorHandler.accept(x);
            }
        });

        clock.setPermanentAction(() -> {
            try {
                render(app);
            } catch (Exception x) {
                errorHandler.accept(x);
            }
        });
    }

    public void setErrorHandler(Consumer<Throwable> errorHandler) {
        this.errorHandler = requireNonNull(errorHandler);
    }

    public void start() {
        clock.setTargetFrameRate(GameConstants.SIMULATION_FPS);
        clock.start();
    }

    public void stop() {
        clock.stop();
    }

    private void render(PacManGamesEngine app) {
        if (app.ui().viewManager().isSelected(GameViewID.GAMEPLAY)) {
            final GameScene currentGameScene = app.gameSceneManager().currentGameScene();
            final GamePlayView playView = app.ui().viewManager().gamePlayView();
            if (currentGameScene instanceof AbstractGameScene abstractGameScene) {
                GamePlayViewRenderer.render(playView, app, clock, abstractGameScene);
            }
            playView.update();
        }
    }
}
