/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.game;

import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import org.tinylog.Logger;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public final class GameLoop {

    private final GameClock clock;
    private Consumer<Throwable> errorHandler;

    public GameLoop(GameClock clock, GameApp app) {
        this.clock = requireNonNull(clock);
        requireNonNull(app);

        this.errorHandler = x -> Logger.error(x, "An error occurred in the game loop");

        clock.setUpdateAction(() -> {
            try {
                final GameContext game = app.game();
                final GameScene currentGameScene = app.gameSceneManager().currentGameScene();

                game.session().newFrameState(clock.currentTick());
                game.playConfig().systems().updateSystem().updateEntities(game);
                game.playConfig().gameFlow().update(game);
                if (currentGameScene != null) {
                    currentGameScene.onTick(game);
                }
            }
            catch (Exception x) {
                errorHandler.accept(x);
            }
        });

        clock.setPermanentAction(() -> {
            try {
                if (app.ui().viewManager().isSelected(GameViewID.GAMEPLAY)) {
                    final GamePlayView view = app.ui().viewManager().gamePlayView();
                    final GameScene currentGameScene = app.gameSceneManager().currentGameScene();
                    if (currentGameScene instanceof AbstractGameScene ags) {
                        view.render(ags, clock.currentTick());
                    }
                    view.updateDashboard();
                    view.updateMiniView();
                }
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
}
