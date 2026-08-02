/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities.bag;


import de.amr.basics.Named;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpritesheetAnimationMap;

public class BagAnimationSpriteMap extends SpritesheetAnimationMap<SpriteID> {

    public BagAnimationSpriteMap(SpriteAnimationContainer container) {
        super(TengenMsPacMan_SpriteSheet.instance());
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationContainer container) {

        return switch (animationID) {
            case ActorAnimationID.BAG -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.BLUE_BAG))
                .initiallyStopped()
                .build(container);

            case ActorAnimationID.JUNIOR -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}
