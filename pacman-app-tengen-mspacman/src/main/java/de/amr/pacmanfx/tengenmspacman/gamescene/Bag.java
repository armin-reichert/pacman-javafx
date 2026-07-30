/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.Identifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnimComponent;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

public class Bag extends GameEntity {

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations(SpriteAnimationContainer container) {
            super(TengenMsPacMan_SpriteSheet.instance());
            factory = id -> createAnimation(id, container);
        }

        private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {

            return switch (animationID) {
                case ActorAnimationID.BAG -> new SpriteAnimationBuilder()
                    .singleSprite(spriteSheet.findSprite(SpriteID.BLUE_BAG))
                    .initiallyStopped()
                    .build(container);

                case ActorAnimationID.JUNIOR -> new SpriteAnimationBuilder()
                    .singleSprite(spriteSheet.findSprite(SpriteID.JUNIOR_PAC))
                    .initiallyStopped()
                    .build(container);

                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag(SpriteAnimationContainer container) {
        name = "Birkin";
        setComponent(MovementComponent.class, new MovementComponent());
        setComponent(SpriteAnimComponent.class, new SpriteAnimComponent());
        requireComponent(SpriteAnimComponent.class).setAnimations(new BagAnimations(container));
    }

    public MovementComponent movement() {
        return requireComponent(MovementComponent.class);
    }

    public void setOpen(GameContext gameContext, boolean open) {
        this.open = open;
        gameContext.systems().spriteAnim().select(this, open ? ActorAnimationID.JUNIOR : ActorAnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}