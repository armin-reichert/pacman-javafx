package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class GhostAnimationSelectionComp implements GameEntityComp {

    private CommonSpriteAnimationID animationID;

    private int frame;

    public GhostAnimationSelectionComp() {
        reset();
    }

    @Override
    public void reset() {
        frame = -1;
    }

    public void select(CommonSpriteAnimationID id, int frame) {
        this.animationID = id;
        this.frame = frame;
    }

    public CommonSpriteAnimationID animationID() {
        return animationID;
    }

    public void setAnimationID(CommonSpriteAnimationID animationID) {
        this.animationID = animationID;
    }

    public int frame() {
        return frame;
    }
}
