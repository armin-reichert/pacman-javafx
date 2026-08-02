/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.stork;


import de.amr.basics.Named;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpritesheetAnimationMap;

import static de.amr.pacmanfx.tengenmspacman.sprites.SpriteID.STORK;

public class StorkSpriteAnimationMap extends SpritesheetAnimationMap<SpriteID> {

    public StorkSpriteAnimationMap(SpriteAnimationContainer container) {
        super(TengenMsPacMan_SpriteSheet.instance());
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationContainer container) {
        if (animationID.equals(ActorAnimationID.STORK_FLYING)) {
            return new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(STORK))
                .frameTicks(8)
                .repeated()
                .build(container);
        }
        throw new IllegalArgumentException("Illegal animation ID: " + animationID);
    }
}
