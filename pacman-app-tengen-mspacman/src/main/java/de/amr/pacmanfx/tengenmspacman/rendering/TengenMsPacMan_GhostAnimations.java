/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationRegistry;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASHING_TICKS = 7;  // TODO check this in emulator

    private final SpriteAnimationRegistry manager;
    private final byte personality;

    public TengenMsPacMan_GhostAnimations(SpriteAnimationRegistry spriteAnimationRegistry, byte personality) {
        super(TengenMsPacMan_SpriteSheet.instance());
        this.personality = requireValidGhostPersonality(personality);
        manager = requireNonNull(spriteAnimationRegistry);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) spriteSheet;
    }

    @Override
    protected SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Ghost.AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(NORMAL_TICKS)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(FRIGHTENED_TICKS)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.GHOST_FLASHING))
                .frameTicks(FLASHING_TICKS)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder(manager)
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build();

            case Ghost.AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet.sprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (Ghost.AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(Ghost.AnimationID.GHOST_POINTS).setCurrentFrameIndex(frameIndex);
        }
    }
}