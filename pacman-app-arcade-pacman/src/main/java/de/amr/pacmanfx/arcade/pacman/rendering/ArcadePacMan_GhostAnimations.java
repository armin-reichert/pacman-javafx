/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationRegistry;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    public enum AnimationID {
        BLINKY_DAMAGED,
        BLINKY_DRESS_PATCHED,
        BLINKY_NAKED
    }

    private final SpriteAnimationRegistry manager;
    private final byte personality;

    public ArcadePacMan_GhostAnimations(SpriteAnimationRegistry manager, byte personality) {
        super(ArcadePacMan_SpriteSheet.instance());
        this.manager = requireNonNull(manager);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimation createAnimation(Object animationID) {
        return switch (animationID) {
            case Ghost.AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().ghostEyesSprites(Direction.LEFT))
                .build();

            case Ghost.AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build();

            case AnimationID.BLINKY_DAMAGED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build();

            case AnimationID.BLINKY_DRESS_PATCHED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build();

            case AnimationID.BLINKY_NAKED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_NAKED))
                .frameTicks(4)
                .repeated()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (Ghost.AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(Ghost.AnimationID.GHOST_POINTS).setCurrentFrameIndex(frameIndex);
        }
    }
}