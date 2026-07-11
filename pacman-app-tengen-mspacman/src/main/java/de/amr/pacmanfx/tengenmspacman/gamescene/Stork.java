/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.Identifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.tengenmspacman.rendering.SpriteID.STORK;

public class Stork extends Actor {

    private static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationContainer container;

        public StorkAnimations(SpriteAnimationContainer container) {
            super(TengenMsPacMan_SpriteSheet.instance());
            this.container = container;
        }

        @Override
        public SpriteAnimationContainer container() {
            return container;
        }

        @Override
        protected SpriteAnimation createAnimation(Identifier animationID) {
            if (animationID.equals(ArcadeMsPacMan_AnimationID.STORK_FLYING)) {
                return new SpriteAnimationBuilder()
                    .sprites(spriteSheet.sprites(STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(container);
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    private boolean bagReleasedFromBeak;

    public Stork(SpriteAnimationContainer container) {
        setAnimations(new StorkAnimations(container));
    }

    public void setBagReleasedFromBeak(boolean released) {
        bagReleasedFromBeak = released;
    }

    public boolean isBagReleasedFromBeak() {
        return bagReleasedFromBeak;
    }
}