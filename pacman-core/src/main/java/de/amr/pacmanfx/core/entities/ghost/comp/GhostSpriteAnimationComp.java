package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class GhostSpriteAnimationComp implements GameEntityComponent {

    private CommonSpriteAnimationID animationID;

    public CommonSpriteAnimationID ghostAnimationID() {
        return animationID;
    }

    public void setAnimationID(CommonSpriteAnimationID animationID) {
        this.animationID = animationID;
    }

    @Override
    public void reset() {
    }
}
