package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class PacAnimationComp implements GameEntityComp {

    private Named animationID;

    private boolean stopped;

    private boolean locked;

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Named animationID() {
        return animationID;
    }

    public void setAnimationID(Named animationID) {
        this.animationID = animationID;
    }

    @Override
    public void reset() {
        stopped = false;
        locked = false;
        animationID = CommonSpriteAnimationID.PAC_MOUTH_SHUT;
    }
}
