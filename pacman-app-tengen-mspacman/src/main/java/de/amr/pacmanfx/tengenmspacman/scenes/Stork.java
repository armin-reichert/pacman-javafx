/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    private static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        public StorkAnimations() {
            super(TengenMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return builder().sprites(spriteSheet.sprites(STORK)).frameTicks(8).repeated().build();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    private boolean bagReleasedFromBeak;

    public Stork() {
        setAnimations(new StorkAnimations());
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}