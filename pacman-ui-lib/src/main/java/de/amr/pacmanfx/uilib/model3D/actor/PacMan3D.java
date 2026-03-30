/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.actor;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.model3D.Models3D;
import de.amr.pacmanfx.uilib.model3D.animation.HeadBangingAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.Pac3DChewingAnimation;
import de.amr.pacmanfx.uilib.model3D.animation.PacMan3DDyingAnimation;

public class PacMan3D extends Pac3D {

    public PacMan3D(AnimationRegistry animations, Pac pac, PacConfig pacConfig) {
        super(animations, pac);

        setBody(Models3D.PAC_MAN_MODEL.createPacBody(pacConfig));
        setJaw(Models3D.PAC_MAN_MODEL.createBlindPacBody(pacConfig));

        animations.register(AnimationID.PAC_CHEWING, new Pac3DChewingAnimation(this));
        animations.register(AnimationID.PAC_DYING,   new PacMan3DDyingAnimation(this));
        animations.register(AnimationID.PAC_MOVING,  new HeadBangingAnimation(PacMan3D.this));

        setMovementAnimationPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        animations.optAnimation(Pac3D.AnimationID.PAC_MOVING, HeadBangingAnimation.class)
            .ifPresent(hba -> hba.update(pac));
    }

    @Override
    public void setMovementAnimationPowerMode(boolean power) {
        animations.optAnimation(Pac3D.AnimationID.PAC_MOVING, HeadBangingAnimation.class)
            .ifPresent(hba -> hba.setPowerMode(power));
    }
}