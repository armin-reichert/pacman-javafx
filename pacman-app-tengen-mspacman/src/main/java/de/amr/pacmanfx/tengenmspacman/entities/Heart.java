/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimComp;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

import static de.amr.basics.spriteanim.SpriteAnimationAccessor.singleSpriteAnimationMap;

public class Heart extends GameEntity {

    public Heart() {
        final SpriteAnimComp animComp = new SpriteAnimComp();
        animComp.setAnimations(
            singleSpriteAnimationMap(
                TengenMsPacMan_SpriteSheet.instance().findSprite(SpriteID.HEART)));
        setComponent(SpriteAnimComp.class, animComp);
    }
}
