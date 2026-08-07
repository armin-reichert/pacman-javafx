package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostAppearance;

import static java.util.Objects.requireNonNull;

public class Ghost3DViewSystem {

    public static void update(Ghost ghost) {
        GhostState state = ghost.ghostStateEnum();
        GhostAppearance variant = switch (state) {
            case EATEN -> GhostAppearance.EATEN;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case FRIGHTENED -> GhostAppearance.FRIGHTENED;
            case HUNTING_PAC, LEAVING_HOUSE -> GhostAppearance.NORMAL;
            case LOCKED -> GhostAppearance.NORMAL; //TODO can also be FRIGHTENED
        };
        final Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);
        if (view3D.appearance() != variant) {
            setActiveVariant(view3D, variant);
        }
        view3D.root().setVisible(ghost.isVisible());
    }

    private static void setActiveVariant(Ghost3DViewComp view3D, GhostAppearance variant) {
        view3D.setAppearance(variant);
        switch (variant) {
            case EATEN      -> view3D.lookEaten();
            case EYES       -> view3D.lookEyesOnly();
            case FLASHING   -> view3D.lookFlashing();
            case FRIGHTENED -> view3D.lookFrightened();
            case NORMAL     -> view3D.lookNormal();
        }
    }

}

