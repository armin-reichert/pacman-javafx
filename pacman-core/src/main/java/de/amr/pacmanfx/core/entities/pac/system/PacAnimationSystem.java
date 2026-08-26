package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;

public class PacAnimationSystem {

    private final ActorSpriteAnimController spriteAnimSystem;

    public PacAnimationSystem(ActorSpriteAnimController spriteAnimSystem) {
        this.spriteAnimSystem = spriteAnimSystem;
    }

    public void update(Pac pac) {
        switch (pac.getPacState()) {
            case SLEEPING -> {
                // Female Pac just cannot shut her mouth for a second!
                final boolean male = pac.state().isMale();
                spriteAnimSystem.select(pac, male ? CommonSpriteAnimationID.PAC_MOUTH_SHUT : CommonSpriteAnimationID.PAC_MOUTH_MOVING);
            }
            case ACTIVE -> {
                if (pac.state().isMoving()) {
                    spriteAnimSystem.select(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                    spriteAnimSystem.playSelected(pac);
                } else {
                    spriteAnimSystem.stopSelected(pac);
                }
            }
            case DEAD -> {
                if (pac.animation().readyForDying()) {
                    spriteAnimSystem.select(pac, CommonSpriteAnimationID.PAC_DYING);
                    spriteAnimSystem.resetSelected(pac);
                    pac.animation().setReadyForDying(false);
                }
                else if (pac.animation().startDying()) {
                    spriteAnimSystem.playSelected(pac);
                    pac.animation().setStartDying(false);
                }
            }
        }
    }

    public void stop(Pac pac) {
        spriteAnimSystem.stopSelected(pac);
    }
}
