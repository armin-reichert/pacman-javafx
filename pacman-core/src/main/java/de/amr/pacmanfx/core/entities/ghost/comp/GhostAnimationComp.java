package de.amr.pacmanfx.core.entities.ghost.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;
import de.amr.pacmanfx.core.entities.ActorAnimationID;

public class GhostAnimationComp implements GameEntityComponent {

    private ActorAnimationID animationID;

    public ActorAnimationID ghostAnimationID() {
        return animationID;
    }

    public void setAnimationID(ActorAnimationID animationID) {
        this.animationID = animationID;
    }

    @Override
    public void reset() {
    }
}
