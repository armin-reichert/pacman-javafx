/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.Identifier;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.*;
import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
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
    public SpriteAnimationContainer container() {
        return container;
    }

    @Override
    protected SpriteAnimation createAnimation(Identifier animationID) {
        final SpriteAnimation animation = switch (animationID) {
            case ArcadePacMan_AnimationID.GHOST_NORMAL -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build();

            case ArcadePacMan_AnimationID.GHOST_FRIGHTENED -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build();

            case ArcadePacMan_AnimationID.GHOST_FLASHING -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build();

            case ArcadePacMan_AnimationID.GHOST_EYES -> SpriteAnimationBuilder.builder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build();

            case ArcadePacMan_AnimationID.GHOST_POINTS -> SpriteAnimationBuilder.builder()
                .sprites(spriteSheet().sprites(GHOST_NUMBERS))
                .initiallyStopped()
                .build();

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };

        animation.setContainer(container);
        return animation;
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void setAnimationFrame(Identifier animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ArcadePacMan_AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ArcadePacMan_AnimationID.GHOST_POINTS).setCurrentFrameIndex(frameIndex);
        }
    }
}