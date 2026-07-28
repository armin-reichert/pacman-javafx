/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.Identifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
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
                case CommonAnimationID.BAG -> new SpriteAnimationBuilder()
                    .singleSprite(spriteSheet.findSprite(SpriteID.BLUE_BAG))
                    .initiallyStopped()
                    .build(container);

                case CommonAnimationID.JUNIOR -> new SpriteAnimationBuilder()
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
        setComponent(Movement.class, new Movement());
        setComponent(SpriteAnim.class, new SpriteAnim());
        assertComponent(SpriteAnim.class).setAnimations(new BagAnimations(container));
    }

    public void setOpen(GameContext gameContext, boolean open) {
        this.open = open;
        gameContext.systems().spriteAnim.select(this, open ? CommonAnimationID.JUNIOR : CommonAnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}