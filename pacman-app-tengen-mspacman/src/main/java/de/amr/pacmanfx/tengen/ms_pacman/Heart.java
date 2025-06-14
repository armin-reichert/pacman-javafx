/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.AnimatedActor;
import de.amr.pacmanfx.uilib.animation.SingleSpriteAnimationMap;

import java.util.Optional;

/**
 * Heart symbol which appears in cur scene 1.
 */
public class Heart extends Actor implements AnimatedActor {

    private final ActorAnimationMap animationMap;

    public Heart(TengenMsPacMan_SpriteSheet spriteSheet) {
        animationMap = new SingleSpriteAnimationMap(spriteSheet.sprite(SpriteID.HEART));
    }

    @Override
    public Optional<ActorAnimationMap> animations() {
        return Optional.of(animationMap);
    }
}
