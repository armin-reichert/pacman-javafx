/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationTimer;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;
import static java.util.Objects.requireNonNull;

public class Bag extends Actor {

    public enum AnimationID { BAG, JUNIOR }

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationTimer timer;

        public BagAnimations(SpriteAnimationTimer spriteAnimationTimer) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            this.timer = requireNonNull(spriteAnimationTimer);
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            return switch (animationID) {
                case AnimationID.JUNIOR -> SpriteAnimation.builder(timer).singleSprite(spriteSheet.sprite(JUNIOR_PAC)).stopped().build();
                case AnimationID.BAG    -> SpriteAnimation.builder(timer).singleSprite(spriteSheet.sprite(BLUE_BAG)).stopped().build();
                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag(SpriteAnimationTimer spriteAnimationTimer) {
        setAnimations(new BagAnimations(spriteAnimationTimer));
    }

    public void setOpen(boolean open) {
        this.open = open;
        animations.selectAnimation(open ? AnimationID.JUNIOR : AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
