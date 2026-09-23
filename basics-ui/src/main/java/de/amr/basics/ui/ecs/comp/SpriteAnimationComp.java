/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.ecs.comp;

import de.amr.basics.ecs.GameEntityComp;
import de.amr.basics.ui.spriteanim.SpriteAnimFacade;

public class SpriteAnimationComp implements GameEntityComp {

    public SpriteAnimationComp() {}

    private SpriteAnimFacade facade = SpriteAnimFacade.EMPTY_SPRITE_ANIMATION_FACADE;

    public void setSpriteAnimations(SpriteAnimFacade facade) {
        this.facade = facade;
    }

    public SpriteAnimFacade spriteAnimations() {
        return facade;
    }
}
