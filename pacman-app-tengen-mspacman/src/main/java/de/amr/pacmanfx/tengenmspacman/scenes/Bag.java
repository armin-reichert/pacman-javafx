/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.AnimationIdentifier;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimator;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationContainer;

public class Bag extends Actor {

    public static class BagAnimations extends SpriteAnimationContainer<SpriteID> {

        private final SpriteAnimator animator;

        public BagAnimations(SpriteAnimator animator) {
            super(TengenMsPacMan_SpriteSheet.instance());
            this.animator = animator;
        }

        @Override
        protected SpriteAnimation createAnimation(AnimationIdentifier animationID) {
            return switch (animationID) {
                case ArcadeMsPacMan_AnimationID.BAG -> SpriteAnimationBuilder.builder()
                    .singleSprite(spriteSheet.sprite(SpriteID.BLUE_BAG))
                    .initiallyStopped()
                    .build(animator);

                case ArcadeMsPacMan_AnimationID.JUNIOR -> SpriteAnimationBuilder.builder()
                    .singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC))
                    .initiallyStopped()
                    .build(animator);

                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag(SpriteAnimator spriteAnimator) {
        setAnimationManager(new BagAnimations(spriteAnimator));
        setOpen(false);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationManager.select(open ? ArcadeMsPacMan_AnimationID.JUNIOR : ArcadeMsPacMan_AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}