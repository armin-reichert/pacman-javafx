/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;

public class GhostAnimationSelectionSystem {

    public GhostAnimationSelectionSystem() {}

    public void update(Ghost ghost, Pac pac, ActorSpriteAnimController spriteAnimController) {
        final CommonSpriteAnimationID id = switch (ghost.state().enumValue()) {
            case LOCKED, LEAVING_HOUSE -> threatenedOrNormalAnimation(ghost, pac);
            case HUNTING_PAC -> CommonSpriteAnimationID.GHOST_NORMAL;
            case FRIGHTENED -> frightenedOrFlashingAnimation(pac);
            case RETURNING_HOME, ENTERING_HOUSE -> CommonSpriteAnimationID.GHOST_EYES;
            case EATEN -> CommonSpriteAnimationID.GHOST_POINTS;
        };

        ghost.animationSelection().setAnimationID(id);
        spriteAnimController.select(ghost, id);
        if (id == CommonSpriteAnimationID.GHOST_POINTS) {
            spriteAnimController.selectAndSetFrame(ghost,
                CommonSpriteAnimationID.GHOST_POINTS, ghost.animationSelection().frame());
        }
        spriteAnimController.playSelected(ghost);
    }

    private CommonSpriteAnimationID threatenedOrNormalAnimation(Ghost ghost, Pac pac) {
        final GhostStateComp state = ghost.state();
        return state.isThreatenedByPac()
            ? frightenedOrFlashingAnimation(pac)
            : CommonSpriteAnimationID.GHOST_NORMAL;
    }

    private CommonSpriteAnimationID frightenedOrFlashingAnimation(Pac pac) {
        final boolean flashing = pac.power().isFadingStart() || pac.power().isFading();
        return flashing ? CommonSpriteAnimationID.GHOST_FLASHING : CommonSpriteAnimationID.GHOST_FRIGHTENED;
    }
}
