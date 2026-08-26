package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class PacAnimationComp implements GameEntityComp {

    private boolean readyForDying;
    private boolean startDying;

    public boolean readyForDying() {
        return readyForDying;
    }

    public void setReadyForDying(boolean readyForDying) {
        this.readyForDying = readyForDying;
    }

    public boolean startDying() {
        return startDying;
    }

    public void setStartDying(boolean startDying) {
        this.startDying = startDying;
    }

    @Override
    public void reset() {
        readyForDying = false;
    }
}
