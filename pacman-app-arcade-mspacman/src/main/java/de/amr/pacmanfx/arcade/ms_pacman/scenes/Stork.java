/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.Named;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

public class Stork extends Actor {

    public static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationSet container;

        public StorkAnimations(SpriteAnimationSet container) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            this.container = container;
        }

        @Override
        public SpriteAnimationSet container() {
            return container;
        }

        @Override
        protected SpriteAnimation createAnimation(Named animationID) {
            if (animationID.equals(ArcadeMsPacMan_AnimationID.STORK_FLYING)) {
                final SpriteAnimation animation = SpriteAnimationBuilder.builder()
                    .sprites(spriteSheet.sprites(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build();
                animation.setContainer(container);
                return animation;
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork(SpriteAnimationSet animationSet) {
        setAnimations(new StorkAnimations(animationSet));
    }
}