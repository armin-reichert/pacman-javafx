/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.Identifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.spriteanim.SpriteAnimComp;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

public class Bag extends GameEntity {

    public static class BagAnimations extends SpriteAnimationMap<SpriteID> {

        public BagAnimations(SpriteAnimationContainer container) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            factory = id -> createAnimation(id, container);
        }

        private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {
            return switch (animationID) {
                case ActorAnimationID.JUNIOR ->
                    new SpriteAnimationBuilder()
                        .singleSprite(spriteSheet.findSprite(JUNIOR_PAC))
                        .initiallyStopped()
                        .build(container);

                case ActorAnimationID.BAG ->
                    new SpriteAnimationBuilder()
                        .singleSprite(spriteSheet.findSprite(BLUE_BAG))
                        .initiallyStopped()
                        .build(container);

                default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
            };
        }
    }

    private boolean open;

    public Bag(SpriteAnimationContainer container) {
        name = "Birkin";
        setComponent(MovementComp.class, new MovementComp());
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());
        requireComponent(SpriteAnimComp.class).setAnimations(new BagAnimations(container));
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public void setOpen(GameContext gameContext, boolean open) {
        this.open = open;
        gameContext.systems().spriteAnim().select(this, open ? ActorAnimationID.JUNIOR : ActorAnimationID.BAG);
    }

    public boolean isOpen() {
        return open;
    }
}
