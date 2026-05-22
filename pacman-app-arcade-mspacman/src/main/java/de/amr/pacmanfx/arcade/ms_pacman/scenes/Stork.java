/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimator;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationContainer;

public class Stork extends Actor {

    public static class StorkAnimations extends SpriteAnimationContainer<SpriteID> {

        private final SpriteAnimator spriteAnimator;

        public StorkAnimations(SpriteAnimator spriteAnimator) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            this.spriteAnimator = spriteAnimator;
        }

        @Override
        protected SpriteAnimation createAnimation(AnimationIdentifier animationID) {
            if (animationID.equals(ArcadeMsPacMan_AnimationID.STORK_FLYING)) {
                return SpriteAnimationBuilder.builder()
                    .sprites(spriteSheet.sprites(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(spriteAnimator);
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork(SpriteAnimator spriteAnimator) {
        setAnimationManager(new StorkAnimations(spriteAnimator));
    }
}