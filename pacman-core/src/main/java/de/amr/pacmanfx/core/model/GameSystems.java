package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.systems.MovementSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;

public final class GameSystems {

    public final MovementSystem movement =  new MovementSystem();
    public final WorldMovementSystem worldMovement = new WorldMovementSystem();
}
