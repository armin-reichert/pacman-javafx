package de.amr.pacmanfx.ui.systems;

import de.amr.pacmanfx.ui.entities3D.pac.system.Pac3DAnimationSystem;
import de.amr.pacmanfx.ui.entities3D.pac.system.Pac3DTransformSystem;
import de.amr.pacmanfx.ui.entities3D.pac.system.Pac3DViewSystem;

public record PacSystems3D(
    Pac3DViewSystem view3DSystem,
    Pac3DTransformSystem transformSystem,
    Pac3DAnimationSystem animationSystem) {
    public PacSystems3D() {
        this(new Pac3DViewSystem(),
            new Pac3DTransformSystem(),
            new Pac3DAnimationSystem());
    }
}
