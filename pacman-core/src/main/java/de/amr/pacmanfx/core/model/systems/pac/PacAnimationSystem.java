package de.amr.pacmanfx.core.model.systems.pac;

import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.Pac;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;

public class PacAnimationSystem {
    private final SpriteAnimSystem spriteAnimSystem;

    public PacAnimationSystem(SpriteAnimSystem spriteAnimSystem) {
        this.spriteAnimSystem = spriteAnimSystem;
    }

    public void update(Pac pac) {
        switch (pac.state()) {
            case ACTIVE -> {
                if (pac.worldNavigation().info.moved) {
                    spriteAnimSystem.playSelected(pac);
                } else {
                    spriteAnimSystem.stopSelected(pac);
                }
            }
            case DEAD -> {

            }
        }
    }

    public void selectDyingAnimation(Pac pac) {
        spriteAnimSystem.select(pac, ActorAnimationID.PAC_DYING);
        spriteAnimSystem.resetSelected(pac);
    }

    public void playDyingAnimation(Pac pac) {
        spriteAnimSystem.playSelected(pac);
    }
}
