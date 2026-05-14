/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D.pac;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.ManagedAnimationsRegistry;
import de.amr.pacmanfx.uilib.model3D.PacManWorld3D;
import de.amr.pacmanfx.uilib.model3D.animation.HeadBangingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacChewingAnimation3D;
import de.amr.pacmanfx.uilib.model3D.animation.PacManDyingAnimation3D;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class PacMan3D extends Pac3D {

    public PacMan3D(
        ManagedAnimationsRegistry animations,
        Pac pac,
        PacConfig pacConfig)
    {
        super(animations, pac);

        setBody(createPacBody(pacConfig));
        setJaw(createBlindPacBody(pacConfig));

        animations.register(AnimationID.PAC_CHEWING, new PacChewingAnimation3D(this));
        animations.register(AnimationID.PAC_DYING,   new PacManDyingAnimation3D(this));
        animations.register(AnimationID.PAC_MOVING,  new HeadBangingAnimation3D(PacMan3D.this));

        setMovementAnimationPowerMode(false);
    }

    @Override
    public void updateMovementAnimation() {
        animations.optAnimation(Pac3D.AnimationID.PAC_MOVING, HeadBangingAnimation3D.class)
            .ifPresent(hba -> hba.update(pac));
    }

    @Override
    public void setMovementAnimationPowerMode(boolean power) {
        animations.optAnimation(Pac3D.AnimationID.PAC_MOVING, HeadBangingAnimation3D.class)
            .ifPresent(hba -> hba.setPowerMode(power));
    }


}