/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.spriteanim.SpriteAnimationID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    private final SpriteAnimationContainer container;
    private final byte personality;

    public ArcadePacMan_GhostAnimations(SpriteAnimationContainer container, byte personality) {
        super(ArcadePacMan_SpriteSheet.instance());
        this.container = requireNonNull(container);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public SpriteAnimation createAnimation(SpriteAnimationID animationID) {
        return switch (animationID) {
            case ArcadePacMan_AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            case ArcadePacMan_AnimationID.BLINKY_DAMAGED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build(container);

            case ArcadePacMan_AnimationID.BLINKY_DRESS_PATCHED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build(container);

            case ArcadePacMan_AnimationID.BLINKY_NAKED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(SpriteID.RED_GHOST_NAKED))
                .frameTicks(4)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(SpriteAnimationID animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ArcadePacMan_AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ArcadePacMan_AnimationID.GHOST_POINTS).setCurrentFrameIndex(frameIndex);
        }
    }
}