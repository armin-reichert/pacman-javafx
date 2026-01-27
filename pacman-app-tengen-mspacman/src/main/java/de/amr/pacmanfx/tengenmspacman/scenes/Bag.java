/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class Bag extends Actor {

    public enum AnimationID { BAG, JUNIOR }

    public static class BagAnimations extends SpriteAnimationManager<SpriteID> {

        public BagAnimations() {
            super(TengenMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            return switch (animationID) {
                case AnimationID.BAG    -> buildAnimation().singleSprite(spriteSheet.sprite(SpriteID.BLUE_BAG)).once();
                case AnimationID.JUNIOR -> buildAnimation().singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC)).once();
                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    };

    private boolean open;

    public Bag() {
        setAnimationManager(new BagAnimations());
        setOpen(false);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationManager.select(open ? AnimationID.JUNIOR : AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}