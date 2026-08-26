package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;

import java.util.Objects;

public class PacAnimationSystem {

    private final ActorSpriteAnimController animController;

    public PacAnimationSystem(ActorSpriteAnimController animController) {
        this.animController = animController;
    }

    public void update(Pac pac) {
        Objects.requireNonNull(pac);

        switch (pac.state().enumValue()) {
            case SLEEPING -> {
                // Female Pac just cannot shut her mouth for a second!
                final boolean male = pac.state().isMale();
                animController.select(pac, male ? CommonSpriteAnimationID.PAC_MOUTH_SHUT : CommonSpriteAnimationID.PAC_MOUTH_MOVING);
            }
            case ACTIVE -> {
                if (pac.state().isMoving()) {
                    animController.select(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                    animController.playSelected(pac);
                } else {
                    animController.stopSelected(pac);
                }
            }
            case DEAD -> {
                if (pac.animation().readyForDying()) {
                    animController.select(pac, CommonSpriteAnimationID.PAC_DYING);
                    animController.resetSelected(pac);
                    pac.animation().setReadyForDying(false);
                }
                else if (pac.animation().startDying()) {
                    animController.playSelected(pac);
                    pac.animation().setStartDying(false);
                }
            }
        }
    }

    public void stop(Pac pac) {
        animController.stopSelected(pac);
    }
}
