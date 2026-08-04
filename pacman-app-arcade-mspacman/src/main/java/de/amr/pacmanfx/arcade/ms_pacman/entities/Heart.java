/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.entities;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimComp;

import static de.amr.basics.spriteanim.SpriteAnimationAccessor.singleSpriteAnimationMap;

public class Heart extends GameEntity {

    public Heart() {
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());
        requireComponent(SpriteAnimComp.class).setAnimations(
            singleSpriteAnimationMap(ArcadeMsPacMan_SpriteSheet.instance().findSprite(SpriteID.HEART)));
    }
}
