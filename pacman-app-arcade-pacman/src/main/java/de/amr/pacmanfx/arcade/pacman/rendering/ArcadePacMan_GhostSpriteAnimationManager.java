/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.ui.spriteanim.*;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.Objects;

public class ArcadePacMan_GhostSpriteAnimationManager extends LazySpriteAnimationManager {

    private final ArcadePacMan_SpriteSheet spriteSheet =  ArcadePacMan_SpriteSheet.instance();
    
    private final GhostPersonality personality;

    public ArcadePacMan_GhostSpriteAnimationManager(SpriteAnimationSystem spriteAnimationSystem, GhostPersonality personality) {
        this.personality = Objects.requireNonNull(personality);
        setFactory(id -> createAnimation(id, spriteAnimationSystem));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimationSystem spriteAnimationSystem) {

        return switch (animationID) {
            case CommonSpriteAnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.ghostEyesSprite(Direction.LEFT))
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.BLINKY_DAMAGED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.BLINKY_PATCHED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build(spriteAnimationSystem);

            case CommonSpriteAnimationID.BLINKY_NAKED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.RED_GHOST_NAKED))
                .frameTicks(4)
                .repeated()
                .build(spriteAnimationSystem);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }
}