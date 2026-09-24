/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.ecs.comp;

import de.amr.basics.ecs.GameEntityComp;
import de.amr.basics.ui.spriteanim.SpriteAnimationAPI;

public class SpriteAnimationComp implements GameEntityComp {

    public SpriteAnimationComp() {}

    private SpriteAnimationAPI facade = SpriteAnimationAPI.EMPTY_SPRITE_ANIMATION;

    public void setSpriteAnimations(SpriteAnimationAPI facade) {
        this.facade = facade;
    }

    public SpriteAnimationAPI spriteAnimations() {
        return facade;
    }
}
