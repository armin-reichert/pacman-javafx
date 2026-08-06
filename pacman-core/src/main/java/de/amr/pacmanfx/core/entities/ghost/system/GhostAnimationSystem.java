package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.entities.ActorAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;

public class GhostAnimationSystem {

    public static void update(Ghost ghost, Pac pac, SpriteAnimSystem animSystem) {
        final ActorAnimationID animationID = switch (ghost.state()) {
            case LOCKED         -> selectLockedStateAnimation(ghost, pac);
            case LEAVING_HOUSE  -> selectLeavingHouseStateAnimation(ghost, pac);
            case HUNTING_PAC, EATEN -> ActorAnimationID.GHOST_NORMAL;
            case FRIGHTENED     -> selectFrightenedOrFlashingAnimation(pac);
            case RETURNING_HOME, ENTERING_HOUSE -> ActorAnimationID.GHOST_EYES;
        };
        animSystem.select(ghost, animationID);
        animSystem.playSelected(ghost);
    }

    private static ActorAnimationID selectLockedStateAnimation(Ghost ghost, Pac pac) {
        final GhostStateComp state = ghost.stateComp();
        return state.isThreatenedByPac()
            ? selectFrightenedOrFlashingAnimation(pac)
            : ActorAnimationID.GHOST_NORMAL;
    }

    private static ActorAnimationID selectLeavingHouseStateAnimation(Ghost ghost, Pac pac) {
        final GhostStateComp state = ghost.stateComp();
        boolean insideHouse = true; // TODO
        return insideHouse && state.isThreatenedByPac()
            ? selectFrightenedOrFlashingAnimation(pac)
            : ActorAnimationID.GHOST_NORMAL;
    }

    private static ActorAnimationID selectFrightenedOrFlashingAnimation(Pac pac) {
        final boolean flashing = pac.power().isFadingStart() || pac.power().isFading();
        return flashing ? ActorAnimationID.GHOST_FLASHING : ActorAnimationID.GHOST_FRIGHTENED;
    }
}
