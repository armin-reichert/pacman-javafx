/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.simulation.HuntingStepResult;

public interface GameContext {

    GameModel game();

    GameFlow flow();

    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    Boolean isCollisionDoubleChecked();

    CollisionStrategy collisionStrategy();

    void setCollisionDoubleChecked(boolean doubleChecked);

    void startNewHuntingStep();

    HuntingStepResult huntingResult();
}
