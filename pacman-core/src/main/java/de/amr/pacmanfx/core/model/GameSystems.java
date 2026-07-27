package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.systems.MovementSystem;
import de.amr.pacmanfx.core.model.systems.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;

public final class GameSystems {

    public final MovementSystem movementSystem =  new MovementSystem();
    public final WorldMovementSystem worldMovementSystem = new WorldMovementSystem();
    public final PacPowerSystem pacPowerSystem = new PacPowerSystem();
}
