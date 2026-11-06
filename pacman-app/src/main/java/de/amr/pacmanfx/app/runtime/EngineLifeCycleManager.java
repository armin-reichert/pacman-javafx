/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.app.runtime;

import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.model.test.TestStateID;
import de.amr.pacmanfx.engine.EngineLifecycle;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.ui.EngineUIContext;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class EngineLifeCycleManager implements EngineLifecycle {

    private final EngineUIContext engine;

    public EngineLifeCycleManager(EngineUIContext engine) {
        this.engine = requireNonNull(engine);
    }

    @Override
    public void createNewSession() {
        if (engine.optCurrentGame().isEmpty()) {
            Logger.error("Cannot create game session, no game is selected!");
            throw new IllegalStateException();
        }
        engine.optCurrentGame().ifPresent(game -> {
            final HUD hud = new HUD(engine.gameVariantManager().currentVariantID());
            final int numLives = engine.gameVariantManager().currentRuntime().gameRules().initialLifeCount();
            game.setSession(new GameSession(hud, numLives));
        });
    }

    @Override
    public void startSession() {
        if (engine.optCurrentGame().isEmpty()) {
            Logger.error("Cannot start game session, no game is selected!");
            throw new IllegalStateException();
        }
        engine.optCurrentGame().ifPresent(game -> {
            game.play().startSession(game);
            engine.viewManager().selectGamePlayView();
            engine.clock().setTargetFrameRate(GameConstants.SIMULATION_FPS);
            engine.clock().start();
            engine.ui().onSessionStart(game.session());
        });
    }

    @Override
    public void endSession() {
        engine.gameSceneManager().optCurrentGameScene().ifPresent(GameScene::deactivate);
        engine.gameSceneManager().removeCurrentGameScene();
        engine.clock().stop();
        engine.soundManager().stopAll();
        engine.ui().onSessionEnd();
    }

    @Override
    public void restartSession() {
        if (engine.optCurrentGame().isEmpty()) {
            Logger.error("Cannot restart game session, no game is selected!");
            throw new IllegalStateException();
        }
        engine.optCurrentGame().ifPresent(game -> {
            final AbstractGameState gameState = game.state();
            if (gameState.id() instanceof TestStateID) {
                gameState.onExit(game);
            }
            endSession();
            createNewSession();
            startSession();
        });
    }

    @Override
    public void terminate() {
        endSession();
        engine.ui().stopShortMessageSystem();
        engine.spriteAnimationSystem().stop();
        engine.watchdog().dispose();
        Logger.info("Application terminated. There is no way back!");
    }
}
