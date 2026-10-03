/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.cutscenes;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;

public class NailDressRapturing extends GameEntity {

    public NailDressRapturing(SpriteAnimationContainer animContainer) {
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());

        assertComponent(SpriteAnimationComp.class).setSpriteAnimations(new DressRaptureAnimation(animContainer));
        setState(NailDressRapturingState.NAIL);
    }

    public void setState(NailDressRapturingState state) {
        final int frame = state.ordinal();
        assertComponent(SpriteAnimationComp.class).spriteAnimations().setAnimationFrame(SpriteID.RED_GHOST_STRETCHED, frame);
    }
}
