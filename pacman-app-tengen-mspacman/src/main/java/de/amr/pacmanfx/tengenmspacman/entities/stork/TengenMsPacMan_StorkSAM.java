/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.stork;


import de.amr.basics.Named;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.spriteanim.LazySAM;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationBuilder;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

import static de.amr.pacmanfx.tengenmspacman.sprites.SpriteID.STORK;

public class TengenMsPacMan_StorkSAM extends LazySAM {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_StorkSAM(SpriteAnimContainer container) {
        setFactory(id -> createAnimation(id, container));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimContainer container) {
        if (animationID.equals(CommonSpriteAnimationID.STORK_FLYING)) {
            return new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(STORK))
                .frameTicks(8)
                .repeated()
                .build(container);
        }
        throw new IllegalArgumentException("Illegal animation ID: " + animationID);
    }
}
