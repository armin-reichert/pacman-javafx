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

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class Bag extends Actor {

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        private final SpriteAnimationSet container;

        public BagAnimations(SpriteAnimationSet container) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            this.container = container;
        }

        @Override
        public SpriteAnimationSet container() {
            return container;
        }

        @Override
        protected SpriteAnimation createAnimation(Named animationID) {
            final SpriteAnimation animation = switch (animationID) {
                case ArcadeMsPacMan_AnimationID.JUNIOR ->
                    SpriteAnimationBuilder.builder()
                        .singleSprite(spriteSheet.sprite(JUNIOR_PAC))
                        .initiallyStopped()
                        .build();

                case ArcadeMsPacMan_AnimationID.BAG ->
                    SpriteAnimationBuilder.builder()
                        .singleSprite(spriteSheet.sprite(BLUE_BAG))
                        .initiallyStopped()
                        .build();

                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
            animation.setContainer(container);
            return animation;
        }
    }

    private boolean open;

    public Bag(SpriteAnimationSet container) {
        setAnimations(new BagAnimations(container));
    }

    public void setOpen(boolean open) {
        this.open = open;
        animations.select(open ? ArcadeMsPacMan_AnimationID.JUNIOR : ArcadeMsPacMan_AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
