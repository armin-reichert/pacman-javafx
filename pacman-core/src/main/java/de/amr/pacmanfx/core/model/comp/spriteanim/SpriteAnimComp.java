package de.amr.pacmanfx.core.model.comp.spriteanim;

import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.GameEntityComponent;

public class SpriteAnimComp implements GameEntityComponent {

    private SpriteAnimationAccess delegate = SpriteAnimationAccess.emptyAnimation();

    public void setAnimations(SpriteAnimationAccess delegate) {
        this.delegate = delegate;
    }

    public SpriteAnimationAccess delegate() {
        return delegate;
    }

    @Override
    public void reset() {}

}
