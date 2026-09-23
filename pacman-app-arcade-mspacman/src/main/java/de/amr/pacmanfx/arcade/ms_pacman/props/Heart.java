/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.rendering.Renderable;

import static de.amr.basics.ui.spriteanim.SpriteAnimFacade.singleSpriteAnimationFacade;

public class Heart extends GameEntity implements Renderable {

    public Heart() {
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
        reqComp(SpriteAnimationComp.class).setSpriteAnimations(
            singleSpriteAnimationFacade(ArcadeMsPacMan_SpriteSheet.instance().findSprite(SpriteID.HEART)));
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.PROPS;
    }
}
