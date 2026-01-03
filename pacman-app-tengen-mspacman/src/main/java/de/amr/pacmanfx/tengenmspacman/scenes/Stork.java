/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    private boolean bagReleasedFromBeak;

    public Stork(TengenMsPacMan_SpriteSheet spriteSheet) {
        final var animations = new SpriteAnimationManager<>(spriteSheet) {
            @Override
            protected SpriteAnimation createAnimation(Object animationID) {
                if (animationID.equals(AnimationID.FLYING)) {
                    return SpriteAnimation.buildAnimation().sprites(spriteSheet.sprites(STORK)).ticksPerFrame(8).repeated();
                }
                throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            }
        };
        setAnimationManager(animations);
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}