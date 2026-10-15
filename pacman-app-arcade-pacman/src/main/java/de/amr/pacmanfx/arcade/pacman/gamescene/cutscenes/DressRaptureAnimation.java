/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.cutscenes;

import de.amr.basics.ui.spriteanim.LazySpriteAnimationManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;

class DressRaptureAnimation extends LazySpriteAnimationManager {

    public DressRaptureAnimation(SpriteAnimationSystem spriteAnimationSystem) {
        setFactory(id -> switch (id) {

            case SpriteID.RED_GHOST_STRETCHED -> new SpriteAnimationBuilder()
                .sprites(ArcadePacMan_SpriteSheet.instance().findSpriteSequence(SpriteID.RED_GHOST_STRETCHED))
                .initiallyStopped()
                .build(spriteAnimationSystem);

            default -> throw new IllegalArgumentException("Unknown animation ID: " + id);
        });
    }
}
