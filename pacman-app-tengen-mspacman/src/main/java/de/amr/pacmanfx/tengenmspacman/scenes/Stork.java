/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationContainer;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;

public class Stork extends Actor {

    private static class StorkAnimations extends SpriteAnimationContainer<SpriteID> {

        private final SpriteAnimationSet animationSet;

        public StorkAnimations(SpriteAnimationSet animationSet) {
            super(TengenMsPacMan_SpriteSheet.instance());
            this.animationSet = animationSet;
        }

        @Override
        protected SpriteAnimation createAnimation(AnimationIdentifier animationID) {
            if (animationID.equals(ArcadeMsPacMan_AnimationID.STORK_FLYING)) {
                return SpriteAnimationBuilder.builder()
                    .sprites(spriteSheet.sprites(STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(animationSet);
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    private boolean bagReleasedFromBeak;

    public Stork(SpriteAnimationSet animationSet) {
        setAnimationManager(new StorkAnimations(animationSet));
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}