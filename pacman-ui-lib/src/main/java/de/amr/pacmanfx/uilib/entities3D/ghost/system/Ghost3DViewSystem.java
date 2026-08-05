package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.ghost.GhostState;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DVariant;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;

public class Ghost3DViewSystem {

    public static void update(Ghost ghost) {
        GhostState state = ghost.state();
        Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);

        Ghost3DVariant variant = selectVariant(ghost, state);
        view3D.setActiveVariant(variant);
        updateVisibility(view3D);
    }

    private static Ghost3DVariant selectVariant(Ghost ghost, GhostState state) {
        boolean flashing = false; //TODO
        if (state == GhostState.FRIGHTENED) {
            return flashing ? Ghost3DVariant.FLASHING : Ghost3DVariant.BLUE;
        }
        if (state == GhostState.EATEN) {
            return Ghost3DVariant.NUMBER;
        }
        return Ghost3DVariant.COLORED;
    }

    private static void updateVisibility(Ghost3DViewComp view) {
        view.coloredGhost().setVisible(view.activeVariant() == Ghost3DVariant.COLORED);
        view.blueGhost().setVisible(view.activeVariant() == Ghost3DVariant.BLUE);
        view.flashingGhost().setVisible(view.activeVariant() == Ghost3DVariant.FLASHING);
        view.numberGhost().setVisible(view.activeVariant() == Ghost3DVariant.NUMBER);
    }
}

