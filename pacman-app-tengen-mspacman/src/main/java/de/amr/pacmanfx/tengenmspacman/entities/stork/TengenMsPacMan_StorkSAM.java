/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.stork;


import de.amr.basics.Named;
import de.amr.basics.ui.spriteanim.*;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

import static de.amr.pacmanfx.tengenmspacman.sprites.SpriteID.STORK;

public class TengenMsPacMan_StorkSAM extends LazySAM {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();

    public TengenMsPacMan_StorkSAM(SpriteAnimationSystem spriteAnimationSystem) {
        setFactory(id -> createAnimation(id, spriteAnimationSystem));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationSystem spriteAnimationSystem) {
        if (animationID.equals(CommonSpriteAnimationID.STORK_FLYING)) {
            return new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(STORK))
                .frameTicks(8)
                .repeated()
                .build(spriteAnimationSystem);
        }
        throw new IllegalArgumentException("Illegal animation ID: " + animationID);
    }
}
