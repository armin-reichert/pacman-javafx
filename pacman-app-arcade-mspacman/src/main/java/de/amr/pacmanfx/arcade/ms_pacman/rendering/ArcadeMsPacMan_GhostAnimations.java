/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationContainer container;
    private final byte personality;

    public ArcadeMsPacMan_GhostAnimations(SpriteAnimationContainer container, byte personality) {
        super(ArcadeMsPacMan_SpriteSheet.instance());
        this.container = requireNonNull(container);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    protected SpriteAnimation createAnimation(Object id) {
        return switch (id) {
            case Ghost.AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case Ghost.AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case Ghost.AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case Ghost.AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case Ghost.AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

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