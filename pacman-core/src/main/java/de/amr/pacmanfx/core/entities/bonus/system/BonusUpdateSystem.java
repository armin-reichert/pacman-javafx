/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.bonus.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusStateComp;
import de.amr.pacmanfx.core.level.GameLevel;

public class BonusUpdateSystem {

    private final BonusStateSystem stateSystem;
    private final BonusMoveAndJumpSystem moveAndJumpSystem;
    private final MovementSystem motor;

    public BonusUpdateSystem(
        BonusStateSystem stateSystem,
        BonusMoveAndJumpSystem moveAndJumpSystem,
        MovementSystem motor)
    {
        this.stateSystem = stateSystem;
        this.moveAndJumpSystem = moveAndJumpSystem;
        this.motor = motor;
    }

    public void update(GameContext game, GameLevel level, Bonus bonus) {
        final BonusStateComp state = bonus.state();
        switch (state.enumValue()) {
            case INACTIVE -> {}
            case EDIBLE -> {
                stateSystem.update(game, bonus);
                moveAndJumpSystem.update(level, bonus, motor);
            }
            case EATEN -> stateSystem.update(game, bonus);
        }
    }
}
