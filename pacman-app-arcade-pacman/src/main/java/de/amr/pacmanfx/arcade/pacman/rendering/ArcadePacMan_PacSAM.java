/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.LazySAM;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;

public class ArcadePacMan_PacSAM extends LazySAM {

    public enum AnimationID implements Named {
        ANIM_BIG_PAC_MAN,
    }

    private final ArcadePacMan_SpriteSheet spriteSheet;

    public ArcadePacMan_PacSAM(SpriteAnimContainer container, ArcadePacMan_SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
        setFactory(id -> createAnimation(id, container));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimContainer container) {
        return switch (animationID) {
            case CommonSpriteAnimationID.PAC_FULL -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.findSprite(SpriteID.PACMAN_FULL))
                .initiallyStopped()
                .build(container);

            // Renderer draws sprites depending on Pac-Man move direction!
            case CommonSpriteAnimationID.PAC_MUNCHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.pacMunchingSprites(Direction.LEFT))
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.PAC_DYING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.PACMAN_DYING))
                .frameTicks(8)
                .build(container);

            case AnimationID.ANIM_BIG_PAC_MAN -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.PACMAN_BIG))
                .frameTicks(3)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}