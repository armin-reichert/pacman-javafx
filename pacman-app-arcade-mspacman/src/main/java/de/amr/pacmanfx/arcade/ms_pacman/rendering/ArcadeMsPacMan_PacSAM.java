/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.ui.spriteanim.*;

public class ArcadeMsPacMan_PacSAM extends LazySAM {

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

    public ArcadeMsPacMan_PacSAM(SpriteAnimationSystem spriteAnimationSystem) {
        setFactory(id -> createAnimation(id, spriteAnimationSystem));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationSystem spriteAnimationSystem) {

        return switch (animationID) {
            case CommonSpriteAnimationID.PAC_MOUTH_SHUT -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.MS_PACMAN_FULL))
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.PAC_MOUTH_MOVING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.msPacManMunchingSprites(Direction.LEFT))
                .repeated()
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MS_PACMAN_DYING))
                .frameTicks(8)
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.MR_PACMAN_MUNCHING_LEFT))
                .frameTicks(2)
                .repeated()
                .build(spriteAnimationSystem);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}