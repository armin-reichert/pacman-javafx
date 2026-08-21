/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

import static de.amr.basics.spriteanim.SpriteAnimFacade.singleSpriteAnimationFacade;

public class Heart extends GameEntity {

    public Heart() {
        final SpriteAnimationComp animationComp = new SpriteAnimationComp();
        animationComp.setSpriteAnimations(
            singleSpriteAnimationFacade(TengenMsPacMan_SpriteSheet.instance().findSprite(SpriteID.HEART))
        );
        setComp(SpriteAnimationComp.class, animationComp);
    }
}
