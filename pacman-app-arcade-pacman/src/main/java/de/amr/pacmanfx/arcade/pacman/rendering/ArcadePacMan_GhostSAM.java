/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.LazySAM;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.model.GhostPersonality;

import java.util.Objects;

public class ArcadePacMan_GhostSAM extends LazySAM {

    private final ArcadePacMan_SpriteSheet spriteSheet =  ArcadePacMan_SpriteSheet.instance();
    
    private final GhostPersonality personality;

    public ArcadePacMan_GhostSAM(SpriteAnimContainer container, GhostPersonality personality) {
        this.personality = Objects.requireNonNull(personality);
        factory = id -> createAnimation(id, container);
    }

    private SpriteAnimation createAnimation(Named animationID, SpriteAnimContainer container) {

        return switch (animationID) {
            case CommonSpriteAnimationID.GHOST_NORMAL -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.ghostNormalSprites(personality, Direction.LEFT))
                .frameTicks(8)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_FRIGHTENED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.GHOST_FRIGHTENED))
                .frameTicks(8)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_FLASHING -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.GHOST_FLASHING))
                .frameTicks(7)
                .repeated()
                .build(container);

            case CommonSpriteAnimationID.GHOST_EYES -> new SpriteAnimationBuilder()
                .singleSprite(spriteSheet.ghostEyesSprite(Direction.LEFT))
                .build(container);

            case CommonSpriteAnimationID.GHOST_POINTS -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.GHOST_NUMBERS))
                .initiallyStopped()
                .build(container);

            case CommonSpriteAnimationID.BLINKY_DAMAGED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.RED_GHOST_DAMAGED))
                .initiallyStopped()
                .build(container);

            case CommonSpriteAnimationID.BLINKY_PATCHED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.RED_GHOST_PATCHED))
                .frameTicks(4)
                .repeated()
                .initiallyStopped()
                .build(container);

            case CommonSpriteAnimationID.BLINKY_NAKED -> new SpriteAnimationBuilder()
                .sprites(spriteSheet.findSprites(SpriteID.RED_GHOST_NAKED))
                .frameTicks(4)
                .repeated()
                .build(container);

            default -> throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        };
    }

    @Override
    public void setAnimationFrame(Named name, int frameIndex) {
        super.setAnimationFrame(name, frameIndex);
        if (CommonSpriteAnimationID.GHOST_POINTS.equals(name)) {
            animation(CommonSpriteAnimationID.GHOST_POINTS).setFrame(frameIndex);
        }
    }
}