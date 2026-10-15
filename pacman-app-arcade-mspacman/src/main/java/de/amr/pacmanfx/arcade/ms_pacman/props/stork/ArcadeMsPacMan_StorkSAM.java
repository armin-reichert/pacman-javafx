/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props.stork;

import de.amr.basics.ui.spriteanim.*;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;

public class ArcadeMsPacMan_StorkSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_StorkSAM(SpriteAnimationSystem spriteAnimationSystem) {
        setFactory(id -> {
            if (id == CommonSpriteAnimationID.STORK_FLYING) {
                return new SpriteAnimationBuilder()
                    .sprites(spriteSheet.findSpriteSequence(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(spriteAnimationSystem);
            }
            throw new IllegalArgumentException("Illegal animation ID: " + id);
        });
    }
}
