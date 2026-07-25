package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.component.MovementSystem;
import de.amr.pacmanfx.core.model.component.WorldMovementSystem;

public final class GameSystems {

    public final MovementSystem movement =  new MovementSystem();
    public final WorldMovementSystem worldMovement = new WorldMovementSystem();
}
