/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;

public class GhostSpriteAnimationSystem {

    public static void update(Ghost ghost, Pac pac) {
        final CommonSpriteAnimationID animationID = switch (ghost.ghostStateEnum()) {
            case LOCKED, LEAVING_HOUSE -> threatenedOrNormalAnimation(ghost, pac);
            case HUNTING_PAC    -> CommonSpriteAnimationID.GHOST_NORMAL;
            case FRIGHTENED     -> frightenedOrFlashingAnimation(pac);
            case EATEN, RETURNING_HOME, ENTERING_HOUSE -> CommonSpriteAnimationID.GHOST_EYES;
        };
        ghost.ghostAnimation().setAnimationID(animationID);
        ghost.spriteAnimation().animation().playSelected();
    }

    private static CommonSpriteAnimationID threatenedOrNormalAnimation(Ghost ghost, Pac pac) {
        final GhostStateComp state = ghost.state();
        return state.isThreatenedByPac()
            ? frightenedOrFlashingAnimation(pac)
            : CommonSpriteAnimationID.GHOST_NORMAL;
    }

    private static CommonSpriteAnimationID frightenedOrFlashingAnimation(Pac pac) {
        final boolean flashing = pac.power().isFadingStart() || pac.power().isFading();
        return flashing ? CommonSpriteAnimationID.GHOST_FLASHING : CommonSpriteAnimationID.GHOST_FRIGHTENED;
    }
}
