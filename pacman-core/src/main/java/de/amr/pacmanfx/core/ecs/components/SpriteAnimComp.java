package de.amr.pacmanfx.core.ecs.components;

import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;

public class SpriteAnimComp implements GameEntityComponent {

    private SpriteAnimationAccess animation = SpriteAnimationAccess.emptyAnimation();

    public void setAnimations(SpriteAnimationAccess delegate) {
        this.animation = delegate;
    }

    public SpriteAnimationAccess animation() {
        return animation;
    }

    @Override
    public void reset() {}
}
