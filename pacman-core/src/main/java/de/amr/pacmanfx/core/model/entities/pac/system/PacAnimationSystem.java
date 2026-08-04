package de.amr.pacmanfx.core.model.entities.pac.system;

import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.pac.Pac;

public class PacAnimationSystem {

    private final SpriteAnimSystem spriteAnimSystem;

    public PacAnimationSystem(SpriteAnimSystem spriteAnimSystem) {
        this.spriteAnimSystem = spriteAnimSystem;
    }

    public void update(Pac pac) {
        switch (pac.getPacState()) {
            case ACTIVE -> {
                if (pac.worldNavigation().info.moved) {
                    spriteAnimSystem.playSelected(pac);
                } else {
                    spriteAnimSystem.stopSelected(pac);
                }
            }
            case DEAD -> {
                if (pac.animation().readyForDying()) {
                    selectDyingAnimation(pac);
                    pac.animation().setReadyForDying(false);
                }
                else if (pac.animation().startDying()) {
                    playDyingAnimation(pac);
                    pac.animation().setStartDying(false);
                }
            }
        }
    }

    public void stop(Pac pac) {
        spriteAnimSystem.stopSelected(pac);
    }

    public void selectDyingAnimation(Pac pac) {
        spriteAnimSystem.select(pac, ActorAnimationID.PAC_DYING);
        spriteAnimSystem.resetSelected(pac);
    }

    public void playDyingAnimation(Pac pac) {
        spriteAnimSystem.playSelected(pac);
    }
}
