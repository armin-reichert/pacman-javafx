package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimFacade;

public class SpriteAnimationComp implements GameEntityComp {

    private SpriteAnimFacade facade = SpriteAnimFacade.EMPTY_SPRITE_ANIMATION_FACADE;

    public void setSpriteAnimations(SpriteAnimFacade facade) {
        this.facade = facade;
    }

    public SpriteAnimFacade spriteAnimations() {
        return facade;
    }
}
