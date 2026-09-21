/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props.stork;

import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.LazySAM;
import de.amr.basics.ui.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;

public class ArcadeMsPacMan_StorkSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_StorkSAM(SpriteAnimationContainer container) {
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
