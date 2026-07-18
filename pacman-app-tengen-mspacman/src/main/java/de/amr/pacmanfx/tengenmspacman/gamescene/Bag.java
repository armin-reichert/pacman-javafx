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
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

public class Bag extends Actor {

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations(SpriteAnimationContainer container) {
            super(TengenMsPacMan_SpriteSheet.instance());
            factory = id -> createAnimation(id, container);
        }

        private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {

            return switch (animationID) {
                case ArcadeMsPacMan_AnimationID.BAG -> new SpriteAnimationBuilder()
                    .singleSprite(spriteSheet.findSprite(SpriteID.BLUE_BAG))
                    .initiallyStopped()
                    .build(container);

                case ArcadeMsPacMan_AnimationID.JUNIOR -> new SpriteAnimationBuilder()
                    .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                    .initiallyStopped()
                    .build(container);

                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag(SpriteAnimationContainer container) {
        setAnimations(new BagAnimations(container));
        setOpen(false);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animations.select(open ? ArcadeMsPacMan_AnimationID.JUNIOR : ArcadeMsPacMan_AnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}