package de.amr.pacmanfx.core.ecs.comp;

import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.pacmanfx.core.ecs.EntityComponent;

public class SpriteAnimationComp implements EntityComponent {

    private SpriteAnimationAccessor animation = SpriteAnimationAccessor.emptyAnimation();

    public void setAnimations(SpriteAnimationAccessor delegate) {
        this.animation = delegate;
    }

    public SpriteAnimationAccessor animation() {
        return animation;
    }
}
