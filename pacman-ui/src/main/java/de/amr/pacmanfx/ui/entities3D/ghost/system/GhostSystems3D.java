package de.amr.pacmanfx.ui.entities3D.ghost.system;

public record GhostSystems3D(Ghost3DMovementSystem movementSystem, GhostView3DSystem viewSystem) {
    public GhostSystems3D() {
        this(new Ghost3DMovementSystem(), new GhostView3DSystem());
    }
}
