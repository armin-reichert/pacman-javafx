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
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnimComponent;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

public class Stork extends GameEntity {

    public static class StorkAnimations extends SpriteAnimationMap<SpriteID> {

        public StorkAnimations(SpriteAnimationContainer container) {
            super(ArcadeMsPacMan_SpriteSheet.instance());
            factory = id -> createAnimation(id, container);
        }

        private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {
            if (animationID.equals(ActorAnimationID.STORK_FLYING)) {
                return new SpriteAnimationBuilder()
                    .sprites(spriteSheet.findSprites(SpriteID.STORK))
                    .frameTicks(8)
                    .repeated()
                    .build(container);
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork(SpriteAnimationContainer animationSet) {
        name = "Beatrix von";
        setComponent(MovementComponent.class, new MovementComponent());
        setComponent(SpriteAnimComponent.class, new SpriteAnimComponent());
        requireComponent(SpriteAnimComponent.class).setAnimations(new StorkAnimations(animationSet));
    }

    public MovementComponent movement() {
        return requireComponent(MovementComponent.class);
    }
}