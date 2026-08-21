package de.amr.pacmanfx.core.ecs.comp;

import de.amr.basics.spriteanim.SpriteAnimationFacade;
import de.amr.pacmanfx.core.ecs.EntityComponent;

public class SpriteAnimationComp implements EntityComponent {

    private SpriteAnimationFacade facade = SpriteAnimationFacade.EMPTY_SPRITE_ANIMATION_FACADE;

    public void setSpriteAnimations(SpriteAnimationFacade facade) {
        this.facade = facade;
    }

    public SpriteAnimationFacade spriteAnimations() {
        return facade;
    }
}
