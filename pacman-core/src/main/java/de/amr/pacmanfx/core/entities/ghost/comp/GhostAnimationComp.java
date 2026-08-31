package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class GhostAnimationComp implements GameEntityComp {

    private Named animationID;

    private boolean stopped;

    private boolean locked;

    private int frame;

    public GhostAnimationComp() {
        reset();
    }

    @Override
    public void reset() {
        frame = -1;
    }

    public void setAnimationFrame(int frame) {
        this.frame = frame;
    }

    public Named animationID() {
        return animationID;
    }

    public void setAnimationID(Named animationID) {
        this.animationID = animationID;
    }

    public int frame() {
        return frame;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
