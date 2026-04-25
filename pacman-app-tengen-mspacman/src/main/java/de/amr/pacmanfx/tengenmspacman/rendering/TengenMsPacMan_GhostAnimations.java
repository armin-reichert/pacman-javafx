/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.spriteanim.SpriteAnimationID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.spriteanim.SpriteAnimationMap;

import static de.amr.pacmanfx.Validations.requireValidGhostPersonality;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GhostAnimations extends SpriteAnimationMap<SpriteID> {

    public static final int NORMAL_TICKS = 8;  // TODO check this in emulator
    public static final int FRIGHTENED_TICKS = 8;  // TODO check this in emulator
    public static final int FLASHING_TICKS = 7;  // TODO check this in emulator

    private final SpriteAnimationContainer container;
    private final byte personality;

    public TengenMsPacMan_GhostAnimations(SpriteAnimationContainer container, byte personality) {
        super(TengenMsPacMan_SpriteSheet.instance());
        this.container = requireNonNull(container);
        this.personality = requireValidGhostPersonality(personality);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return (TengenMsPacMan_SpriteSheet) spriteSheet;
    }

    @Override
    protected SpriteAnimation createAnimation(SpriteAnimationID animationID) {
        return switch (animationID) {
            case ArcadePacMan_AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(NORMAL_TICKS)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(FRIGHTENED_TICKS)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.GHOST_FLASHING))
                .frameTicks(FLASHING_TICKS)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet.sprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID " + animationID);
        };
    }

    @Override
    public void setAnimationFrame(SpriteAnimationID animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ArcadePacMan_AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ArcadePacMan_AnimationID.GHOST_POINTS).setCurrentFrameIndex(frameIndex);
        }
    }
}