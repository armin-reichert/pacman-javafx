/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationDriver;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    public static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationDriver manager;

        public StorkAnimations(SpriteAnimationDriver spriteAnimationDriver) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            manager = requireNonNull(spriteAnimationDriver);
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return SpriteAnimationBuilder.builder(manager)
                    .sprites(spriteSheet.sprites(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork(SpriteAnimationDriver spriteAnimationDriver) {
        setAnimations(new StorkAnimations(spriteAnimationDriver));
    }
}