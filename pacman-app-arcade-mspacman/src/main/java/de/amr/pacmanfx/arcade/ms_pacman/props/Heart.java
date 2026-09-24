/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.props;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;

import static de.amr.basics.ui.spriteanim.SpriteAnimationAPI.singleSpriteAnimation;

public class Heart extends GameEntity {

    public Heart() {
        final var animationComp = new SpriteAnimationComp();
        animationComp.setSpriteAnimations(
            singleSpriteAnimation(ArcadeMsPacMan_SpriteSheet.instance().findSprite(SpriteID.HEART)));
        setComp(SpriteAnimationComp.class, animationComp);
    }
}
