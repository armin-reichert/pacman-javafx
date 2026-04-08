/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;
import static java.util.Objects.requireNonNull;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    public static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationTimer timer;

        public StorkAnimations(SpriteAnimationTimer spriteAnimationTimer) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            timer = requireNonNull(spriteAnimationTimer);
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return builder(timer).sprites(spriteSheet.sprites(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork(SpriteAnimationTimer spriteAnimationTimer) {
        setAnimations(new StorkAnimations(spriteAnimationTimer));
    }
}