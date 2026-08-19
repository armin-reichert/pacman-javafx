package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.EntityComponent;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class GhostSpriteAnimationComp implements EntityComponent {

    private int pointsIndex;

    private CommonSpriteAnimationID animationID;

    public GhostSpriteAnimationComp() {
        reset();
    }

    @Override
    public void reset() {
        pointsIndex = -1;
    }

    public CommonSpriteAnimationID animationID() {
        return animationID;
    }

    public void setAnimationID(CommonSpriteAnimationID animationID) {
        this.animationID = animationID;
    }

    public int pointsIndex() {
        return pointsIndex;
    }

    public void setPointsIndex(int pointsIndex) {
        this.pointsIndex = pointsIndex;
    }
}
