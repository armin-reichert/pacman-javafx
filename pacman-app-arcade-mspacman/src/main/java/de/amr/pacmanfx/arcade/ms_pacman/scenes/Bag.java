/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;
import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.builder;

public class Bag extends Actor {

    public enum AnimationID { BAG, JUNIOR }

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations() {
            super(ArcadeMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            return switch (animationID) {
                case AnimationID.JUNIOR -> builder().singleSprite(spriteSheet.sprite(JUNIOR_PAC)).stopped().build();
                case AnimationID.BAG    -> builder().singleSprite(spriteSheet.sprite(BLUE_BAG)).stopped().build();
                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag() {
        setAnimations(new BagAnimations());
    }

    public void setOpen(boolean open) {
        this.open = open;
        animations.selectAnimation(open ? AnimationID.JUNIOR : AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
