/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    private static class StorkAnimations extends SpriteAnimationManager<SpriteID> {

        public StorkAnimations() {
            super(TengenMsPacMan_SpriteSheet.INSTANCE);
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return buildAnimation().sprites(spriteSheet.sprites(STORK)).ticksPerFrame(8).repeated();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    private boolean bagReleasedFromBeak;

    public Stork() {
        setAnimationManager(new StorkAnimations());
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}