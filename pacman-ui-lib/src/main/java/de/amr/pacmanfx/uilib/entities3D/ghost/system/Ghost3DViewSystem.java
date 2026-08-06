package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.GhostAppearance;

public class Ghost3DViewSystem {

    public static void update(Ghost ghost) {
        GhostState state = ghost.ghostState();
        GhostAppearance variant = selectVariant(state);
        Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);
        view3D.setActiveVariant(variant);

        view3D.root().setVisible(ghost.isVisible());
    }

    private static GhostAppearance selectVariant(GhostState state) {
        return switch (state) {
            case EATEN -> GhostAppearance.EATEN;
            case ENTERING_HOUSE -> GhostAppearance.EYES;
            case FRIGHTENED -> GhostAppearance.FRIGHTENED; // TODO: flashing
            case HUNTING_PAC -> GhostAppearance.NORMAL;
            case LEAVING_HOUSE -> GhostAppearance.NORMAL;
            case LOCKED -> GhostAppearance.NORMAL; //TODO can also be FRIGHTENED
            case RETURNING_HOME -> GhostAppearance.EYES;
        };
    }
}

