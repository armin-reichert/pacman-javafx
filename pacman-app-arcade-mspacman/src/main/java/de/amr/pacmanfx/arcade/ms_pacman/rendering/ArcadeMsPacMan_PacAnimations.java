/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ActorAnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

public class ArcadeMsPacMan_PacAnimations extends SpriteAnimationMap<SpriteID> {

    public ArcadeMsPacMan_PacAnimations(SpriteAnimationContainer container) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Identifier animationID, SpriteAnimationContainer container) {

        return switch (animationID) {
            case ActorAnimationID.PAC_FULL -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.MS_PACMAN_FULL))
                .build(container);

            case ActorAnimationID.PAC_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().msPacManMunchingSprites(Direction.LEFT))
                .repeated()
                .build(container);

            case ActorAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build(container);

            case ActorAnimationID.MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MR_PACMAN_MUNCHING_LEFT))
                .frameTicks(2)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return (ArcadeMsPacMan_SpriteSheet) super.spriteSheet();
    }
}