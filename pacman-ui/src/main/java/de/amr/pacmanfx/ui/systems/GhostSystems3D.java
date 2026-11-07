package de.amr.pacmanfx.ui.systems;

import de.amr.pacmanfx.ui.entities3D.ghost.system.Ghost3DMovementSystem;
import de.amr.pacmanfx.ui.entities3D.ghost.system.GhostView3DSystem;

public record GhostSystems3D(Ghost3DMovementSystem movementSystem, GhostView3DSystem viewSystem) {
    public GhostSystems3D() {
        this(new Ghost3DMovementSystem(), new GhostView3DSystem());
    }
}
