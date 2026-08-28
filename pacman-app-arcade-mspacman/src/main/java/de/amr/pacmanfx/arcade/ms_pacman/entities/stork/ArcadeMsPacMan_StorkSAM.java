/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.stork;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.spriteanim.LazySAM;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationBuilder;

public class ArcadeMsPacMan_StorkSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_StorkSAM(SpriteAnimContainer container) {
        setFactory(id -> {
            if (id == CommonSpriteAnimationID.STORK_FLYING) {
                return new SpriteAnimationBuilder()
                    .sprites(spriteSheet.findSpriteSequence(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(container);
            }
            throw new IllegalArgumentException("Illegal animation ID: " + id);
        });
    }
}
