package de.amr.pacmanfx.core.ecs.comp;

import de.amr.pacmanfx.core.spriteanim.SpriteAnimFacade;
import de.amr.pacmanfx.core.ecs.EntityComponent;

public class SpriteAnimationComp implements EntityComponent {

    private SpriteAnimFacade facade = SpriteAnimFacade.EMPTY_SPRITE_ANIMATION_FACADE;

    public void setSpriteAnimations(SpriteAnimFacade facade) {
        this.facade = facade;
    }

    public SpriteAnimFacade spriteAnimations() {
        return facade;
    }
}
