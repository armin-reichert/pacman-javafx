/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;

public class GhostAnimationSystem {

    public GhostAnimationSystem() {}

    public void update(Ghost ghost, ActorSpriteAnimController spriteAnimController) {
        final boolean pacHasPower = ghost.state().hasPacPower();
        final boolean pacPowerFading = ghost.state().isPacPowerFading();
        final boolean inKillChain = ghost.state().killChainIndex() != -1;

        final CommonSpriteAnimationID id = switch (ghost.state().enumValue()) {
            case LOCKED, LEAVING_HOUSE -> {
                if (pacHasPower) {
                    yield inKillChain ? CommonSpriteAnimationID.GHOST_NORMAL : frightenedAnim(pacPowerFading);
                }
                yield CommonSpriteAnimationID.GHOST_NORMAL;
            }
            case HUNTING_PAC -> CommonSpriteAnimationID.GHOST_NORMAL;
            case FRIGHTENED -> frightenedAnim(pacPowerFading);
            case RETURNING_HOME, ENTERING_HOUSE -> CommonSpriteAnimationID.GHOST_EYES;
            case EATEN -> CommonSpriteAnimationID.GHOST_POINTS;
        };

        ghost.animationSelection().setAnimationID(id);
        spriteAnimController.select(ghost, id);

        if (id == CommonSpriteAnimationID.GHOST_POINTS && ghost.animationSelection().disabled()) {
            // Points "animation" just displays selected image/frame
            // Animation index is 0-based, animation frame 0 shows points for *first* killed ghost...
            spriteAnimController.selectAndSetFrame(ghost, CommonSpriteAnimationID.GHOST_POINTS, ghost.state().killChainIndex());
            spriteAnimController.stopSelected(ghost);
        }

        if (ghost.animationSelection().disabled()) {
            spriteAnimController.stopSelected(ghost);
        } else {
            spriteAnimController.playSelected(ghost);
        }
    }

    public void setDisabled(Ghost ghost, boolean disabled) {
        ghost.animationSelection().setDisabled(disabled);
    }

    private CommonSpriteAnimationID frightenedAnim(boolean pacPowerFading) {
        return pacPowerFading ? CommonSpriteAnimationID.GHOST_FLASHING : CommonSpriteAnimationID.GHOST_FRIGHTENED;
    }
}
