/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.sprites;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.spriteanim.LazySAM;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationBuilder;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GhostSAM extends LazySAM {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASHING_TICKS = 7;  // TODO check this in emulator

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance(); 

    private final GhostPersonality personality;

    public TengenMsPacMan_GhostSAM(SpriteAnimContainer container, GhostPersonality personality) {
        this.personality = requireNonNull(personality);
        setFactory(id -> createAnimation(id, container));
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimContainer container) {

        return switch (animationID) {
            case CommonSpriteAnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(NORMAL_TICKS)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(FRIGHTENED_TICKS)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSpriteSequence(SpriteID.GHOST_FLASHING))
                .frameTicks(FLASHING_TICKS)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.ghostEyesSprite(Direction.LEFT))
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }
}