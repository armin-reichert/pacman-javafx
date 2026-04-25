/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.spriteanim.SpriteAnimationID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.spriteanim.SpriteAnimationMap;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;

public class Stork extends Actor {

    private static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        public StorkAnimations() {
            super(TengenMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(SpriteAnimationID animationID) {
            if (animationID.equals(ArcadeMsPacMan_AnimationID.STORK_FLYING)) {
                return SpriteAnimationBuilder.builder()
                    .sprites(spriteSheet.sprites(STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(SpriteAnimationContainer.instance());
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