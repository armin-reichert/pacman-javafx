package de.amr.pacmanfx.uilib.entities3D.ghost.system;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DAnimationComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostAppearance;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.anim.GhostFlashingAnimation3D;

public class Ghost3DAppearanceSystem {

    public static void update(Ghost ghost) {
        final GhostStateComp state = ghost.state();

        final GhostAppearance appearance = switch (state.ghostStateEnum()) {
            case LOCKED -> state.isThreatenedByPac() ? appearFrightenedOrFlashing(state) : GhostAppearance.NORMAL;
            case EATEN -> GhostAppearance.EATEN;
            case ENTERING_HOUSE, RETURNING_HOME -> GhostAppearance.EYES;
            case FRIGHTENED -> appearFrightenedOrFlashing(state);
            case HUNTING_PAC, LEAVING_HOUSE -> GhostAppearance.NORMAL;
        };
        setAppearance(ghost, appearance);
    }

    private static GhostAppearance appearFrightenedOrFlashing(GhostStateComp state) {
        return state.flashing() ? GhostAppearance.FLASHING : GhostAppearance.FRIGHTENED;
    }

    private static void setAppearance(Ghost ghost, GhostAppearance appearance) {
        final Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);
        final Ghost3DAnimationComp animation3D = ghost.requireComponent(Ghost3DAnimationComp.class);

        view3D.setAppearance(appearance);
        switch (appearance) {
            case EATEN, EYES -> {
                view3D.lookEyesOnly();
                animation3D.lookEyesOnly();
            }
            case FLASHING -> {
                view3D.lookFrightened();
                ensureFlashingPlays(animation3D.flashing());
            }
            case FRIGHTENED -> {
                view3D.lookFrightened();
                animation3D.lookFrightened();
            }
            case NORMAL -> {
                view3D.lookNormal();
                animation3D.lookNormal();
            }
        }
    }

    private static void ensureFlashingPlays(GhostFlashingAnimation3D flashing) {
        if (!flashing.isRunning()) {
            flashing.playOrContinue();
        }
    }
}

