package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;

import java.util.Objects;

public class PacAnimationSystem {

    public PacAnimationSystem() {}

    public void setDisabled(Pac pac, boolean disabled) {
        pac.animation().setDisabled(disabled);
    }

    public void update(Pac pac) {
        Objects.requireNonNull(pac);

        switch (pac.state().enumValue()) {
            case SLEEPING -> {
                // Female Pac just cannot shut her mouth for a second!
                final boolean male = pac.state().isMale();
                pac.animation().setAnimationID(male
                    ? CommonSpriteAnimationID.PAC_MOUTH_SHUT
                    : CommonSpriteAnimationID.PAC_MOUTH_MOVING);
            }
            case ACTIVE -> {
                pac.animation().setAnimationID(CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                pac.animation().setDisabled(!pac.state().isMoving());
            }
            case DEAD -> {
                pac.animation().setAnimationID(CommonSpriteAnimationID.PAC_DYING);
            }
        }
    }
}
