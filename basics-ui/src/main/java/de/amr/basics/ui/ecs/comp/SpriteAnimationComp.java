/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.ecs.comp;

import de.amr.basics.ui.spriteanim.SpriteAnimationManager;

public class SpriteAnimationComp {

    public SpriteAnimationComp() {}

    private SpriteAnimationManager facade = SpriteAnimationManager.EMPTY;

    public void setSpriteAnimations(SpriteAnimationManager facade) {
        this.facade = facade;
    }

    public SpriteAnimationManager spriteAnimations() {
        return facade;
    }
}
