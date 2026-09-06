/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.pac.system;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacAnimationComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.entities.pac.system.*;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.tengenmspacman.entities.pac.comp.PacBoosterComp;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_PacAnimationSystem extends PacAnimationSystem {

    public TengenMsPacMan_PacAnimationSystem(ActorSpriteAnimController animController) {
        super(animController);
    }

    @Override
    public void update(Pac pac, GameRules rules) {
        requireNonNull(pac);

        final PacStateComp state = pac.state();
        final PacAnimationComp animation = pac.animation();

        if (animation.isLocked()) {
            return;
        }

        switch (state.enumValue()) {
            case SLEEPING -> {
                final boolean boosterEnabled = pac.reqComp(PacBoosterComp.class).boosterEnabled();
                if (boosterEnabled) {
                    animation.setAnimationID(TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER);
                } else {
                    animation.setAnimationID(rules.initialPacAnimationID());
                }
                animation.setStopped(true);
            }
            case ACTIVE -> {
                final boolean boosterEnabled = pac.reqComp(PacBoosterComp.class).boosterEnabled();
                if (boosterEnabled) {
                    animation.setAnimationID(TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER);
                } else {
                    animation.setAnimationID(CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                }
                animation.setStopped(!pac.worldNavigation().info().moved);
            }
        }

        animController.select(pac, animation.animationID());
        if (animation.isStopped()) {
            animController.stopSelected(pac);
        } else {
            animController.playSelected(pac);
        }
    }
}
