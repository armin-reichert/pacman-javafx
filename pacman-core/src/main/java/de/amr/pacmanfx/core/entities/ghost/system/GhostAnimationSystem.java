package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.entities.ActorAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;

public class GhostAnimationSystem {

    public static void update(Ghost ghost, Pac pac) {
        final ActorAnimationID animationID = switch (ghost.ghostStateEnum()) {
            case LOCKED, LEAVING_HOUSE -> threatenedOrNormalAnimation(ghost, pac);
            case HUNTING_PAC    -> ActorAnimationID.GHOST_NORMAL;
            case FRIGHTENED     -> frightenedOrFlashingAnimation(pac);
            case EATEN, RETURNING_HOME, ENTERING_HOUSE -> ActorAnimationID.GHOST_EYES;
        };
        ghost.ghostAnimation().setAnimationID(animationID);
    }

    private static ActorAnimationID threatenedOrNormalAnimation(Ghost ghost, Pac pac) {
        final GhostStateComp state = ghost.state();
        return state.isThreatenedByPac()
            ? frightenedOrFlashingAnimation(pac)
            : ActorAnimationID.GHOST_NORMAL;
    }

    private static ActorAnimationID frightenedOrFlashingAnimation(Pac pac) {
        final boolean flashing = pac.power().isFadingStart() || pac.power().isFading();
        return flashing ? ActorAnimationID.GHOST_FLASHING : ActorAnimationID.GHOST_FRIGHTENED;
    }
}
