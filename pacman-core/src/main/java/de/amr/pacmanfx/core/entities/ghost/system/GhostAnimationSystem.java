/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostAnimationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;

import static java.util.Objects.requireNonNull;

public class GhostAnimationSystem {

    private final ActorSpriteAnimController spriteAnimController;

    public GhostAnimationSystem(ActorSpriteAnimController spriteAnimController) {
        this.spriteAnimController = requireNonNull(spriteAnimController);
    }

    public void update(Ghost ghost) {
        requireNonNull(ghost);

        final GhostStateComp state = ghost.state();
        final GhostAnimationComp animation = ghost.animation();

        if (animation.isLocked()) {
            return;
        }

        final boolean pacHasPower    = state.hasPacPower();
        final boolean pacPowerFading = state.isPacPowerFading();
        final boolean inKillChain    = state.killChainIndex() != -1;

        final Named animationID = switch (state.enumValue()) {
            case LOCKED, LEAVING_HOUSE -> pacHasPower && !inKillChain
                ? frightenedAnimID(pacPowerFading)
                : CommonSpriteAnimationID.GHOST_NORMAL;
            case HUNTING_PAC -> CommonSpriteAnimationID.GHOST_NORMAL;
            case FRIGHTENED -> frightenedAnimID(pacPowerFading);
            case EATEN, RETURNING_HOME, ENTERING_HOUSE -> CommonSpriteAnimationID.GHOST_EYES;
        };

        animation.setAnimationID(animationID);

        spriteAnimController.select(ghost, animationID);
        if (ghost.animation().isStopped()) {
            spriteAnimController.stopSelected(ghost);
        } else {
            spriteAnimController.playSelected(ghost);
        }
    }

    private Named frightenedAnimID(boolean pacPowerFading) {
        return pacPowerFading ? CommonSpriteAnimationID.GHOST_FLASHING : CommonSpriteAnimationID.GHOST_FRIGHTENED;
    }
}
