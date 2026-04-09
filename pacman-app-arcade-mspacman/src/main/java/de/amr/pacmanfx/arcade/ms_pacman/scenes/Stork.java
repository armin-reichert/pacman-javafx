/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;
import static java.util.Objects.requireNonNull;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    public static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationManager manager;

        public StorkAnimations(SpriteAnimationManager spriteAnimationManager) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            manager = requireNonNull(spriteAnimationManager);
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return builder(manager).sprites(spriteSheet.sprites(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork(SpriteAnimationManager spriteAnimationManager) {
        setAnimations(new StorkAnimations(spriteAnimationManager));
    }
}