/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.ui.spriteanim.*;

public class ArcadePacMan_PacSpriteAnimationManager extends LazySpriteAnimationManager {

    public enum AnimationID implements Named {
        ANIM_BIG_PAC_MAN,
    }

    private final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_PacSpriteAnimationManager(SpriteAnimationSystem animationSystem, ArcadePacMan_SpriteSheet spriteSheet) {
        super(animationSystem);
        this.spriteSheet = spriteSheet;
        setFactory(id -> createAnimation(id, animationSystem));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationSystem animationSystem) {
        return switch (animationID) {
            case CommonSpriteAnimationID.PAC_MOUTH_SHUT -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build(animationSystem);

            // Renderer draws sprites depending on Pac-Man move direction!
            case CommonSpriteAnimationID.PAC_MOUTH_MOVING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build(animationSystem);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build(animationSystem);

            case AnimationID.ANIM_BIG_PAC_MAN -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.PACMAN_BIG))
                .frameTicks(3)
                .repeated()
                .build(animationSystem);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}