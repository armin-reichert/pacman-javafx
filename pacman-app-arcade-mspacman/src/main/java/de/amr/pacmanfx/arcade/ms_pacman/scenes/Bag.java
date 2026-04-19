/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.uilib.spriteanim.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class Bag extends Actor {

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations() {
            super(ArcadeMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            return switch (animationID) {
                case ArcadeMsPacMan_AnimationID.JUNIOR ->
                    SpriteAnimationBuilder.builder()
                        .singleSprite(spriteSheet.sprite(JUNIOR_PAC))
                        .initiallyStopped()
                        .build(SpriteAnimationContainer.instance());
                case ArcadeMsPacMan_AnimationID.BAG ->
                    SpriteAnimationBuilder.builder()
                        .singleSprite(spriteSheet.sprite(BLUE_BAG))
                        .initiallyStopped()
                        .build(SpriteAnimationContainer.instance());
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
        animations.selectAnimation(open ? ArcadeMsPacMan_AnimationID.JUNIOR : ArcadeMsPacMan_AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
