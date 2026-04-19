/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationContainer;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static java.util.Objects.requireNonNull;

public class Bag extends Actor {

    public enum AnimationID { BAG, JUNIOR }

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations() {
            super(TengenMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            return switch (animationID) {
                case AnimationID.BAG -> SpriteAnimationBuilder.builder()
                    .singleSprite(spriteSheet.sprite(SpriteID.BLUE_BAG))
                    .initiallyStopped()
                    .build();
                case AnimationID.JUNIOR -> SpriteAnimationBuilder.builder()
                    .singleSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC))
                    .initiallyStopped()
                    .build();
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