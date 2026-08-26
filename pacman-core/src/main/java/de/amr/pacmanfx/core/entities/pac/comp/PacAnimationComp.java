package de.amr.pacmanfx.core.entities.pac.comp;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.ecs.GameEntityComp;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class PacAnimationComp implements GameEntityComp {

    private Named animationID;

    private boolean disabled;

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Named animationID() {
        return animationID;
    }

    public void setAnimationID(Named animationID) {
        this.animationID = animationID;
    }

    @Override
    public void reset() {
        disabled = false;
        animationID = CommonSpriteAnimationID.PAC_MOUTH_SHUT;
    }
}
