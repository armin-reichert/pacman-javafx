package de.amr.pacmanfx.core.model.systems.pac;

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
}
