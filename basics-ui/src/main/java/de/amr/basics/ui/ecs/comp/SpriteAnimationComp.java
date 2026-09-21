package de.amr.basics.ui.ecs.comp;

import de.amr.basics.ui.ecs.GameEntityComp;
import de.amr.basics.ui.spriteanim.SpriteAnimFacade;

public class SpriteAnimationComp implements GameEntityComp {

    private SpriteAnimFacade facade = SpriteAnimFacade.EMPTY_SPRITE_ANIMATION_FACADE;

    public void setSpriteAnimations(SpriteAnimFacade facade) {
        this.facade = facade;
    }

    public SpriteAnimFacade spriteAnimations() {
        return facade;
    }
}
