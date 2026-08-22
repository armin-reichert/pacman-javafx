/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.LazySAM;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class ArcadeMsPacMan_PacSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_PacSAM(SpriteAnimContainer container) {
        setFactory(id -> createAnimation(id, container));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimContainer container) {

        return switch (animationID) {
            case CommonSpriteAnimationID.PAC_FULL -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.MS_PACMAN_FULL))
                .build(container);

            case CommonSpriteAnimationID.PAC_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.msPacManMunchingSprites(Direction.LEFT))
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build(container);

            case CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.MR_PACMAN_MUNCHING_LEFT))
                .frameTicks(2)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}