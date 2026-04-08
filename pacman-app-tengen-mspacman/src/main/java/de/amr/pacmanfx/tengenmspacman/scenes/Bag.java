/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;

public class Bag extends Actor {

    public enum AnimationID { BAG, JUNIOR }

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations() {
            super(TengenMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            return switch (animationID) {
                case AnimationID.BAG    -> builder().singleSprite(spriteSheet.sprite(SpriteID.BLUE_BAG)).stopped().build();
                case AnimationID.JUNIOR -> builder().singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC)).stopped().build();
                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag() {
        setAnimations(new BagAnimations());
        setOpen(false);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animations.selectAnimation(open ? AnimationID.JUNIOR : AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}