package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.ghost.GhostState;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostAppearance;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;

public class Ghost3DViewSystem {

    public static void update(Ghost ghost) {
        GhostState state = ghost.state();
        Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);

        GhostAppearance variant = selectVariant(ghost, state);
        view3D.setActiveVariant(variant);
    }

    private static GhostAppearance selectVariant(Ghost ghost, GhostState state) {
        boolean flashing = false; //TODO
        if (state == GhostState.FRIGHTENED) {
            return flashing ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
        }
        if (state == GhostState.EATEN) {
            return GhostAppearance.NUMBER;
        }
        return GhostAppearance.NORMAL;
    }

}

