/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities.stork;

import de.amr.basics.Named;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.entities.ActorAnimationID;
import de.amr.pacmanfx.uilib.rendering.SpritesheetAnimationMap;

public class StorkSAM extends SpritesheetAnimationMap<SpriteID> {

    public StorkSAM(SpriteAnimationContainer container) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationContainer container) {
        if (animationID.equals(ActorAnimationID.STORK_FLYING)) {
            return new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.STORK))
                .frameTicks(8)
                .repeated()
                .build(container);
        }
        throw new IllegalArgumentException("Illegal animation ID: " + animationID);
    }
}
