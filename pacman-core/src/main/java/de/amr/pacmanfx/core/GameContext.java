/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.simulation.HuntingStepResult;

import java.util.Optional;

public interface GameContext {

    CoinMechanism coinMechanism();

    GameFlow gameFlow();

    default State<GameContext> gameState() {
        return gameFlow().state();
    }

    GameRules gameRules();

    GameModel gameModel();

    default Optional<GameLevel> optCurrentGameLevel() {
        return gameModel().optGameLevel();
    }

    boolean isCollisionDoubleChecked();

    CollisionStrategy collisionStrategy();

    void startNewHuntingStep();

    HuntingStepResult huntingResult();
}
