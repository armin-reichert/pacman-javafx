/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    public Stork(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        final var animations = new SpriteAnimationManager<>(spriteSheet) {
            @Override
            protected SpriteAnimation createAnimation(Object animationID) {
                if (animationID.equals(AnimationID.FLYING)) {
                    return SpriteAnimation.buildAnimation()
                        .sprites(spriteSheet.sprites(SpriteID.STORK))
                        .ticksPerFrame(8)
                        .repeated();
                }
                throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            }
        };
        setAnimationManager(animations);
    }
}