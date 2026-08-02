/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities;


import de.amr.basics.Naming;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class BagSpriteAnimationMap extends SpriteAnimationMap<SpriteID> {

    public BagSpriteAnimationMap(SpriteAnimationContainer container) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Naming animationID, SpriteAnimationContainer container) {
        return switch (animationID) {
            case ActorAnimationID.JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(JUNIOR_PAC))
                .initiallyStopped()
                .build(container);

            case ActorAnimationID.BAG -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(BLUE_BAG))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}
