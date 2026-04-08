/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;
import static java.util.Objects.requireNonNull;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    private static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationTimer timer;

        public StorkAnimations(SpriteAnimationTimer spriteAnimationTimer) {
            super(TengenMsPacMan_SpriteSheet.instance());
            timer = requireNonNull(spriteAnimationTimer);
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return builder(timer).sprites(spriteSheet.sprites(STORK)).frameTicks(8).repeated().build();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    private boolean bagReleasedFromBeak;

    public Stork(SpriteAnimationTimer spriteAnimationTimer) {
        setAnimations(new StorkAnimations(spriteAnimationTimer));
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}