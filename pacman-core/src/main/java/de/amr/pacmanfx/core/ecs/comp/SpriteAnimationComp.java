package de.amr.pacmanfx.core.ecs.comp;

import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class SpriteAnimationComp implements GameEntityComponent {

    private SpriteAnimationAccessor animation = SpriteAnimationAccessor.emptyAnimation();

    public void setAnimations(SpriteAnimationAccessor delegate) {
        this.animation = delegate;
    }

    public SpriteAnimationAccessor animation() {
        return animation;
    }

    @Override
    public void reset() {}
}
