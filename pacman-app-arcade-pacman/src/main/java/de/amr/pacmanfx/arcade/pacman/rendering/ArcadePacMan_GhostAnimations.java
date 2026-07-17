/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
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
    public SpriteAnimationContainer container() {
        return container;
    }

    @Override
    public SpriteAnimation createAnimation(Identifier animationID) {

        return switch (animationID) {
            case ArcadePacMan_AnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet().ghostEyesSprite(Direction.LEFT))
                .build(container);

            case ArcadePacMan_AnimationID.GHOST_POINTS -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            case ArcadePacMan_AnimationID.BLINKY_DAMAGED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build(container);

            case ArcadePacMan_AnimationID.BLINKY_DRESS_PATCHED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build(container);

            case ArcadePacMan_AnimationID.BLINKY_NAKED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet().findSprites(SpriteID.RED_GHOST_NAKED))
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
    public void setAnimationFrame(Identifier animationID, int frameIndex) {
        super.setAnimationFrame(animationID, frameIndex);
        if (ArcadePacMan_AnimationID.GHOST_POINTS.equals(animationID)) {
            animation(ArcadePacMan_AnimationID.GHOST_POINTS).setFrame(frameIndex);
        }
    }
}