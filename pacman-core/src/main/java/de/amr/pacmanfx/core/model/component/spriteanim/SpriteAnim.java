package de.amr.pacmanfx.core.model.component.spriteanim;

import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.component.EntityComponent;

public class SpriteAnim implements EntityComponent {

    private SpriteAnimationAccess animations = SpriteAnimationAccess.emptyAnimation();

    @Override
    public void reset() {}

    public SpriteAnimationAccess animations() {
        return animations;
    }

    public void setAnimations(SpriteAnimationAccess animations) {
        this.animations = animations;
    }
}
