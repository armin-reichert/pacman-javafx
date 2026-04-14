/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationRegistry;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationRegistry manager;
    private final byte personality;

    public ArcadeMsPacMan_GhostAnimations(SpriteAnimationRegistry spriteAnimationRegistry, byte personality) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.personality = requireValidGhostPersonality(personality);
        this.manager = requireNonNull(spriteAnimationRegistry);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case Ghost.AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build();

            case Ghost.AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder(manager)
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build();

            case Ghost.AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder(manager)
                .sprites(spriteSheet().sprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + id);
        };
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Object animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (Ghost.AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(Ghost.AnimationID.GHOST_POINTS).setCurrentFrameIndex(frameIndex);
        }
    }
}