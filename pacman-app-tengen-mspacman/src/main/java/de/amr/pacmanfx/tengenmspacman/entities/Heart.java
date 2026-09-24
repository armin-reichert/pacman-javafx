/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.entities;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;

import static de.amr.basics.ui.spriteanim.SpriteAnimationAPI.singleSpriteAnimation;

public class Heart extends GameEntity implements Renderable {

    public Heart() {
        final SpriteAnimationComp animationComp = new SpriteAnimationComp();
        animationComp.setSpriteAnimations(
            singleSpriteAnimation(TengenMsPacMan_SpriteSheet.instance().findSprite(SpriteID.HEART))
        );
        setComp(SpriteAnimationComp.class, animationComp);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }
}
