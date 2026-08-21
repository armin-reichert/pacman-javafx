package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.pacmanfx.core.ecs.systems.SpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;

public class PacAnimationSystem {

    private final SpriteAnimController spriteAnimSystem;

    public PacAnimationSystem(SpriteAnimController spriteAnimSystem) {
        this.spriteAnimSystem = spriteAnimSystem;
    }

    public void update(Pac pac) {
        switch (pac.getPacState()) {
            case ALIVE -> {
                if (pac.state().isMoving()) {
                    spriteAnimSystem.select(pac, CommonSpriteAnimationID.PAC_MUNCHING);
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
