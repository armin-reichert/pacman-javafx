package de.amr.pacmanfx.core.ecs.comp;

import de.amr.basics.spriteanim.SpriteAnimationFacade;
import de.amr.pacmanfx.core.ecs.EntityComponent;

public class SpriteAnimationComp implements EntityComponent {

    private SpriteAnimationFacade animation = SpriteAnimationFacade.emptyAnimation();

    public void setAnimations(SpriteAnimationFacade delegate) {
        this.animation = delegate;
    }

    public SpriteAnimationFacade animation() {
        return animation;
    }
}
